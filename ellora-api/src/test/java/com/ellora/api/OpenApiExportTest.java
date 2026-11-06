package com.ellora.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class OpenApiExportTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Export OpenAPI 3.0 specification directly to contracts/openapi.json")
    void exportOpenApiSpecification() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String rawJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertNotNull(rawJson, "OpenAPI specification payload must not be null");

        // Parse and validate structure
        JsonNode rootNode = objectMapper.readTree(rawJson);
        assertTrue(rootNode.has("openapi"), "Specification must contain 'openapi' version");
        assertTrue(rootNode.has("info"), "Specification must contain 'info'");
        assertTrue(rootNode.has("paths"), "Specification must contain 'paths'");
        assertTrue(rootNode.path("paths").has("/healthz"), "Specification must contain '/healthz' endpoint");
        assertTrue(
                rootNode.path("components").path("schemas").has("HealthStatus"),
                "Components must contain HealthStatus schema");
        assertTrue(
                rootNode.path("components").path("schemas").has("ProblemDetail"),
                "Components must contain ProblemDetail schema");
        assertTrue(
                rootNode.path("components").path("schemas").has("InvalidParam"),
                "Components must contain InvalidParam schema");

        // Format indented JSON
        ObjectMapper prettyMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
        String prettyJson = prettyMapper.writeValueAsString(rootNode);

        // Resolve contracts/openapi.json target path
        Path targetFile = resolveContractsPath().resolve("openapi.json");
        Files.createDirectories(targetFile.getParent());
        Files.writeString(targetFile, prettyJson + "\n", StandardCharsets.UTF_8);

        assertTrue(Files.exists(targetFile), "contracts/openapi.json must exist after export");
    }

    private Path resolveContractsPath() {
        // Look in current working directory
        Path current = Paths.get("contracts");
        if (Files.exists(current)) {
            return current.toAbsolutePath();
        }
        // Look in parent directory (when running from submodule directory)
        Path parent = Paths.get("../contracts");
        if (Files.exists(parent)) {
            return parent.toAbsolutePath().normalize();
        }
        // Fallback: look relative to current execution root
        return Paths.get("contracts").toAbsolutePath();
    }
}
