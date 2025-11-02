package com.resumeanalyser.resume_analyser_backend.controller;

import com.resumeanalyser.resume_analyser_backend.dto.*;
import com.resumeanalyser.resume_analyser_backend.model.Job;
import com.resumeanalyser.resume_analyser_backend.service.AIAnalyzerService;
import com.resumeanalyser.resume_analyser_backend.service.JobService;
import com.resumeanalyser.resume_analyser_backend.service.ResumeParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/analyze")
public class ResumeAnalysisController {

    private final ResumeParserService resumeParserService;
    private final JobService jobService;
    private final AIAnalyzerService aiAnalyzerService;

    public ResumeAnalysisController(
            ResumeParserService resumeParserService,
            JobService jobService,
            AIAnalyzerService aiAnalyzerService
    ) {
        this.resumeParserService = resumeParserService;
        this.jobService = jobService;
        this.aiAnalyzerService = aiAnalyzerService;
    }

    @PostMapping("/resume")
    public ResponseEntity<List<AnalysisResultDTO>> analyzeResume(@RequestParam("file") MultipartFile file) {
        try {
            // Step 1 — Extract Resume
            String text = resumeParserService.extractTextFromResume(file);
            ResumeDTO resume = new ResumeDTO("Unknown", "N/A", text);

            // Step 2 — Fetch All Jobs
            List<Job> jobs = jobService.getAllJobs();

            // Step 3 — Convert to DTOs
            List<JobDTO> jobDTOs = jobs.stream().map(job ->
                    new JobDTO(
                            job.getId(),
                            job.getJobTitle(),
                            job.getCompany(),
                            job.getLocation(),
                            job.getJobType(),
                            job.getExperience(),
                            job.getDescription(),
                            job.getSkillsRequired()
                    )
            ).toList();

            // Step 4 — One AI call for all jobs 🚀
            List<AnalysisResultDTO> results = aiAnalyzerService.analyzeBatch(resume, jobDTOs);

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
