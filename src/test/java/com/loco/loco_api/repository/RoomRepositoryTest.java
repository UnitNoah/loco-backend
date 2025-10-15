package com.loco.loco_api.repository;

import com.loco.loco_api.domain.room.Room;
import com.loco.loco_api.domain.user.UserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoomRepositoryTest {
    @Autowired private EntityManager em;
    @Autowired private RoomRepository rooms;
    @Autowired UserRepository users;


    @Test
    void findByIsPrivateFalseOrderByCreatedAtDesc_returnsOnlyPublic_inDescOrder() throws Exception {
        var host = users.save(UserEntity.builder()
                .nickname("홍길동").provider("google").oauthId("x").build());

        // mix of public/private, create times differ by insertion order
        var pub1 = rooms.save(Room.builder().name("공개1").description("d").isPrivate(false).inviteCode("A1").thumbnail("t").host(host).build());
        Thread.sleep(5);
        var priv1 = rooms.save(Room.builder().name("비공개1").description("d").isPrivate(true).inviteCode("X1").thumbnail("t").host(host).build());
        Thread.sleep(5);
        var pub2 = rooms.save(Room.builder().name("공개2").description("d").isPrivate(false).inviteCode("A2").thumbnail("t").host(host).build());

        List<Room> result = rooms.findByIsPrivateFalseOrderByCreatedAtDesc();

        assertThat(result).extracting(Room::getName).containsExactly("공개2", "공개1"); // desc by createdAt
        assertThat(result).allMatch(r -> !r.isPrivate());
    }

    @Test
    void findByIsPrivateTrueOrderByCreatedAtDesc_returnsOnlyPrivate_inDescOrder() throws Exception {
        var host = users.save(UserEntity.builder()
                .nickname("임꺽정").provider("google").oauthId("y").build());

        var priv1 = rooms.save(Room.builder().name("비공개1").description("d").isPrivate(true).inviteCode("X1").thumbnail("t").host(host).build());
        Thread.sleep(5);
        var pub1  = rooms.save(Room.builder().name("공개1").description("d").isPrivate(false).inviteCode("A1").thumbnail("t").host(host).build());
        Thread.sleep(5);
        var priv2 = rooms.save(Room.builder().name("비공개2").description("d").isPrivate(true).inviteCode("X2").thumbnail("t").host(host).build());

        List<Room> result = rooms.findByIsPrivateTrueOrderByCreatedAtDesc();

        assertThat(result).extracting(Room::getName).containsExactly("비공개2", "비공개1");
        assertThat(result).allMatch(Room::isPrivate);
    }

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