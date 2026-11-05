package com.ellora.api.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.exception.DomainException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Import(GlobalExceptionHandlerTest.ExceptionTriggerController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    record DummyValidationRequest(@NotBlank(message = "Tên không được để trống") String name) {}

    @RestController
    @RequestMapping("/test/exceptions")
    static class ExceptionTriggerController {

        @PostMapping("/validation")
        void triggerValidation(@Valid @RequestBody DummyValidationRequest request) {}

        @GetMapping("/business")
        void triggerBusinessRule() {
            throw new BusinessRuleException("Slot thời gian đã có khách đặt.");
        }

        @GetMapping("/domain")
        void triggerDomain() {
            throw new DomainException("Dữ liệu domain không hợp lệ.");
        }

        @GetMapping("/generic")
        void triggerGeneric() {
            throw new RuntimeException("Unexpected database failure.");
        }
    }

    @Test
    @DisplayName("MethodArgumentNotValidException returns 400 Bad Request with Problem Details")
    void shouldHandleValidationException() throws Exception {
        mockMvc.perform(post("/test/exceptions/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type").value("https://ellora.vn/errors/validation-error"))
                .andExpect(jsonPath("$.title").value("Dữ liệu không hợp lệ"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.invalidParams[0].name").value("name"))
                .andExpect(jsonPath("$.invalidParams[0].reason").value("Tên không được để trống"));
    }

    @Test
    @DisplayName("BusinessRuleException returns 422 Unprocessable Entity with Problem Details")
    void shouldHandleBusinessRuleException() throws Exception {
        mockMvc.perform(get("/test/exceptions/business"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type").value("https://ellora.vn/errors/business-rule-violation"))
                .andExpect(jsonPath("$.title").value("Vi phạm quy tắc nghiệp vụ"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.detail").value("Slot thời gian đã có khách đặt."))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("DomainException returns 400 Bad Request with Problem Details")
    void shouldHandleDomainException() throws Exception {
        mockMvc.perform(get("/test/exceptions/domain"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type").value("https://ellora.vn/errors/domain-error"))
                .andExpect(jsonPath("$.title").value("Lỗi nghiệp vụ cơ bản"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Dữ liệu domain không hợp lệ."))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("Generic Exception returns 500 Internal Server Error with sanitized Problem Details")
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/test/exceptions/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type").value("https://ellora.vn/errors/internal-server-error"))
                .andExpect(jsonPath("$.title").value("Lỗi hệ thống nội bộ"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."))
                .andExpect(jsonPath("$.traceId").exists());
    }
}
