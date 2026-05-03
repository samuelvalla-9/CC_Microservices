package org.citycare.notificationservice.dto.request;

import lombok.Data;

/**
 * Inbound event payload sent by ComplianceService.
 */
@Data
public class ComplianceEventRequest {
    private Long complianceId;
    private Long entityId;
    private String entityType;   // FACILITY | PATIENT | EMERGENCY
    private String result;       // PASS | FAIL | PENDING
    private String eventType;    // RECORD_CREATED | AUDIT_CREATED | AUDIT_COMPLETED
    private Long officerId;
    private String findings;
    private String recipientEmail;
}
