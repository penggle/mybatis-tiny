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
