package net.likelion.bebc25.fixgo.auth.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.entity.AsStatusHistory;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class})
class MemberRepositoryTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AsReceptionFixture receptionFixture;

    private Long newMemberId;

    private Member createMember(String email, String name, Role role) {
        return new Member(
                email,
                "1234",
                name,
                "010-1234-5678",
                "서울시 강남구",
                role,
                ""
        );
    }

    @BeforeEach
    void setup() {
        Member member = createMember("test1@test.com", "test1", Role.ROLE_ADMIN);
        em.persist(member);

        newMemberId = member.getId();

        em.flush();
        em.clear();
    }

    @Test
    void findsAssignableTechniciansWithWorkloadAndHistoricalEstimate() {
        Member busy = memberRepository.save(createMember("busy@example.com", "Kim Z", Role.ROLE_TECHNICIAN));
        Member idle = memberRepository.save(createMember("idle@example.com", "Kim A", Role.ROLE_TECHNICIAN));
        Member inactive = createMember("inactive@example.com", "Kim I", Role.ROLE_TECHNICIAN);
        inactive.changeStatus(Status.INACTIVE);
        memberRepository.save(inactive);

        receptionFixture.save().updateTechnician(busy);
        AsReception ongoing = receptionFixture.save();
        ongoing.updateTechnician(busy);
        em.persist(new Repair(ongoing, busy, WarrantyResult.PAID, RepairStatus.REPAIRING));
        AsReception ongoingWithoutRepair = receptionFixture.save();
        ongoingWithoutRepair.updateTechnician(busy);
        ongoingWithoutRepair.changeStatus(AsReceptionStatus.REPAIR_IN_PROGRESS);

        AsReception completed = receptionFixture.save();
        completed.updateTechnician(busy);
        em.persist(new Repair(completed, busy, WarrantyResult.PAID, RepairStatus.COMPLETED));
        AsStatusHistory started = new AsStatusHistory(completed, "APPROVED", "REPAIRING", null, busy);
        AsStatusHistory finished = new AsStatusHistory(completed, "REPAIRING", "COMPLETED", null, busy);
        em.persist(started);
        em.persist(finished);
        em.flush();
        setHistoryTime(started, LocalDateTime.of(2026, 10, 1, 10, 0));
        setHistoryTime(finished, LocalDateTime.of(2026, 10, 1, 10, 30));
        em.clear();

        Page<MemberRepository.TechnicianCandidate> first = memberRepository.findAssignableTechnicians(
                "kim", "INSPECTION_WAITING_COUNT", "DESC", PageRequest.of(0, 1));
        assertThat(first.getTotalElements()).isEqualTo(2);
        MemberRepository.TechnicianCandidate candidate = first.getContent().getFirst();
        assertThat(candidate.getTechnicianId()).isEqualTo(busy.getId());
        assertThat(candidate.getInspectionWaitingCount()).isEqualTo(1);
        assertThat(candidate.getRepairInProgressCount()).isEqualTo(2);
        assertThat(candidate.getEstimatedDurationMinutes()).isEqualTo(30.0);

        Page<MemberRepository.TechnicianCandidate> last = memberRepository.findAssignableTechnicians(
                "", "ESTIMATED_DURATION_MINUTES", "ASC", PageRequest.of(1, 1));
        candidate = last.getContent().getFirst();
        assertThat(candidate.getTechnicianId()).isEqualTo(idle.getId());
        assertThat(candidate.getInspectionWaitingCount()).isZero();
        assertThat(candidate.getRepairInProgressCount()).isZero();
        assertThat(candidate.getEstimatedDurationMinutes()).isNull();
    }

    private void setHistoryTime(AsStatusHistory history, LocalDateTime time) {
        em.createNativeQuery("UPDATE as_status_history SET created_at = :time WHERE id = :id")
                .setParameter("time", time)
                .setParameter("id", history.getId())
                .executeUpdate();
    }

    @Test
    @DisplayName("직원 등록 및 조회")
    void addNewMember() {
        Member existingMember = em.find(Member.class, newMemberId);

        Member member = createMember("test2@example.com", "test2", Role.ROLE_ADMIN);

        memberRepository.save(member);
        em.flush();
        em.clear();

        Member findMember = memberRepository.findByEmail("test2@example.com").orElse(null);

        assertThat(findMember).isNotNull();
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getId()).isNotEqualTo(existingMember.getId());
        assertThat(findMember.getAddress()).isEqualTo("서울시 강남구");
    }

    @Test
    @DisplayName("이메일로 직원 조회")
    void findByEmail() {
        Member findMember = memberRepository.findByEmail("test1@test.com").orElse(null);

        assertThat(findMember).isNotNull();
        assertThat(findMember.getId()).isEqualTo(newMemberId);
        assertThat(findMember.getEmail()).isEqualTo("test1@test.com");
        assertThat(memberRepository.findByEmail("notfound@test.com")).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인")
    void existsByEmail() {
        assertThat(memberRepository.existsByEmail("test1@test.com")).isTrue();
        assertThat(memberRepository.existsByEmail("notfound@test.com")).isFalse();
    }

    @Test
    @DisplayName("기간 내 직원 수 조회")
    void countByCreatedAtBetween() {
        Member technician = createMember("tech1@test.com", "tech1", Role.ROLE_TECHNICIAN);
        memberRepository.save(technician);
        em.flush();
        em.clear();

        LocalDateTime start = LocalDateTime.now().minusMinutes(1);
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);
        LocalDateTime pastStart = LocalDateTime.now().minusDays(2);
        LocalDateTime pastEnd = LocalDateTime.now().minusDays(1);

        assertThat(memberRepository.countByCreatedAtBetween(start, end)).isEqualTo(2);
        assertThat(memberRepository.countByCreatedAtBetween(pastStart, pastEnd)).isEqualTo(0);
    }

    @Test
    @DisplayName("생성일 내림차순으로 전체 직원 조회")
    void findAllByOrderByCreatedAtDesc() {
        Member technician = createMember("tech1@test.com", "tech1", Role.ROLE_TECHNICIAN);
        memberRepository.save(technician);
        em.flush();
        em.clear();

        List<Member> members = memberRepository.findAllByOrderByCreatedAtDesc();

        assertThat(members).hasSize(2);
        assertThat(members).extracting(Member::getEmail)
                .containsExactlyInAnyOrder("tech1@test.com", "test1@test.com");
        assertThat(members.get(0).getCreatedAt())
                .isAfterOrEqualTo(members.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("특정 상태를 제외한 직원 조회")
    void findAllByStatusNotOrderByCreatedAtDesc() {
        Member active = createMember("tech1@test.com", "tech1", Role.ROLE_TECHNICIAN);
        Member deleted = createMember("recv1@test.com", "recv1", Role.ROLE_RECEPTIONIST);
        memberRepository.save(active);
        memberRepository.save(deleted);
        deleted.changeStatus(Status.DELETED);
        em.flush();
        em.clear();

        List<Member> members = memberRepository.findAllByStatusNotOrderByCreatedAtDesc(Status.DELETED);

        assertThat(members).hasSize(2);
        assertThat(members).extracting(Member::getEmail)
                .containsExactlyInAnyOrder("test1@test.com", "tech1@test.com");
        assertThat(members).noneMatch(m -> m.getStatus() == Status.DELETED);
    }

    @Test
    @DisplayName("삭제되지 않은 직원만 이메일로 조회")
    void findByEmailAndStatusNot() {
        Member deleted = createMember("tech1@test.com", "tech1", Role.ROLE_TECHNICIAN);
        memberRepository.save(deleted);
        deleted.changeStatus(Status.DELETED);
        em.flush();
        em.clear();

        assertThat(memberRepository.findByEmailAndStatusNot("test1@test.com", Status.DELETED)).isPresent();
        assertThat(memberRepository.findByEmailAndStatusNot("tech1@test.com", Status.DELETED)).isEmpty();
        assertThat(memberRepository.findByEmailAndStatusNot("notfound@test.com", Status.DELETED)).isEmpty();
    }

    @Test
    @DisplayName("삭제되지 않은 직원만 Id로 조회")
    void findByIdAndStatusNot() {
        Member deleted = createMember("tech1@test.com", "tech1", Role.ROLE_TECHNICIAN);
        memberRepository.save(deleted);
        deleted.changeStatus(Status.DELETED);
        em.flush();
        em.clear();

        assertThat(memberRepository.findByIdAndStatusNot(newMemberId, Status.DELETED)).isPresent();
        assertThat(memberRepository.findByIdAndStatusNot(deleted.getId(), Status.DELETED)).isEmpty();
        assertThat(memberRepository.findByIdAndStatusNot(999L, Status.DELETED)).isEmpty();
    }

    @Test
    @DisplayName("상태로 직원 검색")
    void searchMembersByStatus() {
        Member active = createMember("tech1@test.com", "tech1", Role.ROLE_TECHNICIAN);
        Member deleted = createMember("recv1@test.com", "recv1", Role.ROLE_RECEPTIONIST);
        memberRepository.save(active);
        memberRepository.save(deleted);
        deleted.changeStatus(Status.DELETED);
        em.flush();
        em.clear();

        PageRequest pageable = PageRequest.of(0, 20);

        Page<Member> deletedMembers = memberRepository.searchMembersByStatus(Status.DELETED, pageable);
        assertThat(deletedMembers.getContent()).extracting(Member::getEmail)
                .containsExactly("recv1@test.com");

        Page<Member> activeMembers = memberRepository.searchMembersByStatus(Status.ACTIVE, pageable);
        assertThat(activeMembers.getContent()).extracting(Member::getEmail)
                .containsExactlyInAnyOrder("test1@test.com", "tech1@test.com");
    }

    @Test
    @DisplayName("키워드와 역할, 상태로 직원 검색")
    void searchMembers() {
        Member technician = createMember("kim.tech@test.com", "김기술", Role.ROLE_TECHNICIAN);
        Member receptionist = createMember("lee.recv@test.com", "이접수", Role.ROLE_RECEPTIONIST);
        Member inactive = createMember("park.tech@test.com", "박기술", Role.ROLE_TECHNICIAN);
        Member deleted = createMember("deleted@test.com", "삭제됨", Role.ROLE_TECHNICIAN);
        memberRepository.save(technician);
        memberRepository.save(receptionist);
        memberRepository.save(inactive);
        memberRepository.save(deleted);
        inactive.changeStatus(Status.INACTIVE);
        deleted.changeStatus(Status.DELETED);
        em.flush();
        em.clear();

        PageRequest pageable = PageRequest.of(0, 20);

        Page<Member> all = memberRepository.searchMembers(null, null, null, Status.DELETED, pageable);
        assertThat(all.getContent()).extracting(Member::getEmail)
                .containsExactlyInAnyOrder(
                        "test1@test.com",
                        "kim.tech@test.com",
                        "lee.recv@test.com",
                        "park.tech@test.com"
                );
        assertThat(all.getContent()).noneMatch(m -> m.getStatus() == Status.DELETED);

        assertThat(memberRepository.searchMembers("김", null, null, Status.DELETED, pageable).getContent())
                .extracting(Member::getName)
                .containsExactly("김기술");

        assertThat(memberRepository.searchMembers("lee.recv", null, null, Status.DELETED, pageable).getContent())
                .extracting(Member::getEmail)
                .containsExactly("lee.recv@test.com");

        assertThat(memberRepository.searchMembers(null, Role.ROLE_TECHNICIAN, null, Status.DELETED, pageable).getContent())
                .extracting(Member::getEmail)
                .containsExactlyInAnyOrder("kim.tech@test.com", "park.tech@test.com");

        assertThat(memberRepository.searchMembers("이", Role.ROLE_RECEPTIONIST, null, Status.DELETED, pageable).getContent())
                .extracting(Member::getEmail)
                .containsExactly("lee.recv@test.com");

        assertThat(memberRepository.searchMembers(null, Role.ROLE_TECHNICIAN, Status.INACTIVE, Status.DELETED, pageable).getContent())
                .extracting(Member::getEmail)
                .containsExactly("park.tech@test.com");

        assertThat(memberRepository.searchMembers("없는키워드", null, null, Status.DELETED, pageable).getContent())
                .isEmpty();
    }
}
