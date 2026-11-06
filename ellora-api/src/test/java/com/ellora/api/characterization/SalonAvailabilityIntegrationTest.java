package com.ellora.api.characterization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class SalonAvailabilityIntegrationTest extends BaseCharacterizationTest {
    @Autowired MockMvc mvc;

    @Test
    void returnsTimesThatFitHoursAndDoNotOverlapBookings() throws Exception {
        UUID owner = insertUser(UUID.randomUUID() + "@example.com", "hash", "Owner", "0900000000", "SALON_OWNER");
        Long salon = insertSalon(owner, "Salon", "Address", "City", "ACTIVE");
        Long category = insertCategory("Nails");
        Long service = insertService(salon, category, "Manicure", new BigDecimal("200000"), 60);
        Long employee = insertEmployee(salon, "Linh", "0900000001");
        jdbcTemplate.update("INSERT INTO salon_working_hours (salon_id, day_of_week, open_time, close_time, closed) "
                + "VALUES (?, 'FRIDAY', '09:00', '12:00', false)", salon);
        insertBooking(null, salon, service, employee,
                OffsetDateTime.parse("2027-01-01T09:30:00+07:00"), 60, new BigDecimal("200000"), "CONFIRMED");

        mvc.perform(get("/api/v1/salons/" + salon + "/availability")
                .param("date", "2027-01-01").param("serviceId", service.toString()).param("employeeId", employee.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("10:30:00"))
                .andExpect(jsonPath("$[1]").value("11:00:00"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }
}
