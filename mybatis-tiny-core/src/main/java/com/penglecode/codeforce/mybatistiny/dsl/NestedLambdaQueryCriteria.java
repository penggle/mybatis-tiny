package com.penglecode.codeforce.mybatistiny.dsl;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.common.domain.OrderBy;
import com.penglecode.codeforce.common.support.BeanIntrospector;
import com.penglecode.codeforce.common.support.SerializableFunction;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * 基于Java8 Lambda表达式的DSL动态查询条件
 * 其输出的实际SQL语句中WHERE条件的顺序位置与DSL动态查询条件的顺序位置完全一致
 *
 * NestedLambdaQueryCriteria(基类)与LambdaQueryCriteria完全可以合成一个类，之所以这样设计的目的是为了在语法层面实现：本Lambda查询条件仅支持一层逻辑嵌套查询
 *
 * 注意：本Lambda查询条件仅支持一层逻辑嵌套查询,例如：
 *  1、WHERE (type = 1 AND age < 12) //仅支持一层逻辑嵌套查询
 *       OR status = 'N'
 *       OR (type = 3 AND age > 22); //仅支持一层逻辑嵌套查询
 *  2、WHERE (type = 1 OR status = 0)  //仅支持一层逻辑嵌套查询
 *      AND name like '张%';
 *
 * @param <E> Example
 * @author pengpeng
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public abstract class NestedLambdaQueryCriteria<E extends EntityObject> extends QueryCriteria<E> {

    private static final long serialVersionUID = 1L;

    /**
     * 当前逻辑运算符，默认为AND
     * 注意：这个值随着DSL语句的执行随时在变
     */
    private LogicOperator currentLogicOperator = LogicOperator.AND;

    NestedLambdaQueryCriteria(E example) {
        super(example);
    }

    public NestedLambdaQueryCriteria<E> and() {
        currentLogicOperator = LogicOperator.AND;
        return this;
    }

    public NestedLambdaQueryCriteria<E> or() {
        currentLogicOperator = LogicOperator.OR;
        return this;
    }

    public <C> NestedLambdaQueryCriteria<E> isNull(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.IS_NULL, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> isNotNull(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.IS_NOT_NULL, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> eq(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.EQ, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> ne(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.NE, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> like(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.LIKE, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> likeLeft(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.LIKE_LEFT, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> likeRight(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.LIKE_RIGHT, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> notLike(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.NOT_LIKE, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> notLikeLeft(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.NOT_LIKE_LEFT, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> notLikeRight(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.NOT_LIKE_RIGHT, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> gt(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.GT, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> lt(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.LT, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> gte(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.GTE, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> lte(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.LTE, column, overrideValue);
    }
    
    public <C> NestedLambdaQueryCriteria<E> between(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.BETWEEN, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> notBetween(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.NOT_BETWEEN, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> in(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.IN, column, overrideValue);
    }

    public <C> NestedLambdaQueryCriteria<E> notIn(SerializableFunction<E,C> column, C... overrideValue) {
        return addColumnCriterion(ConditionOperator.NOT_IN, column, overrideValue);
    }

    @Override
    protected NestedLambdaQueryCriteria<E> dynamic(boolean frozenCriteria) {
        return (NestedLambdaQueryCriteria<E>) super.dynamic(frozenCriteria);
    }

    @Override
    protected NestedLambdaQueryCriteria<E> orderBy(OrderBy... orderBys) {
        return (NestedLambdaQueryCriteria<E>) super.orderBy(orderBys);
    }

    @Override
    protected NestedLambdaQueryCriteria<E> orderBy(List<OrderBy> orderBys) {
        return (NestedLambdaQueryCriteria<E>) super.orderBy(orderBys);
    }

    @Override
    protected NestedLambdaQueryCriteria<E> limit(int limit) {
        return (NestedLambdaQueryCriteria<E>) super.limit(limit);
    }

    protected <C> NestedLambdaQueryCriteria<E> addColumnCriterion(ConditionOperator operator, SerializableFunction<E,C> column, C... overrideValue) {
        checkCriteriaFrozen();
        Assert.notNull(column, "Parameter 'column' can not be null!");
        ImmutablePair<String,Object> conditionAndValue = introspectConditionStatement(operator.getType(), column, overrideValue);
        addCriterion(new ColumnCriterion(currentLogicOperator.toString(), TABLE_ALIAS_NAME + "." + conditionAndValue.getLeft() + " " + operator.getOperator(), operator.getType(), conditionAndValue.getRight(), operator.opValue(conditionAndValue.getRight())));
        return this;
    }

    protected NestedLambdaQueryCriteria<E> addNestedCriterion(Consumer<NestedLambdaQueryCriteria<E>> consumer) {
        checkCriteriaFrozen();
        NestedCriterion nestedCriterion = new NestedCriterion(currentLogicOperator.toString());
        addCriterion(nestedCriterion);
        NestedLambdaQueryCriteria<E> nestedCriteria = new LambdaQueryCriteria<>(getExample());
        consumer.accept(nestedCriteria);
        nestedCriteria.getCriteria1().forEach(criterion2 -> {
            /*
             * 本Lambda条件查询仅支持1层逻辑嵌套查询,例如：
             * WHERE (type = 1 AND age = 3) OR status = 'N';
             * WHERE (type = 1 OR status = 0) AND name like '张%';
             */
            if(criterion2 instanceof ColumnCriterion) {
                nestedCriterion.addCriterion((ColumnCriterion) criterion2);
            }
        });
        return this;
    }

    /**
     * 自省出条件查询语句
     * @param operatorType     - 运算符类型：0-无参数条件语句(例如is null、is not null),
     *                                    1-仅一个参数的语句(例如 user_name = 'aaa'),
     *                                    2-仅两个参数的语句(birthday between '1990-01-01' and '2000-12-31'),
     *                                    3-代表in语句
     * @param column            - example中对应字段的getter方法引用
     * @param overrideValue     - 如果不为null，则覆盖example中对应字段的值
     * @param <C>               - Condtion-Value
     * @return
     */
    protected <C> ImmutablePair<String,Object> introspectConditionStatement(int operatorType, SerializableFunction<E,C> column, C... overrideValue) {
        Field field = BeanIntrospector.introspectField(column);
        String columnName = getColumnName(field);
        C fieldValue = column.apply(getExample());
        Object preferValue; //优先使用overrideValue作为最终的SQL查询条件的值
        switch (operatorType) {
            case 0 : //没有参数的条件,例如:is null，is not null
                preferValue = null;
                break;
            case 1 : //具有一个参数的条件,例如: user_name = 'aaa'
                preferValue = (overrideValue != null && overrideValue.length == 1) ? overrideValue[0] : fieldValue;
                break;
            case 2 : //具有两个参数的条件,例如: birthday between '1990-01-01' and '2000-12-31'
                preferValue = (overrideValue != null && overrideValue.length == 2) ? overrideValue : new Object[]{fieldValue, fieldValue};
                break;
            default : //in条件,例如: type in (1, 2, 3, 4)
                if(overrideValue == null || overrideValue.length == 0) {
                    preferValue = new Object[0];
                } else if(overrideValue.length == 1) {
                    if(overrideValue[0] instanceof Collection) { //特殊处理in语句中传Collection类型的情况
                        preferValue = ((Collection<?>) overrideValue[0]).toArray();
                    } else if(overrideValue[0] == null) {
                        preferValue = new Object[0];
                    } else {
                        preferValue = overrideValue;
                    }
                } else {
                    preferValue = overrideValue;
                }
        }
        return new ImmutablePair<>(columnName, preferValue);
    }

}
