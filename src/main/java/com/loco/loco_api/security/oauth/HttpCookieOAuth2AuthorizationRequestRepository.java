package com.loco.loco_api.security.oauth;

import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  public static final String OAUTH2_AUTH_REQUEST_COOKIE = "oauth2_auth_request";
  public static final String REDIRECT_URI_COOKIE        = "redirect_uri";
  private static final int EXPIRE_SECONDS = 180; // 3분

  @Value("${security.cookies.force-secure:true}")
  private boolean forceSecure; // 운영: true(항상 Secure), 로컬: false 허용

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE)
            .map(c -> deserialize(c.getValue(), OAuth2AuthorizationRequest.class))
            .orElse(null);
  }

  @Override
  public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                       HttpServletRequest request, HttpServletResponse response) {
    if (authorizationRequest == null) {
      deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE);
      deleteCookie(request, response, REDIRECT_URI_COOKIE);
      return;
    }
    String value = serialize(authorizationRequest);
    addCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE, value, EXPIRE_SECONDS);

    String redirectUri = request.getParameter("redirect_uri");
    if (redirectUri != null && !redirectUri.isBlank()) {
      addCookie(request, response, REDIRECT_URI_COOKIE, redirectUri, EXPIRE_SECONDS);
    }
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                               HttpServletResponse response) {
    OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
    deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE);
    return req;
  }

  // ---- helpers ----
  private static java.util.Optional<Cookie> getCookie(HttpServletRequest req, String name) {
    if (req.getCookies() == null) return java.util.Optional.empty();
    for (Cookie c : req.getCookies()) if (name.equals(c.getName())) return java.util.Optional.of(c);
    return java.util.Optional.empty();
  }

  /** Secure + HttpOnly + SameSite=Lax 로 쿠키 설정 */
  private void addCookie(HttpServletRequest req, HttpServletResponse res,
                         String name, String value, int maxAge) {
    boolean secure = forceSecure || isSecure(req);
    ResponseCookie rc = ResponseCookie.from(name, value)
            .path("/")
            .httpOnly(true)
            .secure(secure)
            .maxAge(maxAge)
            .sameSite("Lax") // OAuth2 auth 요청 보관은 Lax로 충분
            .build();
    res.addHeader(HttpHeaders.SET_COOKIE, rc.toString());
  }

  private void deleteCookie(HttpServletRequest req, HttpServletResponse res, String name) {
    boolean secure = forceSecure || isSecure(req);
    ResponseCookie rc = ResponseCookie.from(name, "")
            .path("/")
            .httpOnly(true)
            .secure(secure)
            .maxAge(0)
            .sameSite("Lax")
            .build();
    res.addHeader(HttpHeaders.SET_COOKIE, rc.toString());
  }

  private static boolean isSecure(HttpServletRequest req) {
    return req.isSecure() || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
  }

  private static String serialize(Object obj) {
    return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(obj));
  }
  private static <T> T deserialize(String val, Class<T> cls) {
    return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(val)));
  }
}
