package com.wms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MaterialDTO {
    private Long id;

    @NotBlank(message = "物料编码不能为空")
    private String materialCode;

    @NotBlank(message = "物料名称不能为空")
    private String materialName;

    private String materialType;
    private String specification;
    private String unit;
    private BigDecimal price;
    private Integer minStock;
    private Integer maxStock;
    private String supplier;
    private String description;
    private Boolean enabled;
    private Integer totalQuantity;
}
