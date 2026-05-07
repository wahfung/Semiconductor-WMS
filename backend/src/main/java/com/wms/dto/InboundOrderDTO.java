package com.wms.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InboundOrderDTO {
    private Long id;
    private String orderNo;
    private String inboundType;
    private Long purchaseOrderId;
    private String purchaseOrderNo;
    private Long warehouseId;
    private String warehouseName;
    private String status;
    private String remark;
    private String operatorName;
    private LocalDateTime inboundTime;
    private LocalDateTime createTime;

    @NotEmpty(message = "入库明细不能为空")
    private List<InboundOrderItemDTO> items;

    @Data
    public static class InboundOrderItemDTO {
        private Long id;
        private Long materialId;
        private String materialCode;
        private String materialName;
        private Long shelfId;
        private String shelfCode;
        private Long slotId;
        private String slotCode;
        private String batchNo;
        private Integer quantity;
        private String remark;
    }
}
