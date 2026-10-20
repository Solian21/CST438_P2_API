package edu.csumb.cst438.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("CST438-Project 02 Team#3")
                .description("CST438 Project 02 REST API for recipes and meal plans")
                .version("v1"));
    }
}
