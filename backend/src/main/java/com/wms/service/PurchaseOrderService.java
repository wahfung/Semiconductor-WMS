package com.wms.service;

import com.wms.dto.PurchaseOrderDTO;
import com.wms.entity.Material;
import com.wms.entity.PurchaseOrder;
import com.wms.entity.PurchaseOrderItem;
import com.wms.entity.User;
import com.wms.repository.MaterialRepository;
import com.wms.repository.PurchaseOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PurchaseOrderService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderService.class);

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private AuthService authService;

    public Page<PurchaseOrderDTO> findAll(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable).map(this::convertToDTO);
    }

    public PurchaseOrderDTO findById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("采购单不存在"));
        return convertToDTO(order);
    }

    @Transactional
    public PurchaseOrderDTO create(PurchaseOrderDTO dto) {
        User currentUser = authService.getCurrentUser();

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo());
        order.setSupplier(dto.getSupplier());
        order.setContact(dto.getContact());
        order.setPhone(dto.getPhone());
        order.setStatus("PENDING");
        order.setRemark(dto.getRemark());
        order.setCreator(currentUser);
        order.setExpectedArrivalTime(dto.getExpectedArrivalTime());

        List<PurchaseOrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PurchaseOrderDTO.PurchaseOrderItemDTO itemDTO : dto.getItems()) {
            Material material = materialRepository.findById(itemDTO.getMaterialId())
                    .orElseThrow(() -> new RuntimeException("物料不存在: " + itemDTO.getMaterialId()));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(order);
            item.setMaterial(material);
            item.setQuantity(itemDTO.getQuantity());
            item.setReceivedQuantity(0);
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setAmount(itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            item.setRemark(itemDTO.getRemark());

            items.add(item);
            totalAmount = totalAmount.add(item.getAmount());
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);

        order = purchaseOrderRepository.save(order);
        logger.info("创建采购单成功: {}", order.getOrderNo());

        return convertToDTO(order);
    }

    @Transactional
    public PurchaseOrderDTO approve(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("采购单不存在"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("采购单状态不允许审批");
        }

        User currentUser = authService.getCurrentUser();
        order.setStatus("APPROVED");
        order.setApprover(currentUser);
        order.setApproveTime(java.time.LocalDateTime.now());

        order = purchaseOrderRepository.save(order);
        logger.info("审批采购单成功: {}", order.getOrderNo());

        return convertToDTO(order);
    }

    @Transactional
    public void cancel(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("采购单不存在"));

        if ("COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("已完成的采购单不能取消");
        }

        order.setStatus("CANCELLED");
        purchaseOrderRepository.save(order);
        logger.info("取消采购单成功: {}", order.getOrderNo());
    }

    private String generateOrderNo() {
        String prefix = "PO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Integer maxNo = purchaseOrderRepository.findMaxOrderNoByPrefix(prefix);
        int nextNo = (maxNo != null ? maxNo : 0) + 1;
        return prefix + String.format("%04d", nextNo);
    }

    private PurchaseOrderDTO convertToDTO(PurchaseOrder order) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setSupplier(order.getSupplier());
        dto.setContact(order.getContact());
        dto.setPhone(order.getPhone());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setRemark(order.getRemark());
        dto.setExpectedArrivalTime(order.getExpectedArrivalTime());
        dto.setApproveTime(order.getApproveTime());
        dto.setCreateTime(order.getCreateTime());

        if (order.getCreator() != null) {
            dto.setCreatorName(order.getCreator().getRealName());
        }
        if (order.getApprover() != null) {
            dto.setApproverName(order.getApprover().getRealName());
        }

        if (order.getItems() != null) {
            List<PurchaseOrderDTO.PurchaseOrderItemDTO> itemDTOs = new ArrayList<>();
            for (PurchaseOrderItem item : order.getItems()) {
                PurchaseOrderDTO.PurchaseOrderItemDTO itemDTO = new PurchaseOrderDTO.PurchaseOrderItemDTO();
                itemDTO.setId(item.getId());
                if (item.getMaterial() != null) {
                    itemDTO.setMaterialId(item.getMaterial().getId());
                    itemDTO.setMaterialCode(item.getMaterial().getMaterialCode());
                    itemDTO.setMaterialName(item.getMaterial().getMaterialName());
                }
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setReceivedQuantity(item.getReceivedQuantity());
                itemDTO.setUnitPrice(item.getUnitPrice());
                itemDTO.setAmount(item.getAmount());
                itemDTO.setRemark(item.getRemark());
                itemDTOs.add(itemDTO);
            }
            dto.setItems(itemDTOs);
        }

        return dto;
    }
}
