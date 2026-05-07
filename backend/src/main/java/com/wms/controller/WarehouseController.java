package com.wms.controller;

import com.wms.dto.ApiResponse;
import com.wms.dto.WarehouseDTO;
import com.wms.service.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping
    public ApiResponse<List<WarehouseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<WarehouseDTO> result = warehouseService.findAll(pageRequest);

        return ApiResponse.success(
                result.getContent(),
                new ApiResponse.PageInfo(result.getTotalElements(), page, size, result.getTotalPages())
        );
    }

    @GetMapping("/enabled")
    public ApiResponse<List<WarehouseDTO>> findAllEnabled() {
        return ApiResponse.success(warehouseService.findAllEnabled());
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseDTO> findById(@PathVariable Long id) {
        try {
            return ApiResponse.success(warehouseService.findById(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<WarehouseDTO> create(@Valid @RequestBody WarehouseDTO dto) {
        try {
            return ApiResponse.success(warehouseService.create(dto), "创建成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseDTO> update(@PathVariable Long id, @Valid @RequestBody WarehouseDTO dto) {
        try {
            return ApiResponse.success(warehouseService.update(id, dto), "更新成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        try {
            warehouseService.delete(id);
            return ApiResponse.success(null, "删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
