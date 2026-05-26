package com.mefy.platemate.entities.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserProfileDtoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializeContainsTopLevelReviewStatusCountsAndMetaEvaluationTotals() throws Exception {
        UserReviewStatusCountsDto statusCounts = new UserReviewStatusCountsDto(1, 2, 3, 4, 5, 6);
        UserReviewEvaluationTotalsDto evaluationTotals = new UserReviewEvaluationTotalsDto(1, 2, 3, 4, 5, 6);
        UserProfileReviewPageMetaDto meta = new UserProfileReviewPageMetaDto(
                0,
                20,
                1L,
                1,
                false,
                false,
                evaluationTotals
        );
        UserProfileReviewPageDto page = new UserProfileReviewPageDto(List.of(), meta);

        UserProfileDto dto = new UserProfileDto();
        dto.setId(1L);
        dto.setUsername("fatih");
        dto.setReviewStatusCounts(statusCounts);
        dto.setPlateReviews(page);

        String json = objectMapper.writeValueAsString(dto);

        assertFalse(json.contains("recentComments"));
        assertTrue(json.contains("\"reviewStatusCounts\""));
        assertTrue(json.contains("\"plateReviews\""));
        assertFalse(json.contains("\"statusCounts\""));
        assertTrue(json.contains("\"evaluationTotals\""));
    }
}
