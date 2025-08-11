package com.loco.loco_api.service;

import com.loco.loco_api.common.dto.notice.response.NoticeResponse;
import com.loco.loco_api.common.dto.notice.response.NoticeUpdateRequest;
import com.loco.loco_api.common.exception.CustomException;
import com.loco.loco_api.common.exception.ErrorCode;
import com.loco.loco_api.common.response.PageResponse;
import com.loco.loco_api.domain.notice.Notice;
import com.loco.loco_api.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

  private final NoticeRepository repository;

  @Transactional(readOnly = true)
  public PageResponse<NoticeResponse> getNotices(Pageable pageable) {
    Page<Notice> page = repository.findAllByDeletedFalse(pageable);

    List<NoticeResponse> content = page.getContent().stream()
            .map(NoticeResponse::from)
            .toList();

    return PageResponse.of(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious()
    );
  }

  @Transactional(readOnly = true)
  public NoticeResponse getNotice(Long id) {
    Notice notice = repository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
    return NoticeResponse.from(notice);
  }

  public NoticeResponse updateNotice(Long id, NoticeUpdateRequest req) {
    Notice notice = repository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
    notice.update(req.title(), req.content()); // Dirty Checking
    return NoticeResponse.from(notice);
  }

  /** 소프트 삭제 */
  public void deleteNotice(Long id) {
    Notice notice = repository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
    repository.delete(notice); // @SQLDelete → deleted=true, deleted_at=timestamp
  }
}
