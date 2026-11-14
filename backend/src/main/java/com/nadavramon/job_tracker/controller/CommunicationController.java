package com.nadavramon.job_tracker.controller;

import com.nadavramon.job_tracker.dto.CommunicationRequest;
import com.nadavramon.job_tracker.dto.CommunicationResponse;
import com.nadavramon.job_tracker.dto.CommunicationUpdateRequest;
import com.nadavramon.job_tracker.service.CommunicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications/{applicationId}/communications")
public class CommunicationController {

    private final CommunicationService communicationService;

    public CommunicationController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @GetMapping
    public List<CommunicationResponse> getCommunications(@PathVariable UUID applicationId) {
        return communicationService.getCommunicationsByApplication(applicationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunicationResponse createCommunication(@PathVariable UUID applicationId, @Valid @RequestBody CommunicationRequest request) {
        return communicationService.createCommunication(applicationId, request);
    }

    @PatchMapping("/{communicationId}")
    public CommunicationResponse updateCommunication(@PathVariable UUID applicationId, @PathVariable UUID communicationId, @Valid @RequestBody CommunicationUpdateRequest request) {
        return communicationService.updateCommunication(applicationId, communicationId, request);
    }

    @DeleteMapping("/{communicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommunication(@PathVariable UUID applicationId, @PathVariable UUID communicationId) {
        communicationService.deleteCommunication(applicationId, communicationId);
    }
}
