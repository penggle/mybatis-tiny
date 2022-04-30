package com.penglecode.codeforce.mybatistiny.examples.util;

import com.penglecode.codeforce.common.domain.EntityObject;

/**
 * @author pengpeng
 * @version 1.0
 */
public interface EnhancedBaseMapper<T extends EntityObject> extends CustomBaseMapper<T> {

    int merge(T entity);

}
