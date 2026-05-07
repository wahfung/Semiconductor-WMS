package com.wms.dto;

import lombok.Data;
import java.util.List;

@Data
public class ShelfDTO {
    private Long id;
    private String shelfCode;
    private String shelfName;
    private Long warehouseId;
    private String warehouseName;
    private Integer rowNum;
    private Integer columnNum;
    private Integer layerNum;
    private Integer positionX;
    private Integer positionY;
    private Integer positionZ;
    private Integer width;
    private Integer depth;
    private Integer height;
    private String shelfType;
    private Integer totalSlots;
    private Integer usedSlots;
    private String description;
    private Boolean enabled;
    private List<ShelfSlotDTO> slots;
}
