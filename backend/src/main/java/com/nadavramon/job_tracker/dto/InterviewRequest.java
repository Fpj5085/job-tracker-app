package com.nadavramon.job_tracker.dto;

import com.nadavramon.job_tracker.enums.InterviewMode;
import com.nadavramon.job_tracker.enums.InterviewResult;
import com.nadavramon.job_tracker.enums.InterviewStage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
public class InterviewRequest {

    @NotNull
    private InterviewStage stage;

    @NotNull
    private LocalDateTime scheduledAt;

    @Size(max = 64)
    private String timezone;

    @NotNull
    private InterviewMode mode;

    private String interviewerNotes;

    private String feedback;

    private InterviewResult result;

    public InterviewStage getStage() {
        return stage;
    }

    public void setStage(InterviewStage stage) {
        this.stage = stage;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public InterviewMode getMode() {
        return mode;
    }

    public void setMode(InterviewMode mode) {
        this.mode = mode;
    }

    public String getInterviewerNotes() {
        return interviewerNotes;
    }

    public void setInterviewerNotes(String interviewerNotes) {
        this.interviewerNotes = interviewerNotes;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public InterviewResult getResult() {
        return result;
    }

    public void setResult(InterviewResult result) {
        this.result = result;
    }

}
