package com.resumeanalyser.resume_analyser_backend.controller;

import com.resumeanalyser.resume_analyser_backend.model.AnalysisResult;
import com.resumeanalyser.resume_analyser_backend.service.AIAnalyzerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIAnalyzerService aiAnalyzerService;

    public AIController(AIAnalyzerService aiAnalyzerService) {
        this.aiAnalyzerService = aiAnalyzerService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResult> analyze(
            @RequestParam("resumeText") String resumeText,
            @RequestParam("jobData") String jobData) {

        AnalysisResult result = aiAnalyzerService.analyzeResume(resumeText, jobData);
        return ResponseEntity.ok(result);
    }
}
