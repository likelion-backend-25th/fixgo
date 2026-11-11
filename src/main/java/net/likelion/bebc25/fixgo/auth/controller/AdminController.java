package net.likelion.bebc25.fixgo.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.dto.MemberDto;
import net.likelion.bebc25.fixgo.auth.service.MemberService;
import net.likelion.bebc25.fixgo.auth.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Admin Member API", description = "관리자용 회원 관리 API")
@RestController
@RequestMapping("/api/v1/admin/member")
@RequiredArgsConstructor
public class AdminController {
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping()
    @Operation(summary = "회원 생성", description = "관리자가 신규 회원을 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원 생성 성공"),
    })
    public ResponseEntity<MemberDto.DetailResponse> createMember(
            @RequestBody @Valid MemberDto.CreateRequest request
    ) {
        MemberDto.DetailResponse response = memberService.signup(request);
        return ResponseEntity.created(URI.create("/api/v1/member" + response.id())).body(response);
    }

    @PatchMapping("/status")
    @Operation(summary = "회원 상태 변경", description = "관리자가 회원의 상태를 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
    })
    public ResponseEntity<MemberDto.BriefResponse> updateStatus(
            @RequestBody @Valid MemberDto.StatusChangeRequest request
    ) {

        return ResponseEntity.ok(memberService.changeStatus(request));
    }

    @PatchMapping("/role")
    @Operation(summary = "회원 역할 변경", description = "관리자가 회원의 역할을 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "역할 변경 성공"),
    })
    public ResponseEntity<MemberDto.BriefResponse> updateRole(
            @RequestBody @Valid MemberDto.RoleChangeRequest request
    ) {
        return ResponseEntity.ok(memberService.changeRole(request));
    }

    @PatchMapping("/profile")
    @Operation(summary = "회원 프로필 수정", description = "관리자가 회원 프로필 정보를 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
    })
    public ResponseEntity<MemberDto.DetailResponse> updateProfile(
            @RequestBody @Valid MemberDto.ProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(memberService.updateProfile(request));
    }

    @DeleteMapping()
    @Operation(summary = "회원 삭제", description = "관리자가 회원을 삭제(소프트 삭제)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 삭제 성공"),
    })
    public ResponseEntity<Void> deleteMember(
            @RequestParam("memberId") Long memberId
    ) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok().build();
    }
}
