package com.penglecode.codeforce.mybatistiny.dsl;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.common.domain.OrderBy;
import com.penglecode.codeforce.common.support.BeanIntrospector;
import com.penglecode.codeforce.common.support.SerializableFunction;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 基于Java8 Lambda表达式的DSL动态查询条件
 * 其输出的实际SQL语句中WHERE条件的顺序位置与DSL动态查询条件的顺序位置完全一致
 *
 * NestedLambdaQueryCriteria(基类)与LambdaQueryCriteria完全可以合成一个类，之所以这样设计的目的是为了在语法层面实现：本Lambda查询条件仅支持一层逻辑嵌套查询
 *
 * 注意：
 *  1、本Lambda查询条件仅支持一层逻辑嵌套查询,例如：
 *      1)、WHERE (type = 1 AND age < 12) //仅支持一层逻辑嵌套查询
 *            OR status = 'N'
 *            OR (type = 3 AND age > 22); //仅支持一层逻辑嵌套查询
 *      2)、WHERE (type = 1 OR status = 0)  //仅支持一层逻辑嵌套查询
 *           AND name like '张%';
 *
 *  2、本Lambda中ORDER BY排序使用的字段是JavaBean中的字段名而不是数据库的字段名，前提是约定自动生成的Mapper.xml中需要对SELECT列进行重命名(重命名为JavaBean中的字段名)
 *
 * @param <E> Example
 * @author pengpeng
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class LambdaQueryCriteria<E extends EntityObject> extends NestedLambdaQueryCriteria<E> {

    private static final long serialVersionUID = 1L;

    public LambdaQueryCriteria(E example) {
        super(example);
    }

    /**
     * 使用给定的Example样例来构造动态查询条件
     * @param example   - Example样例
     * @param <E>
     * @return
     */
    public static <E extends EntityObject> LambdaQueryCriteria<E> of(E example) {
        return new LambdaQueryCriteria<>(example);
    }

    /**
     * 使用给定的Example实例来构造动态查询条件
     * @param exampleSupplier   - Example样例的Supplier(例如XxxEntity::new)
     * @param <E>
     * @return
     */
    public static <E extends EntityObject> LambdaQueryCriteria<E> ofSupplier(Supplier<E> exampleSupplier) {
        return new LambdaQueryCriteria<>(exampleSupplier.get());
    }

    /**
     * 逻辑嵌套查询条件(仅支持一层逻辑嵌套查询!!!)
     * @param consumer
     * @return
     */
    public LambdaQueryCriteria<E> and(Consumer<NestedLambdaQueryCriteria<E>> consumer) {
        return (LambdaQueryCriteria<E>) and().addNestedCriterion(consumer);
    }

    /**
     * 逻辑嵌套查询条件(仅支持一层逻辑嵌套查询!!!)
     * @param consumer
     * @return
     */
    public LambdaQueryCriteria<E> or(Consumer<NestedLambdaQueryCriteria<E>> consumer) {
        return (LambdaQueryCriteria<E>) or().addNestedCriterion(consumer);
    }

    /**
     * 排序
     * (注意：使用实体Java类中的字段作为排序SQL中的字段)
     * @param column
     * @param <C>
     * @return
     */
    public <C> LambdaQueryCriteria<E> asc(SerializableFunction<E,C> column) {
        return orderBy(column, OrderBy.Direction.ASC);
    }

    /**
     * 排序
     * (注意：使用实体Java类中的字段作为排序SQL中的字段)
     * @param column
     * @param <C>
     * @return
     */
    public <C> LambdaQueryCriteria<E> desc(SerializableFunction<E,C> column) {
        return orderBy(column, OrderBy.Direction.DESC);
    }

    /**
     * 排序
     * (注意：使用实体Java类中的字段作为排序SQL中的字段)
     * @param property  - 实体Java类中的字段名或者SELECT列的index值
     * @return
     */
    public LambdaQueryCriteria<E> asc(String property) {
        return orderBy(property, OrderBy.Direction.ASC);
    }

    /**
     * 排序
     * (注意：使用实体Java类中的字段作为排序SQL中的字段)
     * @param property  - 实体Java类中的字段名或者SELECT列的index值
     * @return
     */
    public LambdaQueryCriteria<E> desc(String property) {
        return orderBy(property, OrderBy.Direction.DESC);
    }

    @Override
    public LambdaQueryCriteria<E> orderBy(OrderBy... orderBys) {
        return (LambdaQueryCriteria<E>) super.orderBy(orderBys);
    }

    @Override
    public LambdaQueryCriteria<E> orderBy(List<OrderBy> orderBys) {
        return (LambdaQueryCriteria<E>) super.orderBy(orderBys);
    }

    @Override
    public LambdaQueryCriteria<E> limit(int limit) {
        return (LambdaQueryCriteria<E>) super.limit(limit);
    }

    /**
     * 排序
     * (注意：使用实体Java类中的字段作为排序SQL中的字段)
     * @param property  - 实体Java类中的字段名或者SELECT列的index值
     * @param direction
     * @return
     */
    protected LambdaQueryCriteria<E> orderBy(String property, OrderBy.Direction direction) {
        Assert.hasText(property, "Parameter 'property' can not be null!");
        Assert.notNull(direction, "Parameter 'direction' can not be null!");
        getOrderBys().add(OrderBy.by(property, direction));
        return this;
    }

    /**
     * 排序
     * (注意：使用实体Java类中的字段作为排序SQL中的字段)
     * @param column
     * @param direction
     * @param <C>
     * @return
     */
    protected  <C> LambdaQueryCriteria<E> orderBy(SerializableFunction<E,C> column, OrderBy.Direction direction) {
        Assert.notNull(column, "Parameter 'column' can not be null!");
        Assert.notNull(direction, "Parameter 'direction' can not be null!");
        Field field = BeanIntrospector.introspectField(column);
        getOrderBys().add(OrderBy.by(field.getName(), direction));
        return this;
    }

    @Override
    public LambdaQueryCriteria<E> and() {
        return (LambdaQueryCriteria<E>) super.and();
    }

    @Override
    public LambdaQueryCriteria<E> or() {
        return (LambdaQueryCriteria<E>) super.or();
    }

    @Override
    public <C> LambdaQueryCriteria<E> isNull(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.isNull(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> isNotNull(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.isNotNull(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> eq(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.eq(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> ne(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.ne(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> like(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.like(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> likeLeft(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.likeLeft(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> likeRight(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.likeRight(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> notLike(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.notLike(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> notLikeLeft(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.notLikeLeft(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> notLikeRight(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.notLikeRight(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> gt(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.gt(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> lt(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.lt(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> gte(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.gte(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> lte(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.lte(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> between(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.between(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> notBetween(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.notBetween(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> in(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.in(getterMethodRef, overrideValue);
    }

    @Override
    public <C> LambdaQueryCriteria<E> notIn(SerializableFunction<E,C> getterMethodRef, C... overrideValue) {
        return (LambdaQueryCriteria<E>) super.notIn(getterMethodRef, overrideValue);
    }

    @Override
    public LambdaQueryCriteria<E> dynamic(boolean frozenCriteria) {
        return (LambdaQueryCriteria<E>) super.dynamic(frozenCriteria);
    }

}
