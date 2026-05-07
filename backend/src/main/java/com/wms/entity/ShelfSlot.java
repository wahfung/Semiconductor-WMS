package com.wms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shelf_slots")
public class ShelfSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String slotCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelf_id")
    private Shelf shelf;

    private Integer rowIndex;

    private Integer columnIndex;

    private Integer layerIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    private Integer quantity;

    private Integer maxCapacity;

    @Column(length = 20)
    private String status;

    private LocalDateTime lastUpdateTime;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        lastUpdateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdateTime = LocalDateTime.now();
    }
}
