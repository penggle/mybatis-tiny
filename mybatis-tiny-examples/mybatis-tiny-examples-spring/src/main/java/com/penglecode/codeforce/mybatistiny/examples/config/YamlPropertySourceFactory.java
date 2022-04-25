package com.penglecode.codeforce.mybatistiny.examples.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Objects;

/**
 * @author pengpeng
 * @version 1.0
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) {
        String sourceName = StringUtils.defaultIfBlank(name, resource.getResource().getFilename());
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        return new PropertiesPropertySource(sourceName, Objects.requireNonNull(factory.getObject()));
    }

}
