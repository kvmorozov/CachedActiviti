package ru.kmorozov.activiti.demo;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.kmorozov.activiti.demo.data.ApplicantRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sbt-morozov-kv on 12.09.2016.
 */
@SpringBootApplication()
@EnableJpaRepositories(basePackageClasses = ApplicantRepository.class)
public class DemoApp {

    public static void main(String[] args) {
        SpringApplication.run(DemoApp.class, args);
    }

    @Bean
    CommandLineRunner init(final RepositoryService repositoryService,
                           final RuntimeService runtimeService,
                           final TaskService taskService) {
        repositoryService.createDeployment()
                .addClasspathResource("processes/Developer_Hiring.bpmn20.xml")
                .addClasspathResource("processes/Developer_Hiring_with_jpa.bpmn20.xml")
                .deploy();

        return strings -> {
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("applicantName", "John Doe");
            variables.put("email", "john.doe@activiti.com");
            variables.put("phoneNumber", "123456789");
            runtimeService.startProcessInstanceByKey("hireProcess", variables);
        };
    }

    @Bean
    InitializingBean usersAndGroupsInitializer(final IdentityService identityService) {
        return () -> {
            Group group = identityService.newGroup("user");
            group.setName("users");
            group.setType("security-role");
            identityService.saveGroup(group);

            User admin = identityService.newUser("admin");
            admin.setPassword("admin");
            identityService.saveUser(admin);
        };
    }
}
