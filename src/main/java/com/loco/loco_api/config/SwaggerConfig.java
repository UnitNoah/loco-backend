package com.loco.loco_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  private static final String SECURITY_SCHEME_NAME = "JWT";

  /**
   * Swagger(OpenAPI 3) 설정 클래스
   * JWT 인증을 사용하는 API 보안을 문서화하며,
   * 전체 서비스 API 명세의 메타데이터(title, description 등)를 정의
   */
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
            .info(apiInfo())
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components()
                    .addSecuritySchemes(SECURITY_SCHEME_NAME,
                            new SecurityScheme()
                                    .name(SECURITY_SCHEME_NAME)
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")
                    )
            );
  }

  /**
   * Swagger 문서 정보
   * @return
   */
  private Info apiInfo() {
    return new Info()
            .title("LOCO API 문서")
            .description("""
                    LOCO는 사용자가 위치 기반으로 주제별 장소(Room)를 공유하고 
                    각 방에 자유롭게 참여하거나 즐겨찾기를 통해 관리할 수 있는 플랫폼입니다.
                    모든 인증된 요청은 OAuth2 기반 소셜 로그인 후 발급된 JWT를 이용하여 보호됩니다.
                    """)
            .version("v1.0.0");
  }
}

