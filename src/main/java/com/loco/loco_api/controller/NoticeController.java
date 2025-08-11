package com.loco.loco_api.controller;

import com.loco.loco_api.common.dto.notice.response.NoticeResponse;
import com.loco.loco_api.common.dto.notice.response.NoticeUpdateRequest;
import com.loco.loco_api.common.response.ApiResponse;
import com.loco.loco_api.common.response.PageResponse;
import com.loco.loco_api.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Tag(name = "Notice", description = "공지 API")
@SecurityRequirement(name = "JWT")
public class NoticeController {

  private final NoticeService service;

  @GetMapping
  @Operation(summary = "공지 목록 조회", description = "페이지네이션 기반 공지 목록을 조회합니다. soft-delete된 데이터는 제외됩니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
  public ApiResponse<PageResponse<NoticeResponse>> getNotices(
          @ParameterObject
          @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    return ApiResponse.success(service.getNotices(pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "공지 상세 조회", description = "단건 공지 상세를 조회합니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지 없음")
  public ApiResponse<NoticeResponse> getNotice(
          @Parameter(description = "공지 ID", example = "1") @PathVariable Long id
  ) {
    return ApiResponse.success(service.getNotice(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "공지 수정", description = "공지의 제목/내용을 수정합니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지 없음")
  public ApiResponse<NoticeResponse> updateNotice(
          @Parameter(description = "공지 ID", example = "1") @PathVariable Long id,
          @RequestBody NoticeUpdateRequest req
  ) {
    return ApiResponse.success(service.updateNotice(id, req));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "공지 삭제(소프트)", description = "공지 데이터를 소프트 삭제합니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지 없음")
  public ApiResponse<Void> deleteNotice(
          @Parameter(description = "공지 ID", example = "1") @PathVariable Long id
  ) {
    service.deleteNotice(id);
    return ApiResponse.success(null);
  }
}
