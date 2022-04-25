package com.penglecode.codeforce.mybatistiny.examples.boot;

import com.penglecode.codeforce.mybatistiny.EnableMybatisTiny;
import com.penglecode.codeforce.mybatistiny.examples.BasePackage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MybatisTiny示例的SpringBoot启动程序
 *
 * @author pengpeng
 * @version 1.0
 */
@EnableMybatisTiny
@AutoConfigurationPackage(basePackageClasses=BasePackage.class)
@SpringBootApplication(scanBasePackageClasses=BasePackage.class)
public class MybatisTinyExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisTinyExampleApplication.class, args);
    }

}
