package net.likelion.bebc25.fixgo.auth.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.dto.MemberDto;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public MemberDto.Response signup(MemberDto.CreateRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("사용 불가능한 이메일입니다.");
        }
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
        int count = memberRepository.countByCreatedAtBetween(start, end);
        Member member = memberRepository.save(request.toEntity(passwordEncoder, count));

        return MemberDto.Response.from(member);
    }

    // 모든 직원 목록 조회
    public List<MemberDto.Response> getAllMembers(Role loginMemberRole) {
        if (!loginMemberRole.equals(Role.ROLE_ADMIN)) {
            return memberRepository.findAllByStatusNotOrderByCreatedAtDesc(Status.DELETED)
                    .stream().map(MemberDto.Response::from)
                    .toList();
        }
        return memberRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(MemberDto.Response::from)
                .toList();
    }

    // 조건별 직원 목록 조회
    public List<MemberDto.Response> getMembersByCondition(MemberDto.MemberSearchRequest request) {
        String keyword = request.keyword();

        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        return memberRepository.searchMembers(keyword, request.role())
                .stream().map(MemberDto.Response::from)
                .toList();
    }

    // Id로 1건 조회
    public MemberDto.Response getMemberById(Long id) {
        return MemberDto.Response.from(memberRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("유효하지 않은 계정입니다.")
        ));
    }

    // 상태 변경
    @Transactional
    public MemberDto.Response changeStatus(MemberDto.StatusChangeRequest request) {
        Member member = memberRepository.findByEmailAndStatusNot(request.email(), Status.DELETED).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 계정입니다.")
        );

        member.changeStatus(request.status());

        return MemberDto.Response.from(member);
    }

    // 비밀번호 변경
    @Transactional
    public MemberDto.Response changePassword(Long loginMemberId, MemberDto.PasswordUpdateRequest request) {
        Member member = memberRepository.findById(loginMemberId).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 계정입니다.")
        );
        if (!member.getEmail().equals(request.email())) {
            throw new IllegalArgumentException("비정상적인 접근입니다.");
        }

        member.changePassword(passwordEncoder.encode(request.password()));

        return MemberDto.Response.from(member);
    }

    // 프로필 정보 변경
    @Transactional
    public MemberDto.Response updateProfile(MemberDto.ProfileUpdateRequest request) {
        Member member = memberRepository.findByEmailAndStatusNot(request.email(), Status.DELETED).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 계정입니다.")
        );

        member.updateProfile(request);

        return MemberDto.Response.from(member);
    }
}
