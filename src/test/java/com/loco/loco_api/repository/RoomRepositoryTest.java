package com.loco.loco_api.repository;

import com.loco.loco_api.domain.room.Room;
import com.loco.loco_api.domain.user.UserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoomRepositoryTest {

    @Autowired private EntityManager em;
    @Autowired private RoomRepository rooms;
    @Autowired private UserRepository users;

    @Test
    void findPublicOrderByCreatedAtDesc_returnsOnlyPublic_inDescOrder_andFetchesHost() throws Exception {
        var host = users.save(UserEntity.builder()
                .nickname("홍길동").provider("google").oauthId("x").build());

        // public / private 섞어서 만들어 생성 시간 차이를 둠
        var pub1 = rooms.save(Room.builder().name("공개1").description("d").isPrivate(false).inviteCode("A1").thumbnail("t").host(host).build());
        Thread.sleep(5);
        var priv1 = rooms.save(Room.builder().name("비공개1").description("d").isPrivate(true).inviteCode("X1").thumbnail("t").host(host).build());
        Thread.sleep(5);
        var pub2 = rooms.save(Room.builder().name("공개2").description("d").isPrivate(false).inviteCode("A2").thumbnail("t").host(host).build());
        em.flush(); em.clear();

        List<Room> result = rooms.findPublicOrderByCreatedAtDesc();

        assertThat(result).extracting(Room::getName).containsExactly("공개2", "공개1"); // createdAt DESC
        assertThat(result).allMatch(r -> !r.isPrivate());
        // host join fetch 확인(프록시 강제 초기화 필요 없이 즉시 사용 가능)
        assertThat(result.get(0).getHost().getNickname()).isEqualTo("홍길동");
    }

    @Test
    void findPrivateOrderByCreatedAtDesc_returnsOnlyPrivate_inDescOrder_andFetchesHost() throws Exception {
        var host = users.save(UserEntity.builder()
                .nickname("임꺽정").provider("google").oauthId("y").build());

        var priv1 = rooms.save(Room.builder().name("비공개1").description("d").isPrivate(true).inviteCode("X1").thumbnail("t").host(host).build());
        Thread.sleep(5);
        var pub1  = rooms.save(Room.builder().name("공개1").description("d").isPrivate(false).inviteCode("A1").thumbnail("t").host(host).build());
        Thread.sleep(5);
        var priv2 = rooms.save(Room.builder().name("비공개2").description("d").isPrivate(true).inviteCode("X2").thumbnail("t").host(host).build());
        em.flush(); em.clear();

        List<Room> result = rooms.findPrivateOrderByCreatedAtDesc();

        assertThat(result).extracting(Room::getName).containsExactly("비공개2", "비공개1");
        assertThat(result).allMatch(Room::isPrivate);
        assertThat(result.get(0).getHost().getNickname()).isEqualTo("임꺽정");
    }

    @Test
    void findActiveByIdFetchHost_returnsRoomWithHost_andExcludesSoftDeleted() {
        var host = users.save(UserEntity.builder()
                .nickname("홍길동").provider("google").oauthId("123456").build());

        Room room = rooms.save(Room.builder()
                .name("스터디룸")
                .description("조용")
                .isPrivate(true)
                .thumbnail("https://cdn.example.com/img.png")
                .inviteCode("ABCD1234")
                .host(host)
                .build());
        em.flush(); em.clear();

        // 활성 방 단건 조회(+ host fetch)
        Room found = rooms.findActiveByIdFetchHost(room.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("스터디룸");
        assertThat(found.getHost().getId()).isEqualTo(host.getId());
        assertThat(found.getHost().getNickname()).isEqualTo("홍길동");

        // 소프트 삭제 후에는 조회에서 제외되어야 함
        rooms.delete(found); // @SQLDelete 적용
        em.flush(); em.clear();

        Optional<Room> afterDelete = rooms.findActiveByIdFetchHost(room.getId());
        assertThat(afterDelete).isEmpty();
    }

    @Test
    void findHostBy_returnsOnlyMyHostedRooms_inDescOrder_andExcludesSoftDeleted() throws Exception {
        var me   = users.save(UserEntity.builder().nickname("me").provider("google").oauthId("me-oauth").build());
        var other= users.save(UserEntity.builder().nickname("other").provider("google").oauthId("other-oauth").build());

        var myOld  = rooms.save(Room.builder().name("내방-과거").description("d").isPrivate(false).inviteCode("A").thumbnail("t").host(me).build());
        Thread.sleep(5);
        var myNew  = rooms.save(Room.builder().name("내방-최신").description("d").isPrivate(true).inviteCode("B").thumbnail("t").host(me).build());
        Thread.sleep(5);
        var notMine = rooms.save(Room.builder().name("남의방").description("d").isPrivate(false).inviteCode("C").thumbnail("t").host(other).build());
        em.flush(); em.clear();

        // 기본: 내 방만, 최신순
        List<Room> list = rooms.findHostBy(me.getId());
        assertThat(list).extracting(Room::getName).containsExactly("내방-최신", "내방-과거");
        assertThat(list).allMatch(r -> r.getHost().getId().equals(me.getId()));

        // 내 오래된 방 소프트 삭제 후, 목록에서 빠지는지 확인
        Room toDelete = rooms.findActiveByIdFetchHost(myOld.getId()).orElseThrow();
        rooms.delete(toDelete);
        em.flush(); em.clear();

        List<Room> listAfterDelete = rooms.findHostBy(me.getId());
        assertThat(listAfterDelete).extracting(Room::getName).containsExactly("내방-최신");
    }
}
