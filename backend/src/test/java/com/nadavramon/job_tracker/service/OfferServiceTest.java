package com.nadavramon.job_tracker.service;

import com.nadavramon.job_tracker.dto.OfferComparisonResponse;
import com.nadavramon.job_tracker.dto.OfferRequest;
import com.nadavramon.job_tracker.dto.OfferResponse;
import com.nadavramon.job_tracker.entity.Application;
import com.nadavramon.job_tracker.entity.Offer;
import com.nadavramon.job_tracker.entity.User;
import com.nadavramon.job_tracker.enums.Currency;
import com.nadavramon.job_tracker.exception.ResourceNotFoundException;
import com.nadavramon.job_tracker.exception.ResourceOwnershipException;
import com.nadavramon.job_tracker.repository.ApplicationRepository;
import com.nadavramon.job_tracker.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private CurrentUserService currentUserService;

    private OfferService offerService;

    private User currentUser;
    private User otherUser;
    private Application ownedApplication;

    @BeforeEach
    void setUp() {
        offerService = new OfferService(offerRepository, applicationRepository, currentUserService,
                Currency.CNY, new BigDecimal("7.25"), new BigDecimal("1.0"), new BigDecimal("7.85"));

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        ownedApplication = new Application();
        ownedApplication.setId(UUID.randomUUID());
        ownedApplication.setUser(currentUser);
        ownedApplication.setCompanyName("Acme");
        ownedApplication.setJobRole("Engineer");
    }

    private OfferRequest makeRequest() {
        OfferRequest request = new OfferRequest();
        request.setBaseSalary(new BigDecimal("30000"));
        request.setBonus(new BigDecimal("60000"));
        request.setEquity(new BigDecimal("50000"));
        request.setCurrency(Currency.CNY);
        request.setLocation("Shanghai");
        request.setPtoDays(10);
        return request;
    }

    private Offer makeOffer(Currency currency, String base, String bonus, String equity) {
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setApplication(ownedApplication);
        offer.setUser(currentUser);
        offer.setBaseSalary(new BigDecimal(base));
        offer.setBonus(bonus == null ? null : new BigDecimal(bonus));
        offer.setEquity(equity == null ? null : new BigDecimal(equity));
        offer.setCurrency(currency);
        offer.setLocation("Shanghai");
        return offer;
    }

    private void stubOwnedApplication() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(applicationRepository.findById(ownedApplication.getId())).thenReturn(Optional.of(ownedApplication));
    }

    @Test
    void normalizedAnnualPackage_BaseTimesTwelvePlusBonusPlusEquity() {
        Offer offer = makeOffer(Currency.CNY, "30000", "60000", "50000");
        // 30000 * 12 + 60000 + 50000 = 470000
        assertEquals(0, new BigDecimal("470000.00").compareTo(offerService.normalizedAnnualPackage(offer)));
    }

    @Test
    void normalizedAnnualPackage_NullComponentsTreatedAsZero() {
        Offer offer = makeOffer(Currency.CNY, "10000", null, null);
        assertEquals(0, new BigDecimal("120000.00").compareTo(offerService.normalizedAnnualPackage(offer)));
    }

    @Test
    void normalizedAnnualPackage_ConvertsUsdToCny() {
        Offer offer = makeOffer(Currency.USD, "10000", null, null);
        // 10000 * 12 * 7.25 = 870000
        assertEquals(0, new BigDecimal("870000.00").compareTo(offerService.normalizedAnnualPackage(offer)));
    }

    @Test
    void compareOffers_SortsDescendingByNormalizedPackage() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        Offer low = makeOffer(Currency.CNY, "10000", null, null);
        Offer high = makeOffer(Currency.USD, "10000", null, null);
        when(offerRepository.findByUser(currentUser)).thenReturn(List.of(low, high));

        OfferComparisonResponse result = offerService.compareOffers();

        assertEquals(2, result.getItems().size());
        assertEquals(Currency.CNY, result.getBaseCurrency());
        assertEquals(0, new BigDecimal("870000.00").compareTo(result.getItems().get(0).getNormalizedAnnualPackage()));
        assertEquals(0, new BigDecimal("120000.00").compareTo(result.getItems().get(1).getNormalizedAnnualPackage()));
    }

    @Test
    void createOffer_MapsAllFields() {
        stubOwnedApplication();
        when(offerRepository.save(any(Offer.class))).thenAnswer(inv -> inv.getArgument(0));

        OfferResponse response = offerService.createOffer(ownedApplication.getId(), makeRequest());

        assertEquals(Currency.CNY, response.getCurrency());
        assertEquals("Acme", response.getCompanyName());
        assertEquals(10, response.getPtoDays());
    }

    @Test
    void createOffer_OtherUsersApplication_ThrowsOwnership() {
        ownedApplication.setUser(otherUser);
        stubOwnedApplication();

        assertThrows(ResourceOwnershipException.class, () -> offerService.createOffer(ownedApplication.getId(), makeRequest()));
    }

    @Test
    void createOffer_ApplicationNotFound_Throws404() {
        when(applicationRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> offerService.createOffer(UUID.randomUUID(), makeRequest()));
    }

    @Test
    void deleteOffer_SoftDeletes() {
        stubOwnedApplication();
        Offer offer = makeOffer(Currency.CNY, "1000", null, null);
        when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
        when(offerRepository.save(any(Offer.class))).thenAnswer(inv -> inv.getArgument(0));

        offerService.deleteOffer(ownedApplication.getId(), offer.getId());

        assertNotNull(offer.getDeletedAt());
    }
}
