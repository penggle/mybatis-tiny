package com.penglecode.codeforce.mybatistiny.core;

import com.penglecode.codeforce.common.domain.EntityObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link EntityMeta}的静态工厂
 *
 * @author pengpeng
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class EntityMetaFactory {

    private static final ConcurrentMap<Class<? extends EntityObject>, EntityMeta<? extends EntityObject>> ENTITY_META_CACHE = new ConcurrentHashMap<>(256);

    public static <E extends EntityObject> EntityMeta<E> getEntityMeta(Class<E> entityClass) {
        return (EntityMeta<E>) ENTITY_META_CACHE.computeIfAbsent(entityClass, EntityMeta::new);
    }

}
