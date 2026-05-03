package org.citycare.notificationservice.dto.request;

import lombok.Data;

/**
 * Inbound event payload sent by FacilityService.
 */
@Data
public class FacilityEventRequest {
    private Long facilityId;
    private String facilityName;
    private String eventType;  // FACILITY_ADDED | STAFF_JOINED | CAPACITY_CRITICAL
    private Long staffId;
    private String staffRole;
    private Integer currentCapacity;
    private Integer maxCapacity;
    private Long notifyUserId;
    private String recipientEmail;
}
