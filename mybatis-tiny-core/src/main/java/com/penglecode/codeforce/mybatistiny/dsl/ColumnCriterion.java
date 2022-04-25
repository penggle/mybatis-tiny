package com.penglecode.codeforce.mybatistiny.dsl;

/**
 * 最底层的基于具体数据库表中列的查询条件
 *
 * @author pengpeng
 * @version 1.0
 */
public class ColumnCriterion extends Criterion {

    private static final long serialVersionUID = 1L;

    /**
     * 查询条件，例如：is null、name = 、age >、between、in
     */
    private final String condition;

    /**
     * 运算符类型：0-无参数条件语句(例如is null、is not null),
     *           1-仅一个参数的语句(例如 user_name = 'aaa'),
     *           2-仅两个参数的语句(birthday between '1990-01-01' and '2000-12-31'),
     *           3-代表in语句
     */
    private final int type;

    /**
     * 参与条件运算所需的原始值
     */
    private final Object rawValue;

    /**
     * 条件运算符所需的值
     * 单值或数组
     */
    private final Object opValue;

    protected ColumnCriterion(String logicOp, String condition, int type, Object rawValue, Object opValue) {
        super(false, logicOp);
        this.condition = condition;
        this.type = type;
        this.rawValue = rawValue;
        this.opValue = opValue;
    }

    public String getCondition() {
        return condition;
    }

    public int getType() {
        return type;
    }

    public Object getRawValue() {
        return rawValue;
    }

    public Object getOpValue() {
        return opValue;
    }

}
