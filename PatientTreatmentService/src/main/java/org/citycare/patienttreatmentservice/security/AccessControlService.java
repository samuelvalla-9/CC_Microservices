package org.citycare.patienttreatmentservice.security;

import lombok.RequiredArgsConstructor;
import org.citycare.patienttreatmentservice.entity.Treatment;
import org.citycare.patienttreatmentservice.repository.TreatmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("patientSecurity")
@RequiredArgsConstructor
public class AccessControlService {

    private final TreatmentRepository treatmentRepository;

    public boolean canAccessPatient(Authentication authentication, Long patientId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (isAdmin) return true;

        String userIdStr = authentication.getName();
        if (userIdStr == null) return false;
        
        try {
            Long userId = Long.parseLong(userIdStr);
            List<Treatment> treatments = treatmentRepository.findByPatientPatientId(patientId);
            return treatments.stream().anyMatch(t -> userId.equals(t.getAssignedById()));
        } catch (NumberFormatException e) {
            return false;
        }
    }
}