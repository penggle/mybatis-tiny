package com.penglecode.codeforce.mybatistiny.examples.util;

import com.penglecode.codeforce.common.util.ReflectionUtils;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author pengpeng
 * @version 1.0
 */
public class CustomMapperTest {

    public static void main(String[] args) {
        Class<?> clazz = StudentMapper.class;
        Type[] superInterfaces = clazz.getGenericInterfaces();

        for(int i = 0; i < superInterfaces.length; i++) {
            System.out.println("------------------------------------------------");
            ParameterizedType superType = (ParameterizedType) superInterfaces[i];
            //System.out.println(superType);
            Class<?> superClass = (Class<?>) superType.getRawType();

            ReflectionUtils.doWithMethods(superClass, method -> {
                System.out.println(method);
            }, method -> {
                Class<?> declaringClass = method.getDeclaringClass();
                return !method.isDefault() && !Modifier.isStatic(method.getModifiers()) && !BaseEntityMapper.class.equals(declaringClass) && BaseEntityMapper.class.isAssignableFrom(declaringClass);
            });
        }

    }

}
