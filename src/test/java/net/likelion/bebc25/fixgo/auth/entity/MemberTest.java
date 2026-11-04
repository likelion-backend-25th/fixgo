package net.likelion.bebc25.fixgo.auth.entity;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
public class MemberTest {
    @Autowired
    private EntityManager em;

    private Long newMemberId;
    private Long memberWithPhoneId;

    @BeforeEach
    void setup() {
        // 회원 등록
        Member member = new Member(
                "test1@test.com",
                "1234",
                "test1",
                "010-1111-1111",
                "서울시 강남구",
                Role.ROLE_ADMIN,
                ""
        );
        em.persist(member); // insert 쿼리문 생성 (실행은 안함)

        newMemberId = member.getId();

        // 전화번호 포함 회원 등록
        Member memberWithPhone = new Member(
                "test2@test.com",
                "5678",
                "test2",
                "010-1234-5678",
                "서울시 서초구",
                Role.ROLE_TECHNICIAN,
                "http://img.test/profile.png"
        );
        em.persist(memberWithPhone);

        memberWithPhoneId = memberWithPhone.getId();

        em.flush(); // 생성해 둔 쿼리문 실행
        em.clear(); // 캐시 삭제
    }

    @Test
    @DisplayName("직원 정보 조회")
    void findMember() {
        Member member = em.find(Member.class, newMemberId);

        assertThat(member).isNotNull();
        assertThat(member.getId()).isEqualTo(newMemberId);
        assertThat(member.getEmail()).isEqualTo("test1@test.com");
        assertThat(member.getPasswordHash()).isEqualTo("1234");
        assertThat(member.getName()).isEqualTo("test1");
        assertThat(member.getPhone()).isEqualTo("010-1111-1111");
        assertThat(member.getAddress()).isEqualTo("서울시 강남구");
        assertThat(member.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(member.getProfileImgUrl()).isEqualTo("");
        assertThat(member.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(member.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("전화번호 포함 직원 정보 조회")
    void findMemberWithPhone() {
        Member member = em.find(Member.class, memberWithPhoneId);

        assertThat(member).isNotNull();
        assertThat(member.getId()).isEqualTo(memberWithPhoneId);
        assertThat(member.getEmail()).isEqualTo("test2@test.com");
        assertThat(member.getPasswordHash()).isEqualTo("5678");
        assertThat(member.getName()).isEqualTo("test2");
        assertThat(member.getPhone()).isEqualTo("010-1234-5678");
        assertThat(member.getAddress()).isEqualTo("서울시 서초구");
        assertThat(member.getRole()).isEqualTo(Role.ROLE_TECHNICIAN);
        assertThat(member.getProfileImgUrl()).isEqualTo("http://img.test/profile.png");
        assertThat(member.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(member.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("삭제 상태로 변경하면 deletedAt이 기록된다")
    void changeStatus_toDeleted() {
        Member member = em.find(Member.class, newMemberId);

        member.changeStatus(Status.DELETED);
        em.flush();
        em.clear();

        Member deleted = em.find(Member.class, newMemberId);
        assertThat(deleted.getStatus()).isEqualTo(Status.DELETED);
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("삭제된 직원을 복구하면 deletedAt이 초기화된다")
    void changeStatus_restoreFromDeleted() {
        Member member = em.find(Member.class, newMemberId);
        member.changeStatus(Status.DELETED);
        em.flush();
        em.clear();

        Member deleted = em.find(Member.class, newMemberId);
        deleted.changeStatus(Status.ACTIVE);
        em.flush();
        em.clear();

        Member restored = em.find(Member.class, newMemberId);
        assertThat(restored.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(restored.getDeletedAt()).isNull();
    }
}
