CREATE TABLE t_product_base_info (
    product_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    product_name VARCHAR(191) NOT NULL COMMENT '商品名称',
    product_url VARCHAR(255) NOT NULL COMMENT '商品URL',
    product_tags VARCHAR(255) NULL DEFAULT NULL COMMENT '商品标签',
    product_type TINYINT(1) NOT NULL DEFAULT '1' COMMENT '商品类型：0-虚拟商品,1-实物商品',
    audit_status TINYINT(1) NOT NULL DEFAULT '0' COMMENT '商品审核状态：0-待审核,1-审核通过,2-审核不通过',
    online_status TINYINT(1) NOT NULL DEFAULT '0' COMMENT '上下架状态：0-已下架,1-已上架',
    shop_id BIGINT UNSIGNED NOT NULL COMMENT '所属店铺ID',
    remark VARCHAR(255) NULL DEFAULT NULL COMMENT '商品备注',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '最近修改时间',
    PRIMARY KEY (product_id)
) COMMENT='商品基础信息表' ENGINE=InnoDB;

CREATE TABLE t_product_extra_info (
    product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
    product_details TEXT NOT NULL COMMENT '商品详情(HTML片段)',
    product_specifications TEXT COMMENT '商品规则参数(HTML片段)',
    product_services TEXT COMMENT '商品服务(HTML片段)',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '最近修改时间',
    PRIMARY KEY (product_id)
) COMMENT='商品额外信息表' ENGINE=InnoDB;

CREATE TABLE t_product_sale_spec (
    product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
    spec_no VARCHAR(16) NOT NULL COMMENT '商品规格编号,两位数字组成',
    spec_name VARCHAR(191) NOT NULL COMMENT '商品规格名称',
    spec_index INT NOT NULL DEFAULT '1' COMMENT '商品规格顺序',
    remark VARCHAR(255) NULL DEFAULT NULL COMMENT '商品规格备注',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '最近修改时间',
    PRIMARY KEY (product_id, spec_no)
) COMMENT='商品销售规格表' ENGINE=InnoDB;

CREATE TABLE t_product_sale_stock (
    product_id BIGINT(20) UNSIGNED NOT NULL COMMENT '商品ID',
    spec_no VARCHAR(64) NOT NULL COMMENT '商品规格编号,多个t_product_sale_spec.spec_no按顺序拼凑',
    spec_name VARCHAR(64) NOT NULL COMMENT '商品规格编号,多个t_product_sale_spec.spec_name按顺序拼凑',
    sell_price BIGINT(19) NOT NULL COMMENT '商品售价(单位分)',
    stock INT NOT NULL DEFAULT '0' COMMENT '库存量',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '最近修改时间',
    PRIMARY KEY (product_id, spec_no)
) COMMENT='商品销售库存表' ENGINE=InnoDB;

CREATE TABLE t_component_meta(
     component_code	VARCHAR(32) NOT NULL COMMENT '组件代码',
     component_name VARCHAR(64) NOT NULL COMMENT '组件名称',
     component_type VARCHAR(32) NOT NULL COMMENT '组件类型',
     component_props VARCHAR(2000) NOT NULL DEFAULT '{}' COMMENT '组件属性配置',
     component_apis VARCHAR(2000) NOT NULL DEFAULT '[]' COMMENT '组件接口列表',
     component_doc VARCHAR(2000) NOT NULL DEFAULT '{}' COMMENT '组件文档信息',
     create_time DATETIME NOT NULL COMMENT '创建时间',
     update_time DATETIME NOT NULL COMMENT '最近更新时间',
     PRIMARY KEY(component_code)
) COMMENT='组件元数据表' ENGINE=InnoDB;

CREATE TABLE t_student (
    id BIGINT(19) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(32) NOT NULL COMMENT '学号',
    name VARCHAR(18) NOT NULL COMMENT '姓名',
    sex VARCHAR(1) NOT NULL COMMENT '性别',
    age TINYINT(3) NOT NULL COMMENT '年龄',
    created DATETIME NOT NULL COMMENT '创建时间',
    updated DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (id) USING BTREE,
    UNIQUE KEY uk_student_code(code)
) COMMENT='学生表' ENGINE=InnoDB;