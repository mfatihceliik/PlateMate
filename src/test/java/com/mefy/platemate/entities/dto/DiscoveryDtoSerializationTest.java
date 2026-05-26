package com.mefy.platemate.entities.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mefy.platemate.entities.concrete.PlateReportSeverity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscoveryDtoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void discoveryTabsSerializesAttentionPlatesField() throws Exception {
        DiscoveryTabsDto tabs = new DiscoveryTabsDto(List.of(), List.of(), List.of(), List.of());

        String json = objectMapper.writeValueAsString(tabs);

        assertTrue(json.contains("\"attentionPlates\""));
        assertFalse(json.contains("\"dangerousPlates\""));
    }

    @Test
    void discoveryPlateCardSerializesTrendPlatesField() throws Exception {
        PlateReportTypeDto reportType = new PlateReportTypeDto(
                "TRAFFIC_RULE_VIOLATION",
                "Trafik Kurali Ihlali",
                "Trafik kurali ihlaline iliskin davranis bildirildi",
                "traffic_rule_violation",
                PlateReportSeverity.RED,
                "#E53935",
                4,
                5
        );

        DiscoveryPlateCardDto card = new DiscoveryPlateCardDto(
                "34ABC123",
                "Istanbul",
                4.2,
                13,
                9L,
                4L,
                2L,
                10L,
                27.5,
                LocalDateTime.now(),
                List.of(reportType)
        );

        String json = objectMapper.writeValueAsString(card);

        assertTrue(json.contains("\"trendPlates\""));
        assertFalse(json.contains("\"topReportTypes\""));
    }

    @Test
    void discoveryHomeSerializesTopCityPlatesField() throws Exception {
        DiscoveryHomeDto home = new DiscoveryHomeDto(
                new DiscoveryDailyStatsDto(1L, 2L, 3L),
                new DiscoveryTabsDto(List.of(), List.of(), List.of(), List.of()),
                List.of(new DiscoveryCityStatDto(34, "Istanbul", 10L)),
                List.of(new CityPlateActivityDto("34ABC123", 5L, 1L, LocalDateTime.now(), 4.7, 25)),
                List.of()
        );

        String json = objectMapper.writeValueAsString(home);

        assertTrue(json.contains("\"topCityPlates\""));
    }
}
