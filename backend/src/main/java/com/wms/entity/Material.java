package com.wms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "materials")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String materialCode;

    @Column(nullable = false, length = 100)
    private String materialName;

    @Column(length = 20)
    private String materialType;

    @Column(length = 50)
    private String specification;

    @Column(length = 20)
    private String unit;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private Integer minStock;

    private Integer maxStock;

    @Column(length = 100)
    private String supplier;

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
