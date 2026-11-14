package com.nadavramon.job_tracker.dto;

import com.nadavramon.job_tracker.enums.CommunicationChannel;
import com.nadavramon.job_tracker.enums.CommunicationDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
public class CommunicationRequest {

    @NotNull
    private CommunicationChannel channel;

    @NotNull
    private CommunicationDirection direction;

    @NotNull
    private LocalDateTime occurredAt;

    @NotBlank
    @Size(max = 2000)
    private String summary;

    @Size(max = 2000)
    private String nextAction;

    private LocalDateTime nextActionAt;

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
