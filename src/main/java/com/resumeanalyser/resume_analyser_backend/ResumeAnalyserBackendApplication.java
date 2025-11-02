package com.resumeanalyser.resume_analyser_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ResumeAnalyserBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(ResumeAnalyserBackendApplication.class, args);
		System.out.println("✅ Resume Analyser Backend is running...");
	}
}
