package com.penglecode.codeforce.mybatistiny.examples.domain.model;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.common.domain.ID;
import com.penglecode.codeforce.mybatistiny.annotations.*;

/**
 * 商品销售库存信息实体
 *
 * @author AutoCodeGenerator
 * @version 1.0
 */
@Table("t_product_sale_stock")
public class ProductSaleStock implements EntityObject {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    @Id(strategy=GenerationType.NONE)
    private Long productId;

    /** 商品规格编号,多个t_product_spec.spec_no按顺序拼凑 */
    @Id(strategy=GenerationType.NONE)
    private String specNo;

    /** 商品规格编号,多个t_product_spec.spec_name按顺序拼凑 */
    private String specName;

    /** 商品售价(单位分) */
    private Long sellPrice;

    /** 库存量 */
    private Integer stock;

    /** 创建时间 */
    @Column(updatable=false, select="DATE_FORMAT({name}, '%Y-%m-%d %T')")
    private String createTime;

    /** 最近修改时间 */
    @Column(select="DATE_FORMAT({name}, '%Y-%m-%d %T')")
    private String updateTime;

    //以下属于辅助字段

    /** stock的范围查询辅助字段 */
    @Transient
    private Integer minStock;

    /** stock的范围查询辅助字段 */
    @Transient
    private Integer maxStock;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getSpecNo() {
        return specNo;
    }

    public void setSpecNo(String specNo) {
        this.specNo = specNo;
    }

    public String getSpecName() {
        return specName;
    }

    public void setSpecName(String specName) {
        this.specName = specName;
    }

    public Long getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Long sellPrice) {
        this.sellPrice = sellPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
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

    public Integer getMinStock() {
        return minStock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
    }

    public Integer getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(Integer maxStock) {
        this.maxStock = maxStock;
    }

    @Override
    public ID identity() {
        return new ID().addKey("productId", productId).addKey("specNo", specNo);
    }

}
