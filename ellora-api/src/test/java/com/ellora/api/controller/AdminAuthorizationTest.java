package com.ellora.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ellora.application.feature.admin.GetAdminDashboardUseCase;
import com.ellora.application.feature.admin.dto.AdminDashboardResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AdminAuthorizationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetAdminDashboardUseCase dashboard;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAdminDashboard() throws Exception {
        when(dashboard.execute()).thenReturn(new AdminDashboardResponse(2, 1, 1, 3, 4, BigDecimal.TEN));
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(3));
    }
}
