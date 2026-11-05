package net.likelion.bebc25.fixgo.auth.controller;

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

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberDto.DetailResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MemberDto.DetailResponse response = memberService.getMemberById(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MemberDto.PasswordUpdateRequest request
    ) {
        Long loginMemberId = userDetails.getId();
        memberService.changePassword(loginMemberId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<MemberDto.BriefResponse>> getMember(
            @ModelAttribute MemberDto.MemberSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(memberService.getMembers(request, pageable));
    }

    @GetMapping("/deleted_member")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MemberDto.BriefResponse>> getDeletedMember(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(memberService.getDeletedMembers(pageable));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDto.DetailResponse> getMemberProfile(
            @PathVariable Long memberId
    ) {
        MemberDto.DetailResponse response = memberService.getMemberById(memberId);
        return ResponseEntity.ok(response);
    }
}
