package org.citycare.complianceservice.feign;

import org.citycare.complianceservice.feign.dto.EmergencyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "emergency-service", fallback = EmergencyClientFallback.class)
public interface EmergencyClient {

    @GetMapping("/api/emergencies/{id}")
    EmergencyResponse getEmergencyById(@PathVariable("id") Long emergencyId);
}
