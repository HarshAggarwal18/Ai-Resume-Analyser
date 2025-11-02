package com.resumeanalyser.resume_analyser_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyser.resume_analyser_backend.dto.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIAnalyzerService {

    private final ChatClient chatClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public AIAnalyzerService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 🚀 Batch analyze: One AI call for all jobs
     */
    public List<AnalysisResultDTO> analyzeBatch(ResumeDTO resume, List<JobDTO> jobs) {
        try {
            String prompt = buildBatchPrompt(resume, jobs);
            String rawResponse = chatClient.prompt(prompt).call().content();

            String fixedJson = sanitizeJsonArray(rawResponse);

            List<AnalysisResultDTO> results = mapper.readValue(
                    fixedJson, new TypeReference<List<AnalysisResultDTO>>() {}
            );

            // Auto calculate matching %
            results.forEach(AnalysisResultDTO::calculateMatchingPercentage);
            return results;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of(new AnalysisResultDTO() {{
                setSummary("❌ Batch analysis failed: " + e.getMessage());
            }});
        }
    }

    /**
     * 🧠 Helper: Sanitizes array JSON (AI sometimes adds extra text)
     */
    private String sanitizeJsonArray(String raw) {
        if (raw == null) return "[]";
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start != -1 && end != -1 && end > start) {
            return raw.substring(start, end + 1);
        }
        return "[" + raw + "]";
    }

    /**
     * 🧩 Builds a single combined prompt for all jobs
     */
    private String buildBatchPrompt(ResumeDTO resume, List<JobDTO> jobs) {
        StringBuilder jobList = new StringBuilder();
        int i = 1;
        for (JobDTO job : jobs) {
            jobList.append(String.format("""
                Job #%d:
                Title: %s
                Company: %s
                Location: %s
                Type: %s
                Experience: %s
                Description: %s
                Required Skills: %s
                
                """,
                    i++,
                    job.getTitle(),
                    job.getCompany(),
                    job.getLocation(),
                    job.getJobType(),
                    job.getExperience(),
                    job.getDescription(),
                    String.join(", ", job.getSkillsRequired())
            ));
        }

        return """
        You are an AI resume-job matching assistant.

        Compare the following resume with each of the listed jobs.
        Return your response strictly as a **valid JSON array** where each element follows this schema:

        [
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
          },
          ...
        ]

        RESUME:
        Name: %s
        Email: %s
        Text: %s

        JOBS:
        %s
        """.formatted(
                resume.getName(),
                resume.getEmail(),
                resume.getText(),
                jobList
        );
    }
}
