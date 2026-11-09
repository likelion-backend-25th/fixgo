package net.likelion.bebc25.fixgo.auth.dto;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MemberDto {
    public record CreateRequest(
            String email,
            String password,
            String name,
            String phone,
            String address,
            Role role,
            String profileImgUrl
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
            String email,
            Status status
    ) {}

    public record RoleChangeRequest(
            String email,
            Role role
    ) {}

    public record PasswordUpdateRequest(
            String email,
            String password
    ) {}

    public record ProfileUpdateRequest(
            String email,
            String name,
            String phone,
            String address,
            String profileImgUrl
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
