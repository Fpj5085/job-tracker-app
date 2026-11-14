package com.nadavramon.job_tracker.service;

import com.nadavramon.job_tracker.dto.OfferComparisonResponse;
import com.nadavramon.job_tracker.dto.OfferRequest;
import com.nadavramon.job_tracker.dto.OfferResponse;
import com.nadavramon.job_tracker.dto.OfferUpdateRequest;
import com.nadavramon.job_tracker.entity.Application;
import com.nadavramon.job_tracker.entity.Offer;
import com.nadavramon.job_tracker.entity.User;
import com.nadavramon.job_tracker.enums.Currency;
import com.nadavramon.job_tracker.exception.ResourceNotFoundException;
import com.nadavramon.job_tracker.exception.ResourceOwnershipException;
import com.nadavramon.job_tracker.repository.ApplicationRepository;
import com.nadavramon.job_tracker.repository.OfferRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OfferService {

    static final int MONTHS_PER_YEAR = 12;

    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final CurrentUserService currentUserService;
    private final Currency baseCurrency;
    private final Map<Currency, BigDecimal> rates;

    public OfferService(OfferRepository offerRepository, ApplicationRepository applicationRepository, CurrentUserService currentUserService,
                        @Value("${offer.base-currency:CNY}") Currency baseCurrency,
                        @Value("${offer.currency-rate.USD:7.25}") BigDecimal rateUsd,
                        @Value("${offer.currency-rate.CNY:1.0}") BigDecimal rateCny,
                        @Value("${offer.currency-rate.EUR:7.85}") BigDecimal rateEur) {
        this.offerRepository = offerRepository;
        this.applicationRepository = applicationRepository;
        this.currentUserService = currentUserService;
        this.baseCurrency = baseCurrency;
        this.rates = new EnumMap<>(Currency.class);
        this.rates.put(Currency.USD, rateUsd);
        this.rates.put(Currency.CNY, rateCny);
        this.rates.put(Currency.EUR, rateEur);
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersByApplication(UUID applicationId) {
        Application application = getOwnedApplication(applicationId);
        return offerRepository.findByApplication(application).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OfferResponse createOffer(UUID applicationId, OfferRequest request) {
        Application application = getOwnedApplication(applicationId);
        Offer offer = new Offer();
        offer.setApplication(application);
        offer.setUser(application.getUser());
        offer.setBaseSalary(request.getBaseSalary());
        offer.setBonus(request.getBonus());
        offer.setEquity(request.getEquity());
        offer.setCurrency(request.getCurrency());
        offer.setLocation(request.getLocation());
        offer.setRemotePolicy(request.getRemotePolicy());
        offer.setInsurance(request.getInsurance());
        offer.setPtoDays(request.getPtoDays());
        offer.setStartDate(request.getStartDate());
        offer.setNotes(request.getNotes());
        return toResponse(offerRepository.save(offer));
    }

    @Transactional
    public OfferResponse updateOffer(UUID applicationId, UUID offerId, OfferUpdateRequest request) {
        Offer offer = getOwnedOffer(applicationId, offerId);
        if (request.getBaseSalary() != null) offer.setBaseSalary(request.getBaseSalary());
        if (request.getBonus() != null) offer.setBonus(request.getBonus());
        if (request.getEquity() != null) offer.setEquity(request.getEquity());
        if (request.getCurrency() != null) offer.setCurrency(request.getCurrency());
        if (request.getLocation() != null) offer.setLocation(request.getLocation());
        if (request.getRemotePolicy() != null) offer.setRemotePolicy(request.getRemotePolicy());
        if (request.getInsurance() != null) offer.setInsurance(request.getInsurance());
        if (request.getPtoDays() != null) offer.setPtoDays(request.getPtoDays());
        if (request.getStartDate() != null) offer.setStartDate(request.getStartDate());
        if (request.getNotes() != null) offer.setNotes(request.getNotes());
        return toResponse(offerRepository.save(offer));
    }

    @Transactional
    public void deleteOffer(UUID applicationId, UUID offerId) {
        Offer offer = getOwnedOffer(applicationId, offerId);
        offer.setDeletedAt(LocalDateTime.now());
        offerRepository.save(offer);
    }

    /**
     * Normalized annual package = (base * 12 + bonus + equity) converted to the base currency
     * using the configured static rate table (no external rate API).
     */
    @Transactional(readOnly = true)
    public OfferComparisonResponse compareOffers() {
        List<Offer> offers = offerRepository.findByUser(getCurrentUser());
        List<OfferComparisonResponse.OfferComparisonItem> items = offers.stream()
                .map(o -> new OfferComparisonResponse.OfferComparisonItem(toResponse(o), normalizedAnnualPackage(o)))
                .sorted(Comparator.comparing(OfferComparisonResponse.OfferComparisonItem::getNormalizedAnnualPackage).reversed())
                .toList();
        return new OfferComparisonResponse(baseCurrency, Map.copyOf(rates), items);
    }

    BigDecimal normalizedAnnualPackage(Offer offer) {
        BigDecimal base = offer.getBaseSalary() == null ? BigDecimal.ZERO : offer.getBaseSalary();
        BigDecimal bonus = offer.getBonus() == null ? BigDecimal.ZERO : offer.getBonus();
        BigDecimal equity = offer.getEquity() == null ? BigDecimal.ZERO : offer.getEquity();
        BigDecimal annual = base.multiply(BigDecimal.valueOf(MONTHS_PER_YEAR)).add(bonus).add(equity);
        return annual.multiply(rates.getOrDefault(offer.getCurrency(), BigDecimal.ONE)).setScale(2, RoundingMode.HALF_UP);
    }

    private Application getOwnedApplication(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getUser().getId().equals(getCurrentUser().getId()))
            throw new ResourceOwnershipException("Access denied");
        return application;
    }

    private Offer getOwnedOffer(UUID applicationId, UUID offerId) {
        getOwnedApplication(applicationId);
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
        if (!offer.getApplication().getId().equals(applicationId))
            throw new ResourceNotFoundException("Offer not found");
        return offer;
    }

    private User getCurrentUser() {
        return currentUserService.getCurrentUser();
    }

    private OfferResponse toResponse(Offer offer) {
        return new OfferResponse(
                offer.getId(),
                offer.getApplication().getId(),
                offer.getApplication().getCompanyName(),
                offer.getApplication().getJobRole(),
                offer.getBaseSalary(),
                offer.getBonus(),
                offer.getEquity(),
                offer.getCurrency(),
                offer.getLocation(),
                offer.getRemotePolicy(),
                offer.getInsurance(),
                offer.getPtoDays(),
                offer.getStartDate(),
                offer.getNotes());
    }
}
