package net.likelion.bebc25.fixgo.portal.repository;

import net.likelion.bebc25.fixgo.portal.entity.CustomerPortalLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerPortalLinkRepository extends JpaRepository<CustomerPortalLink, Long> {
    Optional<CustomerPortalLink> findByToken(String token);

    boolean existsByAsReceptionId(Long asReceptionId);

    Optional<CustomerPortalLink> findByAsReceptionId(Long asReceptionId);
}