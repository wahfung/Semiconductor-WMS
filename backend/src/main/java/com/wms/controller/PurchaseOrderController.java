package com.wms.controller;

import com.wms.dto.ApiResponse;
import com.wms.dto.PurchaseOrderDTO;
import com.wms.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ApiResponse<List<PurchaseOrderDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<PurchaseOrderDTO> result = purchaseOrderService.findAll(pageRequest);

        return ApiResponse.success(
                result.getContent(),
                new ApiResponse.PageInfo(result.getTotalElements(), page, size, result.getTotalPages())
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrderDTO> findById(@PathVariable Long id) {
        try {
            return ApiResponse.success(purchaseOrderService.findById(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<PurchaseOrderDTO> create(@Valid @RequestBody PurchaseOrderDTO dto) {
        try {
            return ApiResponse.success(purchaseOrderService.create(dto), "创建成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<PurchaseOrderDTO> approve(@PathVariable Long id) {
        try {
            return ApiResponse.success(purchaseOrderService.approve(id), "审批成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<?> cancel(@PathVariable Long id) {
        try {
            purchaseOrderService.cancel(id);
            return ApiResponse.success(null, "取消成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
