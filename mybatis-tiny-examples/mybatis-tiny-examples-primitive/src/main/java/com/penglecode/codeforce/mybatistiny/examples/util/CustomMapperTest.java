package com.penglecode.codeforce.mybatistiny.examples.util;

import com.penglecode.codeforce.common.util.ClassUtils;
import com.penglecode.codeforce.common.util.ReflectionUtils;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author pengpeng
 * @version 1.0
 */
public class CustomMapperTest {

    public static void test1() {
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

    public static void test2() {
        Set<Class<BaseEntityMapper<?>>> sets = new LinkedHashSet<>();
        Class<BaseEntityMapper<?>> targetMapperClass = (Class<BaseEntityMapper<?>>) ClassUtils.resolveClassName(StudentMapper.class.getName(), ClassUtils.getDefaultClassLoader());
        doWithSuperTypes(targetMapperClass, sets);
        //System.out.println(sets);
        List<Class<BaseEntityMapper<?>>> lists = new ArrayList<>(sets);
        Collections.reverse(lists);
        //System.out.println(lists);
        for(Class<BaseEntityMapper<?>> baseEntityMapperClass : lists) {
            System.out.println("=======================" + baseEntityMapperClass.getName() + "=======================");
            for(Method method : baseEntityMapperClass.getDeclaredMethods()) {
                if(!method.isDefault() && !Modifier.isStatic(method.getModifiers())) {
                    System.out.println(method);
                }
            }
        }
    }

    protected static void doWithSuperTypes(Class<BaseEntityMapper<?>> baseMapperClass, Set<Class<BaseEntityMapper<?>>> offer) {
        if(!BaseEntityMapper.class.equals(baseMapperClass)) {
            Class<BaseEntityMapper<?>>[] superInterfaces = (Class<BaseEntityMapper<?>>[]) baseMapperClass.getInterfaces();
            for(Class<BaseEntityMapper<?>> superInterface : superInterfaces) {
                if(!BaseEntityMapper.class.equals(superInterface)) {
                    //System.out.println(superInterface);
                    offer.add(superInterface);
                    doWithSuperTypes(superInterface, offer);
                }
            }
        }
    }

    public static void main(String[] args) {
        test2();
    }

}
