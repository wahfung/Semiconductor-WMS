package com.wms.controller;

import com.wms.dto.ApiResponse;
import com.wms.dto.MaterialDTO;
import com.wms.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @GetMapping
    public ApiResponse<List<MaterialDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<MaterialDTO> result;

        if (keyword != null && !keyword.isEmpty()) {
            result = materialService.search(keyword, pageRequest);
        } else {
            result = materialService.findAll(pageRequest);
        }

        return ApiResponse.success(
                result.getContent(),
                new ApiResponse.PageInfo(result.getTotalElements(), page, size, result.getTotalPages())
        );
    }

    @GetMapping("/enabled")
    public ApiResponse<List<MaterialDTO>> findAllEnabled() {
        return ApiResponse.success(materialService.findAllEnabled());
    }

    @GetMapping("/type/{materialType}")
    public ApiResponse<List<MaterialDTO>> findByType(@PathVariable String materialType) {
        return ApiResponse.success(materialService.findByType(materialType));
    }

    @GetMapping("/{id}")
    public ApiResponse<MaterialDTO> findById(@PathVariable Long id) {
        try {
            return ApiResponse.success(materialService.findById(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<MaterialDTO> create(@Valid @RequestBody MaterialDTO dto) {
        try {
            return ApiResponse.success(materialService.create(dto), "创建成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<MaterialDTO> update(@PathVariable Long id, @Valid @RequestBody MaterialDTO dto) {
        try {
            return ApiResponse.success(materialService.update(id, dto), "更新成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        try {
            materialService.delete(id);
            return ApiResponse.success(null, "删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
