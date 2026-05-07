package com.wms.dto;

import lombok.Data;

@Data
public class ShelfSlotDTO {
    private Long id;
    private String slotCode;
    private Long shelfId;
    private String shelfCode;
    private Integer rowIndex;
    private Integer columnIndex;
    private Integer layerIndex;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String materialType;
    private Integer quantity;
    private Integer maxCapacity;
    private String status;
}
