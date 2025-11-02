package com.resumeanalyser.resume_analyser_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchScoreDTO {
    private double overall;
    private double skillsMatch;
    private double experienceMatch;
    private double educationMatch;

    // ✅ Computed total percentage (0–100)
    private double matchingPercentage;

    /**
     * Calculates overall match percentage using weighted average
     */
    public void calculateMatchingPercentage() {
        // Customize weightage as you wish
        this.matchingPercentage =
                (overall * 0.4 + skillsMatch * 0.3 + experienceMatch * 0.2 + educationMatch * 0.1) * 100;
    }
}
