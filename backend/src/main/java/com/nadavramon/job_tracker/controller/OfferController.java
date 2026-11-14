package com.nadavramon.job_tracker.controller;

import com.nadavramon.job_tracker.dto.OfferComparisonResponse;
import com.nadavramon.job_tracker.dto.OfferRequest;
import com.nadavramon.job_tracker.dto.OfferResponse;
import com.nadavramon.job_tracker.dto.OfferUpdateRequest;
import com.nadavramon.job_tracker.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications/{applicationId}/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public List<OfferResponse> getOffers(@PathVariable UUID applicationId) {
        return offerService.getOffersByApplication(applicationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferResponse createOffer(@PathVariable UUID applicationId, @Valid @RequestBody OfferRequest request) {
        return offerService.createOffer(applicationId, request);
    }

    @PatchMapping("/{offerId}")
    public OfferResponse updateOffer(@PathVariable UUID applicationId, @PathVariable UUID offerId, @Valid @RequestBody OfferUpdateRequest request) {
        return offerService.updateOffer(applicationId, offerId, request);
    }

    @DeleteMapping("/{offerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOffer(@PathVariable UUID applicationId, @PathVariable UUID offerId) {
        offerService.deleteOffer(applicationId, offerId);
    }
}
