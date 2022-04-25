package com.penglecode.codeforce.mybatistiny.mapper;

/**
 * Mybatis-Mapper超级标记接口
 * 改超级接口可以作为@MapperScan#markerInterface配置的候选值，因为实际项目中并不是每个XxxMapper都有一个实体(一个表)与之对应，比如专门用于统计查询的Mapper接口
 *
 * @author pengpeng
 * @version 1.0
 */
public interface BaseMapper {
}
