package com.wms.controller;

import com.wms.dto.ApiResponse;
import com.wms.dto.StocktakingOrderDTO;
import com.wms.service.StocktakingOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocktaking-orders")
public class StocktakingOrderController {

    @Autowired
    private StocktakingOrderService stocktakingOrderService;

    @GetMapping
    public ApiResponse<List<StocktakingOrderDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<StocktakingOrderDTO> result = stocktakingOrderService.findAll(pageRequest);

        return ApiResponse.success(
                result.getContent(),
                new ApiResponse.PageInfo(result.getTotalElements(), page, size, result.getTotalPages())
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<StocktakingOrderDTO> findById(@PathVariable Long id) {
        try {
            return ApiResponse.success(stocktakingOrderService.findById(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<StocktakingOrderDTO> create(@Valid @RequestBody StocktakingOrderDTO dto) {
        try {
            return ApiResponse.success(stocktakingOrderService.create(dto), "创建成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{orderId}/items/{itemId}")
    public ApiResponse<StocktakingOrderDTO> updateItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body) {
        try {
            Integer actualQuantity = body.get("actualQuantity");
            return ApiResponse.success(stocktakingOrderService.updateItem(orderId, itemId, actualQuantity));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<StocktakingOrderDTO> complete(@PathVariable Long id) {
        try {
            return ApiResponse.success(stocktakingOrderService.complete(id), "盘点完成");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
