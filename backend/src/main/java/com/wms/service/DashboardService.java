package com.wms.service;

import com.wms.dto.DashboardDTO;
import com.wms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private StocktakingOrderRepository stocktakingOrderRepository;

    public DashboardDTO getDashboardData() {
        DashboardDTO dashboard = new DashboardDTO();

        dashboard.setTotalMaterials(materialRepository.count());
        dashboard.setTotalInventory(inventoryRepository.count());
        dashboard.setTodayInbound(inboundOrderRepository.count());
        dashboard.setTodayOutbound(outboundOrderRepository.count());
        dashboard.setPendingPurchase(purchaseOrderRepository.count());
        dashboard.setPendingStocktaking(stocktakingOrderRepository.count());

        List<DashboardDTO.MaterialTypeStats> materialTypeStats = new ArrayList<>();
        materialTypeStats.add(new DashboardDTO.MaterialTypeStats("原材料", 45L, 12500));
        materialTypeStats.add(new DashboardDTO.MaterialTypeStats("半制品", 32L, 8300));
        materialTypeStats.add(new DashboardDTO.MaterialTypeStats("产成品", 28L, 5600));
        dashboard.setMaterialTypeStats(materialTypeStats);

        List<DashboardDTO.WarehouseStats> warehouseStats = new ArrayList<>();
        warehouseRepository.findAllEnabled().forEach(w -> {
            warehouseStats.add(new DashboardDTO.WarehouseStats(
                    w.getWarehouseName(),
                    w.getTotalCapacity() != null ? w.getTotalCapacity() : 1000,
                    w.getUsedCapacity() != null ? w.getUsedCapacity() : 0
            ));
        });
        dashboard.setWarehouseStats(warehouseStats);

        List<DashboardDTO.RecentOperation> recentOperations = new ArrayList<>();
        recentOperations.add(new DashboardDTO.RecentOperation("入库", "IN202401290001", "硅晶片入库50片", "张伟", "10:30"));
        recentOperations.add(new DashboardDTO.RecentOperation("出库", "OUT202401290001", "封装基板出库100片", "李明", "09:45"));
        recentOperations.add(new DashboardDTO.RecentOperation("采购", "PO202401290001", "采购光刻胶500ml", "王芳", "09:00"));
        dashboard.setRecentOperations(recentOperations);

        return dashboard;
    }
}
