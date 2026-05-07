package com.wms.service;

import com.wms.dto.ShelfDTO;
import com.wms.dto.ShelfSlotDTO;
import com.wms.entity.Shelf;
import com.wms.entity.ShelfSlot;
import com.wms.entity.Warehouse;
import com.wms.repository.ShelfRepository;
import com.wms.repository.ShelfSlotRepository;
import com.wms.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ShelfService {

    private static final Logger logger = LoggerFactory.getLogger(ShelfService.class);

    @Autowired
    private ShelfRepository shelfRepository;

    @Autowired
    private ShelfSlotRepository shelfSlotRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    public Page<ShelfDTO> findAll(Pageable pageable) {
        return shelfRepository.findAllEnabled(pageable).map(this::convertToDTO);
    }

    public ShelfDTO findById(Long id) {
        Shelf shelf = shelfRepository.findByIdWithWarehouse(id)
                .orElseThrow(() -> new RuntimeException("货架不存在"));
        return convertToDTOWithSlots(shelf);
    }

    public List<ShelfDTO> findByWarehouseId(Long warehouseId) {
        List<Shelf> shelves = shelfRepository.findByWarehouseId(warehouseId);
        return shelves.stream()
                .map(this::convertToDTOWithSlots)
                .collect(Collectors.toList());
    }

    public List<ShelfDTO> findAllWithWarehouse() {
        return shelfRepository.findAllWithWarehouse().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShelfDTO create(ShelfDTO dto) {
        if (shelfRepository.existsByShelfCode(dto.getShelfCode())) {
            throw new RuntimeException("货架编码已存在");
        }

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("仓库不存在"));

        Shelf shelf = new Shelf();
        BeanUtils.copyProperties(dto, shelf, "id", "warehouseId", "warehouseName", "slots");
        shelf.setWarehouse(warehouse);
        shelf.setEnabled(true);
        shelf.setTotalSlots(dto.getRowNum() * dto.getColumnNum() * dto.getLayerNum());
        shelf.setUsedSlots(0);

        shelf = shelfRepository.save(shelf);

        createShelfSlots(shelf);

        logger.info("创建货架成功: {}", shelf.getShelfCode());

        return convertToDTOWithSlots(shelf);
    }

    private void createShelfSlots(Shelf shelf) {
        List<ShelfSlot> slots = new ArrayList<>();
        for (int row = 1; row <= shelf.getRowNum(); row++) {
            for (int col = 1; col <= shelf.getColumnNum(); col++) {
                for (int layer = 1; layer <= shelf.getLayerNum(); layer++) {
                    ShelfSlot slot = new ShelfSlot();
                    slot.setSlotCode(String.format("%s-%d-%d-%d", shelf.getShelfCode(), row, col, layer));
                    slot.setShelf(shelf);
                    slot.setRowIndex(row);
                    slot.setColumnIndex(col);
                    slot.setLayerIndex(layer);
                    slot.setQuantity(0);
                    slot.setMaxCapacity(100);
                    slot.setStatus("EMPTY");
                    slots.add(slot);
                }
            }
        }
        shelfSlotRepository.saveAll(slots);
    }

    @Transactional
    public ShelfDTO update(Long id, ShelfDTO dto) {
        Shelf shelf = shelfRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("货架不存在"));

        if (!shelf.getShelfCode().equals(dto.getShelfCode()) &&
                shelfRepository.existsByShelfCode(dto.getShelfCode())) {
            throw new RuntimeException("货架编码已存在");
        }

        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                    .orElseThrow(() -> new RuntimeException("仓库不存在"));
            shelf.setWarehouse(warehouse);
        }

        shelf.setShelfCode(dto.getShelfCode());
        shelf.setShelfName(dto.getShelfName());
        shelf.setPositionX(dto.getPositionX());
        shelf.setPositionY(dto.getPositionY());
        shelf.setPositionZ(dto.getPositionZ());
        shelf.setWidth(dto.getWidth());
        shelf.setDepth(dto.getDepth());
        shelf.setHeight(dto.getHeight());
        shelf.setShelfType(dto.getShelfType());
        shelf.setDescription(dto.getDescription());

        shelf = shelfRepository.save(shelf);
        logger.info("更新货架成功: {}", shelf.getShelfCode());

        return convertToDTOWithSlots(shelf);
    }

    @Transactional
    public void delete(Long id) {
        Shelf shelf = shelfRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("货架不存在"));
        shelf.setEnabled(false);
        shelfRepository.save(shelf);
        logger.info("删除货架成功: {}", shelf.getShelfCode());
    }

    private ShelfDTO convertToDTO(Shelf shelf) {
        ShelfDTO dto = new ShelfDTO();
        BeanUtils.copyProperties(shelf, dto);
        if (shelf.getWarehouse() != null) {
            dto.setWarehouseId(shelf.getWarehouse().getId());
            dto.setWarehouseName(shelf.getWarehouse().getWarehouseName());
        }
        return dto;
    }

    private ShelfDTO convertToDTOWithSlots(Shelf shelf) {
        ShelfDTO dto = convertToDTO(shelf);
        List<ShelfSlot> slots = shelfSlotRepository.findByShelfIdWithMaterial(shelf.getId());
        dto.setSlots(slots.stream().map(this::convertSlotToDTO).collect(Collectors.toList()));
        return dto;
    }

    private ShelfSlotDTO convertSlotToDTO(ShelfSlot slot) {
        ShelfSlotDTO dto = new ShelfSlotDTO();
        BeanUtils.copyProperties(slot, dto);
        if (slot.getShelf() != null) {
            dto.setShelfId(slot.getShelf().getId());
            dto.setShelfCode(slot.getShelf().getShelfCode());
        }
        if (slot.getMaterial() != null) {
            dto.setMaterialId(slot.getMaterial().getId());
            dto.setMaterialCode(slot.getMaterial().getMaterialCode());
            dto.setMaterialName(slot.getMaterial().getMaterialName());
            dto.setMaterialType(slot.getMaterial().getMaterialType());
        }
        return dto;
    }
}
