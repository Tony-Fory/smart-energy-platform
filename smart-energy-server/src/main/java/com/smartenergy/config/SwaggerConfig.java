package com.smartenergy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 配置
 *
 * @author smart-energy
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI smartEnergyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Energy Platform API")
                        .description("智慧能源管理平台接口文档")
                        .version("v0.1.0"));
    }
}
