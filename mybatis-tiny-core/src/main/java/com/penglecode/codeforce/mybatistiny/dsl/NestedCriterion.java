package com.penglecode.codeforce.mybatistiny.dsl;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 嵌套逻辑查询条件
 * 注意：本Lambda查询条件仅支持一层逻辑嵌套查询,例如：
 *  1、WHERE (type = 1 AND age < 12) OR status = 'N' OR (type = 3 AND age > 22);
 *  2、WHERE (type = 1 OR status = 0) AND name like '张%';
 *
 * @author pengpeng
 * @version 1.0
 */
public class NestedCriterion extends Criterion {

    private static final long serialVersionUID = 1L;

    private final Set<ColumnCriterion> criteria2;

    protected NestedCriterion(String logicOp) {
        super(true, logicOp);
        this.criteria2 = new LinkedHashSet<>();
    }

    public boolean addCriterion(ColumnCriterion criterion) {
        return criteria2.add(criterion);
    }

    public Set<ColumnCriterion> getCriteria2() {
        return criteria2;
    }
}
