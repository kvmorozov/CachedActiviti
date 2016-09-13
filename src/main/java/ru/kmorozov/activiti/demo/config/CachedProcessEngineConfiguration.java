package ru.kmorozov.activiti.demo.config;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.ibatis.session.Configuration;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

/**
 * Created by sbt-morozov-kv on 13.09.2016.
 */
public class CachedProcessEngineConfiguration {

    public static SpringProcessEngineConfiguration getCachedConfig(SpringProcessEngineConfiguration parentConfig) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SpringProcessEngineConfiguration.class);
        enhancer.setCallback(new CachedProcessEngineHandler(parentConfig));

        return (SpringProcessEngineConfiguration) enhancer.create();
    }

    private static class CachedProcessEngineHandler implements InvocationHandler {

        private SpringProcessEngineConfiguration config;

        CachedProcessEngineHandler(SpringProcessEngineConfiguration config) {
            this.config = config;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object originalResult = method.invoke(config, args);

            if (method.getName().equals("initMybatisConfiguration")) {
                return CachedMybatisConfiguration.getCachedConfiguration((Configuration) originalResult);
            }

            return originalResult;
        }
    }
}
