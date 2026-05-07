package com.wms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderDTO {
    private Long id;
    private String orderNo;

    @NotBlank(message = "供应商不能为空")
    private String supplier;

    private String contact;
    private String phone;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private String creatorName;
    private String approverName;
    private LocalDateTime approveTime;
    private LocalDateTime expectedArrivalTime;
    private LocalDateTime createTime;

    @NotEmpty(message = "采购明细不能为空")
    private List<PurchaseOrderItemDTO> items;

    @Data
    public static class PurchaseOrderItemDTO {
        private Long id;
        private Long materialId;
        private String materialCode;
        private String materialName;
        private Integer quantity;
        private Integer receivedQuantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private String remark;
    }
}
