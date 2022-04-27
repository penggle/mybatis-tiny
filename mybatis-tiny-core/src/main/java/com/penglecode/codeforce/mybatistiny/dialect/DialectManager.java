package com.penglecode.codeforce.mybatistiny.dialect;

import org.springframework.util.Assert;

import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 数据库方言管理器，类似于{@link DriverManager}
 *
 * @author pengpeng
 * @version 1.0
 */
public final class DialectManager {

    /**
     * 已注册的数据库方言
     */
    private static final Map<String, Dialect> REGISTERED_DIALECTS = new HashMap<>();

    private DialectManager() {}

    /**
     * 注册额外的数据库方言，方言扩展入口
     *
     * @param databaseId    - 数据库ID，例如mysql,oracle等，不区分大小写
     * @param dialect       - 方言实现
     */
    public synchronized static void regDialect(String databaseId, Dialect dialect) {
        REGISTERED_DIALECTS.put(databaseId.toUpperCase(), dialect);
    }

    /**
     * 根据databaseId获取数据库方言
     *
     * @param databaseId    - 数据库ID，例如mysql,oracle等，不区分大小写
     * @return 返回数据库方言
     */
    public static Dialect getDialect(String databaseId) {
        Dialect dialect = REGISTERED_DIALECTS.get(databaseId.toUpperCase());
        Assert.notNull(dialect, String.format("No suitable DatabaseDialect found for databaseId(%s)!", databaseId));
        return dialect;
    }

    /**
     * 判断指定的databaseId对应的方言是否已经注册了
     *
     * @param databaseId    - 数据库ID，例如mysql,oracle等，不区分大小写
     * @return 是否存在指定数据库的方言
     */
    public static boolean hasDialect(String databaseId) {
        return REGISTERED_DIALECTS.containsKey(databaseId.toUpperCase());
    }

}
