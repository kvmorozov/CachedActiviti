package ru.kmorozov.activiti.demo.config;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

/**
 * Created by sbt-morozov-kv on 13.09.2016.
 */
public class CachedMappedStatement {

    public static MappedStatement getCachedMappedStatement(MappedStatement mappedStatement) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SpringProcessEngineConfiguration.class);
        enhancer.setCallback(new CachedMappedStatementHandler(mappedStatement));

        return (MappedStatement) enhancer.create();
    }

    private static class CachedMappedStatementHandler implements InvocationHandler {

        private MappedStatement mappedStatement;

        CachedMappedStatementHandler(MappedStatement mappedStatement) {
            this.mappedStatement = mappedStatement;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object originalResult = method.invoke(mappedStatement, args);

            if (method.getName().equals("useCache")) {
                return true;
            }

            return originalResult;
        }
    }
}
