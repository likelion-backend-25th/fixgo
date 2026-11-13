package net.likelion.bebc25.fixgo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MemberDto {
    public record CreateRequest(
            @Schema(description = "회원 이메일", example = "user@fixgo.com")
            @Email @NotNull String email,
            @Schema(description = "비밀번호", example = "password123!")
            @NotNull String password,
            @Schema(description = "회원 이름", example = "김병호")
            @NotNull String name,
            @Schema(description = "휴대폰 번호", example = "010-1234-5678")
            @NotNull String phone,
            @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
            @NotNull String address,
            @Schema(description = "역할", example = "TECHNICIAN")
            @NotNull Role role
    ) {
        public Member toEntity(PasswordEncoder passwordEncoder) {
            String passwordHash = passwordEncoder.encode(password);
            return new Member(email, passwordHash, name, phone, address, role);
        }
    }

    public record MemberSearchRequest(
            @Schema(description = "검색 키워드(이름/이메일)", example = "김병호")
            String keyword,
            @Schema(description = "역할 필터", example = "TECHNICIAN")
            Role role,
            @Schema(description = "상태 필터", example = "ACTIVE")
            Status status
    ) {}

    public record StatusChangeRequest(
            @Schema(description = "회원 이메일", example = "user@fixgo.com")
            @Email @NotNull String email,
            @Schema(description = "변경할 상태", example = "INACTIVE")
            @NotNull Status status
    ) {}

    public record RoleChangeRequest(
            @Schema(description = "회원 이메일", example = "user@fixgo.com")
            @Email @NotNull String email,
            @Schema(description = "변경할 역할", example = "RECEPTIONIST")
            @NotNull Role role
    ) {}

    public record PasswordUpdateRequest(
            @Schema(description = "회원 이메일", example = "user@fixgo.com")
            @Email @NotNull String email,
            @Schema(description = "새 비밀번호", example = "newPassword123!")
            @NotNull String password
    ) {}

    public record ProfileUpdateRequest(
            @Schema(description = "회원 이메일", example = "user@fixgo.com")
            @Email @NotNull String email,
            @Schema(description = "회원 이름", example = "김병호")
            @NotNull String name,
            @Schema(description = "휴대폰 번호", example = "010-9876-5432")
            @NotNull String phone,
            @Schema(description = "주소", example = "서울특별시 서초구 반포대로 45")
            @NotNull String address
    ) {}

    public record BriefResponse(
            @Schema(description = "회원 ID", example = "1")
            Long id,
            @Schema(description = "회원 이메일", example = "user@fixgo.com")
            String email,
            @Schema(description = "회원 이름", example = "김병호")
            String name,
            @Schema(description = "역할", example = "TECHNICIAN")
            Role role,
            @Schema(description = "상태", example = "ACTIVE")
            Status status
    ) {
        public static BriefResponse from(Member member) {
            return new BriefResponse(
                    member.getId(),
                    member.getEmail(),
                    member.getName(),
                    member.getRole(),
                    member.getStatus()
            );
        }
    }

    public enum TechnicianSort {
        NAME, INSPECTION_WAITING_COUNT, REPAIR_IN_PROGRESS_COUNT, ESTIMATED_DURATION_MINUTES
    }

    public record TechnicianResponse(
            @Schema(description = "기술자 ID", example = "3")
            Long technicianId,
            @Schema(description = "기술자 이름", example = "이수리")
            String name,
            @Schema(description = "기술자 이메일", example = "tech@fixgo.com")
            String email,
            @Schema(description = "상태", example = "ACTIVE")
            Status status,
            @Schema(description = "점검 대기 건수", example = "2")
            long inspectionWaitingCount,
            @Schema(description = "수리 진행 중 건수", example = "1")
            long repairInProgressCount,
            @Schema(description = "예상 소요 시간(분)", example = "90.0")
            Double estimatedDurationMinutes
    ) {}

    public record DetailResponse(
            @Schema(description = "회원 ID", example = "1")
            Long id,
            @Schema(description = "회원 이메일", example = "user@fixgo.com")
            String email,
            @Schema(description = "회원 이름", example = "김병호")
            String name,
            @Schema(description = "휴대폰 번호", example = "010-1234-5678")
            String phone,
            @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
            String address,
            @Schema(description = "역할", example = "TECHNICIAN")
            Role role,
            @Schema(description = "상태", example = "ACTIVE")
            Status status
    ) {
        public static DetailResponse from(Member member) {
            return new DetailResponse(
                    member.getId(),
                    member.getEmail(),
                    member.getName(),
                    member.getPhone(),
                    member.getAddress(),
                    member.getRole(),
                    member.getStatus()
            );
        }
    }
}
