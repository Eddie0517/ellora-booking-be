package com.ellora.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 3.0 configuration.
 * Configures API metadata, server environments, JWT security schemes,
 * and canonical schema components (ProblemDetail, InvalidParam, PagedResult).
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ellora Nail Salon Booking API")
                        .description("API Documentation for Ellora Nail Salon Management and Booking System")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Containerized Environment")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("ES256 signed JWT Bearer Token")));
    }

    @Bean
    public OpenApiCustomizer openApiSchemaCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }
            components.addSchemas("InvalidParam", createInvalidParamSchema());
            components.addSchemas("ProblemDetail", createProblemDetailSchema());
            components.addSchemas("PagedResult", createPagedResultSchema());
        };
    }

    private Schema<?> createInvalidParamSchema() {
        return new ObjectSchema()
                .name("InvalidParam")
                .addProperty("name", new StringSchema().example("email"))
                .addProperty("reason", new StringSchema().example("Email không đúng định dạng RFC 5322"))
                .required(List.of("name", "reason"));
    }

    private Schema<?> createProblemDetailSchema() {
        return new ObjectSchema()
                .name("ProblemDetail")
                .description("RFC 9457 Problem Details schema")
                .addProperty(
                        "type",
                        new StringSchema().format("uri").example("https://ellora.vn/errors/auth.invalid_credentials"))
                .addProperty("title", new StringSchema().example("Thông tin xác thực không hợp lệ"))
                .addProperty("status", new IntegerSchema().example(401))
                .addProperty("detail", new StringSchema().example("Email hoặc mật khẩu không chính xác."))
                .addProperty("instance", new StringSchema().example("/api/auth/login"))
                .addProperty("traceId", new StringSchema().example("c2b5e297-88eb-4209-906d-e4ebc52119c8"))
                .addProperty(
                        "invalidParams",
                        new ArraySchema().items(new Schema<>().$ref("#/components/schemas/InvalidParam")))
                .required(List.of("type", "title", "status"));
    }

    private Schema<?> createPagedResultSchema() {
        return new ObjectSchema()
                .name("PagedResult")
                .addProperty("items", new ArraySchema().items(new ObjectSchema()))
                .addProperty("page", new IntegerSchema().example(1))
                .addProperty("pageSize", new IntegerSchema().example(20))
                .addProperty("totalItems", new IntegerSchema().format("int64").example(120))
                .addProperty("totalPages", new IntegerSchema().example(6))
                .required(List.of("items", "page", "pageSize", "totalItems", "totalPages"));
    }
}
