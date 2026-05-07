package com.wms.controller;

import com.wms.dto.ApiResponse;
import com.wms.dto.OutboundOrderDTO;
import com.wms.service.OutboundOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outbound-orders")
public class OutboundOrderController {

    @Autowired
    private OutboundOrderService outboundOrderService;

    @GetMapping
    public ApiResponse<List<OutboundOrderDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<OutboundOrderDTO> result = outboundOrderService.findAll(pageRequest);

        return ApiResponse.success(
                result.getContent(),
                new ApiResponse.PageInfo(result.getTotalElements(), page, size, result.getTotalPages())
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<OutboundOrderDTO> findById(@PathVariable Long id) {
        try {
            return ApiResponse.success(outboundOrderService.findById(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<OutboundOrderDTO> create(@Valid @RequestBody OutboundOrderDTO dto) {
        try {
            return ApiResponse.success(outboundOrderService.create(dto), "创建成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<OutboundOrderDTO> confirm(@PathVariable Long id) {
        try {
            return ApiResponse.success(outboundOrderService.confirm(id), "出库确认成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
