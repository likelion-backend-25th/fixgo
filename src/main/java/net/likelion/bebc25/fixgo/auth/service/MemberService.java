package net.likelion.bebc25.fixgo.auth.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.dto.MemberDto;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import net.likelion.bebc25.fixgo.auth.exception.AbnormalApproachException;
import net.likelion.bebc25.fixgo.auth.exception.AuthErrorCode;
import net.likelion.bebc25.fixgo.auth.exception.EmployeeHasActiveRepairsException;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AsReceptionRepository asReceptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    // 회원가입
    @Transactional
    public MemberDto.DetailResponse signup(MemberDto.CreateRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("사용 불가능한 이메일입니다.");
        }
        Member member = memberRepository.save(request.toEntity(passwordEncoder));

        return MemberDto.DetailResponse.from(member);
    }

    // 직원 목록 조회
    public Page<MemberDto.BriefResponse> getMembers(
            MemberDto.MemberSearchRequest request,
            Pageable pageable
    ) {
        return memberRepository
                .searchMembers(request.keyword(), request.role(), request.status(), Status.DELETED, pageable)
                .map(MemberDto.BriefResponse::from);
    }

    public Page<MemberDto.TechnicianResponse> getAssignableTechnicians(
            String keyword,
            MemberDto.TechnicianSort sortBy,
            Sort.Direction direction,
            Pageable pageable
    ) {
        String name = keyword == null ? "" : keyword.strip();
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return memberRepository.findAssignableTechnicians(name, sortBy.name(), direction.name(), page)
                .map(row -> new MemberDto.TechnicianResponse(
                        row.getTechnicianId(), row.getName(), row.getEmail(), Status.ACTIVE,
                        row.getInspectionWaitingCount(), row.getRepairInProgressCount(),
                        row.getEstimatedDurationMinutes()
                ));
    }

    public Page<MemberDto.BriefResponse> getDeletedMembers(
            Pageable pageable
    ) {
        return memberRepository
                .searchMembersByStatus(Status.DELETED, pageable)
                .map(MemberDto.BriefResponse::from);
    }

    // Id로 1건 조회
    public MemberDto.DetailResponse getMemberById(Long id) {
        return MemberDto.DetailResponse.from(memberRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 계정입니다.")
        ));
    }

    // 상태 변경
    @Transactional
    public MemberDto.BriefResponse changeStatus(MemberDto.StatusChangeRequest request) {
        Member member = memberRepository.findByEmailWithLock(request.email()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 계정입니다.")
        );

        if (request.status() != Status.ACTIVE) {
            ensureNoOpenReceptions(member, "계정 상태를 변경");
        }
        member.changeStatus(request.status());
        refreshTokenService.revokeRefreshTokens(member.getId());

        return MemberDto.BriefResponse.from(member);
    }

    // 직책 변경
    @Transactional
    public MemberDto.BriefResponse changeRole(MemberDto.RoleChangeRequest request) {
        Member member = memberRepository.findByEmailWithLock(request.email())
                .filter(it -> it.getStatus() != Status.DELETED)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 계정입니다."));

        if (request.role() != Role.ROLE_TECHNICIAN) {
            ensureNoOpenReceptions(member, "직책을 변경");
        }
        member.changeRole(request.role());
        refreshTokenService.revokeRefreshTokens(member.getId());

        return MemberDto.BriefResponse.from(member);
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Long loginMemberId, MemberDto.PasswordUpdateRequest request) {
        Member member = memberRepository.findById(loginMemberId).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 계정입니다.")
        );
        if (!member.getEmail().equals(request.email())) {
            throw new AbnormalApproachException(
                    AuthErrorCode.ABNORMAL_APPROACH,
                    "비정상적인 접근입니다."
            );
        }

        member.changePassword(passwordEncoder.encode(request.password()));
    }

    // 프로필 정보 변경
    @Transactional
    public MemberDto.DetailResponse updateProfile(MemberDto.ProfileUpdateRequest request) {
        Member member = memberRepository.findByEmailAndStatusNot(request.email(), Status.DELETED).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 계정입니다.")
        );

        member.updateProfile(request);

        return MemberDto.DetailResponse.from(member);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findByIdWithLock(memberId)
                .filter(it -> it.getStatus() != Status.DELETED)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 계정입니다."));
        ensureNoOpenReceptions(member, "삭제");

        member.changeStatus(Status.DELETED);
        refreshTokenService.revokeRefreshTokens(memberId);
    }

    private void ensureNoOpenReceptions(Member member, String action) {
        if (asReceptionRepository.existsByTechnician_IdAndStatusNot(member.getId(), AsReceptionStatus.COMPLETED)) {
            throw new EmployeeHasActiveRepairsException(
                    AuthErrorCode.EMPLOYEE_HAS_ACTIVE_REPAIRS,
                    "현재 진행 중인 A/S 건이 있어 " + action + "할 수 없습니다."
            );
        }
    }
}
