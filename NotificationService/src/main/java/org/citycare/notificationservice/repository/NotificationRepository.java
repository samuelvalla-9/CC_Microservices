package org.citycare.notificationservice.repository;

import org.citycare.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedDateDesc(Long userId);

    List<Notification> findByUserIdAndStatusOrderByCreatedDateDesc(Long userId, Notification.NotificationStatus status);

    List<Notification> findByCategoryOrderByCreatedDateDesc(Notification.Category category);

    List<Notification> findByEntityIdAndCategory(Long entityId, Notification.Category category);

    long countByUserIdAndStatus(Long userId, Notification.NotificationStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.status = 'UNREAD'")
    int markAllAsReadByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP WHERE n.notificationId = :id")
    int markAsRead(Long id);
}
