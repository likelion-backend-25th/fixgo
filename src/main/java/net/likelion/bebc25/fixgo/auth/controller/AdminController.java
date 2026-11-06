package net.likelion.bebc25.fixgo.auth.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.dto.MemberDto;
import net.likelion.bebc25.fixgo.auth.service.MemberService;
import net.likelion.bebc25.fixgo.auth.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/admin/member")
@RequiredArgsConstructor
public class AdminController {
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping()
    public ResponseEntity<MemberDto.DetailResponse> createMember(
            @RequestBody MemberDto.CreateRequest request
    ) {
        MemberDto.DetailResponse response = memberService.signup(request);
        return ResponseEntity.created(URI.create("/api/v1/member" + response.id())).body(response);
    }

    @PatchMapping("/status")
    public ResponseEntity<MemberDto.BriefResponse> updateStatus(
            @RequestBody MemberDto.StatusChangeRequest request
    ) {

        return ResponseEntity.ok(memberService.changeStatus(request));
    }

    @PatchMapping("/role")
    public ResponseEntity<MemberDto.BriefResponse> updateRole(
            @RequestBody MemberDto.RoleChangeRequest request
    ) {
        return ResponseEntity.ok(memberService.changeRole(request));
    }

    @PatchMapping("/profile")
    public ResponseEntity<MemberDto.DetailResponse> updateProfile(
            @RequestBody MemberDto.ProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(memberService.updateProfile(request));
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteMember(
            @RequestParam("memberId") Long memberId
    ) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok().build();
    }
}
