# Mybatis-Tiny



## 基本简介

Mybatis-Tiny是什么？Mybatis-Tiny是一个基于Mybatis框架的一层极简的扩展，它旨在使用DSL的方式对单表进行CRUD操作，类似于Mybatis-Plus框架，但它绝不是重复造轮子！区别于别的类似框架（如Mybatis-Plus、Fluent-Mybatis等）的实现方式，它采用一种逆向曲线救国的实现方式，通过较少的代码，极简的扩展实现了类似于他们大多数的功能，完全满足日常开发中对单表的各种CRUD操作。



## 快速入门

> **Talk is cheap，show me the code！**

- #### 插入操作

  ```java
  ProductBaseInfo productBase = ...;
  List<ProductSaleSpec> productSaleSpecs = ...;
  productBaseInfoMapper.insert(productBase);
  //基于JDBC-Batch特性的批量插入操作。
  //顺便说一句：对于MySQL不建议在XML中使用<foreach/>来拼接insert into values(..),(..),(...);诚然MySQL底层驱动在开启JDBC-Batch特性时也是将多条单个insert语句改写成insert into values(..),(..),(...)，但是作为客户端程序无法掌握SQL语句字节大小，小了体现不出来JDBC-Batch特性的威力，大了容易报错，所以这个度还是让驱动自己去掌控。
  //注意对于MySQL需要开启秘籍参数(rewriteBatchedStatements=true)才能正在开启JDBC-Batch特性
  productSaleSpecMapper.batchUpdate(productSaleSpecs, productSaleSpec -> productSaleSpecMapper.insert(productSaleSpec));
  ```

- #### 更新操作

  ```java
  //根据ID更新
  ProductBaseInfo productBase = ...;
  Map<String,Object> updateColumns1 = MapLambdaBuilder.of(productBase)
          .with(ProductBaseInfo::getProductName)
          .with(ProductBaseInfo::getRemark)
          .withDefault(ProductBaseInfo::getProductType, 1)
          .withOverride(ProductBaseInfo::getAuditStatus, 0)
          .withOverride(ProductBaseInfo::getOnlineStatus, 0)
          .withOverride(ProductBaseInfo::getUpdateTime, DateTimeUtils.formatNow())
          .build();
  productBaseInfoMapper.updateById(productBase.getProductId(), updateColumns1);
  //productBaseInfoMapper.updateById(productBase.identity(), updateColumns);
  
  //根据条件更新
  Map<String,Object> updateColumns2 = MapLambdaBuilder.<ProductBaseInfo>ofEmpty()
          .withOverride(ProductBaseInfo::getOnlineStatus, 0)
          .withOverride(ProductBaseInfo::getUpdateTime, DateTimeUtils.formatNow())
          .build();
  QueryCriteria<ProductBaseInfo> updateCriteria2 = LambdaQueryCriteria.ofSupplier(ProductBaseInfo::new)
          .eq(ProductBaseInfo::getProductType, 1)
      	.in(ProductBaseInfo::getAuditStatus, 0, 1)
          .limit(5);
  productBaseInfoMapper.updateByCriteria(updateCriteria2, updateColumns2);
  
  //批量更新
  List<ProductSaleStock> productSaleStocks = ...;
  String nowTime = DateTimeUtils.formatNow();
  productSaleStockMapper.batchUpdate(productSaleStocks, productSaleStock -> {
      Map<String,Object> updateColumns = MapLambdaBuilder.of(productSaleStock)
          .withOverride(ProductSaleStock::getSellPrice, productSaleStock.getSellPrice() - productSaleStock.getSellPrice() % 100)
          .withOverride(ProductSaleStock::getUpdateTime, nowTime)
          .build();
      productSaleStockMapper.updateById(productSaleStock.identity(), updateColumns);
  });
  ```
  
- #### 查询操作

  ```java
  //根据ID查
  ProductBaseInfo productBase1 = productBaseInfoMapper.selectById(1L);
  
  ProductBaseInfo productBase2 = productBaseInfoMapper.selectById(10L, new QueryColumns(ProductBaseInfo::getProductId, ProductBaseInfo::getProductName, ProductBaseInfo::getAuditStatus, ProductBaseInfo::getOnlineStatus));
  
  ID id = new ID().addKey(ProductSaleSpec::getProductId, 1L).addKey(ProductSaleSpec::getSpecNo, "101");
  ProductSaleSpec productSaleSpec = productSaleSpecMapper.selectById(id);
  
  //根据多个ID查询
  List<ProductBaseInfo> productBases = productBaseInfoMapper.selectListByIds(Arrays.asList(5L, 6L, 7L, 8L, 9L));
  
  List<ID> ids = new ArrayList<>();
  ids.add(new ID().addKey(ProductSaleSpec::getProductId, 1L).addKey(ProductSaleSpec::getSpecNo, "101"));
  ids.add(new ID().addKey(ProductSaleSpec::getProductId, 1L).addKey(ProductSaleSpec::getSpecNo, "102"));
  ids.add(new ID().addKey(ProductSaleSpec::getProductId, 1L).addKey(ProductSaleSpec::getSpecNo, "103"));
  List<ProductSaleSpec> productSaleSpecs = productSaleSpecMapper.selectListByIds(ids);
  
  //根据条件查询
  QueryCriteria<ProductSaleSpec> queryCriteria1 = LambdaQueryCriteria.ofSupplier(ProductSaleSpec::new)
                  .eq(ProductSaleSpec::getProductId, 1L)
                  .eq(ProductSaleSpec::getSpecNo, "101");
  ProductSaleSpec productSaleSpec = productSaleSpecMapper.selectByCriteria(queryCriteria1);
  
  ProductSaleStock queryRequest1 = ...;
  QueryCriteria<ProductSaleStock> queryCriteria2 = LambdaQueryCriteria.of(queryRequest1)
                  .eq(ProductSaleStock::getProductId)
                  .likeRight(ProductSaleStock::getSpecNo)
                  .between(ProductSaleStock::getStock, queryRequest1.getMinStock(), queryRequest1.getMaxStock())
                  .orderBy(OrderBy.desc(ProductSaleStock::getSellPrice));
  List<ProductSaleStock> productStocks = productSaleStockMapper.selectListByCriteria(queryCriteria2);
  
  QueryCriteria<ProductBaseInfo> queryCriteria3 = LambdaQueryCriteria.of(queryRequest2)
                  .and(nestedCriteria -> nestedCriteria.like(ProductBaseInfo::getProductName, "华为")
                          .or().like(ProductBaseInfo::getProductName, "HUAWEI"))
                  .eq(ProductBaseInfo::getProductType)
                  .eq(ProductBaseInfo::getOnlineStatus)
                  .in(ProductBaseInfo::getAuditStatus, queryRequest.getAuditStatuses().toArray())
                  .orderBy(OrderBy.desc(ProductBaseInfo::getCreateTime))
                  .dynamic(true); //自动过滤掉为空值(null|空串|空数组|空集合)的查询参数
  List<ProductBaseInfo> productBases1 = productBaseInfoMapper.selectListByCriteria(queryCriteria3);
  
  //分页查询1
  Page page = Page.of(1, 10);
  QueryCriteria<ProductBaseInfo> queryCriteria4 = LambdaQueryCriteria.of(queryRequest)
                  .likeRight(ProductBaseInfo::getProductName)
                  .eq(ProductBaseInfo::getProductType)
                  .eq(ProductBaseInfo::getOnlineStatus)
                  .in(ProductBaseInfo::getAuditStatus, queryRequest.getAuditStatuses().toArray())
                  .orderBy(page.getOrderBys())
                  .dynamic(true); //自动过滤掉为空值(null|空串|空数组|空集合)的查询参数
  List<ProductBaseInfo> productBases2 = productBaseInfoMapper.selectPageListByCriteria(queryCriteria4, new RowBounds(page.offset(), page.limit()));
  //设置总记录数
  page.setTotalRowCount(productBaseInfoMapper.selectPageCountByCriteria(queryCriteria4));
  
  //分页查询2(等效与上面)
  Page page = Page.of(2, 10);
  List<ProductBaseInfo> productBases2 = EntityMapperHelper.selectEntityObjectListByPage(productBaseInfoMapper, queryCriteria4, page);
  ```

- #### 删除操作

  ```java
  //根据ID删除
  productBaseInfoMapper.deleteById(2L);
  productExtraInfoMapper.deleteById(2L);
  
  //根据条件删除
  QueryCriteria<ProductSaleSpec> queryCriteria1 = LambdaQueryCriteria.ofSupplier(ProductSaleSpec::new)
                  .eq(ProductSaleSpec::getProductId, 2L)
                  .limit(5);
  productSaleSpecMapper.deleteByCriteria(queryCriteria1);
  ```

- 更多示例请见：https://github.com/penggle/mybatis-tiny/tree/main/mybatis-tiny-examples





## 特性及限制

- 支持单一主键或联合主键，单一主键时主键策略支持：IDENTITY(数据库自增的)，SEQUENCE(基于序列的)，NONE(无，客户端自己设置主键)

  > 重复造轮子的初衷也是被Mybatis-Plus只能使用单一主键给恶心到了

- Entity实体类是基于注解的（注解类的设计基本与JPA的注解规范一致）；实体类实现`EntityObject`接口并实现数据出站处理方法来实现decode能力，例如：

  ```java
  @Table("t_product_base_info")
  public class ProductBaseInfo implements EntityObject {
  
      /** 商品ID */
      @Id(strategy=GenerationType.IDENTITY)
      private Long productId;
  
      /** 商品名称 */
      private String productName;
  
  	...
  
      /** 审核状态：0-待审核,1-审核通过,2-审核不通过 */
      private Integer auditStatus;
  
      /** 上下架状态：0-已下架,1-已上架 */
      private Integer onlineStatus;
      
      /** 所属店铺ID */
      //shopId字段在所有update操作时不会被更新(不在update列中)
      @Column(updatable=false)
      private Long shopId;
  
      /** 商品备注 */
      private String remark;
  
      /** 创建时间 */
      //createTime字段在所有update操作时不会被更新(不在update列中)
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
      
      //getter/setter...
      
      @Override
      public Long identity() {
          return productId;
      }
  
      /**
       * 这个方法在所有SELECT操作返回结果集前都会由Mybatis
       * 插件DomainObjectQueryInterceptor自动执行
       */
      @Override
      public ProductBaseInfo processOutbound() {
          Optional.ofNullable(ProductTypeEnum.of(productType)).map(ProductTypeEnum::getTypeName).ifPresent(this::setProductTypeName);
          Optional.ofNullable(ProductAuditStatusEnum.of(auditStatus)).map(ProductAuditStatusEnum::getStatusName).ifPresent(this::setAuditStatusName);
          Optional.ofNullable(ProductOnlineStatusEnum.of(onlineStatus)).map(ProductOnlineStatusEnum::getStatusName).ifPresent(this::setOnlineStatusName);
          return this;
      }
      
  }
  ```

- 支持基于Lambda的DSL方式查询是必须的，例如：

  ```java
  ProductBaseInfo queryRequest = ...
  QueryCriteria<ProductBaseInfo> queryCriteria = LambdaQueryCriteria.of(queryRequest)
          .likeRight(ProductBaseInfo::getProductName)
          .eq(ProductBaseInfo::getProductType)
          .eq(ProductBaseInfo::getOnlineStatus, 1) //固定某个查询条件值
          .in(ProductBaseInfo::getAuditStatus, queryRequest.getAuditStatuses().toArray())
          .orderBy(OrderBy.desc(ProductBaseInfo::getCreateTime))
          .limit(5)
          .dynamic(true); //自动过滤掉空值(null|空串|空数组|空集合)查询参数;
  List<ProductBaseInfo> productBases = productBaseInfoMapper.selectListByCriteria(queryCriteria);
  ```

- 支持指定SELECT返回列、UPDATE更新列那都是必须的，例如：

  ```java
  //更新指定列
  ProductBaseInfo updateRequest = ...
  Map<String,Object> updateColumns = MapLambdaBuilder.of(updateRequest)
          .with(ProductBaseInfo::getProductName)
          .with(ProductBaseInfo::getRemark)
          .withDefault(ProductBaseInfo::getProductType, 1)
          .withOverride(ProductBaseInfo::getAuditStatus, 0)
          .withOverride(ProductBaseInfo::getOnlineStatus, 0)
          .withOverride(ProductBaseInfo::getUpdateTime, DateTimeUtils.formatNow())
          .build();
  productBaseInfoMapper.updateById(updateRequest.identity(), updateColumns);
  
  //查询返回指定列
  ProductBaseInfo productBase = productBaseInfoMapper.selectById(1L, new QueryColumns(ProductBaseInfo::getProductId, ProductBaseInfo::getProductName, ProductBaseInfo::getAuditStatus, ProductBaseInfo::getOnlineStatus));
  ```

- 自带基于RowBounds的分页功能，不管是调用`BaseEntityMapper#selectPageListByCriteria(QueryCriteria<T>, RowBounds)`还是调用自定义的分页查询方法`XxxMapper#selectXxxListByPage(Xxx condition, RowBounds)`都将会被自动分页，例如：

  ```java
  public List<ProductBaseInfo> queryProductListByPage(ProductBaseInfo queryRequest, Page page) {
      QueryCriteria<ProductBaseInfo>> queryCriteria = LambdaQueryCriteria.of(queryRequest)
                  .like(ProductBaseInfo::getProductName)
                  .eq(ProductBaseInfo::getProductType)
                  .eq(ProductBaseInfo::getOnlineStatus)
                  .in(ProductBaseInfo::getAuditStatus, queryRequest.getAuditStatuses().toArray())
                  .orderBy(page.getOrderBys())
                  .dynamic(true); //自动过滤掉为空值(null|空串|空数组|空集合)的查询参数
      List<ProductBaseInfo> productBases = productBaseInfoMapper.selectPageListByCriteria(queryCriteria, new RowBounds(page.offset(), page.limit()));
      page.setTotalRowCount(productBaseInfoMapper.selectPageCountByCriteria(queryCriteria)); //设置总记录数
      return productBases;
  }
  ```

- 在Xxx实体对象的XxxMapper中自定义方法肯定是可以的：

  见`ProductBaseInfoMapper.xml`

- WHERE条件逻辑嵌套查询仅支持嵌套一层（在单表操作中仅支持一层嵌套已经能满足绝大多数要求了），例如：

  ```java
  QueryCriteria<ProductBaseInfo> queryCriteria = LambdaQueryCriteria.of(queryRequest)
                  //仅支持一层嵌套
                  .and(nestedCriteria -> nestedCriteria.like(ProductBaseInfo::getProductName, "华为")
                          .or().like(ProductBaseInfo::getProductName, "HUAWEI"))
                  .eq(ProductBaseInfo::getProductType)
                  .eq(ProductBaseInfo::getOnlineStatus)
                  .in(ProductBaseInfo::getAuditStatus, queryRequest.getAuditStatuses().toArray())
                  .orderBy(page.getOrderBys())
                  .dynamic(true); //自动过滤掉为空值(null|空串|空数组|空集合)的查询参数
  ```

  上面DSL语句的实际输出SQL如下：

  ```sql
  - ==>  Preparing: SELECT t.product_id productId, t.product_name productName, t.product_type productType, t.audit_status auditStatus, t.online_status onlineStatus FROM t_product_base_info t WHERE ( t.product_name like ? OR t.product_name like ? ) AND t.product_type = ? AND t.audit_status in ( ? , ? , ? ) ORDER BY t.create_time DESC LIMIT 0, 10
  - ==> Parameters: %华为%(String), %HUAWEI%(String), 1(Integer), 0(Integer), 1(Integer), 2(Integer)
  - <==      Total: 10
  ```

- 扩展了Mybatis的`org.apache.ibatis.executor.Executor`，叫`DynamicExecutor`，用于解决在使用mybatis-spring框架时在同一个事务中不能切换ExecutorType的蛋疼问题（如果你硬要这么做，你将会得到一个异常：'Cannot change the ExecutorType when there is an existing transaction'），这个`DynamicExecutor`就是来解决这个问题的。

- 仅支持单表CRUD操作，不支持多表JOIN，不支持聚合查询(聚合函数+GROUP BY)

  > 写这个框架的当初初衷仅仅是为了能够省去编写XxxMapper.xml，如果做多表JOIN及聚合查询的话，则就失去了使用Mybatis的意义了，还不如直接使用JPA。试想你把一个复杂查询通过DSL的方式写在JAVA代码中，这跟十多年前在JAVA或者JSP代码中写SQL一样，感觉很恶心。

- 仅提供了通用的BaseEntityMapper，没有提供BaseService之类的，BaseEntityMapper的方法如下：

  ![](BaseEntityMapper.png)

  

## 使用方式

Mybatis-Tiny是一层很薄的东西，没有任何特性化的自定义配置，其仅依赖Mybatis本身（不依赖于Spring或SpringBoot）

其Maven依赖：

```xml
<dependency>
    <groupId>io.github.penggle</groupId>
    <artifactId>mybatis-tiny-core</artifactId>
    <!-- 版本说明：3.5指的是基于Mybatis 3.5.x版本的意思 -->
    <version>3.5</version>
</dependency>
```



下面列举三种使用场景。

- 只使用Mybatis（无Spring、SpringBoot等大型框架的支持）

  ```java
  //我不管你其他配置是啥，只要sqlSessionFactory实例是通过DecoratedSqlSessionFactoryBuilder弄出来的就行了！！！
  SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new DecoratedSqlSessionFactoryBuilder();
  SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(Resources.getResourceAsStream("mybatis-config.xml"));
  ```

- 仅与Spring框架（准确地说是mybatis-spring）集成使用

  引入相关Maven依赖后在配置类上使用注解`@EnableMybatisTiny`即可，例如：

  ```java
  import com.penglecode.codeforce.mybatistiny.EnableMybatisTiny;
  import com.penglecode.codeforce.mybatistiny.core.DecoratedSqlSessionFactoryBuilder;
  import com.penglecode.codeforce.mybatistiny.examples.BasePackage;
  import com.zaxxer.hikari.HikariConfig;
  import com.zaxxer.hikari.HikariDataSource;
  import org.apache.commons.lang3.ArrayUtils;
  import org.apache.ibatis.annotations.Mapper;
  import org.mybatis.spring.SqlSessionFactoryBean;
  import org.mybatis.spring.annotation.MapperScan;
  import org.springframework.context.EnvironmentAware;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.ComponentScan;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.context.annotation.PropertySource;
  import org.springframework.jdbc.datasource.DataSourceTransactionManager;
  import org.springframework.transaction.annotation.EnableTransactionManagement;
  
  @Configuration
  @EnableMybatisTiny
  @EnableTransactionManagement(proxyTargetClass=true)
  @MapperScan(basePackageClasses=BasePackage.class, annotationClass=Mapper.class)
  @ComponentScan(basePackageClasses=BasePackage.class)
  @PropertySource(value="classpath:application.yml", factory=YamlPropertySourceFactory.class)
  public class MybatisConfiguration {
  
      @Bean
      public DataSource dataSource() {
          Properties properties = ...
          return new HikariDataSource(new HikariConfig(properties));
      }
  
      @Bean
      public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) {
          SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
          sqlSessionFactoryBean.setDataSource(dataSource);
          //这里用不用Mybatis-Tiny提供的DecoratedSqlSessionFactoryBuilder是可选的
          //如果不用，在Spring环境下Mybatis-Tiny框架是有应对的弥补措施的
          //sqlSessionFactoryBean.setSqlSessionFactoryBuilder(new DecoratedSqlSessionFactoryBuilder());
          sqlSessionFactoryBean.setConfigLocation(getConfigLocation());
          sqlSessionFactoryBean.setTypeAliasesPackage(getTypeAliasesPackage());
          sqlSessionFactoryBean.setTypeAliasesSuperType(getTypeAliasesSuperType());
          sqlSessionFactoryBean.setMapperLocations(getMapperLocations());
          return sqlSessionFactoryBean;
      }
  
      @Bean
      public DataSourceTransactionManager transactionManager(DataSource dataSource) {
          return new DataSourceTransactionManager(dataSource);
      }
      
      ...
      
  }
  ```

- 仅与SpringBoot框架（准确地说是mybatis-spring-boot-starter）集成使用

  引入相关Maven依赖后在SpringBoot启动类上使用注解`@EnableMybatisTiny`即可，例如：

  ```java
  
  import com.penglecode.codeforce.mybatistiny.EnableMybatisTiny;
  import com.penglecode.codeforce.mybatistiny.examples.BasePackage;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  
  @EnableMybatisTiny
  @SpringBootApplication(scanBasePackageClasses=BasePackage.class)
  public class MybatisTinyExampleApplication {
  
      public static void main(String[] args) {
          SpringApplication.run(MybatisTinyExampleApplication.class, args);
      }
  
  }
  ```

  `mybatis-spring-boot-starter`及`DataSource`的配置照旧就好了，`application.yml`例如：

  ```yaml
  #SpringBoot应用的名称
  spring:
      application:
          name: mybatis-tiny-examples-springboot
      #Hikari 连接池配置
      datasource:
          hikari:
              #连接池名字
              pool-name: defaultHikariCP
              #最小空闲连接数量
              minimum-idle: 5
              #空闲连接存活最大时间，默认600000(10分钟)
              idle-timeout: 180000
              #连接池最大连接数，默认是10
              maximum-pool-size: 10
              #池中连接的默认自动提交行为，默认值true
              auto-commit: true
              #池中连接的最长生命周期，0表示无限生命周期，默认1800000(30分钟)
              max-lifetime: 1800000
              #等待来自池的连接的最大毫秒数，默认30000(30秒)
              connection-timeout: 30000
              #连接测试语句
              connection-test-query: SELECT 1
  		username: root
          password: 123456
          url: jdbc:mysql://127.0.0.1:3306/examples?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&serverTimezone=GMT%2B8&useSSL=false&rewriteBatchedStatements=true&useCursorFetch=true
  
  #Mybatis-SpringBoot配置
  mybatis:
      config-location: classpath:config/mybatis/mybatis-config.xml
      mapper-locations: classpath*:com/penglecode/codeforce/mybatistiny/examples/**/*Mapper.xml
      type-aliases-package: com.penglecode.codeforce.mybatistiny.examples
      type-aliases-super-type: com.penglecode.codeforce.common.domain.DomainObject
  ```

- 其他框架集成Mybatis-Tiny

  我不管你其他框架具体是啥，只要sqlSessionFactory实例是通过DecoratedSqlSessionFactoryBuilder弄出来的就行了！！！

## 实现原理

> 区别于别的类似框架（如Mybatis-Plus、Fluent-Mybatis等）的实现方式，它采用一种逆向曲线救国的实现方式，通过较少的代码，极简的扩展实现了类似于他们大多数的功能，完全满足日常开发中对单表的各种CRUD操作。

这是我在上面基本简介中对它的阐述，如果你有一定的基础和兴趣看看源码，再回过头来我想你肯定赞同我上面所言非虚。

- #### 实现要点

  1. `BaseEntityMapper`的定义：

     ```java
     package com.penglecode.codeforce.mybatistiny.mapper;
     
     import com.penglecode.codeforce.common.domain.EntityObject;
     import com.penglecode.codeforce.mybatistiny.dsl.QueryColumns;
     import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
     import com.penglecode.codeforce.mybatistiny.support.EntityMapperHelper;
     import org.apache.ibatis.annotations.Flush;
     import org.apache.ibatis.annotations.Param;
     import org.apache.ibatis.cursor.Cursor;
     import org.apache.ibatis.executor.BatchResult;
     import org.apache.ibatis.session.RowBounds;
     
     import java.io.Serializable;
     import java.util.List;
     import java.util.Map;
     import java.util.function.Consumer;
     
     /**
      * 实体对象(EntityObject)基本CRUD操作的Mybatis-Mapper基类
      *
      * @author pengpeng
      * @version 1.0
      */
     public interface BaseEntityMapper<T extends EntityObject> extends BaseMapper {
     
         /**
          * 这里需要保持与BaseXxxMapper中的@Param参数名一致
          */
         String QUERY_CRITERIA_PARAM_NAME = "criteria";
     
         /**
          * 插入实体
          *
          * @param entity	- 实体对象
          * @return 返回被更新条数
          */
         int insert(T entity);
     
         /**
          * 根据ID更新指定的实体字段
          *
          * @param id			- 主键ID
          * @param columns		- 被更新的字段键值对
          * @return 返回被更新条数
          */
         int updateById(@Param("id") Serializable id, @Param("columns") Map<String,Object> columns);
     
         /**
          * 根据指定的条件更新指定的实体字段
          *
          * @param criteria		- 更新范围条件(不能为null)
          * @param columns		- 被更新的字段键值对
          * @return 返回被更新条数
          */
         int updateByCriteria(@Param("criteria") QueryCriteria<T> criteria, @Param("columns") Map<String,Object> columns);
     
         /**
          * 根据ID删除实体
          *
          * @param id		- 主键ID
          * @return 返回被删除条数
          */
         int deleteById(@Param("id") Serializable id);
     
         /**
          * 根据多个ID批量删除实体
          *
          * @param ids		- 主键ID列表
          * @return 返回被删除条数
          */
         int deleteByIds(@Param("ids") List<? extends Serializable> ids);
     
         /**
          * 根据指定的条件删除实体数据
          *
          * @param criteria	- 删除范围条件(不能为null)
          * @return 返回被删除条数
          */
         int deleteByCriteria(@Param("criteria") QueryCriteria<T> criteria);
     
         /**
          * 根据指定的updateOperation来批量操作(新增、更新、删除)entityList, 例如：
          *
          * List<Account> accountList = ...;
          *
          * 1、批量新增
          * accountMapper.batchUpdate(accountList, accountMapper::insert);
          *
          * 2、根据ID来批量更新
          * accountMapper.batchUpdate(accountList, (account) -> {
          *      Map<String,Object> updateColumns = MapLambdaBuilder.of(account)
          *              .with(Account::getBalance)
          *              .with(Account::getStatus)
          *              .with(Account::getUpdateTime)
          *              .build();
          *      accountMapper.updateById(account.identity(), updateColumns);
          * });
          *
          * 3、根据自定义条件来批量更新
          * accountMapper.batchUpdate(accountList, (account) -> {
          *      Map<String,Object> updateColumns = MapLambdaBuilder.of(account)
          *              .with(Account::getBalance)
          *              .with(Account::getStatus)
          *              .with(Account::getUpdateTime)
          *              .build();
          *      QueryCriteria<Account> queryCriteria = LambdaQueryCriteria.of(account)
          *              .eq(Account::getIdCard);
          *      accountMapper.updateByCriteria(queryCriteria, updateColumns);
          * });
          *
          * 4、根据ID来批量删除
          * (大批量删除走原生JDBC-Batch)
          * accountMapper.batchUpdate(accountList, account -> accountMapper.deleteById(account.identity()));
          *
          * @return
          */
         default int batchUpdate(List<T> entityList, Consumer<T> updateOperation) {
             return EntityMapperHelper.batchUpdateEntityObjects(entityList, updateOperation, this);
         }
     
         /**
          * 根据ID查询单个结果集
          *
          * @param id		- 主键ID
          * @param columns 	- 指定查询返回的列(这里的列指的是实体对象<T>中的字段)，这里使用JAVA可变参数特性的讨巧写法，实际只取columns[0]为参数
          * @return 返回单个结果集
          */
         T selectById(@Param("id") Serializable id, @Param("columns") QueryColumns... columns);
     
         /**
          * 根据条件获取查询单个结果集
          * (注意：如果匹配到多个结果集将报错)
          *
          * @param criteria	- 查询条件(不能为null)
          * @param columns 	- 指定查询返回的列(这里的列指的是实体对象<T>中的字段)，这里使用JAVA可变参数特性的讨巧写法，实际只取columns[0]为参数
          * @return 返回单个结果集
          */
         T selectByCriteria(@Param("criteria") QueryCriteria<T> criteria, @Param("columns") QueryColumns... columns);
     
         /**
          * 根据条件获取查询COUNT
          *
          * @param criteria	- 查询条件(不能为null)
          * @return 返回单个结果集
          */
         int selectCountByCriteria(@Param("criteria") QueryCriteria<T> criteria);
     
         /**
          * 根据多个ID查询结果集
          *
          * @param ids		- 主键ID列表
          * @param columns 	- 指定查询返回的列(这里的列指的是实体对象<T>中的字段)，这里使用JAVA可变参数特性的讨巧写法，实际只取columns[0]为参数
          * @return 返回结果集
          */
         List<T> selectListByIds(@Param("ids") List<? extends Serializable> ids, @Param("columns") QueryColumns... columns);
     
         /**
          * 查询所有结果集(需要在事务中使用，否则查询不到数据)
          *
          * @param columns 	- 指定查询返回的列(这里的列指的是实体对象<T>中的字段)，这里使用JAVA可变参数特性的讨巧写法，实际只取columns[0]为参数
          * @return 返回所有结果集
          */
         Cursor<T> selectAllList(@Param("columns") QueryColumns... columns);
     
         /**
          * 查询所有结果集计数
          * @return 返回所有记录数
          */
         int selectAllCount();
     
         /**
          * 根据条件查询结果集
          *
          * @param criteria	- 查询条件(为null则查询所有)
          * @param columns 	- 指定查询返回的列(这里的列指的是实体对象<T>中的字段)，这里使用JAVA可变参数特性的讨巧写法，实际只取columns[0]为参数
          * @return 返回结果集
          */
         List<T> selectListByCriteria(@Param("criteria") QueryCriteria<T> criteria, @Param("columns") QueryColumns... columns);
     
         /**
          * 根据条件查询结果集(分页)
          *
          * @param criteria	- 查询条件(为null则查询所有)
          * @param rowBounds	- 分页参数
          * @param columns 	- 指定查询返回的列(这里的列指的是实体对象<T>中的字段)，这里使用JAVA可变参数特性的讨巧写法，实际只取columns[0]为参数
          * @return 返回结果集
          */
         List<T> selectPageListByCriteria(@Param("criteria") QueryCriteria<T> criteria, RowBounds rowBounds, @Param("columns") QueryColumns... columns);
     
         /**
          * 根据条件查询结果集计数
          *
          * @param criteria	- 查询条件(为null则查询所有)
          * @return 返回记录数
          */
         int selectPageCountByCriteria(@Param("criteria") QueryCriteria<T> criteria);
     
         /**
          * 刷新(发送)批量语句到数据库Server端执行，并返回结果
          *
          * @return
          */
         @Flush
         List<BatchResult> flushStatements();
     
     }
     
     ```

     其中我对`updateXxx`类方法为啥传一个Map作为update列的设计做个解释：

     - 回归实际项目中，肯定并不是全部列都需要被update，例如create_time列

     - 你想传Entity对象进来作为参数，然后想值不为空(null、空串)的字段都需要进行update，那么问题来了：有个需求就是要将某个字段update为空(null、空串)，请问这时我怎么能两者兼顾？这个是一个矛盾的事情，没办法兼容

     - 所以综上所述，被update的列改成了Map类型的参数，辅以`MapLambdaBuilder`来解决，就像下面这样使用：

       ```java
       Map<String,Object> updateColumns = MapLambdaBuilder.of(productBase)
                           .with(ProductBaseInfo::getProductName)
                           .with(ProductBaseInfo::getRemark)
                           .with(ProductBaseInfo::getAuditStatus)
                           .with(ProductBaseInfo::getOnlineStatus)
                           .withOverride(ProductBaseInfo::getUpdateTime, nowTime)
                           .build();
       productBaseInfoMapper.updateById(productBase.identity(), updateColumns);
       ```

       

  2. Xxx实体对象的通用Mapper接口（`BaseEntityMapper`）对应的`XxxMapper.xml`是通过freemarker模板(`BaseEntityMapper.ftl`)在应用启动时（准确地说是在**第一次**调用`SqlSession#getMapper(Class type)`方法的时候）自动生成代码的（你可以通过打开日志查看生成的`XxxMapper.xml`是啥样子），然后并加载进入Mybatis的`Configuration`中（实际是变成了许多`MappedStatement`对象了）。

     <u>**这一步解决了偷懒省去编写`XxxMapper.xml`的麻烦事。**</u>

  3. 基于Lambda的DSL方式查询实现通过下面几个组合实现的：

     - `com.penglecode.codeforce.mybatistiny.dsl`包下的`QueryCriteria`、`LambdaQueryCriteria`、`NestedLambdaQueryCriteria`等主要实现DSL语法
     - `com/penglecode/codeforce/mybatistiny/mapper/CommonMybatisMapper.xml`则提供了一个全局公共的Mybatis动态条件语句实现，这里我就不贴源码了。
     - DSL这块的实现三言两语也说不清，还是需要看看源码才能知道其中的巧妙之处。

  4. 简而言之：

     - 某Xxx实体的`XxxMapper.xml`是通过freemarker自动生成代码的
     - DSL是运行时动态条件`QueryCriteria`配合`CommonMybatisMapper.xml`实现的

- #### 解惑示例

  下面这个`ProductBaseInfoMapper.xml`就是自动生成的，可以通过打开日志：

  ```xml
  <logger name="com.penglecode.codeforce.mybatistiny" level="DEBUG"/>
  ```

  在启动时进行查看，其中引用了全局的`CommonMybatisMapper.CommonWhereCriteriaClause`作为动态条件的实现

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
  <mapper namespace="com.penglecode.codeforce.mybatistiny.examples.dal.mapper.ProductBaseInfoMapper">
  
      <!-- Auto-Generation Code Start -->
      <!--
          每个继承BaseEntityMapper的Mybatis-Mapper接口都会自动生成对应的如下XML-Mapper
      -->
  
      <resultMap id="SelectBaseResultMap" type="com.penglecode.codeforce.mybatistiny.examples.domain.model.ProductBaseInfo">
          <id column="productId" jdbcType="BIGINT" property="productId"/>
          <result column="productName" jdbcType="VARCHAR" property="productName" />
          <result column="productUrl" jdbcType="VARCHAR" property="productUrl" />
          <result column="productTags" jdbcType="VARCHAR" property="productTags" />
          <result column="productType" jdbcType="INTEGER" property="productType" />
          <result column="auditStatus" jdbcType="INTEGER" property="auditStatus" />
          <result column="onlineStatus" jdbcType="INTEGER" property="onlineStatus" />
          <result column="shopId" jdbcType="BIGINT" property="shopId" />
          <result column="remark" jdbcType="VARCHAR" property="remark" />
          <result column="createTime" jdbcType="VARCHAR" property="createTime" />
          <result column="updateTime" jdbcType="VARCHAR" property="updateTime" />
      </resultMap>
  
      <sql id="SelectBaseColumnsClause">
          <trim suffixOverrides=",">
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'productId')">
                  t.product_id   productId,
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'productName')">
                  t.product_name   productName,
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'productUrl')">
                  t.product_url   productUrl,
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'productTags')">
                  t.product_tags   productTags,
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'productType')">
                  t.product_type   productType,
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'auditStatus')">
                  t.audit_status   auditStatus,
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'onlineStatus')">
                  t.online_status   onlineStatus,
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'shopId')">
                  t.shop_id   shopId,
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'remark')">
                  t.remark   remark,
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'createTime')">
                  DATE_FORMAT(t.create_time, '%Y-%m-%d %T')   createTime,
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'updateTime')">
                  DATE_FORMAT(t.update_time, '%Y-%m-%d %T')   updateTime,
              </if>
          </trim>
      </sql>
  
      <sql id="UpdateDynamicColumnsClause">
          <trim suffixOverrides=",">
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'productName')">
                  t.product_name = #{columns.productName, jdbcType=VARCHAR},
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'productUrl')">
                  t.product_url = #{columns.productUrl, jdbcType=VARCHAR},
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'productTags')">
                  t.product_tags = #{columns.productTags, jdbcType=VARCHAR},
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'productType')">
                  t.product_type = #{columns.productType, jdbcType=INTEGER},
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'auditStatus')">
                  t.audit_status = #{columns.auditStatus, jdbcType=INTEGER},
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'onlineStatus')">
                  t.online_status = #{columns.onlineStatus, jdbcType=INTEGER},
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'remark')">
                  t.remark = #{columns.remark, jdbcType=VARCHAR},
              </if>
              <if test="@com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper@containsColumn(columns, 'updateTime')">
                  t.update_time = #{columns.updateTime, jdbcType=VARCHAR},
              </if>
          </trim>
      </sql>
  
      <insert id="insert" keyProperty="productId" parameterType="ProductBaseInfo" statementType="PREPARED" useGeneratedKeys="true">
          INSERT INTO t_product_base_info(
              product_id,
              product_name,
              product_url,
              product_tags,
              product_type,
              audit_status,
              online_status,
              shop_id,
              remark,
              create_time,
              update_time
          ) VALUES (
              #{productId, jdbcType=BIGINT},
              #{productName, jdbcType=VARCHAR},
              #{productUrl, jdbcType=VARCHAR},
              #{productTags, jdbcType=VARCHAR},
              #{productType, jdbcType=INTEGER},
              #{auditStatus, jdbcType=INTEGER},
              #{onlineStatus, jdbcType=INTEGER},
              #{shopId, jdbcType=BIGINT},
              #{remark, jdbcType=VARCHAR},
              #{createTime, jdbcType=VARCHAR},
              #{updateTime, jdbcType=VARCHAR}
          )
      </insert>
  
      <update id="updateById" parameterType="java.util.Map" statementType="PREPARED">
          UPDATE t_product_base_info t
             SET <include refid="UpdateDynamicColumnsClause"/>
           WHERE t.product_id = #{id, jdbcType=BIGINT}
      </update>
  
      <update id="updateByCriteria" parameterType="java.util.Map" statementType="PREPARED">
          UPDATE t_product_base_info t
             SET <include refid="UpdateDynamicColumnsClause"/>
          <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
      </update>
  
      <delete id="deleteById" parameterType="java.util.Map" statementType="PREPARED">
          DELETE t FROM t_product_base_info t
           WHERE t.product_id = #{id, jdbcType=BIGINT}
      </delete>
  
      <delete id="deleteByIds" parameterType="java.util.Map" statementType="PREPARED">
          DELETE t FROM t_product_base_info t
           WHERE t.product_id in
          <foreach collection="ids" index="index" item="id" open="(" separator="," close=")">
              #{id, jdbcType=BIGINT}
          </foreach>
      </delete>
  
      <delete id="deleteByCriteria" parameterType="java.util.Map" statementType="PREPARED">
          DELETE t FROM t_product_base_info t
          <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
      </delete>
  
      <select id="selectById" parameterType="java.util.Map" resultMap="SelectBaseResultMap" statementType="PREPARED">
          SELECT <include refid="SelectBaseColumnsClause"/>
            FROM t_product_base_info t
           WHERE t.product_id = #{id, jdbcType=BIGINT}
      </select>
  
      <select id="selectByCriteria" parameterType="java.util.Map" resultMap="SelectBaseResultMap" statementType="PREPARED">
          SELECT <include refid="SelectBaseColumnsClause"/>
            FROM t_product_base_info t
          <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
      </select>
  
      <select id="selectCountByCriteria" parameterType="java.util.Map" resultType="java.lang.Integer" statementType="PREPARED">
          SELECT COUNT(*)
            FROM t_product_base_info t
          <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
      </select>
  
      <select id="selectListByIds" parameterType="java.util.Map" resultMap="SelectBaseResultMap" statementType="PREPARED">
          SELECT <include refid="SelectBaseColumnsClause"/>
            FROM t_product_base_info t
           WHERE t.product_id in
          <foreach collection="ids" index="index" item="id" open="(" separator="," close=")">
              #{id, jdbcType=BIGINT}
          </foreach>
      </select>
  
      <select id="selectAllList" parameterType="java.util.Map" resultMap="SelectBaseResultMap" resultSetType="FORWARD_ONLY" statementType="PREPARED">
          SELECT <include refid="SelectBaseColumnsClause"/>
            FROM t_product_base_info t
      </select>
  
      <select id="selectAllCount" parameterType="java.util.Map" resultType="java.lang.Integer" statementType="PREPARED">
          SELECT COUNT(*) FROM t_product_base_info t
      </select>
  
      <select id="selectListByCriteria" parameterType="java.util.Map" resultMap="SelectBaseResultMap" statementType="PREPARED">
          SELECT <include refid="SelectBaseColumnsClause"/>
            FROM t_product_base_info t
          <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
          <include refid="CommonMybatisMapper.CommonOrderByCriteriaClause"/>
      </select>
  
      <select id="selectPageListByCriteria" parameterType="java.util.Map" resultMap="SelectBaseResultMap" statementType="PREPARED">
          SELECT <include refid="SelectBaseColumnsClause"/>
            FROM t_product_base_info t
          <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
          <include refid="CommonMybatisMapper.CommonOrderByCriteriaClause"/>
      </select>
  
      <select id="selectPageCountByCriteria" parameterType="java.util.Map" resultType="java.lang.Integer" statementType="PREPARED">
          SELECT COUNT(*)
            FROM t_product_base_info t
          <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
      </select>
      <!-- Auto-Generation Code End -->
  
  </mapper>
  ```

- 
