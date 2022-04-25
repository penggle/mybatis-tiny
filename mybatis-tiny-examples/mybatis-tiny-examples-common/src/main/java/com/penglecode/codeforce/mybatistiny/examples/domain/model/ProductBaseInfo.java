package com.penglecode.codeforce.mybatistiny.examples.domain.model;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.mybatistiny.annotations.*;
import com.penglecode.codeforce.mybatistiny.examples.domain.enums.ProductAuditStatusEnum;
import com.penglecode.codeforce.mybatistiny.examples.domain.enums.ProductOnlineStatusEnum;
import com.penglecode.codeforce.mybatistiny.examples.domain.enums.ProductTypeEnum;

import java.util.List;
import java.util.Optional;

/**
 * 商品基础信息实体
 *
 * @author AutoCodeGenerator
 * @version 1.0
 */
@Table("t_product_base_info")
public class ProductBaseInfo implements EntityObject {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    @Id(strategy=GenerationType.IDENTITY)
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品URL */
    private String productUrl;

    /** 商品标签 */
    private String productTags;

    /** 商品类型：0-虚拟商品,1-实物商品 */
    private Integer productType;

    /** 审核状态：0-待审核,1-审核通过,2-审核不通过 */
    private Integer auditStatus;

    /** 上下架状态：0-已下架,1-已上架 */
    private Integer onlineStatus;

    /** 所属店铺ID */
    @Column(updatable=false)
    private Long shopId;

    /** 商品备注 */
    private String remark;

    /** 创建时间 */
    @Column(updatable=false, select="DATE_FORMAT({name}, '%Y-%m-%d %T')")
    private String createTime;

    /** 最近修改时间 */
    @Column(select="DATE_FORMAT({name}, '%Y-%m-%d %T')")
    private String updateTime;

    //以下属于辅助字段

    /** productType的查询结果辅助字段 */
    @Transient
    private String productTypeName;

    /** auditStatus的查询结果辅助字段 */
    @Transient
    private String auditStatusName;

    /** onlineStatus的查询结果辅助字段 */
    @Transient
    private String onlineStatusName;

    /** auditStatus的IN查询条件辅助字段 */
    @Transient
    private List<Integer> auditStatuses;

    /** createTime的范围查询条件辅助字段 */
    @Transient
    private String startCreateTime;

    /** createTime的范围查询条件辅助字段 */
    @Transient
    private String endCreateTime;

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

    public Integer getProductType() {
        return productType;
    }

    public void setProductType(Integer productType) {
        this.productType = productType;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Integer getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(Integer onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
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

    public String getProductTypeName() {
        return productTypeName;
    }

    public void setProductTypeName(String productTypeName) {
        this.productTypeName = productTypeName;
    }

    public String getAuditStatusName() {
        return auditStatusName;
    }

    public void setAuditStatusName(String auditStatusName) {
        this.auditStatusName = auditStatusName;
    }

    public String getOnlineStatusName() {
        return onlineStatusName;
    }

    public void setOnlineStatusName(String onlineStatusName) {
        this.onlineStatusName = onlineStatusName;
    }

    public List<Integer> getAuditStatuses() {
        return auditStatuses;
    }

    public void setAuditStatuses(List<Integer> auditStatuses) {
        this.auditStatuses = auditStatuses;
    }

    public String getStartCreateTime() {
        return startCreateTime;
    }

    public void setStartCreateTime(String startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    public String getEndCreateTime() {
        return endCreateTime;
    }

    public void setEndCreateTime(String endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    @Override
    public Long identity() {
        return productId;
    }

    @Override
    public ProductBaseInfo processOutbound() {
        Optional.ofNullable(ProductTypeEnum.of(productType)).map(ProductTypeEnum::getTypeName).ifPresent(this::setProductTypeName);
        Optional.ofNullable(ProductAuditStatusEnum.of(auditStatus)).map(ProductAuditStatusEnum::getStatusName).ifPresent(this::setAuditStatusName);
        Optional.ofNullable(ProductOnlineStatusEnum.of(onlineStatus)).map(ProductOnlineStatusEnum::getStatusName).ifPresent(this::setOnlineStatusName);
        return this;
    }
}
