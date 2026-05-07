package com.wms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "warehouses")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String warehouseCode;

    @Column(nullable = false, length = 50)
    private String warehouseName;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String manager;

    @Column(length = 20)
    private String phone;

    private Integer totalCapacity;

    private Integer usedCapacity;

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
