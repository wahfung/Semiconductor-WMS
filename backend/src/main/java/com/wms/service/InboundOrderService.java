package com.wms.service;

import com.wms.dto.InboundOrderDTO;
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
public class InboundOrderService {

    private static final Logger logger = LoggerFactory.getLogger(InboundOrderService.class);

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ShelfRepository shelfRepository;

    @Autowired
    private ShelfSlotRepository shelfSlotRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private AuthService authService;

    public Page<InboundOrderDTO> findAll(Pageable pageable) {
        return inboundOrderRepository.findAll(pageable).map(this::convertToDTO);
    }

    public InboundOrderDTO findById(Long id) {
        InboundOrder order = inboundOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("入库单不存在"));
        return convertToDTO(order);
    }

    @Transactional
    public InboundOrderDTO create(InboundOrderDTO dto) {
        User currentUser = authService.getCurrentUser();

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("仓库不存在"));

        InboundOrder order = new InboundOrder();
        order.setOrderNo(generateOrderNo());
        order.setInboundType(dto.getInboundType());
        order.setWarehouse(warehouse);
        order.setStatus("PENDING");
        order.setRemark(dto.getRemark());
        order.setOperator(currentUser);

        List<InboundOrderItem> items = new ArrayList<>();
        for (InboundOrderDTO.InboundOrderItemDTO itemDTO : dto.getItems()) {
            Material material = materialRepository.findById(itemDTO.getMaterialId())
                    .orElseThrow(() -> new RuntimeException("物料不存在"));

            InboundOrderItem item = new InboundOrderItem();
            item.setInboundOrder(order);
            item.setMaterial(material);
            item.setBatchNo(itemDTO.getBatchNo());
            item.setQuantity(itemDTO.getQuantity());
            item.setRemark(itemDTO.getRemark());

            if (itemDTO.getShelfId() != null) {
                Shelf shelf = shelfRepository.findById(itemDTO.getShelfId())
                        .orElseThrow(() -> new RuntimeException("货架不存在"));
                item.setShelf(shelf);
            }

            if (itemDTO.getSlotId() != null) {
                ShelfSlot slot = shelfSlotRepository.findById(itemDTO.getSlotId())
                        .orElseThrow(() -> new RuntimeException("货位不存在"));
                item.setSlot(slot);
            }

            items.add(item);
        }

        order.setItems(items);
        order = inboundOrderRepository.save(order);
        logger.info("创建入库单成功: {}", order.getOrderNo());

        return convertToDTO(order);
    }

    @Transactional
    public InboundOrderDTO confirm(Long id) {
        InboundOrder order = inboundOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("入库单不存在"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("入库单状态不允许确认");
        }

        for (InboundOrderItem item : order.getItems()) {
            Inventory inventory = new Inventory();
            inventory.setMaterial(item.getMaterial());
            inventory.setWarehouse(order.getWarehouse());
            inventory.setShelf(item.getShelf());
            inventory.setSlot(item.getSlot());
            inventory.setBatchNo(item.getBatchNo());
            inventory.setQuantity(item.getQuantity());
            inventory.setAvailableQuantity(item.getQuantity());
            inventory.setLockedQuantity(0);
            inventory.setStatus("NORMAL");
            inventoryRepository.save(inventory);

            if (item.getSlot() != null) {
                ShelfSlot slot = item.getSlot();
                slot.setMaterial(item.getMaterial());
                slot.setQuantity(slot.getQuantity() + item.getQuantity());
                slot.setStatus("OCCUPIED");
                shelfSlotRepository.save(slot);

                if (item.getShelf() != null) {
                    Shelf shelf = item.getShelf();
                    shelf.setUsedSlots(shelf.getUsedSlots() + 1);
                    shelfRepository.save(shelf);
                }
            }
        }

        order.setStatus("COMPLETED");
        order.setInboundTime(LocalDateTime.now());
        order = inboundOrderRepository.save(order);
        logger.info("确认入库单成功: {}", order.getOrderNo());

        return convertToDTO(order);
    }

    private String generateOrderNo() {
        String prefix = "IN" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Integer maxNo = inboundOrderRepository.findMaxOrderNoByPrefix(prefix);
        int nextNo = (maxNo != null ? maxNo : 0) + 1;
        return prefix + String.format("%04d", nextNo);
    }

    private InboundOrderDTO convertToDTO(InboundOrder order) {
        InboundOrderDTO dto = new InboundOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setInboundType(order.getInboundType());
        dto.setStatus(order.getStatus());
        dto.setRemark(order.getRemark());
        dto.setInboundTime(order.getInboundTime());
        dto.setCreateTime(order.getCreateTime());

        if (order.getWarehouse() != null) {
            dto.setWarehouseId(order.getWarehouse().getId());
            dto.setWarehouseName(order.getWarehouse().getWarehouseName());
        }

        if (order.getOperator() != null) {
            dto.setOperatorName(order.getOperator().getRealName());
        }

        if (order.getItems() != null) {
            List<InboundOrderDTO.InboundOrderItemDTO> itemDTOs = new ArrayList<>();
            for (InboundOrderItem item : order.getItems()) {
                InboundOrderDTO.InboundOrderItemDTO itemDTO = new InboundOrderDTO.InboundOrderItemDTO();
                itemDTO.setId(item.getId());
                if (item.getMaterial() != null) {
                    itemDTO.setMaterialId(item.getMaterial().getId());
                    itemDTO.setMaterialCode(item.getMaterial().getMaterialCode());
                    itemDTO.setMaterialName(item.getMaterial().getMaterialName());
                }
                itemDTO.setBatchNo(item.getBatchNo());
                itemDTO.setQuantity(item.getQuantity());
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
