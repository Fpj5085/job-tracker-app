package com.nadavramon.job_tracker.dto;

import java.util.UUID;
import java.util.UUID;
import com.nadavramon.job_tracker.enums.CommunicationChannel;
import com.nadavramon.job_tracker.enums.CommunicationDirection;
import java.time.LocalDateTime;
import java.lang.String;
import java.lang.String;
import java.time.LocalDateTime;

public class CommunicationResponse {

    private UUID id;
    private UUID applicationId;
    private CommunicationChannel channel;
    private CommunicationDirection direction;
    private LocalDateTime occurredAt;
    private String summary;
    private String nextAction;
    private LocalDateTime nextActionAt;

    public CommunicationResponse(UUID id, UUID applicationId, CommunicationChannel channel, CommunicationDirection direction, LocalDateTime occurredAt, String summary, String nextAction, LocalDateTime nextActionAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.channel = channel;
        this.direction = direction;
        this.occurredAt = occurredAt;
        this.summary = summary;
        this.nextAction = nextAction;
        this.nextActionAt = nextActionAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public CommunicationChannel getChannel() {
        return channel;
    }

    public CommunicationDirection getDirection() {
        return direction;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getSummary() {
        return summary;
    }

    public String getNextAction() {
        return nextAction;
    }

    public LocalDateTime getNextActionAt() {
        return nextActionAt;
    }

}
