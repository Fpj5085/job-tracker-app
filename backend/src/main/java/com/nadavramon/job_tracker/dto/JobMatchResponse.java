package com.nadavramon.job_tracker.dto;

import java.util.List;

public class JobMatchResponse {

    private int score;
    private int matchedWeight;
    private int totalWeight;
    private List<String> matchedKeywords;
    private List<String> missingKeywords;

    public JobMatchResponse(int score, int matchedWeight, int totalWeight, List<String> matchedKeywords, List<String> missingKeywords) {
        this.score = score;
        this.matchedWeight = matchedWeight;
        this.totalWeight = totalWeight;
        this.matchedKeywords = matchedKeywords;
        this.missingKeywords = missingKeywords;
    }

    public int getScore() {
        return score;
    }

    public int getMatchedWeight() {
        return matchedWeight;
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }
}
