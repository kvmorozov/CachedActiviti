package ru.kmorozov.activiti.demo.config;

import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ActivitiProperties;
import org.activiti.spring.boot.JpaProcessEngineAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.IOException;

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
        return CachedProcessEngineConfiguration.
                getCachedConfig(super.springProcessEngineConfiguration(dataSource, entityManagerFactory, transactionManager, springAsyncExecutor));
    }
}
