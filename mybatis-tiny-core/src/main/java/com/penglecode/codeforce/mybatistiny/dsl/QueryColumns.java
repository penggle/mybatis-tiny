package com.penglecode.codeforce.mybatistiny.dsl;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.common.support.BeanIntrospector;
import com.penglecode.codeforce.common.support.SerializableFunction;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 用于指定查询语句返回的列(这里的column指定就是领域对象字段名，例如userName而不是user_name)，例如：
 *
 *      1、只选择特定的列：userMapper.selectModelById(id, new QueryColumns<User>(User::getUserId, User::getUserName));
 *      2、根据条件选择列：orderMapper.selectModelById(id, new QueryColumns<Order>(column -> column.startWith("product")));
 *
 * 这个类的设计并不完美，即它不能增加与QueryCriteria<T>一样的泛型，
 * 因为加了泛型后，与{@link BaseEntityMapper}中的selectXxx(.., @Param("columns") QueryColumns... columns)方法后面的可变参数
 * 就形成了全局的[Unchecked generics array creation for varargs parameter]问题
 *
 * @author pengpeng
 * @version 1.0
 */
public class QueryColumns {

    /**
     * 被选择的列对应的领域对象字段名
     */
    private final Set<String> columns;

    /**
     * 根据条件选择列
     */
    private final Predicate<String> predicate;

    @SafeVarargs
    public <T extends EntityObject> QueryColumns(SerializableFunction<T,?>... columns) {
        Set<String> propertyNames = new LinkedHashSet<>();
        if(columns != null && columns.length > 0) {
            for(SerializableFunction<T,?> selectColumn : columns) {
                Field selectField = BeanIntrospector.introspectField(selectColumn);
                propertyNames.add(selectField.getName());
            }
        }
        this.columns = Collections.unmodifiableSet(propertyNames);
        this.predicate = null;
    }

    public QueryColumns(Predicate<String> selectPredicate) {
        this.predicate = selectPredicate;
        this.columns = Collections.emptySet();
    }

    public Set<String> getColumns() {
        return columns;
    }

    public Predicate<String> getPredicate() {
        return predicate;
    }

}
