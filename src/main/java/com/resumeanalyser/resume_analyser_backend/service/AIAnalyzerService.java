package com.resumeanalyser.resume_analyser_backend.service;

import com.resumeanalyser.resume_analyser_backend.model.AnalysisResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIAnalyzerService {

    private final ChatClient chatClient;

    public AIAnalyzerService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AnalysisResult analyzeResume(String resumeText, String jobData) {
        String prompt = buildPrompt(resumeText, jobData);

        // Spring AI handles the request + maps JSON to your POJO automatically
        return chatClient
                .prompt(prompt)
                .call()
                .entity(AnalysisResult.class);
    }

    private String buildPrompt(String resumeText, String jobData) {
        return """
            You are an AI resume-job matching assistant.

            Compare the following resume and job description, and return a structured JSON strictly following this schema:

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

            RESUME:
            """ + resumeText + """

            JOB:
            """ + jobData + """
            """;
    }
}
