package com.nadavramon.job_tracker.entity;

import com.nadavramon.job_tracker.enums.CommunicationChannel;
import com.nadavramon.job_tracker.enums.CommunicationDirection;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "communications")
@SQLRestriction("deleted_at IS NULL")
public class Communication extends BaseEntity {

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
    private CommunicationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private CommunicationDirection direction;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(name = "next_action", columnDefinition = "text")
    private String nextAction;

    @Column(name = "next_action_at")
    private LocalDateTime nextActionAt;

    // getters/setters
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

    public CommunicationChannel getChannel() {
        return channel;
    }

    public void setChannel(CommunicationChannel channel) {
        this.channel = channel;
    }

    public CommunicationDirection getDirection() {
        return direction;
    }

    public void setDirection(CommunicationDirection direction) {
        this.direction = direction;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public LocalDateTime getNextActionAt() {
        return nextActionAt;
    }

    public void setNextActionAt(LocalDateTime nextActionAt) {
        this.nextActionAt = nextActionAt;
    }

}
