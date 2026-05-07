package com.wms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shelves")
public class Shelf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String shelfCode;

    @Column(nullable = false, length = 50)
    private String shelfName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    private Integer rowNum;

    private Integer columnNum;

    private Integer layerNum;

    private Integer positionX;

    private Integer positionY;

    private Integer positionZ;

    private Integer width;

    private Integer depth;

    private Integer height;

    @Column(length = 20)
    private String shelfType;

    private Integer totalSlots;

    private Integer usedSlots;

    @Column(length = 500)
    private String description;

    private Boolean enabled = true;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
