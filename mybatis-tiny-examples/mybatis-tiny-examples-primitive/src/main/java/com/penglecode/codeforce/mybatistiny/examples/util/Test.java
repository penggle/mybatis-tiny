package com.penglecode.codeforce.mybatistiny.examples.util;

import java.util.*;

/**
 * @author pengpeng
 * @version 1.0
 */
public class Test {

    public static void main(String[] args) {
        Map<String,Object> map1 = new LinkedHashMap<>();
        map1.put("id", "123");
        map1.put("name", "haha");
        map1.put("age", 25);
        System.out.println(map1);
        Map<String,Object> map2 = new LinkedHashMap<>();
        map2.put("name", "asan");
        map2.put("sex", "男");
        System.out.println(map2);

        map1.putAll(map2);
        System.out.println(map1);

        List<Integer> list = Arrays.asList(1, 12, 3, 6, 0);
        list.sort(Integer::compareTo);
        System.out.println(list);
    }

}
