package com.nadavramon.job_tracker.service;

import com.nadavramon.job_tracker.dto.InterviewRequest;
import com.nadavramon.job_tracker.dto.InterviewResponse;
import com.nadavramon.job_tracker.dto.InterviewUpdateRequest;
import com.nadavramon.job_tracker.entity.Application;
import com.nadavramon.job_tracker.entity.Interview;
import com.nadavramon.job_tracker.entity.User;
import com.nadavramon.job_tracker.enums.InterviewMode;
import com.nadavramon.job_tracker.enums.InterviewResult;
import com.nadavramon.job_tracker.enums.InterviewStage;
import com.nadavramon.job_tracker.exception.ResourceNotFoundException;
import com.nadavramon.job_tracker.exception.ResourceOwnershipException;
import com.nadavramon.job_tracker.repository.ApplicationRepository;
import com.nadavramon.job_tracker.repository.InterviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private CurrentUserService currentUserService;

    private InterviewService interviewService;

    private User currentUser;
    private User otherUser;
    private Application ownedApplication;

    @BeforeEach
    void setUp() {
        interviewService = new InterviewService(interviewRepository, applicationRepository, currentUserService);

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        ownedApplication = new Application();
        ownedApplication.setId(UUID.randomUUID());
        ownedApplication.setUser(currentUser);
    }

    private InterviewRequest makeRequest() {
        InterviewRequest request = new InterviewRequest();
        request.setStage(InterviewStage.TECHNICAL);
        request.setScheduledAt(LocalDateTime.now().plusDays(3));
        request.setTimezone("Asia/Shanghai");
        request.setMode(InterviewMode.ONLINE);
        request.setInterviewerNotes("Bring laptop");
        return request;
    }

    private void stubOwnedApplication() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(applicationRepository.findById(ownedApplication.getId())).thenReturn(Optional.of(ownedApplication));
    }

    @Test
    void createInterview_DefaultsResultToPending() {
        stubOwnedApplication();
        when(interviewRepository.save(any(Interview.class))).thenAnswer(inv -> inv.getArgument(0));

        InterviewResponse response = interviewService.createInterview(ownedApplication.getId(), makeRequest());

        assertEquals(InterviewResult.PENDING, response.getResult());
        assertEquals(InterviewStage.TECHNICAL, response.getStage());
        assertEquals(ownedApplication.getId(), response.getApplicationId());
    }

    @Test
    void createInterview_SoftDeletedFieldsNeverSet() {
        stubOwnedApplication();
        ArgumentCaptor<Interview> captor = ArgumentCaptor.forClass(Interview.class);
        when(interviewRepository.save(any(Interview.class))).thenAnswer(inv -> inv.getArgument(0));

        interviewService.createInterview(ownedApplication.getId(), makeRequest());

        verify(interviewRepository).save(captor.capture());
        assertNull(captor.getValue().getDeletedAt());
        assertEquals(currentUser.getId(), captor.getValue().getUser().getId());
    }

    @Test
    void createInterview_ApplicationNotFound_Throws404() {
        when(applicationRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> interviewService.createInterview(UUID.randomUUID(), makeRequest()));
    }

    @Test
    void createInterview_ApplicationOwnedByOther_ThrowsOwnership() {
        ownedApplication.setUser(otherUser);
        stubOwnedApplication();

        assertThrows(ResourceOwnershipException.class, () -> interviewService.createInterview(ownedApplication.getId(), makeRequest()));
    }

    @Test
    void getInterviewsByApplication_ReturnsList() {
        stubOwnedApplication();
        Interview interview = new Interview();
        interview.setId(UUID.randomUUID());
        interview.setApplication(ownedApplication);
        interview.setStage(InterviewStage.HR_SCREEN);
        interview.setScheduledAt(LocalDateTime.now());
        interview.setMode(InterviewMode.PHONE);
        interview.setResult(InterviewResult.PENDING);
        when(interviewRepository.findByApplicationOrderByScheduledAtAsc(ownedApplication)).thenReturn(List.of(interview));

        List<InterviewResponse> result = interviewService.getInterviewsByApplication(ownedApplication.getId());

        assertEquals(1, result.size());
        assertEquals(InterviewStage.HR_SCREEN, result.get(0).getStage());
    }

    @Test
    void updateInterview_AppliesOnlyProvidedFields() {
        stubOwnedApplication();
        Interview interview = new Interview();
        interview.setId(UUID.randomUUID());
        interview.setApplication(ownedApplication);
        interview.setStage(InterviewStage.HR_SCREEN);
        interview.setScheduledAt(LocalDateTime.now());
        interview.setMode(InterviewMode.PHONE);
        interview.setResult(InterviewResult.PENDING);
        when(interviewRepository.findById(interview.getId())).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(inv -> inv.getArgument(0));

        InterviewUpdateRequest request = new InterviewUpdateRequest();
        request.setResult(InterviewResult.PASS);
        request.setFeedback("Strong candidate");

        InterviewResponse response = interviewService.updateInterview(ownedApplication.getId(), interview.getId(), request);

        assertEquals(InterviewResult.PASS, response.getResult());
        assertEquals(InterviewStage.HR_SCREEN, response.getStage());
    }

    @Test
    void deleteInterview_SoftDeletes() {
        stubOwnedApplication();
        Interview interview = new Interview();
        interview.setId(UUID.randomUUID());
        interview.setApplication(ownedApplication);
        when(interviewRepository.findById(interview.getId())).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(inv -> inv.getArgument(0));

        interviewService.deleteInterview(ownedApplication.getId(), interview.getId());

        assertNotNull(interview.getDeletedAt());
    }

    @Test
    void getUpcomingInterviews_PassesWindowToRepository() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(interviewRepository.findByUserAndResultAndScheduledAtBetweenOrderByScheduledAtAsc(eq(currentUser), eq(InterviewResult.PENDING), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<InterviewResponse> result = interviewService.getUpcomingInterviews(24);

        assertTrue(result.isEmpty());
    }
}
