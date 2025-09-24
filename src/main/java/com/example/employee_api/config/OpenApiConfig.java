package com.example.employee_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class OpenApiConfig {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Bean
    public OpenAPI customOpenAPI() {
        int endpointCount = getEndpointCount();

        return new OpenAPI()
                .info(new Info()
                        .title("Employee API")
                        .description("Employee Management System API Documentation\n\n" +
                                   "📊 **Total Available Endpoints: " + endpointCount + "**\n\n" +
                                   "This API provides comprehensive employee management functionality including:\n" +
                                   "• Employee CRUD operations\n" +
                                   "• Department management\n" +
                                   "• Performance reviews\n" +
                                   "• Document handling\n" +
                                   "• Authentication & authorization\n\n" +
                                   "🔐 **Authentication**: Bearer token (JWT) required for most endpoints")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    private int getEndpointCount() {
        return requestMappingHandlerMapping.getHandlerMethods().size();
    }
}