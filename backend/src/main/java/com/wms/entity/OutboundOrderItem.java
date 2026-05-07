package com.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "outbound_order_items")
public class OutboundOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OutboundOrder outboundOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelf_id")
    private Shelf shelf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private ShelfSlot slot;

    @Column(length = 50)
    private String batchNo;

    private Integer quantity;

    @Column(length = 200)
    private String remark;
}
