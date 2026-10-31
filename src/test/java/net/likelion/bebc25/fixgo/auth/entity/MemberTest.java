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

    @BeforeEach
    void setup() {
        // 회원 등록
        Member member = new Member("admin01", "test1@test.com", "1234", "test1", Role.ROLE_ADMIN, "");
        em.persist(member); // insert 쿼리문 생성 (실행은 안함)

        newMemberId = member.getId();

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
        assertThat(member.getName()).isEqualTo("test1");
        assertThat(member.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }
}
