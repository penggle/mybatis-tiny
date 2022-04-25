package com.penglecode.codeforce.mybatistiny.examples.domain.model;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.mybatistiny.annotations.Column;
import com.penglecode.codeforce.mybatistiny.annotations.GenerationType;
import com.penglecode.codeforce.mybatistiny.annotations.Id;
import com.penglecode.codeforce.mybatistiny.annotations.Table;


/**
 * 商品额外信息实体
 *
 * @author AutoCodeGenerator
 * @version 1.0
 */
@Table("t_product_extra_info")
public class ProductExtraInfo implements EntityObject {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    @Id(strategy=GenerationType.NONE)
    private Long productId;

    /** 商品详情(HTML片段) */
    private String productDetails;

    /** 商品规则参数(HTML片段) */
    private String productSpecifications;

    /** 商品服务(HTML片段) */
    private String productServices;

    /** 创建时间 */
    @Column(updatable=false, select="DATE_FORMAT({name}, '%Y-%m-%d %T')")
    private String createTime;

    /** 最近修改时间 */
    @Column(select="DATE_FORMAT({name}, '%Y-%m-%d %T')")
    private String updateTime;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    public String getProductSpecifications() {
        return productSpecifications;
    }

    public void setProductSpecifications(String productSpecifications) {
        this.productSpecifications = productSpecifications;
    }

    public String getProductServices() {
        return productServices;
    }

    public void setProductServices(String productServices) {
        this.productServices = productServices;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public Long identity() {
        return productId;
    }

}
