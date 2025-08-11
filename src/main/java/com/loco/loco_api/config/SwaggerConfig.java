package com.loco.loco_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  // Swagger(OpenAPI)에서 JWT 인증 방식을 명명할 때 사용할 키 값
  private static final String SECURITY_SCHEME_NAME = "JWT";

  /**
   * OpenAPI 전역 설정 Bean
   * - API 문서 기본 정보(title, version 등) 지정
   * - JWT 인증 스키마를 SecurityScheme로 등록
   * - 모든 요청에 기본적으로 JWT SecurityRequirement를 적용
   */
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
            // 문서 메타 정보
            .info(new Info()
                    .title("LOCO API 문서")  // Swagger UI 상단에 표시될 제목
                    .version("v1.0.0")     // 문서 버전
            )
            // JWT 인증 스키마 적용 (모든 요청에 Authorization: Bearer 토큰 필요)
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components().addSecuritySchemes(
                    SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                            .name(SECURITY_SCHEME_NAME)      // SecurityRequirement와 매칭될 이름
                            .type(SecurityScheme.Type.HTTP)  // HTTP 인증 방식
                            .scheme("bearer")                // Bearer 토큰
                            .bearerFormat("JWT")             // JWT 형식 지정
            ));
  }

  /**
   * API 그룹 설정 Bean
   * - /v3/api-docs/v1 엔드포인트 생성
   * - pathsToMatch() 경로에 해당하는 API만 그룹에 포함
   * - Swagger UI에서 그룹 선택 가능
   */
  @Bean
  public GroupedOpenApi v1OpenApi() {
    return GroupedOpenApi.builder()
            .group("v1")                 // 그룹명 (/v3/api-docs/v1) → Swagger UI에서 'v1'로 표시
            .pathsToMatch("/api/v1/**")  // 해당 경로 패턴에 속하는 API만 이 그룹에 포함
            .build();
  }
}
