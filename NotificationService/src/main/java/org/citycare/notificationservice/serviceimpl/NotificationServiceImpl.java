package org.citycare.notificationservice.serviceimpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.citycare.notificationservice.dto.request.*;
import org.citycare.notificationservice.entity.Notification;
import org.citycare.notificationservice.feign.AuthClient;
import org.citycare.notificationservice.repository.NotificationRepository;
import org.citycare.notificationservice.service.EmailService;
import org.citycare.notificationservice.service.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final AuthClient authClient;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public Notification createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .entityId(request.getEntityId())
                .message(request.getMessage())
                .title(request.getTitle())
                .category(request.getCategory())
                .recipientEmail(request.getRecipientEmail())
                .channel(request.getChannel() != null ? request.getChannel() : Notification.Channel.IN_APP)
                .status(Notification.NotificationStatus.UNREAD)
                .build();

        Notification saved = saveAndPublish(notification);

        if (request.getRecipientEmail() != null && !request.getRecipientEmail().isBlank()) {
            emailService.sendEmail(request.getRecipientEmail(),
                    request.getTitle() != null ? request.getTitle() : request.getCategory().name(),
                    request.getMessage());
        }

        return saved;
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    @Override
    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdAndStatusOrderByCreatedDateDesc(userId, Notification.NotificationStatus.UNREAD);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, Notification.NotificationStatus.UNREAD);
    }

    @Override
    @Transactional
    public Notification markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    public List<Notification> getByCategory(Notification.Category category) {
        return notificationRepository.findByCategoryOrderByCreatedDateDesc(category);
    }

    // ── Emergency Events ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public Notification handleEmergencyEvent(EmergencyEventRequest event) {
        String title;
        String message;

        switch (event.getEventType()) {
            case "REPORTED" -> {
                title = "🚨 Emergency Reported";
                message = String.format(
                        "Emergency #%d of type [%s] has been reported at location: %s. Help is being arranged.",
                        event.getEmergencyId(), event.getType(), event.getLocation());
            }
            case "DISPATCHED" -> {
                title = "🚑 Ambulance Dispatched";
                message = String.format(
                        "An ambulance has been dispatched for Emergency #%d. Please stay at your location: %s.",
                        event.getEmergencyId(), event.getLocation());
                
                // Notify citizen
                Notification citizenNotif = Notification.builder()
                        .userId(event.getCitizenId())
                        .entityId(event.getEmergencyId())
                        .title(title)
                        .message(message)
                        .category(Notification.Category.EMERGENCY)
                        .recipientEmail(event.getRecipientEmail())
                        .channel(Notification.Channel.IN_APP)
                        .status(Notification.NotificationStatus.UNREAD)
                        .build();
                saveAndPublish(citizenNotif);
                
                if (event.getRecipientEmail() != null) {
                    emailService.sendEmail(event.getRecipientEmail(), title, message);
                }
                
                // Notify all admins
                try {
                    List<AuthClient.UserResponse> admins = authClient.getUsersByRole("ADMIN");
                    String adminTitle = "🚨 Patient Awaiting Admission";
                    String adminMessage = String.format(
                            "Emergency #%d has been dispatched. Patient awaiting admission at location: %s. Type: %s",
                            event.getEmergencyId(), event.getLocation(), event.getType());
                    
                    for (AuthClient.UserResponse admin : admins) {
                        Notification adminNotif = Notification.builder()
                                .userId(admin.id())
                                .entityId(event.getEmergencyId())
                                .title(adminTitle)
                                .message(adminMessage)
                                .category(Notification.Category.EMERGENCY)
                                .recipientEmail(admin.email())
                                .channel(Notification.Channel.IN_APP)
                                .status(Notification.NotificationStatus.UNREAD)
                                .build();
                        saveAndPublish(adminNotif);
                        
                        if (admin.email() != null) {
                            emailService.sendEmail(admin.email(), adminTitle, adminMessage);
                        }
                    }
                    log.info("Notified {} admin(s) about dispatched emergency #{}", admins.size(), event.getEmergencyId());
                } catch (Exception e) {
                    log.error("Failed to notify admins about dispatched emergency: {}", e.getMessage());
                }
                
                log.info("Emergency notification created for citizen {} | event: {}", event.getCitizenId(), event.getEventType());
                return citizenNotif;
            }
            case "STATUS_CHANGED" -> {
                title = "📋 Emergency Status Updated";
                message = String.format(
                        "Emergency #%d status has been updated to: %s.",
                        event.getEmergencyId(), event.getNewStatus());
            }
            case "RESOLVED" -> {
                title = "✅ Emergency Resolved";
                message = String.format(
                        "Emergency #%d has been resolved. Thank you for using CityCare.",
                        event.getEmergencyId());
            }
            default -> {
                title = "Emergency Update";
                message = "There is an update on your emergency case #" + event.getEmergencyId();
            }
        }

        Notification notification = Notification.builder()
                .userId(event.getCitizenId())
                .entityId(event.getEmergencyId())
                .title(title)
                .message(message)
                .category(Notification.Category.EMERGENCY)
                .recipientEmail(event.getRecipientEmail())
                .channel(Notification.Channel.IN_APP)
                .status(Notification.NotificationStatus.UNREAD)
                .build();

        Notification saved = saveAndPublish(notification);

        if (event.getRecipientEmail() != null) {
            emailService.sendEmail(event.getRecipientEmail(), title, message);
        }

        log.info("Emergency notification created for citizen {} | event: {}", event.getCitizenId(), event.getEventType());
        return saved;
    }

    // ── Patient Events ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Notification handlePatientEvent(PatientEventRequest event) {
        String title;
        String message;

        switch (event.getEventType()) {
            case "ADMITTED" -> {
                title = "🏥 Patient Admitted";
                message = String.format(
                        "Patient #%d has been admitted to Facility #%d. Current status: ADMITTED.",
                        event.getPatientId(), event.getFacilityId());
            }
            case "DISCHARGED" -> {
                title = "🏠 Patient Discharged";
                message = String.format(
                        "Patient #%d has been discharged from Facility #%d. We wish you a speedy recovery!",
                        event.getPatientId(), event.getFacilityId());
            }
            case "TREATMENT_ADDED" -> {
                title = "💊 New Treatment Added";
                message = String.format(
                        "A new treatment has been recorded for Patient #%d by Doctor #%d: %s",
                        event.getPatientId(), event.getDoctorId(), event.getDescription());
            }
            case "STATUS_CHANGED" -> {
                title = "📊 Patient Status Updated";
                message = String.format(
                        "Status for Patient #%d has been updated to: %s.",
                        event.getPatientId(), event.getNewStatus());
            }
            default -> {
                title = "Patient Update";
                message = "There is an update for Patient #" + event.getPatientId();
            }
        }

        Notification notification = Notification.builder()
                .userId(event.getCitizenId())
                .entityId(event.getPatientId())
                .title(title)
                .message(message)
                .category(Notification.Category.PATIENT)
                .recipientEmail(event.getRecipientEmail())
                .channel(Notification.Channel.IN_APP)
                .status(Notification.NotificationStatus.UNREAD)
                .build();

        Notification saved = saveAndPublish(notification);

        if (event.getRecipientEmail() != null) {
            emailService.sendEmail(event.getRecipientEmail(), title, message);
        }

        log.info("Patient notification created for citizen {} | event: {}", event.getCitizenId(), event.getEventType());
        return saved;
    }

    // ── Compliance Events ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public Notification handleComplianceEvent(ComplianceEventRequest event) {
        String title;
        String message;

        switch (event.getEventType()) {
            case "RECORD_CREATED" -> {
                title = "📄 Compliance Record Created";
                message = String.format(
                        "A compliance record #%d has been created for entity #%d [%s]. Result: %s.",
                        event.getComplianceId(), event.getEntityId(), event.getEntityType(), event.getResult());
            }
            case "AUDIT_CREATED" -> {
                title = "🔍 Audit Initiated";
                message = String.format(
                        "An audit has been initiated by Officer #%d for entity #%d [%s].",
                        event.getOfficerId(), event.getEntityId(), event.getEntityType());
            }
            case "AUDIT_COMPLETED" -> {
                title = "✅ Audit Completed";
                message = String.format(
                        "Audit for entity #%d [%s] has been completed. Findings: %s",
                        event.getEntityId(), event.getEntityType(), event.getFindings());
            }
            default -> {
                title = "Compliance Update";
                message = "Compliance update for entity #" + event.getEntityId();
            }
        }

        Long notifyUserId = event.getOfficerId() != null ? event.getOfficerId() : event.getEntityId();

        Notification notification = Notification.builder()
                .userId(notifyUserId)
                .entityId(event.getComplianceId())
                .title(title)
                .message(message)
                .category(Notification.Category.COMPLIANCE)
                .recipientEmail(event.getRecipientEmail())
                .channel(Notification.Channel.IN_APP)
                .status(Notification.NotificationStatus.UNREAD)
                .build();

        Notification saved = saveAndPublish(notification);

        if (event.getRecipientEmail() != null) {
            emailService.sendEmail(event.getRecipientEmail(), title, message);
        }

        log.info("Compliance notification created | event: {}", event.getEventType());
        return saved;
    }

    // ── Auth Events ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Notification handleAuthEvent(AuthEventRequest event) {
        String title;
        String message;

        switch (event.getEventType()) {
            case "USER_REGISTERED" -> {
                title = "👋 Welcome to CityCare!";
                message = String.format(
                        "Welcome, %s! Your account has been successfully created with role: %s. You can now access CityCare services.",
                        event.getName(), event.getRole());
            }
            case "PASSWORD_CHANGED" -> {
                title = "🔐 Password Changed";
                message = String.format(
                        "Hi %s, your CityCare account password was recently changed. If this wasn't you, contact support immediately.",
                        event.getName());
            }
            case "ROLE_UPDATED" -> {
                title = "🔄 Role Updated";
                message = String.format(
                        "Hi %s, your account role has been updated to: %s.",
                        event.getName(), event.getRole());
            }
            default -> {
                title = "Account Update";
                message = "There is an update on your CityCare account.";
            }
        }

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .entityId(event.getUserId())
                .title(title)
                .message(message)
                .category(Notification.Category.AUTH)
                .recipientEmail(event.getRecipientEmail())
                .channel(Notification.Channel.IN_APP)
                .status(Notification.NotificationStatus.UNREAD)
                .build();

        Notification saved = saveAndPublish(notification);

        if (event.getRecipientEmail() != null) {
            emailService.sendEmail(event.getRecipientEmail(), title, message);
        }

        log.info("Auth notification created for user {} | event: {}", event.getUserId(), event.getEventType());
        return saved;
    }

    // ── Facility Events ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Notification handleFacilityEvent(FacilityEventRequest event) {
        String title;
        String message;

        switch (event.getEventType()) {
            case "FACILITY_ADDED" -> {
                title = "🏗️ New Facility Added";
                message = String.format(
                        "A new healthcare facility '%s' (ID: #%d) has been registered in CityCare.",
                        event.getFacilityName(), event.getFacilityId());
            }
            case "STAFF_JOINED" -> {
                title = "👨‍⚕️ New Staff Member";
                message = String.format(
                        "A new staff member (ID: #%d, Role: %s) has joined Facility '%s'.",
                        event.getStaffId(), event.getStaffRole(), event.getFacilityName());
            }
            case "CAPACITY_CRITICAL" -> {
                title = "⚠️ Facility Capacity Critical";
                message = String.format(
                        "Facility '%s' is reaching critical capacity: %d / %d beds occupied. Immediate attention required.",
                        event.getFacilityName(), event.getCurrentCapacity(), event.getMaxCapacity());
            }
            default -> {
                title = "Facility Update";
                message = "Update for Facility #" + event.getFacilityId();
            }
        }

        Long userId = event.getNotifyUserId() != null ? event.getNotifyUserId() : event.getFacilityId();

        Notification notification = Notification.builder()
                .userId(userId)
                .entityId(event.getFacilityId())
                .title(title)
                .message(message)
                .category(Notification.Category.FACILITY)
                .recipientEmail(event.getRecipientEmail())
                .channel(Notification.Channel.IN_APP)
                .status(Notification.NotificationStatus.UNREAD)
                .build();

        Notification saved = saveAndPublish(notification);

        if (event.getRecipientEmail() != null) {
            emailService.sendEmail(event.getRecipientEmail(), title, message);
        }

        log.info("Facility notification created | event: {}", event.getEventType());
        return saved;
    }

    @Override
    @Transactional
    public Notification handleDocumentEvent(DocumentEventRequest event) {
        String title = "Document Verification Required";
        String message = String.format(
                "New document uploaded by %s (Citizen #%d) awaiting verification.",
                event.getCitizenName(), event.getCitizenId());

        Notification firstSaved = null;

        try {
            List<AuthClient.UserResponse> admins = authClient.getUsersByRole("ADMIN");
            for (AuthClient.UserResponse admin : admins) {
                Notification notif = Notification.builder()
                        .userId(admin.id())
                        .entityId(event.getDocumentId())
                        .title(title)
                        .message(message)
                        .category(Notification.Category.COMPLIANCE)
                        .recipientEmail(admin.email())
                        .channel(Notification.Channel.IN_APP)
                        .status(Notification.NotificationStatus.UNREAD)
                        .build();
                Notification saved = saveAndPublish(notif);
                if (firstSaved == null) firstSaved = saved;

                if (admin.email() != null) {
                    emailService.sendEmail(admin.email(), title, message);
                }
            }
            log.info("Document event: notified {} admin(s) for citizen #{}", admins.size(), event.getCitizenId());
        } catch (Exception e) {
            log.warn("Failed to resolve admins for document event: {}", e.getMessage());
        }

        return firstSaved;
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    private Notification saveAndPublish(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(saved.getUserId()),
                    "/queue/notifications",
                    saved
            );
        } catch (Exception ex) {
            log.warn("Failed to publish notification {} to websocket user {}: {}",
                    saved.getNotificationId(), saved.getUserId(), ex.getMessage());
        }
        return saved;
    }
}
