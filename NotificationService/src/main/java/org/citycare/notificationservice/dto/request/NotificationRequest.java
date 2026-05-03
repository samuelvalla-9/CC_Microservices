package org.citycare.notificationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.citycare.notificationservice.entity.Notification;

@Data
public class NotificationRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    private Long entityId;

    @NotBlank(message = "message is required")
    private String message;

    private String title;

    @NotNull(message = "category is required")
    private Notification.Category category;

    private String recipientEmail;

    private Notification.Channel channel;
}
