package kz.infologia.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile implements Persistable<Long> {

    @Id
    @Column(name = "telegram_id")
    private Long telegramId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "telegram_id")
    private User user;

    @Column(name = "course")
    private String course;

    @Column(name = "cohort")
    private String cohort;

    @Column(name = "status")
    private String status;

    @Column(name = "curator")
    private String curator;

    @Column(name = "notes", length = 1024)
    private String notes;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    @Default
    private boolean isNew = true;

    @PrePersist
    @PreUpdate
    protected void onWrite() {
        updatedAt = LocalDateTime.now();
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        this.isNew = false;
    }

    @Override
    public Long getId() {
        return telegramId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
