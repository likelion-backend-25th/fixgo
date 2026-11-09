package net.likelion.bebc25.fixgo.portal.dto;

import net.likelion.bebc25.fixgo.portal.entity.CustomerPortalLink;

import java.time.LocalDateTime;

public record CustomerPortalLinkResponse(
        String token,
        LocalDateTime expiresAt
) {
    public static CustomerPortalLinkResponse from(CustomerPortalLink link) {
        return new CustomerPortalLinkResponse(link.getToken(), link.getExpiresAt());
    }
}
