package com.penglecode.codeforce.mybatistiny.dsl;

/**
 * SQL条件运算符
 *
 * @author pengpeng
 * @version 1.0
 */
enum ConditionOperator {
    IS_NULL("is null", 0),
    IS_NOT_NULL("is not null", 0),
    EQ("=", 1),
    NE("!=", 1),
    LIKE("like", 1) {
        @Override
        public Object opValue(Object value) {
            return "%" + value + "%";
        }
    },
    LIKE_LEFT("like", 1) {
        @Override
        public Object opValue(Object value) {
            return "%" + value;
        }
    },
    LIKE_RIGHT("like", 1) {
        @Override
        public Object opValue(Object value) {
            return value + "%";
        }
    },
    NOT_LIKE("not like", 1) {
        @Override
        public Object opValue(Object value) {
            return "%" + value + "%";
        }
    },
    NOT_LIKE_LEFT("not like", 1) {
        @Override
        public Object opValue(Object value) {
            return "%" + value;
        }
    },
    NOT_LIKE_RIGHT("not like", 1) {
        @Override
        public Object opValue(Object value) {
            return value + "%";
        }
    },
    GT(">", 1),
    LT("<", 1),
    GTE(">=", 1),
    LTE("<=", 1),
    BETWEEN("between", 2),
    NOT_BETWEEN("not between", 2),
    IN("in", 3),
    NOT_IN("not in", 3);

    /**
     * SQL条件运算符
     */
    private final String operator;

    /**
     * 运算符类型：0-无参数条件语句(例如is null、is not null),
     *           1-仅一个参数的语句(例如 user_name = 'aaa'),
     *           2-仅两个参数的语句(birthday between '1990-01-01' and '2000-12-31'),
     *           3-代表in语句
     */
    private final int type;

    ConditionOperator(String operator, int type) {
        this.operator = operator;
        this.type = type;
    }

    public String getOperator() {
        return operator;
    }

    public int getType() {
        return type;
    }

    /**
     * 处理条件运算所需的值
     * @param value
     * @return
     */
    public Object opValue(Object value) {
        return value;
    }

}
