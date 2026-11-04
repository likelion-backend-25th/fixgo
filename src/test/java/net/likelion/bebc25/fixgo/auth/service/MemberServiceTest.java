package net.likelion.bebc25.fixgo.auth.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.dto.MemberDto;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import net.likelion.bebc25.fixgo.auth.exception.AbnormalApproachException;
import net.likelion.bebc25.fixgo.auth.exception.EmployeeHasActiveRepairsException;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.config.PasswordEncoderConfig;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({MemberService.class, PasswordEncoderConfig.class, JpaConfig.class})
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RepairRepository repairRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AsReceptionRepository asReceptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member saveMember(String email, Role role) {
        return memberRepository.save(new Member(
                email,
                passwordEncoder.encode("password123"),
                "테스트",
                "010-1234-5678",
                "서울시 강남구",
                role,
                "http://img.test/profile.png"
        ));
    }

    private void saveActiveRepair(Member technician) {
        Customer customer = customerRepository.save(
                new Customer("테스트 고객", "010-1111-2222", "customer@test.com", "12345", "서울시", "강남구", 1L)
        );
        Product product = productRepository.save(
                new Product(1L, "TEST-SERIAL-001", LocalDateTime.now(), 1L)
        );
        AsReception asReception = asReceptionRepository.save(new AsReception(
                "AS-TEST-001",
                customer,
                product,
                technician,
                technician,
                "메인보드 이상",
                "전원이 켜지지 않는 증상",
                "테스트 접수",
                ReturnMethod.DELIVERY,
                null
        ));
        repairRepository.save(new Repair(asReception, technician, WarrantyResult.FREE, RepairStatus.REQUESTED));
    }

    @Test
    @DisplayName("회원가입에 성공하면 저장된 직원 상세 정보를 반환한다")
    void signup_success() {
        MemberDto.CreateRequest request = new MemberDto.CreateRequest(
                "new@test.com",
                "password123",
                "신규직원",
                "010-0000-0000",
                "서울시 송파구",
                Role.ROLE_TECHNICIAN,
                "http://img.test/new.png"
        );

        MemberDto.DetailResponse response = memberService.signup(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("new@test.com");
        assertThat(response.name()).isEqualTo("신규직원");
        assertThat(response.phone()).isEqualTo("010-0000-0000");
        assertThat(response.address()).isEqualTo("서울시 송파구");
        assertThat(response.role()).isEqualTo(Role.ROLE_TECHNICIAN);
        assertThat(response.profileImgUrl()).isEqualTo("http://img.test/new.png");
        assertThat(response.status()).isEqualTo(Status.ACTIVE);
        assertThat(memberRepository.existsByEmail("new@test.com")).isTrue();
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 회원가입에 실패한다")
    void signup_duplicateEmail() {
        saveMember("dup@test.com", Role.ROLE_ADMIN);

        MemberDto.CreateRequest request = new MemberDto.CreateRequest(
                "dup@test.com",
                "password123",
                "중복직원",
                "010-0000-0000",
                "서울시 강남구",
                Role.ROLE_ADMIN,
                ""
        );

        assertThatThrownBy(() -> memberService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용 불가능한 이메일입니다.");
        assertThat(memberRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("조건 없이 직원 목록을 조회하면 삭제된 직원을 제외한다")
    void getMembers_excludeDeleted() {
        saveMember("admin@test.com", Role.ROLE_ADMIN);
        saveMember("tech@test.com", Role.ROLE_TECHNICIAN);
        Member deleted = saveMember("recv@test.com", Role.ROLE_RECEPTIONIST);
        deleted.changeStatus(Status.DELETED);

        Page<MemberDto.BriefResponse> responses = memberService.getMembers(
                new MemberDto.MemberSearchRequest(null, null, null),
                PageRequest.of(0, 20)
        );

        assertThat(responses.getContent()).hasSize(2);
        assertThat(responses.getContent()).extracting(MemberDto.BriefResponse::email)
                .containsExactlyInAnyOrder("admin@test.com", "tech@test.com");
        assertThat(responses.getContent()).noneMatch(r -> r.email().equals("recv@test.com"));
    }

    @Test
    @DisplayName("키워드와 역할, 상태로 직원을 조건 검색한다")
    void getMembers_withKeywordRoleAndStatus() {
        Member activeTech = saveMember("kim@test.com", Role.ROLE_TECHNICIAN);
        saveMember("lee@test.com", Role.ROLE_RECEPTIONIST);
        Member inactiveTech = saveMember("park@test.com", Role.ROLE_TECHNICIAN);
        inactiveTech.changeStatus(Status.INACTIVE);

        Page<MemberDto.BriefResponse> responses = memberService.getMembers(
                new MemberDto.MemberSearchRequest("kim", Role.ROLE_TECHNICIAN, Status.ACTIVE),
                PageRequest.of(0, 20)
        );

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().getFirst().email()).isEqualTo("kim@test.com");
        assertThat(responses.getContent().getFirst().id()).isEqualTo(activeTech.getId());
    }

    @Test
    @DisplayName("역할만으로 직원 목록을 필터링한다")
    void getMembers_filterByRole() {
        saveMember("tech@test.com", Role.ROLE_TECHNICIAN);
        saveMember("admin@test.com", Role.ROLE_ADMIN);

        Page<MemberDto.BriefResponse> responses = memberService.getMembers(
                new MemberDto.MemberSearchRequest(null, Role.ROLE_TECHNICIAN, null),
                PageRequest.of(0, 20)
        );

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().getFirst().email()).isEqualTo("tech@test.com");
    }

    @Test
    @DisplayName("삭제된 직원 목록만 조회한다")
    void getDeletedMembers() {
        saveMember("active@test.com", Role.ROLE_ADMIN);
        Member deleted1 = saveMember("deleted1@test.com", Role.ROLE_TECHNICIAN);
        Member deleted2 = saveMember("deleted2@test.com", Role.ROLE_RECEPTIONIST);
        deleted1.changeStatus(Status.DELETED);
        deleted2.changeStatus(Status.DELETED);

        Page<MemberDto.BriefResponse> responses = memberService.getDeletedMembers(PageRequest.of(0, 20));

        assertThat(responses.getContent()).hasSize(2);
        assertThat(responses.getContent()).extracting(MemberDto.BriefResponse::email)
                .containsExactlyInAnyOrder("deleted1@test.com", "deleted2@test.com");
        assertThat(responses.getContent()).allMatch(r -> r.status() == Status.DELETED);
    }

    @Test
    @DisplayName("Id로 직원 상세 정보를 조회한다")
    void getMemberById_success() {
        Member member = saveMember("tech@test.com", Role.ROLE_TECHNICIAN);

        MemberDto.DetailResponse response = memberService.getMemberById(member.getId());

        assertThat(response.id()).isEqualTo(member.getId());
        assertThat(response.email()).isEqualTo("tech@test.com");
        assertThat(response.name()).isEqualTo("테스트");
        assertThat(response.phone()).isEqualTo("010-1234-5678");
        assertThat(response.address()).isEqualTo("서울시 강남구");
        assertThat(response.role()).isEqualTo(Role.ROLE_TECHNICIAN);
        assertThat(response.profileImgUrl()).isEqualTo("http://img.test/profile.png");
        assertThat(response.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 Id로 조회하면 예외가 발생한다")
    void getMemberById_notFound() {
        assertThatThrownBy(() -> memberService.getMemberById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 계정입니다.");
    }

    @Test
    @DisplayName("직원 상태를 변경한다")
    void changeStatus_success() {
        saveMember("tech@test.com", Role.ROLE_TECHNICIAN);

        MemberDto.BriefResponse response = memberService.changeStatus(
                new MemberDto.StatusChangeRequest("tech@test.com", Status.INACTIVE)
        );

        assertThat(response.email()).isEqualTo("tech@test.com");
        assertThat(response.status()).isEqualTo(Status.INACTIVE);
        assertThat(memberRepository.findByEmail("tech@test.com").orElseThrow().getStatus())
                .isEqualTo(Status.INACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 계정의 상태를 변경하면 예외가 발생한다")
    void changeStatus_notFound() {
        assertThatThrownBy(() -> memberService.changeStatus(
                new MemberDto.StatusChangeRequest("gone@test.com", Status.INACTIVE)
        ))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 계정입니다.");
    }

    @Test
    @DisplayName("삭제된 계정을 ACTIVE로 복구한다")
    void changeStatus_restoreDeletedAccount() {
        Member member = saveMember("tech@test.com", Role.ROLE_TECHNICIAN);
        member.changeStatus(Status.DELETED);
        assertThat(member.getDeletedAt()).isNotNull();

        MemberDto.BriefResponse response = memberService.changeStatus(
                new MemberDto.StatusChangeRequest("tech@test.com", Status.ACTIVE)
        );

        Member restored = memberRepository.findByEmail("tech@test.com").orElseThrow();
        assertThat(response.status()).isEqualTo(Status.ACTIVE);
        assertThat(restored.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(restored.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("직원 직책을 변경한다")
    void changeRole_success() {
        saveMember("tech@test.com", Role.ROLE_TECHNICIAN);

        MemberDto.BriefResponse response = memberService.changeRole(
                new MemberDto.RoleChangeRequest("tech@test.com", Role.ROLE_ADMIN)
        );

        assertThat(response.email()).isEqualTo("tech@test.com");
        assertThat(response.role()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(memberRepository.findByEmail("tech@test.com").orElseThrow().getRole())
                .isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("존재하지 않는 계정의 직책을 변경하면 예외가 발생한다")
    void changeRole_notFound() {
        assertThatThrownBy(() -> memberService.changeRole(
                new MemberDto.RoleChangeRequest("gone@test.com", Role.ROLE_ADMIN)
        ))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 계정입니다.");
    }

    @Test
    @DisplayName("비밀번호를 변경한다")
    void changePassword_success() {
        Member member = saveMember("tech@test.com", Role.ROLE_TECHNICIAN);

        memberService.changePassword(
                member.getId(),
                new MemberDto.PasswordUpdateRequest("tech@test.com", "newPassword")
        );

        assertThat(passwordEncoder.matches(
                "newPassword",
                memberRepository.findByEmail("tech@test.com").orElseThrow().getPasswordHash()
        )).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 계정의 비밀번호를 변경하면 예외가 발생한다")
    void changePassword_notFound() {
        assertThatThrownBy(() -> memberService.changePassword(
                999L,
                new MemberDto.PasswordUpdateRequest("gone@test.com", "newPassword")
        ))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 계정입니다.");
    }

    @Test
    @DisplayName("로그인 계정과 요청 이메일이 다르면 비밀번호 변경에 실패한다")
    void changePassword_emailMismatch() {
        Member member = saveMember("tech@test.com", Role.ROLE_TECHNICIAN);

        assertThatThrownBy(() -> memberService.changePassword(
                member.getId(),
                new MemberDto.PasswordUpdateRequest("other@test.com", "newPassword")
        ))
                .isInstanceOf(AbnormalApproachException.class)
                .hasMessage("비정상적인 접근입니다.");
    }

    @Test
    @DisplayName("프로필 정보를 변경한다")
    void updateProfile_success() {
        saveMember("tech@test.com", Role.ROLE_TECHNICIAN);

        MemberDto.DetailResponse response = memberService.updateProfile(
                new MemberDto.ProfileUpdateRequest(
                        "tech@test.com",
                        "변경된이름",
                        "010-9999-8888",
                        "서울시 마포구",
                        "http://img.test/updated.png"
                )
        );

        assertThat(response.name()).isEqualTo("변경된이름");
        assertThat(response.phone()).isEqualTo("010-9999-8888");
        assertThat(response.address()).isEqualTo("서울시 마포구");
        assertThat(response.profileImgUrl()).isEqualTo("http://img.test/updated.png");

        Member updated = memberRepository.findByEmail("tech@test.com").orElseThrow();
        assertThat(updated.getName()).isEqualTo("변경된이름");
        assertThat(updated.getPhone()).isEqualTo("010-9999-8888");
        assertThat(updated.getAddress()).isEqualTo("서울시 마포구");
    }

    @Test
    @DisplayName("존재하지 않는 계정의 프로필을 변경하면 예외가 발생한다")
    void updateProfile_notFound() {
        assertThatThrownBy(() -> memberService.updateProfile(
                new MemberDto.ProfileUpdateRequest(
                        "gone@test.com",
                        "이름",
                        "010-0000-0000",
                        "서울시 강남구",
                        ""
                )
        ))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 계정입니다.");
    }

    @Test
    @DisplayName("진행 중인 A/S가 없는 직원을 삭제하면 DELETED 상태가 된다")
    void deleteMember_success() {
        Member member = saveMember("tech@test.com", Role.ROLE_TECHNICIAN);

        memberService.deleteMember(member.getId());

        Member deleted = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(deleted.getStatus()).isEqualTo(Status.DELETED);
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 계정을 삭제하면 예외가 발생한다")
    void deleteMember_notFound() {
        assertThatThrownBy(() -> memberService.deleteMember(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 계정입니다.");
    }

    @Test
    @DisplayName("삭제된 계정을 다시 삭제하면 예외가 발생한다")
    void deleteMember_alreadyDeleted() {
        Member member = saveMember("tech@test.com", Role.ROLE_TECHNICIAN);
        member.changeStatus(Status.DELETED);

        assertThatThrownBy(() -> memberService.deleteMember(member.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 계정입니다.");
    }

    @Test
    @DisplayName("진행 중인 A/S가 있는 기사는 삭제할 수 없다")
    void deleteMember_technicianHasActiveRepairs() {
        Member technician = saveMember("tech@test.com", Role.ROLE_TECHNICIAN);
        saveActiveRepair(technician);

        assertThatThrownBy(() -> memberService.deleteMember(technician.getId()))
                .isInstanceOf(EmployeeHasActiveRepairsException.class)
                .hasMessage("현재 진행 중인 A/S 건이 있어 삭제할 수 없습니다.");
    }
}
