package com.nadavramon.job_tracker.controller;

import com.nadavramon.job_tracker.dto.InterviewRequest;
import com.nadavramon.job_tracker.dto.InterviewResponse;
import com.nadavramon.job_tracker.dto.InterviewUpdateRequest;
import com.nadavramon.job_tracker.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications/{applicationId}/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    public List<InterviewResponse> getInterviews(@PathVariable UUID applicationId) {
        return interviewService.getInterviewsByApplication(applicationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewResponse createInterview(@PathVariable UUID applicationId, @Valid @RequestBody InterviewRequest request) {
        return interviewService.createInterview(applicationId, request);
    }

    @GetMapping("/{interviewId}")
    public InterviewResponse getInterview(@PathVariable UUID applicationId, @PathVariable UUID interviewId) {
        return interviewService.getInterview(applicationId, interviewId);
    }

    @PatchMapping("/{interviewId}")
    public InterviewResponse updateInterview(@PathVariable UUID applicationId, @PathVariable UUID interviewId, @Valid @RequestBody InterviewUpdateRequest request) {
        return interviewService.updateInterview(applicationId, interviewId, request);
    }

    @DeleteMapping("/{interviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInterview(@PathVariable UUID applicationId, @PathVariable UUID interviewId) {
        interviewService.deleteInterview(applicationId, interviewId);
    }
}
