package com.thoth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocOpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("THOTH C.O.R.E API")
                .version("1.0.0")
                .description("Computer Operations Resources Environment - API REST")
                .contact(new Contact()
                    .name("THOTH Support")
                    .email("support@thoth-core.com")));
    }
}