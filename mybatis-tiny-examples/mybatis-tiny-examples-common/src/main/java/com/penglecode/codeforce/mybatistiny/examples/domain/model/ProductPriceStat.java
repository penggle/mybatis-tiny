package com.penglecode.codeforce.mybatistiny.examples.domain.model;

import com.penglecode.codeforce.common.domain.DomainObject;

/**
 * 商品价格统计信息领域对象
 *
 * @author pengpeng
 * @version 1.0
 */
public class ProductPriceStat implements DomainObject {

    private static final long serialVersionUID = 1L;

    private Long productId;

    private String productName;

    private Integer saleSpecCount;

    private Long avgSellPrice;

    private Long minSellPrice;

    private Long maxSellPrice;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getSaleSpecCount() {
        return saleSpecCount;
    }

    public void setSaleSpecCount(Integer saleSpecCount) {
        this.saleSpecCount = saleSpecCount;
    }

    public Long getAvgSellPrice() {
        return avgSellPrice;
    }

    public void setAvgSellPrice(Long avgSellPrice) {
        this.avgSellPrice = avgSellPrice;
    }

    public Long getMinSellPrice() {
        return minSellPrice;
    }

    public void setMinSellPrice(Long minSellPrice) {
        this.minSellPrice = minSellPrice;
    }

    public Long getMaxSellPrice() {
        return maxSellPrice;
    }

    public void setMaxSellPrice(Long maxSellPrice) {
        this.maxSellPrice = maxSellPrice;
    }

}
