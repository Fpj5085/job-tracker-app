package com.nadavramon.job_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class JobMatchRequest {

    @NotBlank(message = "JD text is required")
    @Size(max = 50000, message = "JD text must be under 50,000 characters")
    private String jdText;

    @Size(max = 20, message = "At most 20 extra keywords")
    private List<@Size(max = 50, message = "Keyword must be under 50 characters") String> extraKeywords;

    public JobMatchRequest() {
    }

    public JobMatchRequest(String jdText, List<String> extraKeywords) {
        this.jdText = jdText;
        this.extraKeywords = extraKeywords;
    }

    public String getJdText() {
        return jdText;
    }

    public void setJdText(String jdText) {
        this.jdText = jdText;
    }

    public List<String> getExtraKeywords() {
        return extraKeywords;
    }

    public void setExtraKeywords(List<String> extraKeywords) {
        this.extraKeywords = extraKeywords;
    }
}
