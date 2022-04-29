package com.penglecode.codeforce.mybatistiny.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link EntityMeta}的静态工厂
 *
 * @author pengpeng
 * @version 1.0
 */
public class EntityMetaFactory {

    private static final ConcurrentMap<Class<?>, EntityMeta> ENTITY_META_CACHE = new ConcurrentHashMap<>(256);

    public static EntityMeta getEntityMeta(Class<?> entityClass) {
        return ENTITY_META_CACHE.computeIfAbsent(entityClass, EntityMeta::new);
    }

}
