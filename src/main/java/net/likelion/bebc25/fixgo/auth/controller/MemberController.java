package net.likelion.bebc25.fixgo.auth.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.dto.MemberDto;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberDto.Response> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MemberDto.Response response = memberService.getMemberById(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<MemberDto.Response> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MemberDto.PasswordUpdateRequest request
    ) {
        Long loginMemberId = userDetails.getId();
        MemberDto.Response response = memberService.changePassword(loginMemberId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDto.Response> getMemberProfile(
            @PathVariable Long memberId
    ) {
        MemberDto.Response response = memberService.getMemberById(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<MemberDto.Response>> getMemberList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<MemberDto.Response> responses = memberService.getAllMembers(userDetails.getMember().getRole());
        return ResponseEntity.ok(responses);
    }


}
