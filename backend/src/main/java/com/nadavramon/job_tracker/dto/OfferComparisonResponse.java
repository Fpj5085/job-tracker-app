package com.nadavramon.job_tracker.dto;

import com.nadavramon.job_tracker.enums.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OfferComparisonResponse {

    private Currency baseCurrency;
    private Map<Currency, BigDecimal> rates;
    private List<OfferComparisonItem> items;

    public OfferComparisonResponse(Currency baseCurrency, Map<Currency, BigDecimal> rates, List<OfferComparisonItem> items) {
        this.baseCurrency = baseCurrency;
        this.rates = rates;
        this.items = items;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public Map<Currency, BigDecimal> getRates() {
        return rates;
    }

    public List<OfferComparisonItem> getItems() {
        return items;
    }

    public static class OfferComparisonItem {

        private OfferResponse offer;
        private BigDecimal normalizedAnnualPackage;

        public OfferComparisonItem(OfferResponse offer, BigDecimal normalizedAnnualPackage) {
            this.offer = offer;
            this.normalizedAnnualPackage = normalizedAnnualPackage;
        }

        public OfferResponse getOffer() {
            return offer;
        }

        public BigDecimal getNormalizedAnnualPackage() {
            return normalizedAnnualPackage;
        }
    }
}
