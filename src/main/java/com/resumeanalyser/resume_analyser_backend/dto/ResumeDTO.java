package com.resumeanalyser.resume_analyser_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeDTO {
    private String candidateName;
    private String email;
    private String extractedText;
}
