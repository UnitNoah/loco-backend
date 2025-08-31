package com.loco.loco_api.security.oauth;

import jakarta.servlet.http.*;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  public static final String OAUTH2_AUTH_REQUEST_COOKIE = "oauth2_auth_request";
  public static final String REDIRECT_URI_COOKIE = "redirect_uri";
  private static final int EXPIRE_SECONDS = 180; // 3분

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
    addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE, value, EXPIRE_SECONDS);

    String redirectUri = request.getParameter("redirect_uri");
    if (redirectUri != null && !redirectUri.isBlank()) {
      addCookie(response, REDIRECT_URI_COOKIE, redirectUri, EXPIRE_SECONDS);
    }
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                               HttpServletResponse response) {
    OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
    deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE);
    return req;
  }

  // ---- cookie helpers ----
  private static java.util.Optional<Cookie> getCookie(HttpServletRequest req, String name) {
    if (req.getCookies() == null) return java.util.Optional.empty();
    for (Cookie c : req.getCookies()) if (name.equals(c.getName())) return java.util.Optional.of(c);
    return java.util.Optional.empty();
  }
  private static void addCookie(HttpServletResponse res, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setHttpOnly(true);           // JS 접근 차단
    cookie.setMaxAge(maxAge);
    res.addCookie(cookie);
  }
  private static void deleteCookie(HttpServletRequest req, HttpServletResponse res, String name) {
    getCookie(req, name).ifPresent(c -> {
      c.setValue("");
      c.setPath("/");
      c.setMaxAge(0);
      res.addCookie(c);
    });
  }
  private static String serialize(Object obj) {
    return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(obj));
  }
  private static <T> T deserialize(String val, Class<T> cls) {
    return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(val)));
  }
}
