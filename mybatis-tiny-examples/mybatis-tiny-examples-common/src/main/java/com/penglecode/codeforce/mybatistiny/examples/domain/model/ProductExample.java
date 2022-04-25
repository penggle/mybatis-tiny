package com.penglecode.codeforce.mybatistiny.examples.domain.model;

import com.penglecode.codeforce.common.domain.DomainObject;

import java.util.List;
import java.util.Map;

/**
 * 商品示例数据
 *
 * @author pengpeng
 * @version 1.0
 */
public class ProductExample implements DomainObject {

    private String productName;

    private Integer productType;

    private String productUrl;

    private String productTags;

    private Long avgSellPrice;

    private Long shopId;

    private Map<Integer,List<String>> productSaleSpecs;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getProductType() {
        return productType;
    }

    public void setProductType(Integer productType) {
        this.productType = productType;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getProductTags() {
        return productTags;
    }

    public void setProductTags(String productTags) {
        this.productTags = productTags;
    }

    public Long getAvgSellPrice() {
        return avgSellPrice;
    }

    public void setAvgSellPrice(Long avgSellPrice) {
        this.avgSellPrice = avgSellPrice;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Map<Integer, List<String>> getProductSaleSpecs() {
        return productSaleSpecs;
    }

    public void setProductSaleSpecs(Map<Integer, List<String>> productSaleSpecs) {
        this.productSaleSpecs = productSaleSpecs;
    }

}
