package ru.kmorozov.activiti.demo.config;

import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Created by sbt-morozov-kv on 13.09.2016.
 */
public class CachedMappedStatement {

    public static MappedStatement getCachedMappedStatement(MappedStatement mappedStatement) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(MappedStatement.class);
        enhancer.setCallback(new CachedMappedStatementHandler());

        return (MappedStatement) enhancer.create();
    }

    public static Class<MappedStatement> getCachedMappedStatementClass() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(MappedStatement.class);
        enhancer.setCallback(new CachedMappedStatementHandler());

        return enhancer.createClass();
    }

    private static class CachedMappedStatementHandler implements MethodInterceptor {
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (method.getName().equals("isUseCache")) {
                return true;
            } else
                return proxy.invokeSuper(obj, args);
        }
    }
}
