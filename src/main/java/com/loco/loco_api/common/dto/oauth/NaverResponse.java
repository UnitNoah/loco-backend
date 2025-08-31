package com.loco.loco_api.common.dto.oauth;

import lombok.Getter;

import java.util.Map;

@Getter
@SuppressWarnings("unchecked")
public class NaverResponse implements OAuth2Response {

  private final Map<String, Object> response; // {response:{...}} 내부만 보관

  public NaverResponse(Map<String, Object> attributes) {
    Object resp = attributes.get("response");
    this.response = resp instanceof Map ? (Map<String, Object>) resp : null;
  }

  @Override
  public String getProvider() {
    return "naver";
  }

  @Override
  public String getProviderId() {
    return response != null ? (String) response.get("id") : null;
  }

  @Override
  public String getEmail() {
    return response != null ? (String) response.get("email") : null;
  }

  @Override
  public String getName() {
    return response != null ? (String) response.get("name") : null;
  }

  @Override
  public String getProfileImageUrl() {
    return response != null ? (String) response.get("profile_image") : null;
  }

}
