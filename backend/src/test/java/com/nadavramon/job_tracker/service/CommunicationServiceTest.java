package com.nadavramon.job_tracker.service;

import com.nadavramon.job_tracker.dto.CommunicationRequest;
import com.nadavramon.job_tracker.dto.CommunicationResponse;
import com.nadavramon.job_tracker.dto.CommunicationUpdateRequest;
import com.nadavramon.job_tracker.entity.Application;
import com.nadavramon.job_tracker.entity.Communication;
import com.nadavramon.job_tracker.entity.User;
import com.nadavramon.job_tracker.enums.CommunicationChannel;
import com.nadavramon.job_tracker.enums.CommunicationDirection;
import com.nadavramon.job_tracker.exception.ResourceNotFoundException;
import com.nadavramon.job_tracker.exception.ResourceOwnershipException;
import com.nadavramon.job_tracker.repository.ApplicationRepository;
import com.nadavramon.job_tracker.repository.CommunicationRepository;
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
public class CommunicationServiceTest {

    @Mock
    private CommunicationRepository communicationRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private CurrentUserService currentUserService;

    private CommunicationService communicationService;

    private User currentUser;
    private User otherUser;
    private Application ownedApplication;

    @BeforeEach
    void setUp() {
        communicationService = new CommunicationService(communicationRepository, applicationRepository, currentUserService);

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        ownedApplication = new Application();
        ownedApplication.setId(UUID.randomUUID());
        ownedApplication.setUser(currentUser);
    }

    private CommunicationRequest makeRequest() {
        CommunicationRequest request = new CommunicationRequest();
        request.setChannel(CommunicationChannel.EMAIL);
        request.setDirection(CommunicationDirection.OUT);
        request.setOccurredAt(LocalDateTime.now().minusHours(2));
        request.setSummary("Sent follow-up email");
        request.setNextAction("Wait for reply");
        request.setNextActionAt(LocalDateTime.now().plusDays(2));
        return request;
    }

    private void stubOwnedApplication() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(applicationRepository.findById(ownedApplication.getId())).thenReturn(Optional.of(ownedApplication));
    }

    @Test
    void createCommunication_MapsAllFields() {
        stubOwnedApplication();
        when(communicationRepository.save(any(Communication.class))).thenAnswer(inv -> inv.getArgument(0));

        CommunicationResponse response = communicationService.createCommunication(ownedApplication.getId(), makeRequest());

        assertEquals(CommunicationChannel.EMAIL, response.getChannel());
        assertEquals(CommunicationDirection.OUT, response.getDirection());
        assertEquals("Sent follow-up email", response.getSummary());
        assertEquals(ownedApplication.getId(), response.getApplicationId());
    }

    @Test
    void createCommunication_SetsUserFromApplication() {
        stubOwnedApplication();
        ArgumentCaptor<Communication> captor = ArgumentCaptor.forClass(Communication.class);
        when(communicationRepository.save(any(Communication.class))).thenAnswer(inv -> inv.getArgument(0));

        communicationService.createCommunication(ownedApplication.getId(), makeRequest());

        verify(communicationRepository).save(captor.capture());
        assertEquals(currentUser.getId(), captor.getValue().getUser().getId());
    }

    @Test
    void createCommunication_ApplicationNotFound_Throws404() {
        when(applicationRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> communicationService.createCommunication(UUID.randomUUID(), makeRequest()));
    }

    @Test
    void createCommunication_OtherUsersApplication_ThrowsOwnership() {
        ownedApplication.setUser(otherUser);
        stubOwnedApplication();

        assertThrows(ResourceOwnershipException.class, () -> communicationService.createCommunication(ownedApplication.getId(), makeRequest()));
    }

    @Test
    void updateCommunication_PartialUpdateKeepsOtherFields() {
        stubOwnedApplication();
        Communication communication = new Communication();
        communication.setId(UUID.randomUUID());
        communication.setApplication(ownedApplication);
        communication.setChannel(CommunicationChannel.EMAIL);
        communication.setDirection(CommunicationDirection.OUT);
        communication.setOccurredAt(LocalDateTime.now());
        communication.setSummary("Original");
        when(communicationRepository.findById(communication.getId())).thenReturn(Optional.of(communication));
        when(communicationRepository.save(any(Communication.class))).thenAnswer(inv -> inv.getArgument(0));

        CommunicationUpdateRequest request = new CommunicationUpdateRequest();
        request.setSummary("Updated summary");

        CommunicationResponse response = communicationService.updateCommunication(ownedApplication.getId(), communication.getId(), request);

        assertEquals("Updated summary", response.getSummary());
        assertEquals(CommunicationChannel.EMAIL, response.getChannel());
    }

    @Test
    void deleteCommunication_SoftDeletes() {
        stubOwnedApplication();
        Communication communication = new Communication();
        communication.setId(UUID.randomUUID());
        communication.setApplication(ownedApplication);
        when(communicationRepository.findById(communication.getId())).thenReturn(Optional.of(communication));
        when(communicationRepository.save(any(Communication.class))).thenAnswer(inv -> inv.getArgument(0));

        communicationService.deleteCommunication(ownedApplication.getId(), communication.getId());

        assertNotNull(communication.getDeletedAt());
    }

    @Test
    void getCommunicationsByApplication_ReturnsList() {
        stubOwnedApplication();
        Communication communication = new Communication();
        communication.setId(UUID.randomUUID());
        communication.setApplication(ownedApplication);
        communication.setChannel(CommunicationChannel.PORTAL);
        communication.setDirection(CommunicationDirection.IN);
        communication.setOccurredAt(LocalDateTime.now());
        when(communicationRepository.findByApplicationOrderByOccurredAtDesc(ownedApplication)).thenReturn(List.of(communication));

        List<CommunicationResponse> result = communicationService.getCommunicationsByApplication(ownedApplication.getId());

        assertEquals(1, result.size());
        assertEquals(CommunicationDirection.IN, result.get(0).getDirection());
    }
}
