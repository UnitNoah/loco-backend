package com.loco.loco_api.config;

import com.loco.loco_api.common.dto.oauth.CustomOAuth2User;
import com.loco.loco_api.domain.user.UserEntity;
import com.loco.loco_api.repository.UserRepository;
import com.loco.loco_api.service.CustomOAuth2UserService;
import com.loco.loco_api.service.JwtService;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final JwtService jwtService;

  /**
   * JWKS(공개키 세트)에서 키를 가져와 RS256 서명/만료를 검증하는 JwtDecoder 빈을 생성한다.
   * 추가로 issuer(iss)와 audience(aud)도 엄격 검증하도록 밸리데이터를 연결한다.
   * 반환된 디코더는 리소스 서버의 JWT 검증에 자동 사용된다.
   */
  @Bean
  public JwtDecoder jwtDecoder(RSAKey rsaJwk) throws Exception {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaJwk.toRSAPublicKey()).build();

    // 내 서버에서 발급한 JWT의 iss 값과 맞춰야 함
    OAuth2TokenValidator<Jwt> withIssuer =
            JwtValidators.createDefaultWithIssuer("https://api.loco.com");

    OAuth2TokenValidator<Jwt> withAudience =
            new JwtClaimValidator<List<String>>("aud",
                    aud -> aud != null && aud.contains("loco-web"));

    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
    return decoder;
  }



  /**
   * Bearer 토큰 추출 전략을 정의한다.
   * 1순위: Authorization 헤더, 2순위: HttpOnly 쿠키(access_token)에서 토큰을 읽는다.
   * 쿠키로 토큰을 전달하는 경우 서버가 대신 꺼내기 위해 필요하다.
   */
  @Bean
  public BearerTokenResolver bearerTokenResolver() {
    final String ACCESS_COOKIE = "access_token";
    DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();

    return request -> {
      // Authorization 헤더 우선
      String headerToken = delegate.resolve(request);
      if (headerToken != null) return headerToken;

      // 쿠키에서 꺼내기
      var cookies = request.getCookies();
      if (cookies != null) {
        for (var c : cookies) {
          if (ACCESS_COOKIE.equals(c.getName())) return c.getValue();
        }
      }
      return null;
    };
  }

  /**
   * 애플리케이션의 보안 필터 체인을 구성한다.
   * - CSRF/폼로그인/Basic 비활성화, CORS 적용
   * - 401/403 예외 응답 일관화
   * - 경로별 접근 정책(공개/인증 필요) 정의
   * - OAuth2 로그인 성공 시 JWT 발급 → 쿠키로 전송
   * - 리소스 서버의 JWT 검증 및 권한 매핑 설정
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
            .csrf(csrf -> {
              CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
              repo.setCookiePath("/");
              repo.setCookieName("XSRF-TOKEN");       // SPA가 읽는 쿠키 이름
              // 기본 헤더: X-XSRF-TOKEN
              CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();

              csrf
                      .csrfTokenRepository(repo)
                      .csrfTokenRequestHandler(handler)
                      .ignoringRequestMatchers(
                              new AntPathRequestMatcher("/.well-known/**", HttpMethod.GET.name()),
                              new AntPathRequestMatcher("/oauth2/authorization/**", HttpMethod.GET.name()),
                              new AntPathRequestMatcher("/login/oauth2/code/**", HttpMethod.GET.name()),
                              new AntPathRequestMatcher("/actuator/health", HttpMethod.GET.name()),
                              new AntPathRequestMatcher("/actuator/info",   HttpMethod.GET.name())
                      );
            })
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults()) // ↓ 아래 corsConfigurationSource() 사용

            // 401/403 응답을 일관되게
            .exceptionHandling(e -> e
                    .authenticationEntryPoint((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                    .accessDeniedHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
            )

            // 경로별 인가
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/", "/public/**",
                            "/.well-known/jwks.json",
                            "/oauth2/**", "/login/**",
                            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                            "/error",
                            "/api/v1/users/logout",   // 로그아웃 허용
                            "/api/v1/users/profile",
                            "/api/v1/rooms/public/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            )

            // OAuth2 인가요청 저장은 세션 사용(간단/안정). 로그인 후엔 JWT로 인증.
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            // OAuth2 로그인 + 성공/실패 핸들러
            .oauth2Login(oauth -> oauth
                    .userInfoEndpoint(ui -> ui
                            // 일반 OAuth2 (네이버) 요청은 CustomOAuth2UserService로 처리
                            .userService(customOAuth2UserService)
                    )
                    .successHandler((req, res, auth) -> {
                      Object principal = auth.getPrincipal();

                      String provider, oauthId, email, displayName, profileImage;

                      if (principal instanceof CustomOAuth2User cu) {
                        // 네이버/카카오
                        provider     = cu.getProvider();
                        oauthId      = cu.getOauthId();
                        email        = cu.getEmail();
                        displayName  = cu.getName();
                        profileImage = cu.getUser().getProfileImage();
                      }
                      else if (principal instanceof org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser gu) {
                        // 구글
                        provider     = "google";
                        oauthId      = gu.getSubject();
                        email        = (String) gu.getAttributes().get("email");
                        displayName  = (String) gu.getAttributes().getOrDefault("name", email);
                        profileImage = (String) gu.getAttributes().get("picture");
                      }
                      else {
                        throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
                      }

                      String sub = provider + "_" + oauthId;

                      String access = jwtService.issueAccessToken(
                              sub,
                              Map.of(
                                      "email", email,
                                      "nickname", displayName,   // 프론트에서 nickname 클레임 읽고 있음
                                      "profileImage", profileImage,
                                      "roles", List.of("ROLE_USER")
                              )
                      );

                      boolean secure = isSecure(req);
                      setCookie(res, "access_token", access, 90000, true, secure, "/", secure ? "None" : "Lax");

                      res.sendRedirect("http://localhost:3000");
                    })

                    .failureHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
            )

            // 리소스 서버: JWT 검증. + roles 클레임 매핑을 위한 converter 설정
            .oauth2ResourceServer(oauth2 -> oauth2
                    .bearerTokenResolver(bearerTokenResolver())             // ★ 추가
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

    return http.build();
  }

  /**
   * JWT의 'roles' 클레임을 Spring Security 권한(GrantedAuthority)으로 변환한다.
   * - scope/scp는 기본 컨버터로 SCOPE_* 권한 생성
   * - roles는 "ROLE_*" 접두어를 보장하여 hasRole/hasAuthority 매칭에 사용 가능하게 한다.
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter(); // scope -> SCOPE_*
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
      // 중복 제거 + 순서 유지
      List<GrantedAuthority> authorities = new ArrayList<>(scopes.convert(jwt));

      Object roles = jwt.getClaim("roles");
      if (roles instanceof Collection<?> col) {
        authorities.addAll(
                col.stream()
                        .map(String::valueOf)
                        .filter(s -> !s.isBlank())
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
      } else if (roles instanceof String s) {
        for (String r : s.split(",")) {
          r = r.trim();
          if (!r.isEmpty()) {
            if (!r.startsWith("ROLE_")) r = "ROLE_" + r;
            authorities.add(new SimpleGrantedAuthority(r));
          }
        }
      }
      return authorities;
    });
    return converter;
  }

  /**
   * CORS 정책을 정의한다.
   * - 허용 Origin/Method/Header 지정
   * - 쿠키 등 크리덴셜 전송 허용 여부
   * - 프리플라이트 캐시 시간(maxAge) 설정
   * 브라우저가 쿠키를 보내려면 정확한 Origin과 allowCredentials(true)가 필요하다.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration c = new CorsConfiguration();

    c.setAllowedOrigins(List.of(
            "http://localhost:3000"          // 로컬 프론트
            // "https://loco.com",       // 운영 프론트
    ));

    // 허용 메서드
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    // 허용 헤더 (인증/JSON 기본)
    c.setAllowedHeaders(List.of("Authorization", "Content-Type"));

    // 쿠키/인증정보 포함 요청 허용 (SameSite=None; Secure + credentials: 'include' 조합이 필요)
    c.setAllowCredentials(true);
    // Preflight 캐시 (초). 프론트 트래픽이 많으면 1~2시간으로 늘려 서버 부담 감소
    c.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return source;
  }

  /**
   * HttpOnly 쿠키를 수동으로 셋팅.
   *
   * @param res       응답 객체
   * @param name      쿠키 이름 (예: "access_token")
   * @param value     쿠키 값 (예: JWT)
   * @param maxAge    만료 시간(초). 0이면 즉시 삭제, 음수면 세션 쿠키
   * @param httpOnly  true면 JS에서 접근 불가(document.cookie 차단) → XSS 완화
   * @param secure    true면 HTTPS에서만 전송 → 크로스도메인에서 SameSite=None이면 필수
   * @param path      쿠키 유효 경로 (보통 "/")
   * @param sameSite  SameSite 정책: "Lax" | "Strict" | "None"
   *                  - Lax: 기본 권장(대부분의 내비게이션 요청 허용)
   *                  - None: 크로스사이트 전송 허용(반드시 Secure=true 필요, HTTPS 필수)
   *                  - Strict: 완전 엄격(대부분의 크로스사이트 전송 차단)
   *
   * 브라우저 특성:
   * - 크로스도메인에서 인증 쿠키를 쓰려면 SameSite=None; Secure 조합이 필요(HTTPS 필수).
   * - 동일 도메인/서브도메인만 쓸 거면 기본 Lax로 충분한 경우가 많음. *
   * */
  private static void setCookie(HttpServletResponse res, String name, String value,
                                int maxAge, boolean httpOnly, boolean secure, String path, String sameSite) {
    StringBuilder sb = new StringBuilder();
    sb.append(name).append("=").append(value)
            .append("; Path=").append(path)
            .append("; Max-Age=").append(maxAge)
            .append("; SameSite=").append(sameSite);
    if (httpOnly) sb.append("; HttpOnly");
    if (secure) sb.append("; Secure");
    res.addHeader("Set-Cookie", sb.toString());
  }

  @Bean
  public OidcUserService customOidcUserService(UserRepository userRepository) {
    return new OidcUserService() {
      @Override
      public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String provider = "google";
        String oauthId = oidcUser.getSubject();
        String email   = (String) oidcUser.getAttributes().get("email");
        String name    = (String) oidcUser.getAttributes().get("name");
        String picture = (String) oidcUser.getAttributes().get("picture");

        userRepository.findByProviderAndOauthId(provider, oauthId)
                .map(u -> {
                  u.updateProfile(name, picture, email);
                  return u;
                })
                .orElseGet(() -> userRepository.save(
                        UserEntity.builder()
                                .provider(provider)
                                .oauthId(oauthId)
                                .email(email)
                                .nickname(name)
                                .profileImageUrl(picture)
                                .build()
                ));

        return oidcUser;
      }
    };
  }


  /**
   * 요청이 HTTPS로 들어왔는지 판단한다.
   * - 서블릿 컨테이너 판단(req.isSecure()) 또는
   * - 프록시/로드밸런서가 전달한 X-Forwarded-Proto 헤더가 https인지 확인한다.
   */
  private static boolean isSecure(jakarta.servlet.http.HttpServletRequest req) {
    return req.isSecure() || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
  }
}
