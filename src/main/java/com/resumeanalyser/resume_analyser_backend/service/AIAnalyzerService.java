package com.resumeanalyser.resume_analyser_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyser.resume_analyser_backend.dto.AIRequestDTO;
import com.resumeanalyser.resume_analyser_backend.dto.AnalysisResultDTO;
import com.resumeanalyser.resume_analyser_backend.dto.JobDTO;
import com.resumeanalyser.resume_analyser_backend.dto.ResumeDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AIAnalyzerService {

    private final ChatClient chatClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public AIAnalyzerService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * ✅ Asynchronous AI analysis
     * This allows multiple resume-job comparisons to run in parallel.
     */
    @Async
    public CompletableFuture<AnalysisResultDTO> analyze(AIRequestDTO dto) {
        try {
            String prompt = buildPrompt(dto.getResume(), dto.getJob());
            String rawResponse = chatClient.prompt(prompt).call().content(); // get raw string

            String fixedJson = sanitizeJson(rawResponse);

            AnalysisResultDTO result = mapper.readValue(fixedJson, AnalysisResultDTO.class);
            result.calculateMatchingPercentage(); // auto-compute the score percentage
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            AnalysisResultDTO fallback = new AnalysisResultDTO();
            fallback.setSummary("❌ Failed to parse AI output: " + e.getMessage());
            fallback.setMatchingPercentage(0.0);
            return CompletableFuture.completedFuture(fallback);
        }
    }

    /**
     * ✅ Clean JSON fixer
     * Removes extra text from model output, ensuring valid JSON before parsing.
     */
    private String sanitizeJson(String raw) {
        if (raw == null) return "{}";

        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return raw.substring(start, end + 1);
        }

        // If the model didn’t return JSON properly, wrap it to avoid parsing errors.
        return "{ \"summary\": \"" + raw.replace("\"", "'") + "\" }";
    }

    /**
     * ✅ Build the LLM prompt
     * Same as before — forces valid JSON schema and structured comparison.
     */
    private String buildPrompt(ResumeDTO resume, JobDTO job) {
        return """
        You are an AI resume–job matching assistant.
        Your response **must be valid JSON only** — no explanations, no markdown, no text before or after.
        Follow this JSON schema exactly:

        {
          "title": "",
          "company": "",
          "location": "",
          "employmentType": "",
          "matchScore": {
            "overall": 0.0,
            "skillsMatch": 0.0,
            "experienceMatch": 0.0,
            "educationMatch": 0.0
          },
          "matchingSkills": [],
          "missingSkills": [],
          "whyFit": "",
          "growthAreas": "",
          "summary": ""
        }

        Now analyze the following:

        RESUME:
        Name: %s
        Email: %s
        Text: %s

        JOB:
        Title: %s
        Company: %s
        Location: %s
        Type: %s
        Experience: %s
        Description: %s
        Skills: %s
        """.formatted(
                resume.getName(),
                resume.getEmail(),
                resume.getText(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getJobType(),
                job.getExperience(),
                job.getDescription(),
                String.join(", ", job.getSkillsRequired())
        );
    }
}
