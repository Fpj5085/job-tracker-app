package com.nadavramon.job_tracker.dto;

import java.util.UUID;
import java.util.UUID;
import com.nadavramon.job_tracker.enums.InterviewStage;
import java.time.LocalDateTime;
import java.lang.String;
import com.nadavramon.job_tracker.enums.InterviewMode;
import java.lang.String;
import java.lang.String;
import com.nadavramon.job_tracker.enums.InterviewResult;

public class InterviewResponse {

    private UUID id;
    private UUID applicationId;
    private InterviewStage stage;
    private LocalDateTime scheduledAt;
    private String timezone;
    private InterviewMode mode;
    private String interviewerNotes;
    private String feedback;
    private InterviewResult result;

    public InterviewResponse(UUID id, UUID applicationId, InterviewStage stage, LocalDateTime scheduledAt, String timezone, InterviewMode mode, String interviewerNotes, String feedback, InterviewResult result) {
        this.id = id;
        this.applicationId = applicationId;
        this.stage = stage;
        this.scheduledAt = scheduledAt;
        this.timezone = timezone;
        this.mode = mode;
        this.interviewerNotes = interviewerNotes;
        this.feedback = feedback;
        this.result = result;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public InterviewStage getStage() {
        return stage;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public String getTimezone() {
        return timezone;
    }

    public InterviewMode getMode() {
        return mode;
    }

    public String getInterviewerNotes() {
        return interviewerNotes;
    }

    public String getFeedback() {
        return feedback;
    }

    public InterviewResult getResult() {
        return result;
    }

}
