package ru.kmorozov.ignite.test;

import org.apache.ignite.Ignite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kmorozov.activiti.demo.ignite.IgniteProvider;

/**
 * Created by sbt-morozov-kv on 19.09.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {IgniteProvider.class})
public class IgniteHelloWorld {

    @Autowired
    private IgniteProvider igniteProvider;

    @Test
    public void sendHelloTest() {
        Ignite ignite = igniteProvider.getIgnite();

        while(true) {
            try {
                ignite.compute().broadcast(() -> System.out.println("Hello World!"));
                Thread.sleep(1000);
            }
            catch (Exception ex) {}
        }
    }
}
