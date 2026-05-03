package org.citycare.notificationservice.dto.request;

import lombok.Data;

/**
 * Inbound event payload sent by AuthService on registration / role changes.
 */
@Data
public class AuthEventRequest {
    private Long userId;
    private String name;
    private String role;
    private String eventType;   // USER_REGISTERED | PASSWORD_CHANGED | ROLE_UPDATED
    private String recipientEmail;
}
