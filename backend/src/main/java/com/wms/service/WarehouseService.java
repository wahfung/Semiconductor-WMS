package com.wms.service;

import com.wms.dto.WarehouseDTO;
import com.wms.entity.Warehouse;
import com.wms.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class WarehouseService {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseService.class);

    @Autowired
    private WarehouseRepository warehouseRepository;

    public Page<WarehouseDTO> findAll(Pageable pageable) {
        return warehouseRepository.findAllEnabled(pageable).map(this::convertToDTO);
    }

    public WarehouseDTO findById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("仓库不存在"));
        return convertToDTO(warehouse);
    }

    public List<WarehouseDTO> findAllEnabled() {
        return warehouseRepository.findAllEnabled().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public WarehouseDTO create(WarehouseDTO dto) {
        if (warehouseRepository.existsByWarehouseCode(dto.getWarehouseCode())) {
            throw new RuntimeException("仓库编码已存在");
        }

        Warehouse warehouse = new Warehouse();
        BeanUtils.copyProperties(dto, warehouse, "id");
        warehouse.setEnabled(true);
        warehouse.setUsedCapacity(0);

        warehouse = warehouseRepository.save(warehouse);
        logger.info("创建仓库成功: {}", warehouse.getWarehouseCode());

        return convertToDTO(warehouse);
    }

    @Transactional
    public WarehouseDTO update(Long id, WarehouseDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("仓库不存在"));

        if (!warehouse.getWarehouseCode().equals(dto.getWarehouseCode()) &&
                warehouseRepository.existsByWarehouseCode(dto.getWarehouseCode())) {
            throw new RuntimeException("仓库编码已存在");
        }

        BeanUtils.copyProperties(dto, warehouse, "id", "createTime", "usedCapacity", "enabled");
        warehouse = warehouseRepository.save(warehouse);
        logger.info("更新仓库成功: {}", warehouse.getWarehouseCode());

        return convertToDTO(warehouse);
    }

    @Transactional
    public void delete(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("仓库不存在"));
        warehouse.setEnabled(false);
        warehouseRepository.save(warehouse);
        logger.info("删除仓库成功: {}", warehouse.getWarehouseCode());
    }

    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        WarehouseDTO dto = new WarehouseDTO();
        BeanUtils.copyProperties(warehouse, dto);
        return dto;
    }
}
