package org.citycare.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.citycare.notificationservice.dto.request.*;
import org.citycare.notificationservice.dto.response.ApiResponse;
import org.citycare.notificationservice.entity.Notification;
import org.citycare.notificationservice.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ── General CRUD ────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Notification>> create(
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Notification created", notificationService.createNotification(request)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Notification>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Notifications for user " + userId,
                notificationService.getNotificationsForUser(userId)));
    }

    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Unread notifications",
                notificationService.getUnreadNotificationsForUser(userId)));
    }

    @GetMapping("/user/{userId}/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Unread count",
                notificationService.getUnreadCount(userId)));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Marked as read", notificationService.markAsRead(id)));
    }

    @PutMapping("/user/{userId}/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> markAllRead(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("All marked as read",
                notificationService.markAllAsRead(userId)));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'CITY_HEALTH_OFFICER')")
    public ResponseEntity<ApiResponse<List<Notification>>> getByCategory(
            @PathVariable Notification.Category category) {
        return ResponseEntity.ok(ApiResponse.ok("Notifications for category " + category,
                notificationService.getByCategory(category)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.ok("Notification deleted", null));
    }

    // ── Service Event Endpoints (called internally by other services) ────────────

    @PostMapping("/events/emergency")
    public ResponseEntity<ApiResponse<Notification>> emergencyEvent(
            @RequestBody EmergencyEventRequest event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Emergency notification sent",
                        notificationService.handleEmergencyEvent(event)));
    }

    @PostMapping("/events/patient")
    public ResponseEntity<ApiResponse<Notification>> patientEvent(
            @RequestBody PatientEventRequest event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Patient notification sent",
                        notificationService.handlePatientEvent(event)));
    }

    @PostMapping("/events/compliance")
    public ResponseEntity<ApiResponse<Notification>> complianceEvent(
            @RequestBody ComplianceEventRequest event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Compliance notification sent",
                        notificationService.handleComplianceEvent(event)));
    }

    @PostMapping("/events/auth")
    public ResponseEntity<ApiResponse<Notification>> authEvent(
            @RequestBody AuthEventRequest event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Auth notification sent",
                        notificationService.handleAuthEvent(event)));
    }

    @PostMapping("/events/facility")
    public ResponseEntity<ApiResponse<Notification>> facilityEvent(
            @RequestBody FacilityEventRequest event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Facility notification sent",
                        notificationService.handleFacilityEvent(event)));
    }
}
