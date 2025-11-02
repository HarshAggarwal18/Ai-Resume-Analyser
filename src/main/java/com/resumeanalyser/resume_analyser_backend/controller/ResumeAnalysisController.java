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
import java.util.concurrent.CompletableFuture;

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
            // Step 1️⃣ — Extract Resume Text
            String text = resumeParserService.extractTextFromResume(file);
            ResumeDTO resume = new ResumeDTO("Unknown", "N/A", text);

            // Step 2️⃣ — Fetch All Jobs
            List<Job> jobs = jobService.getAllJobs();

            // Step 3️⃣ — Analyze Each Job (Parallel Execution)
            List<CompletableFuture<AnalysisResultDTO>> futures = new ArrayList<>();
            for (Job job : jobs) {
                JobDTO jobDTO = new JobDTO(
                        job.getId(),
                        job.getJobTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getJobType(),
                        job.getExperience(),
                        job.getDescription(),
                        job.getSkillsRequired()
                );

                AIRequestDTO request = new AIRequestDTO(resume, jobDTO);
                futures.add(aiAnalyzerService.analyze(request)); // async
            }

            // Step 4️⃣ — Wait for All Tasks to Complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Step 5️⃣ — Collect All Results
            List<AnalysisResultDTO> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
