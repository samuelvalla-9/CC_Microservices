package org.citycare.notificationservice.dto.request;

import lombok.Data;

/**
 * Inbound event payload sent by EmergencyService when an emergency is reported.
 */
@Data
public class EmergencyEventRequest {
    private Long emergencyId;
    private Long citizenId;
    private String type;         // ACCIDENT / HEART_ATTACK / FIRE
    private String location;
    private Long dispatcherId;   // nullable – set when dispatched
    private String eventType;    // REPORTED | DISPATCHED | STATUS_CHANGED | RESOLVED
    private String newStatus;
    private String recipientEmail;
}
