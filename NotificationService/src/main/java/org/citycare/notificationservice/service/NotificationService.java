package org.citycare.notificationservice.service;

import org.citycare.notificationservice.dto.request.*;
import org.citycare.notificationservice.entity.Notification;

import java.util.List;

public interface NotificationService {

    Notification createNotification(NotificationRequest request);

    List<Notification> getNotificationsForUser(Long userId);

    List<Notification> getUnreadNotificationsForUser(Long userId);

    long getUnreadCount(Long userId);

    Notification markAsRead(Long notificationId);

    int markAllAsRead(Long userId);

    List<Notification> getByCategory(Notification.Category category);

    // Event handlers — called by each service via REST
    Notification handleEmergencyEvent(EmergencyEventRequest event);

    Notification handlePatientEvent(PatientEventRequest event);

    Notification handleComplianceEvent(ComplianceEventRequest event);

    Notification handleAuthEvent(AuthEventRequest event);

    Notification handleFacilityEvent(FacilityEventRequest event);

    Notification handleDocumentEvent(DocumentEventRequest event);

    void deleteNotification(Long id);
}
