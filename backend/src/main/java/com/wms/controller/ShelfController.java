package com.wms.controller;

import com.wms.dto.ApiResponse;
import com.wms.dto.ShelfDTO;
import com.wms.service.ShelfService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shelves")
public class ShelfController {

    @Autowired
    private ShelfService shelfService;

    @GetMapping
    public ApiResponse<List<ShelfDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ShelfDTO> result = shelfService.findAll(pageRequest);

        return ApiResponse.success(
                result.getContent(),
                new ApiResponse.PageInfo(result.getTotalElements(), page, size, result.getTotalPages())
        );
    }

    @GetMapping("/all")
    public ApiResponse<List<ShelfDTO>> findAllWithWarehouse() {
        return ApiResponse.success(shelfService.findAllWithWarehouse());
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ApiResponse<List<ShelfDTO>> findByWarehouseId(@PathVariable Long warehouseId) {
        return ApiResponse.success(shelfService.findByWarehouseId(warehouseId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShelfDTO> findById(@PathVariable Long id) {
        try {
            return ApiResponse.success(shelfService.findById(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<ShelfDTO> create(@Valid @RequestBody ShelfDTO dto) {
        try {
            return ApiResponse.success(shelfService.create(dto), "创建成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<ShelfDTO> update(@PathVariable Long id, @Valid @RequestBody ShelfDTO dto) {
        try {
            return ApiResponse.success(shelfService.update(id, dto), "更新成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        try {
            shelfService.delete(id);
            return ApiResponse.success(null, "删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
