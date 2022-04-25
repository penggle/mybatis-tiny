package com.penglecode.codeforce.mybatistiny.examples.config;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.common.util.ClassUtils;
import com.penglecode.codeforce.common.util.StringUtils;
import com.penglecode.codeforce.mybatistiny.EnableMybatisTiny;
import com.penglecode.codeforce.mybatistiny.core.DecoratedSqlSessionFactoryBuilder;
import com.penglecode.codeforce.mybatistiny.examples.BasePackage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Mybatis与Spring手动集成配置
 *
 * @author pengpeng
 * @version 1.0
 */
@Configuration
@EnableMybatisTiny
@EnableTransactionManagement(proxyTargetClass=true)
@MapperScan(basePackageClasses=BasePackage.class, annotationClass=Mapper.class)
@ComponentScan(basePackageClasses=BasePackage.class)
@PropertySource(value="classpath:application.yml", factory=YamlPropertySourceFactory.class)
public class MybatisConfiguration implements EnvironmentAware {

    private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();

    private static final String DATASOURCE_CONFIG_PREFIX = "spring.datasource.";

    private static final String MYBATIS_CONFIG_PREFIX = "spring.mybatis.";

    private ConfigurableEnvironment environment;

    @Bean
    public DataSource dataSource() {
        MutablePropertySources propertySources = environment.getPropertySources();
        Properties properties = new Properties();
        for(org.springframework.core.env.PropertySource<?> propertySource : propertySources) {
            if(propertySource instanceof EnumerablePropertySource) {
                String[] propertyNames = ((EnumerablePropertySource<?>) propertySource).getPropertyNames();
                for(String propertyName : propertyNames) {
                    if(propertyName.startsWith(DATASOURCE_CONFIG_PREFIX)) {
                        String name = StringUtils.kebabNamingToCamel(propertyName.substring(DATASOURCE_CONFIG_PREFIX.length()));
                        properties.put(name, environment.getProperty(propertyName));
                    }
                }
            }
        }
        return new HikariDataSource(new HikariConfig(properties));
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setSqlSessionFactoryBuilder(new DecoratedSqlSessionFactoryBuilder());
        sqlSessionFactoryBean.setConfigLocation(getConfigLocation());
        sqlSessionFactoryBean.setTypeAliasesPackage(getTypeAliasesPackage());
        sqlSessionFactoryBean.setTypeAliasesSuperType(getTypeAliasesSuperType());
        sqlSessionFactoryBean.setMapperLocations(getMapperLocations());
        return sqlSessionFactoryBean;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    protected Resource getConfigLocation() {
        return RESOURCE_PATTERN_RESOLVER.getResource(Objects.requireNonNull(environment.getProperty(MYBATIS_CONFIG_PREFIX + "config-location")));
    }

    protected String getTypeAliasesPackage() {
        return environment.getProperty(MYBATIS_CONFIG_PREFIX + "type-aliases-package", BasePackage.class.getPackage().getName());
    }

    protected Class<?> getTypeAliasesSuperType() {
        String superTypeName = environment.getProperty(MYBATIS_CONFIG_PREFIX + "type-aliases-super-type");
        try {
            if(StringUtils.isNotBlank(superTypeName)) {
                return ClassUtils.forName(superTypeName, ClassUtils.getDefaultClassLoader());
            }
        } catch (ClassNotFoundException e) {
            //ignore
        }
        return EntityObject.class;
    }

    protected Resource[] getMapperLocations() {
        String defaultMapperLocation = String.format("classpath*:%s/**/*Mapper.xml", BasePackage.class.getPackage().getName().replace(".", "/"));
        String[] mapperLocations = environment.getProperty(MYBATIS_CONFIG_PREFIX + "mapper-locations", String[].class);
        mapperLocations = ArrayUtils.isNotEmpty(mapperLocations) ? mapperLocations : new String[] {defaultMapperLocation};
        return Stream.of(mapperLocations).flatMap(mapperLocation -> Stream.of(getResources(mapperLocation))).toArray(Resource[]::new);
    }

    protected Resource[] getResources(String mapperLocation) {
        try {
            return RESOURCE_PATTERN_RESOLVER.getResources(mapperLocation);
        } catch (IOException e) {
            return new Resource[0];
        }
    }

}
