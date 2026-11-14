package com.nadavramon.job_tracker.controller;

import com.nadavramon.job_tracker.dto.InterviewResponse;
import com.nadavramon.job_tracker.service.InterviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interviews")
public class InterviewReminderController {

    private final InterviewService interviewService;

    public InterviewReminderController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /** Browser-side polling endpoint for the 24h interview reminder (no message queue). */
    @GetMapping("/upcoming")
    public List<InterviewResponse> getUpcoming(@RequestParam(defaultValue = "24") int hours) {
        return interviewService.getUpcomingInterviews(hours);
    }
}
