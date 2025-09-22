package kz.infologia.bot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "telegram_id")
    private Long telegramId;

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "is_bot")
    private Boolean isBot;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "password_hash", length = 60)
    private String passwordHash;

    @Column(name = "is_authorized")
    private Boolean authorized;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (lastActivity == null) {
            lastActivity = now;
        }
        if (role == null) {
            role = Role.STUDENT;
        }
        if (authorized == null) {
            authorized = Boolean.FALSE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastActivity = LocalDateTime.now();
        if (authorized == null) {
            authorized = Boolean.FALSE;
        }
    }
}
// FIXME
