package com.mefy.platemate.api.controllers;

import com.mefy.platemate.api.controllers.concrete.PlateController;
import com.mefy.platemate.business.abstracts.IPlateSearchService;
import com.mefy.platemate.business.abstracts.IPlateService;
import com.mefy.platemate.config.jwt.JwtAuthenticationInterceptor;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.entities.dto.PlateDetailDto;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlateSearchAuthControllerTest {

    @Test
    void searchReturnsUnauthorizedWhenTokenMissing() throws Exception {
        IPlateService plateService = mock(IPlateService.class);
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        JwtAuthenticationInterceptor interceptor = new JwtAuthenticationInterceptor(jwtTokenProvider);
        IPlateSearchService plateSearchService = mock(IPlateSearchService.class);
        PlateController controller = new PlateController(plateService, plateSearchService);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(interceptor)
                .build();

        mockMvc.perform(get("/api/plates/search").param("plate", "34ABC123"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(plateService);
    }

    @Test
    void searchReturnsOkWhenTokenValid() throws Exception {
        IPlateService plateService = mock(IPlateService.class);
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        JwtAuthenticationInterceptor interceptor = new JwtAuthenticationInterceptor(jwtTokenProvider);
        IPlateSearchService plateSearchService = mock(IPlateSearchService.class);
        PlateController controller = new PlateController(plateService, plateSearchService);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(interceptor)
                .build();

        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(77L);
        when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("fatih");

        PlateDetailDto dto = new PlateDetailDto();
        dto.setId(1L);
        dto.setPlateCode("34ABC123");
        dto.setCityName("Istanbul");
        dto.setRatingAverage(0.0);
        dto.setReviewCount(0);
        dto.setTotalRatingSum(0L);
        dto.setRecentReviews(List.of());
        dto.setRecentReportTypes(List.of());
        when(plateSearchService.searchByPlateCode("34ABC123", 77L))
                .thenReturn(new SuccessDataResult<>(dto, "ok"));

        mockMvc.perform(get("/api/plates/search")
                        .param("plate", "34ABC123")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plateCode").value("34ABC123"));

        verify(plateSearchService).searchByPlateCode("34ABC123", 77L);
    }

    @Test
    void detailEndpointReturnsNotFoundAfterRemoval() throws Exception {
        IPlateService plateService = mock(IPlateService.class);
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        JwtAuthenticationInterceptor interceptor = new JwtAuthenticationInterceptor(jwtTokenProvider);
        IPlateSearchService plateSearchService = mock(IPlateSearchService.class);
        PlateController controller = new PlateController(plateService, plateSearchService);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(interceptor)
                .build();

        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(77L);
        when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("fatih");

        mockMvc.perform(get("/api/plates/34ABC123/detail")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(plateService);
    }
}
