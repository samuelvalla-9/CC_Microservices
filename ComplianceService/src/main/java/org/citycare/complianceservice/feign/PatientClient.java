package org.citycare.complianceservice.feign;

import org.citycare.complianceservice.feign.dto.PatientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "patient-treatment-service", fallback = PatientClientFallback.class)
public interface PatientClient {

    @GetMapping("/api/patients/{id}")
    PatientResponse getPatientById(@PathVariable("id") Long patientId);
}
