package com.penglecode.codeforce.mybatistiny.examples.domain.enums;

/**
 * 商品审核状态枚举
 *
 * @author AutoCodeGenerator
 * @version 1.0
 */
public enum ProductAuditStatusEnum {

    WAIT_AUDIT(0, "待审核"),
    AUDIT_PASSED(1, "审核通过"),
    AUDIT_UNPASSED(2, "审核不通过");

    private final Integer statusCode;

    private final String statusName;

    ProductAuditStatusEnum(Integer statusCode, String statusName) {
        this.statusCode = statusCode;
        this.statusName = statusName;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getStatusName() {
        return statusName;
    }

    public static ProductAuditStatusEnum of(Integer statusCode) {
        if(statusCode != null) {
            for(ProductAuditStatusEnum em : values()) {
                if(em.getStatusCode().equals(statusCode)) {
                    return em;
                }
            }
        }
        return null;
    }
}
