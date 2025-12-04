package org.example.microservicenotification.repository;

import org.example.microservicenotification.entity.Notification;
import org.example.microservicenotification.entity.NotificationStatus;
import org.example.microservicenotification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findByUserId(Long userId);

  List<Notification> findByReservationId(Long reservationId);

  List<Notification> findByStatus(NotificationStatus status);

  List<Notification> findByType(NotificationType type);

  List<Notification> findByChannel(String channel);

  List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer retryCount);

  @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.status = :status ORDER BY n.createdAt DESC")
  List<Notification> findByUserIdAndStatus(@Param("userId") Long userId,
                                           @Param("status") NotificationStatus status);

  @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status")
  Long countByStatus(@Param("status") NotificationStatus status);

  @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = :status")
  Long countByUserIdAndStatus(@Param("userId") Long userId,
                              @Param("status") NotificationStatus status);

  @Query("SELECT n FROM Notification n WHERE n.status IN ('PENDING', 'FAILED') AND n.retryCount < :maxRetries AND n.createdAt < :cutoffTime")
  List<Notification> findNotificationsForRetry(@Param("maxRetries") Integer maxRetries,
                                               @Param("cutoffTime") LocalDateTime cutoffTime);

  Optional<Notification> findFirstByUserIdAndTypeAndStatusOrderByCreatedAtDesc(Long userId,
                                                                               NotificationType type,
                                                                               NotificationStatus status);
}
