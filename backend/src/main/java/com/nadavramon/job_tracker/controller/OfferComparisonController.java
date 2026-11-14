package com.nadavramon.job_tracker.controller;

import com.nadavramon.job_tracker.dto.OfferComparisonResponse;
import com.nadavramon.job_tracker.service.OfferService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/offers")
public class OfferComparisonController {

    private final OfferService offerService;

    public OfferComparisonController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/compare")
    public OfferComparisonResponse compare() {
        return offerService.compareOffers();
    }
}
