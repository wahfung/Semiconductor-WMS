package com.wms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WarehouseDTO {
    private Long id;

    @NotBlank(message = "仓库编码不能为空")
    private String warehouseCode;

    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;

    private String address;
    private String manager;
    private String phone;
    private Integer totalCapacity;
    private Integer usedCapacity;
    private String description;
    private Boolean enabled;
}
