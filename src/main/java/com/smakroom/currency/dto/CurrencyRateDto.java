package com.smakroom.currency.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateDto {
    private BigDecimal usd;
    private BigDecimal eur;
    private String updatedAt;

    public Map<String, BigDecimal> toMap() {
        return Map.of("USD", usd, "EUR", eur);
    }
}
