package com.wms.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardDTO {
    private Long totalMaterials;
    private Long totalInventory;
    private Long todayInbound;
    private Long todayOutbound;
    private Long pendingPurchase;
    private Long pendingStocktaking;
    private List<MaterialTypeStats> materialTypeStats;
    private List<WarehouseStats> warehouseStats;
    private List<RecentOperation> recentOperations;

    @Data
    public static class MaterialTypeStats {
        private String type;
        private Long count;
        private Integer quantity;

        public MaterialTypeStats(String type, Long count, Integer quantity) {
            this.type = type;
            this.count = count;
            this.quantity = quantity;
        }
    }

    @Data
    public static class WarehouseStats {
        private String warehouseName;
        private Integer totalCapacity;
        private Integer usedCapacity;
        private Double usageRate;

        public WarehouseStats(String warehouseName, Integer totalCapacity, Integer usedCapacity) {
            this.warehouseName = warehouseName;
            this.totalCapacity = totalCapacity;
            this.usedCapacity = usedCapacity;
            this.usageRate = totalCapacity > 0 ? (double) usedCapacity / totalCapacity * 100 : 0;
        }
    }

    @Data
    public static class RecentOperation {
        private String type;
        private String orderNo;
        private String description;
        private String operatorName;
        private String time;

        public RecentOperation(String type, String orderNo, String description, String operatorName, String time) {
            this.type = type;
            this.orderNo = orderNo;
            this.description = description;
            this.operatorName = operatorName;
            this.time = time;
        }
    }
}
