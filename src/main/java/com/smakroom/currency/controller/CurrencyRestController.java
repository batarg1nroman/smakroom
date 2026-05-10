package com.smakroom.currency.controller;

import com.smakroom.currency.dto.CurrencyRateDto;
import com.smakroom.currency.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyRestController {

    private final CurrencyService currencyService;

    @GetMapping("/rates")
    public ResponseEntity<CurrencyRateDto> getRates() {
        return ResponseEntity.ok(currencyService.getRates());
    }

    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convert(
            @RequestParam BigDecimal amount,
            @RequestParam String currency) {
        BigDecimal converted = currencyService.convertFromRub(amount, currency);
        return ResponseEntity.ok(Map.of(
                "originalRub", amount,
                "currency", currency.toUpperCase(),
                "converted", converted
        ));
    }
}
