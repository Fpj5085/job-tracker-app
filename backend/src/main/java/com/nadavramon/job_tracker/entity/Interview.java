package com.nadavramon.job_tracker.entity;

import com.nadavramon.job_tracker.enums.InterviewMode;
import com.nadavramon.job_tracker.enums.InterviewResult;
import com.nadavramon.job_tracker.enums.InterviewStage;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "interviews")
@SQLRestriction("deleted_at IS NULL")
public class Interview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewStage stage;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(length = 64)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InterviewMode mode;

    @Column(columnDefinition = "text")
    private String interviewerNotes;

    @Column(columnDefinition = "text")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InterviewResult result;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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
