package com.platform.infrastructure.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.platform.domain.entity.EconomicIndicator;
import com.platform.domain.entity.IndicatorValue;
import com.platform.domain.entity.Region;
import com.platform.domain.repository.EconomicIndicatorRepository;
import com.platform.domain.repository.IndicatorValueRepository;
import com.platform.domain.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorldBankSyncService {

    private final WebClient worldBankWebClient;
    private final RegionRepository regionRepository;
    private final EconomicIndicatorRepository indicatorRepository;
    private final IndicatorValueRepository valueRepository;

    @Scheduled(cron = "${app.world-bank.sync-cron}")
    @Transactional
    public void syncAll() {
        log.info("Starting World Bank data synchronization");
        List<EconomicIndicator> indicators = indicatorRepository.findByWorldBankCodeIsNotNull();
        List<Region> regions = regionRepository.findAll();

        for (EconomicIndicator indicator : indicators) {
            for (Region region : regions) {
                syncIndicatorForRegion(indicator, region);
            }
        }
        log.info("World Bank sync completed");
    }

    private void syncIndicatorForRegion(EconomicIndicator indicator, Region region) {
        try {
            String url = "/country/{country}/indicator/{indicator}?format=json&per_page=10&mrv=10"
                    .replace("{country}", region.getCountryCode().toLowerCase())
                    .replace("{indicator}", indicator.getWorldBankCode());

            List<List<Object>> response = worldBankWebClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (response == null || response.size() < 2) return;

            List<WorldBankEntry> entries = parseEntries(response.get(1));
            for (WorldBankEntry entry : entries) {
                if (entry.value() == null || entry.date() == null) continue;
                int year = Integer.parseInt(entry.date());
                Optional<IndicatorValue> existing = valueRepository
                        .findByRegionIdAndIndicatorIdAndYear(region.getId(), indicator.getId(), year);
                if (existing.isEmpty()) {
                    valueRepository.save(IndicatorValue.builder()
                            .region(region)
                            .indicator(indicator)
                            .year(year)
                            .value(BigDecimal.valueOf(entry.value()))
                            .sourceUrl("https://api.worldbank.org/v2")
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to sync {} for region {}: {}", indicator.getWorldBankCode(),
                    region.getCode(), e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<WorldBankEntry> parseEntries(Object raw) {
        if (!(raw instanceof List<?> list)) return List.of();
        return list.stream()
                .filter(item -> item instanceof java.util.Map)
                .map(item -> {
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) item;
                    String date = (String) map.get("date");
                    Object val = map.get("value");
                    Double value = val instanceof Number n ? n.doubleValue() : null;
                    return new WorldBankEntry(date, value);
                }).toList();
    }

    private record WorldBankEntry(String date, Double value) {}
}
