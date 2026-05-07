package com.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "stocktaking_order_items")
public class StocktakingOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private StocktakingOrder stocktakingOrder;

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

    private Integer systemQuantity;

    private Integer actualQuantity;

    private Integer differenceQuantity;

    @Column(length = 20)
    private String status;

    @Column(length = 200)
    private String remark;
}
