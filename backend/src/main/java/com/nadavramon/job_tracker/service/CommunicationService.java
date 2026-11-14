package com.nadavramon.job_tracker.service;

import com.nadavramon.job_tracker.dto.CommunicationRequest;
import com.nadavramon.job_tracker.dto.CommunicationResponse;
import com.nadavramon.job_tracker.dto.CommunicationUpdateRequest;
import com.nadavramon.job_tracker.entity.Application;
import com.nadavramon.job_tracker.entity.Communication;
import com.nadavramon.job_tracker.entity.User;
import com.nadavramon.job_tracker.exception.ResourceNotFoundException;
import com.nadavramon.job_tracker.exception.ResourceOwnershipException;
import com.nadavramon.job_tracker.repository.ApplicationRepository;
import com.nadavramon.job_tracker.repository.CommunicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CommunicationService {

    private final CommunicationRepository communicationRepository;
    private final ApplicationRepository applicationRepository;
    private final CurrentUserService currentUserService;

    public CommunicationService(CommunicationRepository communicationRepository, ApplicationRepository applicationRepository, CurrentUserService currentUserService) {
        this.communicationRepository = communicationRepository;
        this.applicationRepository = applicationRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<CommunicationResponse> getCommunicationsByApplication(UUID applicationId) {
        Application application = getOwnedApplication(applicationId);
        return communicationRepository.findByApplicationOrderByOccurredAtDesc(application).stream().map(this::toResponse).toList();
    }

    @Transactional
    public CommunicationResponse createCommunication(UUID applicationId, CommunicationRequest request) {
        Application application = getOwnedApplication(applicationId);
        Communication communication = new Communication();
        communication.setApplication(application);
        communication.setUser(application.getUser());
        communication.setChannel(request.getChannel());
        communication.setDirection(request.getDirection());
        communication.setOccurredAt(request.getOccurredAt());
        communication.setSummary(request.getSummary());
        communication.setNextAction(request.getNextAction());
        communication.setNextActionAt(request.getNextActionAt());
        return toResponse(communicationRepository.save(communication));
    }

    @Transactional
    public CommunicationResponse updateCommunication(UUID applicationId, UUID communicationId, CommunicationUpdateRequest request) {
        Communication communication = getOwnedCommunication(applicationId, communicationId);
        if (request.getChannel() != null) communication.setChannel(request.getChannel());
        if (request.getDirection() != null) communication.setDirection(request.getDirection());
        if (request.getOccurredAt() != null) communication.setOccurredAt(request.getOccurredAt());
        if (request.getSummary() != null) communication.setSummary(request.getSummary());
        if (request.getNextAction() != null) communication.setNextAction(request.getNextAction());
        if (request.getNextActionAt() != null) communication.setNextActionAt(request.getNextActionAt());
        return toResponse(communicationRepository.save(communication));
    }

    @Transactional
    public void deleteCommunication(UUID applicationId, UUID communicationId) {
        Communication communication = getOwnedCommunication(applicationId, communicationId);
        communication.setDeletedAt(LocalDateTime.now());
        communicationRepository.save(communication);
    }

    private Application getOwnedApplication(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getUser().getId().equals(getCurrentUser().getId()))
            throw new ResourceOwnershipException("Access denied");
        return application;
    }

    private Communication getOwnedCommunication(UUID applicationId, UUID communicationId) {
        getOwnedApplication(applicationId);
        Communication communication = communicationRepository.findById(communicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Communication not found"));
        if (!communication.getApplication().getId().equals(applicationId))
            throw new ResourceNotFoundException("Communication not found");
        return communication;
    }

    private User getCurrentUser() {
        return currentUserService.getCurrentUser();
    }

    private CommunicationResponse toResponse(Communication communication) {
        return new CommunicationResponse(
                communication.getId(),
                communication.getApplication().getId(),
                communication.getChannel(),
                communication.getDirection(),
                communication.getOccurredAt(),
                communication.getSummary(),
                communication.getNextAction(),
                communication.getNextActionAt());
    }
}
