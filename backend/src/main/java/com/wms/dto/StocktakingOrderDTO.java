package com.wms.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StocktakingOrderDTO {
    private Long id;
    private String orderNo;
    private String stocktakingType;
    private Long warehouseId;
    private String warehouseName;
    private String status;
    private String remark;
    private String operatorName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private List<StocktakingOrderItemDTO> items;

    @Data
    public static class StocktakingOrderItemDTO {
        private Long id;
        private Long materialId;
        private String materialCode;
        private String materialName;
        private Long shelfId;
        private String shelfCode;
        private Long slotId;
        private String slotCode;
        private String batchNo;
        private Integer systemQuantity;
        private Integer actualQuantity;
        private Integer differenceQuantity;
        private String status;
        private String remark;
    }
}
