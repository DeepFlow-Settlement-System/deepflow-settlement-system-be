package com.deepflow.settlementsystem.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "DeepFlow-정산 서비스 API",
                version = "v1",
                description = "DeepFlow 팀-정산 서비스 프로젝트 API 명세서입니다.\n"
        ),
        servers = {
                @Server(url = "https://t2.mobidic.shop", description = "배포 서버"),
                @Server(url = "http://localhost:8080", description = "로컬 개발 서버")
        }
)
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
}
