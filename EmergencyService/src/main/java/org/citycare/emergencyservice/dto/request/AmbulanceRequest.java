package org.citycare.emergencyservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AmbulanceRequest {
    @NotBlank private String vehicleNumber;
    private String model;
}
