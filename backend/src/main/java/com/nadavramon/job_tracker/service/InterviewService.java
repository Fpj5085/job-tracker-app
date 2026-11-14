package com.nadavramon.job_tracker.service;

import com.nadavramon.job_tracker.dto.InterviewRequest;
import com.nadavramon.job_tracker.dto.InterviewResponse;
import com.nadavramon.job_tracker.dto.InterviewUpdateRequest;
import com.nadavramon.job_tracker.entity.Application;
import com.nadavramon.job_tracker.entity.Interview;
import com.nadavramon.job_tracker.entity.User;
import com.nadavramon.job_tracker.enums.InterviewResult;
import com.nadavramon.job_tracker.exception.ResourceNotFoundException;
import com.nadavramon.job_tracker.exception.ResourceOwnershipException;
import com.nadavramon.job_tracker.repository.ApplicationRepository;
import com.nadavramon.job_tracker.repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final CurrentUserService currentUserService;

    public InterviewService(InterviewRepository interviewRepository, ApplicationRepository applicationRepository, CurrentUserService currentUserService) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsByApplication(UUID applicationId) {
        Application application = getOwnedApplication(applicationId);
        return interviewRepository.findByApplicationOrderByScheduledAtAsc(application).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getUpcomingInterviews(int hours) {
        LocalDateTime now = LocalDateTime.now();
        return interviewRepository
                .findByUserAndResultAndScheduledAtBetweenOrderByScheduledAtAsc(getCurrentUser(), InterviewResult.PENDING, now, now.plusHours(hours))
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public InterviewResponse createInterview(UUID applicationId, InterviewRequest request) {
        Application application = getOwnedApplication(applicationId);
        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setUser(application.getUser());
        interview.setStage(request.getStage());
        interview.setScheduledAt(request.getScheduledAt());
        interview.setTimezone(request.getTimezone());
        interview.setMode(request.getMode());
        interview.setInterviewerNotes(request.getInterviewerNotes());
        interview.setFeedback(request.getFeedback());
        interview.setResult(request.getResult() != null ? request.getResult() : InterviewResult.PENDING);
        return toResponse(interviewRepository.save(interview));
    }

    @Transactional
    public InterviewResponse updateInterview(UUID applicationId, UUID interviewId, InterviewUpdateRequest request) {
        Interview interview = getOwnedInterview(applicationId, interviewId);
        if (request.getStage() != null) interview.setStage(request.getStage());
        if (request.getScheduledAt() != null) interview.setScheduledAt(request.getScheduledAt());
        if (request.getTimezone() != null) interview.setTimezone(request.getTimezone());
        if (request.getMode() != null) interview.setMode(request.getMode());
        if (request.getInterviewerNotes() != null) interview.setInterviewerNotes(request.getInterviewerNotes());
        if (request.getFeedback() != null) interview.setFeedback(request.getFeedback());
        if (request.getResult() != null) interview.setResult(request.getResult());
        return toResponse(interviewRepository.save(interview));
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterview(UUID applicationId, UUID interviewId) {
        return toResponse(getOwnedInterview(applicationId, interviewId));
    }

    @Transactional
    public void deleteInterview(UUID applicationId, UUID interviewId) {
        Interview interview = getOwnedInterview(applicationId, interviewId);
        interview.setDeletedAt(LocalDateTime.now());
        interviewRepository.save(interview);
    }

    private Application getOwnedApplication(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getUser().getId().equals(getCurrentUser().getId()))
            throw new ResourceOwnershipException("Access denied");
        return application;
    }

    private Interview getOwnedInterview(UUID applicationId, UUID interviewId) {
        getOwnedApplication(applicationId);
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
        if (!interview.getApplication().getId().equals(applicationId))
            throw new ResourceNotFoundException("Interview not found");
        return interview;
    }

    private User getCurrentUser() {
        return currentUserService.getCurrentUser();
    }

    private InterviewResponse toResponse(Interview interview) {
        return new InterviewResponse(
                interview.getId(),
                interview.getApplication().getId(),
                interview.getStage(),
                interview.getScheduledAt(),
                interview.getTimezone(),
                interview.getMode(),
                interview.getInterviewerNotes(),
                interview.getFeedback(),
                interview.getResult());
    }
}
