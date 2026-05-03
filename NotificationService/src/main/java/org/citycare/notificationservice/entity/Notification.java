package org.citycare.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(nullable = false)
    private Long userId;

    private Long entityId;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    private LocalDateTime readAt;

    private String recipientEmail;

    private String title;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    public enum Category {
        EMERGENCY, PATIENT, FACILITY, COMPLIANCE, AUTH, TREATMENT
    }

    public enum NotificationStatus {
        UNREAD, READ, SENT, FAILED
    }

    public enum Channel {
        IN_APP, EMAIL, SMS
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        if (this.status == null) this.status = NotificationStatus.UNREAD;
        if (this.channel == null) this.channel = Channel.IN_APP;
    }
}
