package net.likelion.bebc25.fixgo.auth.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private MemberRepository memberRepository;

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
    @DisplayName("직원 등록 및 조회")
    void addNewMember() {
        Member existingMember = em.find(Member.class, newMemberId);

        Member member = new Member(
                "Admin001",
                "test2@example.com",
                "1234",
                "test2",
                "010-0000-0000",
                Role.ROLE_ADMIN,
                ""
        );

        memberRepository.save(member);
        em.flush();
        em.clear();

        Member findMember = memberRepository.findByEmail("test2@example.com").orElse(null);

        assertThat(findMember).isNotNull();
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getId()).isNotEqualTo(existingMember.getId());
    }

}
