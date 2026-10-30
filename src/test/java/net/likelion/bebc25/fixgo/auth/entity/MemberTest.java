package net.likelion.bebc25.fixgo.auth.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberTest {
    @Autowired
    private EntityManager em;

    private Long newMemberId;
    private Long memberWithPhoneId;

    @BeforeEach
    void setup() {
        // 회원 등록
        Member member = new Member("admin01", "test1@test.com", "1234", "test1", Role.ROLE_ADMIN, "");
        em.persist(member); // insert 쿼리문 생성 (실행은 안함)

        newMemberId = member.getId();

        // 전화번호 포함 회원 등록
        Member memberWithPhone = new Member(
                "tech01",
                "test2@test.com",
                "5678",
                "test2",
                "010-1234-5678",
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
        assertThat(member.getCode()).isEqualTo("admin01");
        assertThat(member.getId()).isEqualTo(newMemberId);
        assertThat(member.getEmail()).isEqualTo("test1@test.com");
        assertThat(member.getPasswordHash()).isEqualTo("1234");
        assertThat(member.getName()).isEqualTo("test1");
        assertThat(member.getPhone()).isNull();
        assertThat(member.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(member.getProfileImgUrl()).isEqualTo("");
        assertThat(member.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(member.getDeleted_at()).isNull();
    }

    @Test
    @DisplayName("전화번호 포함 직원 정보 조회")
    void findMemberWithPhone() {
        Member member = em.find(Member.class, memberWithPhoneId);

        assertThat(member).isNotNull();
        assertThat(member.getId()).isEqualTo(memberWithPhoneId);
        assertThat(member.getCode()).isEqualTo("tech01");
        assertThat(member.getEmail()).isEqualTo("test2@test.com");
        assertThat(member.getPasswordHash()).isEqualTo("5678");
        assertThat(member.getName()).isEqualTo("test2");
        assertThat(member.getPhone()).isEqualTo("010-1234-5678");
        assertThat(member.getRole()).isEqualTo(Role.ROLE_TECHNICIAN);
        assertThat(member.getProfileImgUrl()).isEqualTo("http://img.test/profile.png");
        assertThat(member.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(member.getDeleted_at()).isNull();
    }
}
