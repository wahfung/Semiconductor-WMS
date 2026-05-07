package com.wms.controller;

import com.wms.dto.ApiResponse;
import com.wms.dto.InventoryDTO;
import com.wms.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public ApiResponse<List<InventoryDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<InventoryDTO> result = inventoryService.findAll(pageRequest);

        return ApiResponse.success(
                result.getContent(),
                new ApiResponse.PageInfo(result.getTotalElements(), page, size, result.getTotalPages())
        );
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ApiResponse<List<InventoryDTO>> findByWarehouseId(@PathVariable Long warehouseId) {
        return ApiResponse.success(inventoryService.findByWarehouseId(warehouseId));
    }

    @GetMapping("/material/{materialId}")
    public ApiResponse<List<InventoryDTO>> findByMaterialId(@PathVariable Long materialId) {
        return ApiResponse.success(inventoryService.findByMaterialId(materialId));
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryDTO> findById(@PathVariable Long id) {
        try {
            return ApiResponse.success(inventoryService.findById(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
