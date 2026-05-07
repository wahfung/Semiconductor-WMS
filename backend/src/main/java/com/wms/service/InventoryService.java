package com.wms.service;

import com.wms.dto.InventoryDTO;
import com.wms.entity.Inventory;
import com.wms.repository.InventoryRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public Page<InventoryDTO> findAll(Pageable pageable) {
        return inventoryRepository.findAllWithDetails(pageable).map(this::convertToDTO);
    }

    public List<InventoryDTO> findByWarehouseId(Long warehouseId) {
        return inventoryRepository.findByWarehouseIdWithDetails(warehouseId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<InventoryDTO> findByMaterialId(Long materialId) {
        return inventoryRepository.findByMaterialId(materialId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public InventoryDTO findById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("库存记录不存在"));
        return convertToDTO(inventory);
    }

    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        BeanUtils.copyProperties(inventory, dto);

        if (inventory.getMaterial() != null) {
            dto.setMaterialId(inventory.getMaterial().getId());
            dto.setMaterialCode(inventory.getMaterial().getMaterialCode());
            dto.setMaterialName(inventory.getMaterial().getMaterialName());
            dto.setMaterialType(inventory.getMaterial().getMaterialType());
            dto.setSpecification(inventory.getMaterial().getSpecification());
            dto.setUnit(inventory.getMaterial().getUnit());
        }

        if (inventory.getWarehouse() != null) {
            dto.setWarehouseId(inventory.getWarehouse().getId());
            dto.setWarehouseName(inventory.getWarehouse().getWarehouseName());
        }

        if (inventory.getShelf() != null) {
            dto.setShelfId(inventory.getShelf().getId());
            dto.setShelfCode(inventory.getShelf().getShelfCode());
        }

        if (inventory.getSlot() != null) {
            dto.setSlotId(inventory.getSlot().getId());
            dto.setSlotCode(inventory.getSlot().getSlotCode());
        }

        return dto;
    }
}
