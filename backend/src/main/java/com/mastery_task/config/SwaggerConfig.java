package com.mastery_task.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI documentProcessingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Document Processing API")
                        .version("1.0")
                        .description("API for uploading, extracting, validating and reviewing business documents."));
    }
}
