package org.citycare.notificationservice.dto.request;

import lombok.Data;

/**
 * Inbound event payload sent by PatientTreatmentService.
 */
@Data
public class PatientEventRequest {
    private Long patientId;
    private Long citizenId;
    private Long facilityId;
    private String eventType;   // ADMITTED | DISCHARGED | TREATMENT_ADDED | STATUS_CHANGED
    private String newStatus;
    private String description; // treatment description (optional)
    private Long doctorId;
    private String recipientEmail;
}
