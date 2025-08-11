package com.loco.loco_api;

import com.loco.loco_api.domain.notice.Notice;
import com.loco.loco_api.repository.NoticeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NoticeRepositoryTest {

  @Autowired NoticeRepository repository;
  @Autowired EntityManager em;

  @Test
  void softDelete_적용시_findAll에서_제외된다() {
    Notice alive = repository.save(Notice.of("t1","c1"));
    Notice toDelete = repository.save(Notice.of("t2","c2"));

    repository.delete(toDelete);      // @SQLDelete → deleted=true
    em.flush();                             // 명시적으로 flush
    em.clear();                             // 1차 캐시 비움

    Page<Notice> page = repository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));
    assertThat(page.getContent()).extracting("id")
            .contains(alive.getId())
            .doesNotContain(toDelete.getId());
  }

  @Test
  void repository_delete가_SQLDelete를_탄다() {
    Notice n = repository.save(Notice.of("t","c"));

    repository.delete(n);
    em.flush(); em.clear();

    // 네이티브로 직접 확인(삭제 포함)
    Object[] row = (Object[]) em.createNativeQuery(
                    "select deleted, deleted_at from notices where id=:id")
            .setParameter("id", n.getId())
            .getSingleResult();

    // Postgres 기준: boolean → Boolean 매핑
    assertThat(row[0]).isEqualTo(Boolean.TRUE);
    assertThat(row[1]).isNotNull();
  }

  @Test
  void softDelete_후_findById는_빈값을_반환한다() {
    Notice n = repository.save(Notice.of("t","c"));
    Long id = n.getId();

    repository.delete(n);
    em.flush(); em.clear();

    // @Where(clause="deleted=false")에 의해 제외
    assertThat(repository.findById(id)).isEmpty();
  }
}
