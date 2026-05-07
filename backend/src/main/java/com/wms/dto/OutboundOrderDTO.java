package com.wms.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OutboundOrderDTO {
    private Long id;
    private String orderNo;
    private String outboundType;
    private Long warehouseId;
    private String warehouseName;
    private String receiver;
    private String receiverAddress;
    private String receiverPhone;
    private String status;
    private String remark;
    private String operatorName;
    private LocalDateTime outboundTime;
    private LocalDateTime createTime;

    @NotEmpty(message = "出库明细不能为空")
    private List<OutboundOrderItemDTO> items;

    @Data
    public static class OutboundOrderItemDTO {
        private Long id;
        private Long materialId;
        private String materialCode;
        private String materialName;
        private Long inventoryId;
        private Long shelfId;
        private String shelfCode;
        private Long slotId;
        private String slotCode;
        private String batchNo;
        private Integer quantity;
        private String remark;
    }
}
