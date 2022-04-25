package com.penglecode.codeforce.mybatistiny.examples.domain.model;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.common.domain.ID;
import com.penglecode.codeforce.mybatistiny.annotations.Column;
import com.penglecode.codeforce.mybatistiny.annotations.GenerationType;
import com.penglecode.codeforce.mybatistiny.annotations.Id;
import com.penglecode.codeforce.mybatistiny.annotations.Table;

/**
 * 商品销售规格信息实体
 *
 * @author AutoCodeGenerator
 * @version 1.0
 */
@Table("t_product_sale_spec")
public class ProductSaleSpec implements EntityObject {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    @Id(strategy= GenerationType.NONE)
    private Long productId;

    /** 商品规格编号,两位数字组成 */
    @Id(strategy=GenerationType.NONE)
    private String specNo;

    /** 商品规格名称 */
    private String specName;

    /** 商品规格顺序 */
    private Integer specIndex;

    /** 商品规格备注 */
    private String remark;

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

    public Integer getSpecIndex() {
        return specIndex;
    }

    public void setSpecIndex(Integer specIndex) {
        this.specIndex = specIndex;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
    public ID identity() {
        return new ID().addKey("productId", productId).addKey("specNo", specNo);
    }

}
