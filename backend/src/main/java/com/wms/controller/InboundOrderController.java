package com.wms.controller;

import com.wms.dto.ApiResponse;
import com.wms.dto.InboundOrderDTO;
import com.wms.service.InboundOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbound-orders")
public class InboundOrderController {

    @Autowired
    private InboundOrderService inboundOrderService;

    @GetMapping
    public ApiResponse<List<InboundOrderDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<InboundOrderDTO> result = inboundOrderService.findAll(pageRequest);

        return ApiResponse.success(
                result.getContent(),
                new ApiResponse.PageInfo(result.getTotalElements(), page, size, result.getTotalPages())
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<InboundOrderDTO> findById(@PathVariable Long id) {
        try {
            return ApiResponse.success(inboundOrderService.findById(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<InboundOrderDTO> create(@Valid @RequestBody InboundOrderDTO dto) {
        try {
            return ApiResponse.success(inboundOrderService.create(dto), "创建成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<InboundOrderDTO> confirm(@PathVariable Long id) {
        try {
            return ApiResponse.success(inboundOrderService.confirm(id), "入库确认成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
