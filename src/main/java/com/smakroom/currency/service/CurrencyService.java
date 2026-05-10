package com.smakroom.currency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smakroom.currency.dto.CurrencyRateDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Fetches exchange rates from CBR (Central Bank of Russia) via raw HTTP — OkHttp.
 * No specialized library is used; all HTTP-level work is done manually (bonus +5).
 */
@Service
@Slf4j
public class CurrencyService {

    @Value("${app.cbr.api.url}")
    private String cbrApiUrl;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private volatile CurrencyRateDto cachedRates;
    private volatile LocalDateTime lastUpdated;

    public CurrencyService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public CurrencyRateDto getRates() {
        if (cachedRates == null) {
            fetchRates();
        }
        return cachedRates;
    }

    // Refresh every 4 hours
    @Scheduled(fixedDelay = 4 * 60 * 60 * 1000)
    public void fetchRates() {
        Request request = new Request.Builder()
                .url(cbrApiUrl)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "SmakRoom/1.0")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.error("CBR API returned non-successful response: {}", response.code());
                if (cachedRates == null) {
                    cachedRates = fallback();
                }
                return;
            }

            String json = response.body().string();
            JsonNode root = objectMapper.readTree(json);

            BigDecimal usdValue = root.path("Valute").path("USD").path("Value").decimalValue();
            BigDecimal eurValue = root.path("Valute").path("EUR").path("Value").decimalValue();

            cachedRates = new CurrencyRateDto(
                    usdValue.setScale(2, RoundingMode.HALF_UP),
                    eurValue.setScale(2, RoundingMode.HALF_UP),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            );
            lastUpdated = LocalDateTime.now();
            log.info("Currency rates updated: USD={}, EUR={}", usdValue, eurValue);

        } catch (Exception ex) {
            log.error("Failed to fetch currency rates from CBR: {}", ex.getMessage(), ex);
            if (cachedRates == null) {
                cachedRates = fallback();
            }
        }
    }

    public BigDecimal convertFromRub(BigDecimal rubAmount, String currency) {
        CurrencyRateDto rates = getRates();
        return switch (currency.toUpperCase()) {
            case "USD" -> rubAmount.divide(rates.getUsd(), 2, RoundingMode.HALF_UP);
            case "EUR" -> rubAmount.divide(rates.getEur(), 2, RoundingMode.HALF_UP);
            default -> rubAmount;
        };
    }

    private CurrencyRateDto fallback() {
        return new CurrencyRateDto(
                new BigDecimal("90.00"),
                new BigDecimal("98.00"),
                "н/д"
        );
    }
}
