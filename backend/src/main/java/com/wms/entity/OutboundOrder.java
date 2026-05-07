package com.wms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "outbound_orders")
public class OutboundOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String orderNo;

    @Column(length = 20)
    private String outboundType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Column(length = 100)
    private String receiver;

    @Column(length = 200)
    private String receiverAddress;

    @Column(length = 20)
    private String receiverPhone;

    @Column(length = 20)
    private String status;

    @Column(length = 500)
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    private LocalDateTime outboundTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "outboundOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OutboundOrderItem> items;

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
