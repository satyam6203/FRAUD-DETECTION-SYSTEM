package com.fraud_detection_service.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fraudServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fraud Detection Service API")
                        .description("Consumes payment events, scores risk, " +
                                "and manages fraud alerts")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Fraud Detection System")))
                .servers(List.of(new Server()
                        .url("http://localhost:8082")
                        .description("Local Development")));
    }
}
