package com.resumeanalyser.resume_analyser_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResultDTO {
    private String title;
    private String company;
    private String location;
    private String employmentType;
    private MatchScoreDTO matchScore;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private String whyFit;
    private String growthAreas;
    private String summary;

    // ✅ top-level overall matching percentage (convenience for frontend)
    private double matchingPercentage;

    /**
     * Calculates and sets overall matching percentage from MatchScoreDTO
     */
    public void calculateMatchingPercentage() {
        if (matchScore != null) {
            matchScore.calculateMatchingPercentage();
            this.matchingPercentage = matchScore.getMatchingPercentage();
        } else {
            this.matchingPercentage = 0.0;
        }
    }
}
