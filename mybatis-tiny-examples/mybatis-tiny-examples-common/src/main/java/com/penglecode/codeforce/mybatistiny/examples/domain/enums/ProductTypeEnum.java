package com.penglecode.codeforce.mybatistiny.examples.domain.enums;

/**
 * 商品类型枚举
 *
 * @author AutoCodeGenerator
 * @version 1.0
 */
public enum ProductTypeEnum {

    VIRTUAL_PRODUCT(0, "虚拟商品"),
    PHYSICAL_PRODUCT(1, "实物商品");

    private final Integer typeCode;

    private final String typeName;

    ProductTypeEnum(Integer typeCode, String typeName) {
        this.typeCode = typeCode;
        this.typeName = typeName;
    }

    public Integer getTypeCode() {
        return typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public static ProductTypeEnum of(Integer typeCode) {
        if(typeCode != null) {
            for(ProductTypeEnum em : values()) {
                if(em.getTypeCode().equals(typeCode)) {
                    return em;
                }
            }
        }
        return null;
    }
}
