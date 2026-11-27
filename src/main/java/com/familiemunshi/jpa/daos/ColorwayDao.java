package com.familiemunshi.jpa.daos;

import com.familiemunshi.common.utils.Constants;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "colorways",
        indexes = {
                @Index(name = "idx_colorways_is_active", columnList = "is_active"),
                @Index(name = "idx_colorways_in_theme", columnList = "in_theme"),
                @Index(name = "idx_colorways_color_standard", columnList = "color_standard"),
                @Index(name = "idx_colorways_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_colorways_code", columnNames = "code")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorwayDao {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String code;

    @Column(name = "marketing_name", nullable = false, length = 150)
    private String marketingName;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "color_specification")
    private String colorSpecification;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "color_standard", nullable = false, length = 20)
    private String colorStandard = Constants.DEFAULT_COLOR_STANDARD;

    @Column(name = "color_reference", length = 50)
    private String colorReference;

    @Column(name = "hex_code", length = 7)
    private String hexCode;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "in_theme", nullable = false)
    private boolean inTheme = false;

    @Builder.Default
    @Column(name = "sustainability_label", nullable = false)
    private boolean sustainabilityLabel = false;

    @Builder.Default
    @Column(name = "plan_sms", nullable = false)
    private boolean planSms = false;

    @Builder.Default
    @Column(name = "plan_3d_sms", nullable = false)
    private boolean plan3dSms = false;

    @Builder.Default
    @Column(name = "actual_sms", nullable = false)
    private boolean actualSms = false;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "clearance_date")
    private LocalDate clearanceDate;

    @Builder.Default
    @BatchSize(size = 50)
    @OrderBy("position ASC")
    @OneToMany(mappedBy = "colorway", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColorwayImageDao> images = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public void addImage(ColorwayImageDao image) {
        image.setColorway(this);
        images.add(image);
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
