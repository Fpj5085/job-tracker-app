package com.nadavramon.job_tracker.controller;

import com.nadavramon.job_tracker.dto.JobMatchRequest;
import com.nadavramon.job_tracker.dto.JobMatchResponse;
import com.nadavramon.job_tracker.service.JobMatchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/applications/{applicationId}/match")
public class JobMatchController {

    private final JobMatchService jobMatchService;

    public JobMatchController(JobMatchService jobMatchService) {
        this.jobMatchService = jobMatchService;
    }

    @PostMapping
    public JobMatchResponse match(@PathVariable UUID applicationId, @Valid @RequestBody JobMatchRequest request) {
        return jobMatchService.match(applicationId, request);
    }
}
