package org.citycare.citizenservice.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.citycare.citizenservice.entity.Citizen;

import java.time.LocalDate;


@Data
public class CitizenProfileRequest {

    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @JsonFormat(pattern = "dd-MM-yyyy", shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private LocalDate dateOfBirth;

    private Citizen.Gender gender;

    private String address;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String contactInfo;
}
