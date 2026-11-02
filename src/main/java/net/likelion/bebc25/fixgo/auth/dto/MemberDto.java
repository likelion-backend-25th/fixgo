package net.likelion.bebc25.fixgo.auth.dto;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MemberDto {
    public record CreateRequest(
            String email,
            String password,
            String name,
            String phone,
            Role role,
            String profileImgUrl
    ) {
        public Member toEntity(PasswordEncoder passwordEncoder, int count) {
            String code = String.format("EMP_%s_%03d", LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")), count);
            String passwordHash = passwordEncoder.encode(password);

            return new Member(code, email, passwordHash, name, phone, role, profileImgUrl);
        }
    }

    public record MemberSearchRequest(
            String keyword,
            Role role
    ) {}

    public record StatusChangeRequest(
            String email,
            Status status
    ) {}

    public record PasswordUpdateRequest(
            String email,
            String password
    ) {}

    public record ProfileUpdateRequest(
            String email,
            String name,
            String phone,
            String profileImgUrl
    ) {}

    public record Response(
            Long id,
            String code,
            String email,
            String name,
            String phone,
            Role role,
            String profileImgUrl
    ) {
        public static Response from(Member member) {
            return new Response(
                    member.getId(),
                    member.getCode(),
                    member.getEmail(),
                    member.getName(),
                    member.getPhone(),
                    member.getRole(),
                    member.getProfileImgUrl()
            );
        }
    }
}
