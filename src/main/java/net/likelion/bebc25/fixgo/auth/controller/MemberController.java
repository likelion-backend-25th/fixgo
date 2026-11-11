package net.likelion.bebc25.fixgo.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.dto.MemberDto;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member API", description = "회원 조회 및 프로필 관리 API")
@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "로그인한 회원의 상세 프로필 정보를 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
    })
    public ResponseEntity<MemberDto.DetailResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MemberDto.DetailResponse response = memberService.getMemberById(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/password")
    @Operation(summary = "비밀번호 변경", description = "로그인한 회원의 비밀번호를 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
    })
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid MemberDto.PasswordUpdateRequest request
    ) {
        Long loginMemberId = userDetails.getId();
        memberService.changePassword(loginMemberId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "회원 목록 조회", description = "검색 조건에 맞는 회원 목록을 페이징하여 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공"),
    })
    public ResponseEntity<Page<MemberDto.BriefResponse>> getMember(
            @ModelAttribute @Valid MemberDto.MemberSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(memberService.getMembers(request, pageable));
    }

    @GetMapping("/deleted_member")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "삭제된 회원 목록 조회", description = "관리자가 삭제된 회원 목록을 페이징하여 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제된 회원 목록 조회 성공"),
    })
    public ResponseEntity<Page<MemberDto.BriefResponse>> getDeletedMember(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(memberService.getDeletedMembers(pageable));
    }

    @GetMapping("/technicians")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    @Operation(summary = "배정 가능 기술자 목록 조회", description = "접수원/관리자가 배정 가능한 기술자 목록을 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기술자 목록 조회 성공"),
    })
    public ResponseEntity<Page<MemberDto.TechnicianResponse>> getAssignableTechnicians(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "NAME") MemberDto.TechnicianSort sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(memberService.getAssignableTechnicians(keyword, sortBy, direction, pageable));
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 상세 조회", description = "회원 ID로 상세 프로필 정보를 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 상세 조회 성공"),
    })
    public ResponseEntity<MemberDto.DetailResponse> getMemberProfile(
            @PathVariable Long memberId
    ) {
        MemberDto.DetailResponse response = memberService.getMemberById(memberId);
        return ResponseEntity.ok(response);
    }
}
