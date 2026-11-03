package net.likelion.bebc25.fixgo.auth.service;

import net.likelion.bebc25.fixgo.auth.dto.MemberDto;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.config.PasswordEncoderConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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
    private PasswordEncoder passwordEncoder;

    private Member saveMember(String code, String email, Role role) {
        return memberRepository.save(new Member(
                code,
                email,
                passwordEncoder.encode("password123"),
                "테스트",
                "010-1234-5678",
                role,
                "http://img.test/profile.png"
        ));
    }

    @Test
    @DisplayName("회원가입에 성공하면 저장된 직원 정보를 반환한다")
    void signup_success() {
        MemberDto.CreateRequest request = new MemberDto.CreateRequest(
                "new@test.com",
                "password123",
                "신규직원",
                "010-0000-0000",
                Role.ROLE_TECHNICIAN,
                "http://img.test/new.png"
        );

        MemberDto.Response response = memberService.signup(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("new@test.com");
        assertThat(response.name()).isEqualTo("신규직원");
        assertThat(response.role()).isEqualTo(Role.ROLE_TECHNICIAN);
        assertThat(response.code()).startsWith("EMP_");
        assertThat(memberRepository.existsByEmail("new@test.com")).isTrue();
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 회원가입에 실패한다")
    void signup_duplicateEmail() {
        saveMember("admin01", "dup@test.com", Role.ROLE_ADMIN);

        MemberDto.CreateRequest request = new MemberDto.CreateRequest(
                "dup@test.com",
                "password123",
                "중복직원",
                null,
                Role.ROLE_ADMIN,
                ""
        );

        assertThatThrownBy(() -> memberService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용 불가능한 이메일입니다.");
        assertThat(memberRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("관리자는 삭제된 직원을 포함한 전체 목록을 조회한다")
    void getAllMembers_asAdmin() {
        saveMember("admin01", "admin@test.com", Role.ROLE_ADMIN);
        Member deleted = saveMember("tech01", "tech@test.com", Role.ROLE_TECHNICIAN);
        deleted.changeStatus(Status.DELETED);

        List<MemberDto.Response> responses = memberService.getAllMembers(Role.ROLE_ADMIN);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(MemberDto.Response::email)
                .containsExactlyInAnyOrder("admin@test.com", "tech@test.com");
        assertThat(responses.get(0).id()).isGreaterThanOrEqualTo(responses.get(1).id());
    }

    @Test
    @DisplayName("관리자가 아니면 삭제된 직원을 제외한 목록을 조회한다")
    void getAllMembers_asNonAdmin() {
        saveMember("admin01", "admin@test.com", Role.ROLE_ADMIN);
        saveMember("tech01", "tech@test.com", Role.ROLE_TECHNICIAN);
        Member deleted = saveMember("recv01", "recv@test.com", Role.ROLE_RECEPTIONIST);
        deleted.changeStatus(Status.DELETED);

        List<MemberDto.Response> responses = memberService.getAllMembers(Role.ROLE_TECHNICIAN);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(MemberDto.Response::email)
                .containsExactlyInAnyOrder("admin@test.com", "tech@test.com");
        assertThat(responses).noneMatch(r -> r.email().equals("recv@test.com"));
    }

    @Test
    @DisplayName("공백 키워드는 null로 바꿔 조건 검색한다")
    void getMembersByCondition_blankKeyword() {
        saveMember("tech01", "tech@test.com", Role.ROLE_TECHNICIAN);
        saveMember("admin01", "admin@test.com", Role.ROLE_ADMIN);

        List<MemberDto.Response> responses = memberService.getMembersByCondition(
                new MemberDto.MemberSearchRequest("   ", Role.ROLE_TECHNICIAN)
        );

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().email()).isEqualTo("tech@test.com");
    }

    @Test
    @DisplayName("키워드와 역할로 직원을 조건 검색한다")
    void getMembersByCondition_withKeywordAndRole() {
        saveMember("tech01", "kim@test.com", Role.ROLE_TECHNICIAN);
        saveMember("recv01", "lee@test.com", Role.ROLE_RECEPTIONIST);

        List<MemberDto.Response> responses = memberService.getMembersByCondition(
                new MemberDto.MemberSearchRequest("kim", Role.ROLE_TECHNICIAN)
        );

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().email()).isEqualTo("kim@test.com");
    }

    @Test
    @DisplayName("직원 상태를 변경한다")
    void changeStatus_success() {
        saveMember("tech01", "tech@test.com", Role.ROLE_TECHNICIAN);

        MemberDto.Response response = memberService.changeStatus(
                new MemberDto.StatusChangeRequest("tech@test.com", Status.INACTIVE)
        );

        assertThat(response.email()).isEqualTo("tech@test.com");
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
    @DisplayName("삭제된 계정의 상태를 변경하면 예외가 발생한다")
    void changeStatus_deletedAccount() {
        Member member = saveMember("tech01", "tech@test.com", Role.ROLE_TECHNICIAN);
        member.changeStatus(Status.DELETED);

        assertThatThrownBy(() -> memberService.changeStatus(
                new MemberDto.StatusChangeRequest("tech@test.com", Status.ACTIVE)
        ))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 계정입니다.");
    }

    @Test
    @DisplayName("비밀번호를 변경한다")
    void changePassword_success() {
        saveMember("tech01", "tech@test.com", Role.ROLE_TECHNICIAN);

        MemberDto.Response response = memberService.changePassword(
                new MemberDto.PasswordUpdateRequest("tech@test.com", "newPassword")
        );

        assertThat(response.email()).isEqualTo("tech@test.com");
        assertThat(passwordEncoder.matches(
                "newPassword",
                memberRepository.findByEmail("tech@test.com").orElseThrow().getPasswordHash()
        )).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 계정의 비밀번호를 변경하면 예외가 발생한다")
    void changePassword_notFound() {
        assertThatThrownBy(() -> memberService.changePassword(
                new MemberDto.PasswordUpdateRequest("gone@test.com", "newPassword")
        ))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 계정입니다.");
    }

    @Test
    @DisplayName("프로필 정보를 변경한다")
    void updateProfile_success() {
        saveMember("tech01", "tech@test.com", Role.ROLE_TECHNICIAN);

        MemberDto.Response response = memberService.updateProfile(
                new MemberDto.ProfileUpdateRequest(
                        "tech@test.com",
                        "변경된이름",
                        "010-9999-8888",
                        "http://img.test/updated.png"
                )
        );

        assertThat(response.name()).isEqualTo("변경된이름");
        assertThat(response.phone()).isEqualTo("010-9999-8888");
        assertThat(response.profileImgUrl()).isEqualTo("http://img.test/updated.png");

        Member updated = memberRepository.findByEmail("tech@test.com").orElseThrow();
        assertThat(updated.getName()).isEqualTo("변경된이름");
        assertThat(updated.getPhone()).isEqualTo("010-9999-8888");
    }

    @Test
    @DisplayName("존재하지 않는 계정의 프로필을 변경하면 예외가 발생한다")
    void updateProfile_notFound() {
        assertThatThrownBy(() -> memberService.updateProfile(
                new MemberDto.ProfileUpdateRequest("gone@test.com", "이름", "010-0000-0000", "")
        ))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 계정입니다.");
    }
}
