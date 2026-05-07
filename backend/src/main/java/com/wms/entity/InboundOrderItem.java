package com.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "inbound_order_items")
public class InboundOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private InboundOrder inboundOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

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
