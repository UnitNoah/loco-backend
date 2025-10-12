package com.loco.loco_api.repository;

import com.loco.loco_api.domain.room.Room;
import com.loco.loco_api.domain.user.UserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoomRepositoryTest {
    @Autowired private EntityManager em;
    @Autowired private RoomRepository rooms;

    @Test
    void save_and_findById() {
        UserEntity host = UserEntity.builder().nickname("홍길동").provider("google").oauthId("123456").build();
        em.persist(host);

        Room room = Room.builder()
                .name("스터디룸")
                .description("조용")
                .isPrivate(true)
                .thumbnail("https://cdn.example.com/img.png")
                .inviteCode("ABCD1234")
                .host(host)
                .build();

        Room saved = rooms.save(room);
        em.flush();

        Room found = rooms.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("스터디룸");
        assertThat(found.getHost().getId()).isEqualTo(host.getId());
    }
}