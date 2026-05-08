package org.citycare.complianceservice.security;

import lombok.RequiredArgsConstructor;
import org.citycare.complianceservice.entity.Audit;
import org.citycare.complianceservice.repository.AuditRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("complianceSecurity")
@RequiredArgsConstructor
public class AccessControlService {

    private final AuditRepository auditRepository;

    public boolean canAccessAudit(Authentication authentication, Long auditId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (isAdmin) return true;

        String userIdStr = authentication.getName();
        if (userIdStr == null) return false;

        try {
            Long userId = Long.parseLong(userIdStr);
            return auditRepository.findById(auditId)
                    .map(audit -> userId.equals(audit.getOfficerId()))
                    .orElse(false);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
