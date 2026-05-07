package com.wms.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InventoryDTO {
    private Long id;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String materialType;
    private String specification;
    private String unit;
    private Long warehouseId;
    private String warehouseName;
    private Long shelfId;
    private String shelfCode;
    private Long slotId;
    private String slotCode;
    private String batchNo;
    private Integer quantity;
    private Integer lockedQuantity;
    private Integer availableQuantity;
    private String status;
    private LocalDateTime productionDate;
    private LocalDateTime expirationDate;
}
