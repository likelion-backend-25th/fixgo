package net.likelion.bebc25.fixgo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MemberDto {
    public record CreateRequest(
            @Email @NotNull String email,
            @NotNull String password,
            @NotNull String name,
            @NotNull String phone,
            @NotNull String address,
            @NotNull Role role,
            @NotNull String profileImgUrl
    ) {
        public Member toEntity(PasswordEncoder passwordEncoder) {
            String passwordHash = passwordEncoder.encode(password);
            return new Member(email, passwordHash, name, phone, address, role, profileImgUrl);
        }
    }

    public record MemberSearchRequest(
            String keyword,
            Role role,
            Status status
    ) {}

    public record StatusChangeRequest(
            @Email @NotNull String email,
            @NotNull Status status
    ) {}

    public record RoleChangeRequest(
            @Email @NotNull String email,
            @NotNull Role role
    ) {}

    public record PasswordUpdateRequest(
            @Email @NotNull String email,
            @NotNull String password
    ) {}

    public record ProfileUpdateRequest(
            @Email @NotNull String email,
            @NotNull String name,
            @NotNull String phone,
            @NotNull String address,
            @NotNull String profileImgUrl
    ) {}

    public record BriefResponse(
            Long id,
            String email,
            String name,
            Role role,
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
            Long technicianId,
            String name,
            String email,
            Status status,
            long inspectionWaitingCount,
            long repairInProgressCount,
            Double estimatedDurationMinutes
    ) {}

    public record DetailResponse(
            Long id,
            String email,
            String name,
            String phone,
            String address,
            Role role,
            String profileImgUrl,
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
                    member.getProfileImgUrl(),
                    member.getStatus()
            );
        }
    }
}
