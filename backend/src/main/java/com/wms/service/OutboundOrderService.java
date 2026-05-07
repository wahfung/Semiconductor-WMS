package com.wms.service;

import com.wms.dto.OutboundOrderDTO;
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
public class OutboundOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OutboundOrderService.class);

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ShelfSlotRepository shelfSlotRepository;

    @Autowired
    private AuthService authService;

    public Page<OutboundOrderDTO> findAll(Pageable pageable) {
        return outboundOrderRepository.findAll(pageable).map(this::convertToDTO);
    }

    public OutboundOrderDTO findById(Long id) {
        OutboundOrder order = outboundOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("出库单不存在"));
        return convertToDTO(order);
    }

    @Transactional
    public OutboundOrderDTO create(OutboundOrderDTO dto) {
        User currentUser = authService.getCurrentUser();

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("仓库不存在"));

        OutboundOrder order = new OutboundOrder();
        order.setOrderNo(generateOrderNo());
        order.setOutboundType(dto.getOutboundType());
        order.setWarehouse(warehouse);
        order.setReceiver(dto.getReceiver());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setStatus("PENDING");
        order.setRemark(dto.getRemark());
        order.setOperator(currentUser);

        List<OutboundOrderItem> items = new ArrayList<>();
        for (OutboundOrderDTO.OutboundOrderItemDTO itemDTO : dto.getItems()) {
            Material material = materialRepository.findById(itemDTO.getMaterialId())
                    .orElseThrow(() -> new RuntimeException("物料不存在"));

            OutboundOrderItem item = new OutboundOrderItem();
            item.setOutboundOrder(order);
            item.setMaterial(material);
            item.setBatchNo(itemDTO.getBatchNo());
            item.setQuantity(itemDTO.getQuantity());
            item.setRemark(itemDTO.getRemark());

            if (itemDTO.getInventoryId() != null) {
                Inventory inventory = inventoryRepository.findById(itemDTO.getInventoryId())
                        .orElseThrow(() -> new RuntimeException("库存不存在"));
                item.setInventory(inventory);
                item.setShelf(inventory.getShelf());
                item.setSlot(inventory.getSlot());
            }

            items.add(item);
        }

        order.setItems(items);
        order = outboundOrderRepository.save(order);
        logger.info("创建出库单成功: {}", order.getOrderNo());

        return convertToDTO(order);
    }

    @Transactional
    public OutboundOrderDTO confirm(Long id) {
        OutboundOrder order = outboundOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("出库单不存在"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("出库单状态不允许确认");
        }

        for (OutboundOrderItem item : order.getItems()) {
            if (item.getInventory() != null) {
                Inventory inventory = item.getInventory();
                if (inventory.getAvailableQuantity() < item.getQuantity()) {
                    throw new RuntimeException("库存不足: " + item.getMaterial().getMaterialName());
                }
                inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() - item.getQuantity());
                inventoryRepository.save(inventory);

                if (item.getSlot() != null) {
                    ShelfSlot slot = item.getSlot();
                    slot.setQuantity(slot.getQuantity() - item.getQuantity());
                    if (slot.getQuantity() <= 0) {
                        slot.setStatus("EMPTY");
                        slot.setMaterial(null);
                        slot.setQuantity(0);
                    }
                    shelfSlotRepository.save(slot);
                }
            }
        }

        order.setStatus("COMPLETED");
        order.setOutboundTime(LocalDateTime.now());
        order = outboundOrderRepository.save(order);
        logger.info("确认出库单成功: {}", order.getOrderNo());

        return convertToDTO(order);
    }

    private String generateOrderNo() {
        String prefix = "OUT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Integer maxNo = outboundOrderRepository.findMaxOrderNoByPrefix(prefix);
        int nextNo = (maxNo != null ? maxNo : 0) + 1;
        return prefix + String.format("%04d", nextNo);
    }

    private OutboundOrderDTO convertToDTO(OutboundOrder order) {
        OutboundOrderDTO dto = new OutboundOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setOutboundType(order.getOutboundType());
        dto.setReceiver(order.getReceiver());
        dto.setReceiverAddress(order.getReceiverAddress());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setStatus(order.getStatus());
        dto.setRemark(order.getRemark());
        dto.setOutboundTime(order.getOutboundTime());
        dto.setCreateTime(order.getCreateTime());

        if (order.getWarehouse() != null) {
            dto.setWarehouseId(order.getWarehouse().getId());
            dto.setWarehouseName(order.getWarehouse().getWarehouseName());
        }

        if (order.getOperator() != null) {
            dto.setOperatorName(order.getOperator().getRealName());
        }

        if (order.getItems() != null) {
            List<OutboundOrderDTO.OutboundOrderItemDTO> itemDTOs = new ArrayList<>();
            for (OutboundOrderItem item : order.getItems()) {
                OutboundOrderDTO.OutboundOrderItemDTO itemDTO = new OutboundOrderDTO.OutboundOrderItemDTO();
                itemDTO.setId(item.getId());
                if (item.getMaterial() != null) {
                    itemDTO.setMaterialId(item.getMaterial().getId());
                    itemDTO.setMaterialCode(item.getMaterial().getMaterialCode());
                    itemDTO.setMaterialName(item.getMaterial().getMaterialName());
                }
                itemDTO.setBatchNo(item.getBatchNo());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setRemark(item.getRemark());

                if (item.getInventory() != null) {
                    itemDTO.setInventoryId(item.getInventory().getId());
                }
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
