package com.retailpos.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_notifications")
public class SystemNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 30)
    private String type = "INFO";

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public SystemNotification() {}

    public SystemNotification(Long id, String title, String message, String type, Boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type != null ? type : "INFO";
        this.isRead = isRead != null ? isRead : false;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static SystemNotificationBuilder builder() { return new SystemNotificationBuilder(); }

    public static class SystemNotificationBuilder {
        private Long id;
        private String title;
        private String message;
        private String type = "INFO";
        private Boolean isRead = false;
        private LocalDateTime createdAt;

        public SystemNotificationBuilder id(Long id) { this.id = id; return this; }
        public SystemNotificationBuilder title(String title) { this.title = title; return this; }
        public SystemNotificationBuilder message(String message) { this.message = message; return this; }
        public SystemNotificationBuilder type(String type) { this.type = type; return this; }
        public SystemNotificationBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }
        public SystemNotificationBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public SystemNotification build() {
            return new SystemNotification(id, title, message, type, isRead, createdAt);
        }
    }
}
