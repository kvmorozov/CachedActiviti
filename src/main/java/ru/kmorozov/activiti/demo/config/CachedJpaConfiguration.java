package ru.kmorozov.activiti.demo.config;

import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ActivitiProperties;
import org.activiti.spring.boot.JpaProcessEngineAutoConfiguration;
import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cglib.proxy.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ru.kmorozov.activiti.demo.ignite.IgniteCacheAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by sbt-morozov-kv on 13.09.2016.
 */

@Configuration
@ConditionalOnClass(name = "javax.persistence.EntityManagerFactory")
@EnableConfigurationProperties(ActivitiProperties.class)
public class CachedJpaConfiguration extends JpaProcessEngineAutoConfiguration.JpaConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(
            DataSource dataSource, EntityManagerFactory entityManagerFactory,
            PlatformTransactionManager transactionManager, SpringAsyncExecutor springAsyncExecutor) throws IOException {
        return
                getCachedConfig(super.springProcessEngineConfiguration(dataSource, entityManagerFactory, transactionManager, springAsyncExecutor));
    }

    private SpringProcessEngineConfiguration getCachedConfig(final SpringProcessEngineConfiguration parentConfig) {
        Enhancer enhancer = new Enhancer();

        CallbackHelper callbackHelper = new CallbackHelper(SpringProcessEngineConfiguration.class, new Class[0]) {
            @Override
            protected Object getCallback(Method method) {
                if (method.getName().equals("initMybatisConfiguration")) {
                    return (MethodInterceptor) (obj, method1, args, proxy) ->
                            getCachedConfiguration((org.apache.ibatis.session.Configuration) proxy.invokeSuper(obj, args));
                } else {
                    return NoOp.INSTANCE;
                }
            }
        };

        enhancer.setSuperclass(SpringProcessEngineConfiguration.class);
        enhancer.setCallbackFilter(callbackHelper);
        enhancer.setCallbacks(callbackHelper.getCallbacks());

        SpringProcessEngineConfiguration result = (SpringProcessEngineConfiguration) enhancer.create();

        result.setDataSource(parentConfig.getDataSource());
        result.setTransactionManager(parentConfig.getTransactionManager());
        result.setDatabaseSchemaUpdate("create-drop");

        return result;
    }

    private org.apache.ibatis.session.Configuration getCachedConfiguration(org.apache.ibatis.session.Configuration configuration) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(org.apache.ibatis.session.Configuration.class);
        enhancer.setCallback(new CachedConfigurationHandler(configuration));

        return (org.apache.ibatis.session.Configuration) enhancer.create();
    }

    private class CachedConfigurationHandler implements InvocationHandler {

        private org.apache.ibatis.session.Configuration configuration;

        CachedConfigurationHandler(org.apache.ibatis.session.Configuration configuration) {
            this.configuration = configuration;

            this.configuration.addCache(IgniteCacheAdapter.INSTANCE);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object originalResult = method.invoke(configuration, args);

            if (method.getName().equals("getMappedStatement")) {
                return getCachedMappedStatement((MappedStatement) originalResult);
            }

            return originalResult;
        }
    }

    private MappedStatement getCachedMappedStatement(MappedStatement mappedStatement) {
        return new MappedStatement
                .Builder(mappedStatement.getConfiguration(), mappedStatement.getId(), mappedStatement.getSqlSource(), mappedStatement.getSqlCommandType())
                .databaseId(mappedStatement.getDatabaseId())
                .resource(mappedStatement.getResource())
                .fetchSize(mappedStatement.getFetchSize())
                .timeout(mappedStatement.getTimeout())
                .statementType(mappedStatement.getStatementType())
                .resultSetType(mappedStatement.getResultSetType())
                .parameterMap(mappedStatement.getParameterMap())
                .resultMaps(mappedStatement.getResultMaps())
                .cache(IgniteCacheAdapter.INSTANCE)
                .useCache(true)
                .build();
    }
}
