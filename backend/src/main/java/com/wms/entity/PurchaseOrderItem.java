package com.wms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    private Integer quantity;

    private Integer receivedQuantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 200)
    private String remark;
}
