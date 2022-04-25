package com.penglecode.codeforce.mybatistiny.dsl;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.common.domain.OrderBy;
import com.penglecode.codeforce.common.util.ClassUtils;
import com.penglecode.codeforce.mybatistiny.annotations.Table;
import com.penglecode.codeforce.mybatistiny.core.EntityMeta;
import com.penglecode.codeforce.mybatistiny.core.EntityMetaFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 实体对象的查询、更新、删除等SQL查询条件
 *
 * @author pengpeng
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public abstract class QueryCriteria<E extends EntityObject> {

    private static final long serialVersionUID = 1L;

    public static final String TABLE_ALIAS_NAME = "t";

    /**
     * 查询条件绑定的example类型
     */
    private final Class<E> exampleClass;

    /**
     * 查询条件绑定的实体类型
     */
    private final Class<E> entityClass;

    /**
     * 查询条件绑定的实体元数据
     */
    private final EntityMeta<E> entityMeta;

    /**
     * 查询条件绑定的example，它是一个对应数据库表的实体数据模型
     */
    private final E example;

    /**
     * 最外层(第一层)查询条件列表
     */
    private final Set<Criterion> criteria1;

    /**
     * 排序对象
     */
    private final List<OrderBy> orderBys;

    /**
     * 查询限制返回行数
     */
    private Integer limit;

    /**
     * 冻结条件
     * 例如在调用dynamic()方法之后就需要冻结条件
     */
    private boolean frozenCriteria;

    QueryCriteria(E example) {
        Assert.notNull(example, "Parameter 'example' can not be null!");
        this.example = example;
        this.exampleClass = resolveExampleClass(example);
        this.entityClass = resolveEntityClass(this.exampleClass);
        this.entityMeta = EntityMetaFactory.getEntityMeta(this.entityClass);
        this.criteria1 = new LinkedHashSet<>();
        this.orderBys = new ArrayList<>();
    }

    /**
     * 动态过滤查询条件，即过滤掉value为空值(null,空串,空数组,空集合)的查询条件,
     * 即实现如下的动态语句：
     * &lt;if test="example.xxx != null && example.xxx != ''"&gt;
     *     AND|OR xxx = #{example.xxx}
     * &lt;/if&gt;
     * @param frozenCriteria - 是否冻结查询条件?为true则在调用dynamic()方法之后就不能再添加新的查询条件了，否则报错
     * @return
     */
    protected QueryCriteria<E> dynamic(boolean frozenCriteria) {
        trimEmptyCriterion(criteria1);
        this.frozenCriteria = frozenCriteria;
        return this;
    }

    /**
     * 应用指定的OrderBy
     * @param orderBys
     * @return
     */
    protected QueryCriteria<E> orderBy(OrderBy... orderBys) {
        Assert.notEmpty(orderBys, "Parameter 'orderBys' can not be null!");
        return orderBy(Arrays.asList(orderBys));
    }

    /**
     * 应用指定的OrderBy
     * @param orderBys
     * @return
     */
    protected QueryCriteria<E> orderBy(List<OrderBy> orderBys) {
        if(!CollectionUtils.isEmpty(orderBys)) {
            for(OrderBy orderBy : orderBys) {
                Optional.ofNullable(checkOrderBy(orderBy)).ifPresent(this.orderBys::add);
            }
        }
        return this;
    }

    /**
     * 应用指定的查询限制返回行数
     * 需要底层数据库支持：
     *  1、对于MySQL则使用limit字句实现
     *  2、对于Oracle则使用rownum隐藏列来实现
     *  n、对于不支持的数据库不会报错，即没有任何效果
     * 注意如果当前是分页查询，则忽略该limit设置
     * @param limit - 该值必须大于0，否则忽略
     * @return
     */
    protected QueryCriteria<E> limit(int limit) {
        Assert.isTrue(limit > 0, "Parameter 'limit' must be > 0!");
        this.limit = limit;
        return this;
    }

    /**
     * 解析example的类型
     *
     * @param example
     * @return
     */
    protected Class<E> resolveExampleClass(E example) {
        Class<E> exampleClass;
        if(example != null) {
            exampleClass = (Class<E>) example.getClass();
        } else {
            exampleClass = ClassUtils.getSuperGenericType(getClass(), QueryCriteria.class, 0);
        }
        Assert.notNull(exampleClass, "Parameter 'exampleClass' can not be null!");
        return exampleClass;
    }

    /**
     * 解析实体对象的类型
     *
     * @param exampleClass
     * @return
     */
    protected Class<E> resolveEntityClass(Class<E> exampleClass) {
        Class<E> entityClass = null;
        while(exampleClass != null && !exampleClass.isInterface()) {
            if(exampleClass.getAnnotation(Table.class) != null) {
                entityClass = exampleClass;
                break;
            }
            exampleClass = (Class<E>) exampleClass.getSuperclass();
        }
        Assert.notNull(entityClass, String.format("The example class[%s] is not an EntityObject, must be annotated with @%s", exampleClass, Table.class.getName()));
        return entityClass;
    }

    /**
     * 检测条件是否已经冻结
     */
    protected void checkCriteriaFrozen() {
        Assert.state(!isFrozenCriteria(), "All query criteria is frozen, can not add new query criteria!");
    }

    /**
     * 递归剔除value为空的条件
     * @param criteria
     */
    protected void trimEmptyCriterion(Set<? extends Criterion> criteria) {
        for(Iterator<? extends Criterion> iterator = criteria.iterator(); iterator.hasNext();) {
            Criterion criterion = iterator.next();
            if(criterion instanceof NestedCriterion) {
                Set<? extends Criterion> nestedCriteria = ((NestedCriterion) criterion).getCriteria2();
                trimEmptyCriterion(nestedCriteria); //递归剔除
                if(nestedCriteria.isEmpty()) { //通过对嵌套逻辑条件的过滤,如果当前嵌套逻辑组为空,则删除之
                    iterator.remove();
                }
            } else if(criterion instanceof ColumnCriterion) {
                Object value = ((ColumnCriterion) criterion).getRawValue();
                if(ObjectUtils.isEmpty(value)) { //过滤掉值为空值(null,空串,空数组,空集合)的条件
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 检查OrderBy
     *
     * @param orderBy
     */
    protected OrderBy checkOrderBy(OrderBy orderBy) {
        //根据客户端传来的排序字段名找实体字段元数据
        EntityMeta.EntityField orderByField = entityMeta.getFieldNameKeyedFields().get(orderBy.getProperty());
        if(orderByField == null) {
            orderByField = entityMeta.getColumnNameKeyedFields().get(orderBy.getProperty());
        }
        if(orderByField != null) { //如果排序列确实存在
            //考虑到在调用BaseEntityMapper中的查询方法时：指定了select列，指定了OrderBy排序字段，但是排序字段不在select列中的情况需要特殊处理，即处统一使用数据库列名来作为排序字段
            orderBy.setProperty(TABLE_ALIAS_NAME + "." + orderByField.getColumnName());
            return orderBy;
        }
        //字段不存在则返回null，即在程序层面规避SQL注入
        return null;
    }

    protected void addCriterion(Criterion abstractCriterion) {
        criteria1.add(abstractCriterion);
    }

    public E getExample() {
        return example;
    }

    public Class<E> getExampleClass() {
        return exampleClass;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    protected Set<Criterion> getCriteria1() {
        return criteria1;
    }

    public List<OrderBy> getOrderBys() {
        return orderBys;
    }

    public Integer getLimit() {
        return limit;
    }

    protected boolean isFrozenCriteria() {
        return frozenCriteria;
    }

    public String getColumnName(Field field) {
        return entityMeta.getFieldNameKeyedFields().get(field.getName()).getColumnName();
    }

}
