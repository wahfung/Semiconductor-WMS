package com.wms.service;

import com.wms.dto.StocktakingOrderDTO;
import com.wms.entity.*;
import com.wms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StocktakingOrderService {

    private static final Logger logger = LoggerFactory.getLogger(StocktakingOrderService.class);

    @Autowired
    private StocktakingOrderRepository stocktakingOrderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ShelfSlotRepository shelfSlotRepository;

    @Autowired
    private AuthService authService;

    public Page<StocktakingOrderDTO> findAll(Pageable pageable) {
        return stocktakingOrderRepository.findAll(pageable).map(this::convertToDTO);
    }

    public StocktakingOrderDTO findById(Long id) {
        StocktakingOrder order = stocktakingOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("盘点单不存在"));
        return convertToDTO(order);
    }

    @Transactional
    public StocktakingOrderDTO create(StocktakingOrderDTO dto) {
        User currentUser = authService.getCurrentUser();

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("仓库不存在"));

        StocktakingOrder order = new StocktakingOrder();
        order.setOrderNo(generateOrderNo());
        order.setStocktakingType(dto.getStocktakingType());
        order.setWarehouse(warehouse);
        order.setStatus("IN_PROGRESS");
        order.setRemark(dto.getRemark());
        order.setOperator(currentUser);
        order.setStartTime(LocalDateTime.now());

        List<Inventory> inventories = inventoryRepository.findByWarehouseIdWithDetails(warehouse.getId());
        List<StocktakingOrderItem> items = new ArrayList<>();

        for (Inventory inventory : inventories) {
            StocktakingOrderItem item = new StocktakingOrderItem();
            item.setStocktakingOrder(order);
            item.setMaterial(inventory.getMaterial());
            item.setShelf(inventory.getShelf());
            item.setSlot(inventory.getSlot());
            item.setBatchNo(inventory.getBatchNo());
            item.setSystemQuantity(inventory.getQuantity());
            item.setActualQuantity(null);
            item.setDifferenceQuantity(null);
            item.setStatus("PENDING");
            items.add(item);
        }

        order.setItems(items);
        order = stocktakingOrderRepository.save(order);
        logger.info("创建盘点单成功: {}", order.getOrderNo());

        return convertToDTO(order);
    }

    @Transactional
    public StocktakingOrderDTO updateItem(Long orderId, Long itemId, Integer actualQuantity) {
        StocktakingOrder order = stocktakingOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("盘点单不存在"));

        if (!"IN_PROGRESS".equals(order.getStatus())) {
            throw new RuntimeException("盘点单状态不允许修改");
        }

        for (StocktakingOrderItem item : order.getItems()) {
            if (item.getId().equals(itemId)) {
                item.setActualQuantity(actualQuantity);
                item.setDifferenceQuantity(actualQuantity - item.getSystemQuantity());
                item.setStatus("COUNTED");
                break;
            }
        }

        order = stocktakingOrderRepository.save(order);
        return convertToDTO(order);
    }

    @Transactional
    public StocktakingOrderDTO complete(Long id) {
        StocktakingOrder order = stocktakingOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("盘点单不存在"));

        if (!"IN_PROGRESS".equals(order.getStatus())) {
            throw new RuntimeException("盘点单状态不允许完成");
        }

        for (StocktakingOrderItem item : order.getItems()) {
            if (item.getActualQuantity() != null && item.getDifferenceQuantity() != null && item.getDifferenceQuantity() != 0) {
                if (item.getSlot() != null) {
                    List<Inventory> inventories = inventoryRepository.findBySlotId(item.getSlot().getId());
                    for (Inventory inventory : inventories) {
                        if (inventory.getMaterial().getId().equals(item.getMaterial().getId())) {
                            inventory.setQuantity(item.getActualQuantity());
                            inventory.setAvailableQuantity(item.getActualQuantity() - inventory.getLockedQuantity());
                            inventoryRepository.save(inventory);
                        }
                    }

                    ShelfSlot slot = item.getSlot();
                    slot.setQuantity(item.getActualQuantity());
                    if (item.getActualQuantity() <= 0) {
                        slot.setStatus("EMPTY");
                        slot.setMaterial(null);
                    }
                    shelfSlotRepository.save(slot);
                }
            }
        }

        order.setStatus("COMPLETED");
        order.setEndTime(LocalDateTime.now());
        order = stocktakingOrderRepository.save(order);
        logger.info("完成盘点单: {}", order.getOrderNo());

        return convertToDTO(order);
    }

    private String generateOrderNo() {
        String prefix = "ST" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Integer maxNo = stocktakingOrderRepository.findMaxOrderNoByPrefix(prefix);
        int nextNo = (maxNo != null ? maxNo : 0) + 1;
        return prefix + String.format("%04d", nextNo);
    }

    private StocktakingOrderDTO convertToDTO(StocktakingOrder order) {
        StocktakingOrderDTO dto = new StocktakingOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setStocktakingType(order.getStocktakingType());
        dto.setStatus(order.getStatus());
        dto.setRemark(order.getRemark());
        dto.setStartTime(order.getStartTime());
        dto.setEndTime(order.getEndTime());
        dto.setCreateTime(order.getCreateTime());

        if (order.getWarehouse() != null) {
            dto.setWarehouseId(order.getWarehouse().getId());
            dto.setWarehouseName(order.getWarehouse().getWarehouseName());
        }

        if (order.getOperator() != null) {
            dto.setOperatorName(order.getOperator().getRealName());
        }

        if (order.getItems() != null) {
            List<StocktakingOrderDTO.StocktakingOrderItemDTO> itemDTOs = new ArrayList<>();
            for (StocktakingOrderItem item : order.getItems()) {
                StocktakingOrderDTO.StocktakingOrderItemDTO itemDTO = new StocktakingOrderDTO.StocktakingOrderItemDTO();
                itemDTO.setId(item.getId());
                if (item.getMaterial() != null) {
                    itemDTO.setMaterialId(item.getMaterial().getId());
                    itemDTO.setMaterialCode(item.getMaterial().getMaterialCode());
                    itemDTO.setMaterialName(item.getMaterial().getMaterialName());
                }
                itemDTO.setBatchNo(item.getBatchNo());
                itemDTO.setSystemQuantity(item.getSystemQuantity());
                itemDTO.setActualQuantity(item.getActualQuantity());
                itemDTO.setDifferenceQuantity(item.getDifferenceQuantity());
                itemDTO.setStatus(item.getStatus());
                itemDTO.setRemark(item.getRemark());

                if (item.getShelf() != null) {
                    itemDTO.setShelfId(item.getShelf().getId());
                    itemDTO.setShelfCode(item.getShelf().getShelfCode());
                }
                if (item.getSlot() != null) {
                    itemDTO.setSlotId(item.getSlot().getId());
                    itemDTO.setSlotCode(item.getSlot().getSlotCode());
                }

                itemDTOs.add(itemDTO);
            }
            dto.setItems(itemDTOs);
        }

        return dto;
    }
}
