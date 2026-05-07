package com.wms.service;

import com.wms.dto.MaterialDTO;
import com.wms.entity.Material;
import com.wms.repository.InventoryRepository;
import com.wms.repository.MaterialRepository;
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
public class MaterialService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialService.class);

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public Page<MaterialDTO> findAll(Pageable pageable) {
        return materialRepository.findAllEnabled(pageable).map(this::convertToDTO);
    }

    public Page<MaterialDTO> search(String keyword, Pageable pageable) {
        return materialRepository.searchByKeyword(keyword, pageable).map(this::convertToDTO);
    }

    public MaterialDTO findById(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物料不存在"));
        return convertToDTO(material);
    }

    public List<MaterialDTO> findAllEnabled() {
        return materialRepository.findAllEnabled().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MaterialDTO> findByType(String materialType) {
        return materialRepository.findByMaterialType(materialType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MaterialDTO create(MaterialDTO dto) {
        if (materialRepository.existsByMaterialCode(dto.getMaterialCode())) {
            throw new RuntimeException("物料编码已存在");
        }

        Material material = new Material();
        BeanUtils.copyProperties(dto, material, "id");
        material.setEnabled(true);

        material = materialRepository.save(material);
        logger.info("创建物料成功: {}", material.getMaterialCode());

        return convertToDTO(material);
    }

    @Transactional
    public MaterialDTO update(Long id, MaterialDTO dto) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物料不存在"));

        if (!material.getMaterialCode().equals(dto.getMaterialCode()) &&
                materialRepository.existsByMaterialCode(dto.getMaterialCode())) {
            throw new RuntimeException("物料编码已存在");
        }

        BeanUtils.copyProperties(dto, material, "id", "createTime", "enabled");
        material = materialRepository.save(material);
        logger.info("更新物料成功: {}", material.getMaterialCode());

        return convertToDTO(material);
    }

    @Transactional
    public void delete(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物料不存在"));
        material.setEnabled(false);
        materialRepository.save(material);
        logger.info("删除物料成功: {}", material.getMaterialCode());
    }

    private MaterialDTO convertToDTO(Material material) {
        MaterialDTO dto = new MaterialDTO();
        BeanUtils.copyProperties(material, dto);
        Integer totalQuantity = inventoryRepository.getTotalQuantityByMaterialId(material.getId());
        dto.setTotalQuantity(totalQuantity != null ? totalQuantity : 0);
        return dto;
    }
}
