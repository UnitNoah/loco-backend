package com.loco.loco_api.common.dto.oauth;

import java.util.Map;

public class GoogleResponse implements OAuth2Response {

  private final Map<String, Object> attributes;

  public GoogleResponse(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String getProvider() {
    return "google";
  }

  @Override
  public String getProviderId() {
    // OIDC: sub (string). 없으면 null 반환
    return (String) attributes.get("sub");
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");      // 동의 안 하면 null
  }

  @Override
  public String getName() {
    return (String) attributes.get("name");       // 동의 안 하면 null
  }

  @Override
  public String getProfileImageUrl() {
    return (String) attributes.get("picture");    // 동의 안 하면 null
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }
}
