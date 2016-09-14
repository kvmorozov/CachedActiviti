package ru.kmorozov.activiti.demo.config;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.mybatis.caches.ignite.IgniteCacheAdapter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

/**
 * Created by sbt-morozov-kv on 13.09.2016.
 */
public class CachedMybatisConfiguration {

    public static Configuration getCachedConfiguration(Configuration configuration) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Configuration.class);
        enhancer.setCallback(new CachedConfigurationHandler(configuration));

        return (Configuration) enhancer.create();
    }

    private static class CachedConfigurationHandler implements InvocationHandler {

        private Configuration configuration;

        CachedConfigurationHandler(Configuration configuration) {
            this.configuration = configuration;

            this.configuration.addCache(new IgniteCacheAdapter("testIgnite"));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object originalResult = method.invoke(configuration, args);

            if (method.getName().equals("getMappedStatement")) {
                CachedMappedStatement.getCachedMappedStatement((MappedStatement) originalResult);
            }

            return originalResult;
        }
    }
}
