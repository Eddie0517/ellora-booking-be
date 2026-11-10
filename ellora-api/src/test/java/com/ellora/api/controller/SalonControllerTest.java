package com.ellora.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ellora.application.common.dto.PagedResult;
import com.ellora.application.feature.salon.CreateSalonUseCase;
import com.ellora.application.feature.salon.GetSalonDetailsUseCase;
import com.ellora.application.feature.salon.GetSalonServicesUseCase;
import com.ellora.application.feature.salon.GetSalonStaffUseCase;
import com.ellora.application.feature.salon.GetSalonsUseCase;
import com.ellora.application.feature.salon.dto.NailServiceResponse;
import com.ellora.application.feature.salon.dto.SalonDetailsResponse;
import com.ellora.application.feature.salon.dto.SalonSummaryResponse;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SalonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetSalonsUseCase getSalonsUseCase;

    @MockBean
    private GetSalonDetailsUseCase getSalonDetailsUseCase;

    @MockBean
    private GetSalonServicesUseCase getSalonServicesUseCase;

    @MockBean
    private GetSalonStaffUseCase getSalonStaffUseCase;

    @MockBean
    private CreateSalonUseCase createSalonUseCase;

    @Test
    @DisplayName("GET /api/v1/salons should return paginated list of salons")
    void shouldReturnPaginatedSalons() throws Exception {
        SalonSummaryResponse salon = new SalonSummaryResponse(
                1L, "Ellora Spa", "123 Nguyen Hue", "Ho Chi Minh", "Quan 1", "0901234567", null, BigDecimal.valueOf(4.8), 25);
        PagedResult<SalonSummaryResponse> pagedResult = PagedResult.of(Collections.singletonList(salon), 1, 20, 1);

        when(getSalonsUseCase.execute(eq("Ho Chi Minh"), eq("Spa"), any(), any(), any(), eq("newest"), anyInt(), anyInt()))
                .thenReturn(pagedResult);

        mockMvc.perform(get("/api/v1/salons")
                        .param("city", "Ho Chi Minh")
                        .param("keyword", "Spa")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Ellora Spa"));
    }

    @Test
    @DisplayName("GET /api/v1/salons/{id} should return salon details")
    void shouldReturnSalonDetails() throws Exception {
        SalonDetailsResponse details = new SalonDetailsResponse(
                1L, UUID.randomUUID(), "Ellora Spa", "Top salon", "123 Nguyen Hue", "Ho Chi Minh", "Quan 1",
                "0901234567", "salon@ellora.vn", null, "ACTIVE", 10.77, 106.70, BigDecimal.valueOf(4.8), 25);

        when(getSalonDetailsUseCase.execute(1L)).thenReturn(details);

        mockMvc.perform(get("/api/v1/salons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ellora Spa"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/v1/salons/{id}/services should return service catalog")
    void shouldReturnSalonServices() throws Exception {
        NailServiceResponse service = new NailServiceResponse(
                10L, 1L, 1L, "Gel Manicure", "Basic gel nails", BigDecimal.valueOf(150000), 45, null, true);

        when(getSalonServicesUseCase.execute(1L)).thenReturn(Collections.singletonList(service));

        mockMvc.perform(get("/api/v1/salons/1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Gel Manicure"))
                .andExpect(jsonPath("$[0].price").value(150000));
    }
}
