package ru.kmorozov.ignite.test;

import org.apache.ignite.Ignite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kmorozov.activiti.demo.ignite.IgniteProvider;

import static org.apache.ignite.internal.IgniteNodeAttributes.ATTR_GRID_NAME;

/**
 * Created by sbt-morozov-kv on 19.09.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/ignite/providerConfig.xml"})
public class IgniteHelloWorld {

    @Autowired
    @Qualifier("clientProvider")
    private IgniteProvider igniteClient;

    @Autowired
    @Qualifier("serverProvider")
    private IgniteProvider igniteServer;

    @Test
    public void sendHelloTest() {
        try (Ignite server = igniteServer.getIgnite(); Ignite client = igniteClient.getIgnite()) {
            int param = 1;
            Integer clientResult = client.compute(client.cluster()
                    .forAttribute(ATTR_GRID_NAME, "testGrid-server")
                    .forServers())
                    .call(() -> {
                        System.out.println("Hello World from client!");
                        return param + 1;
                    });
            System.out.println(clientResult);
            server.compute(server.cluster()
                    .forAttribute(ATTR_GRID_NAME, "testGrid-server1")
                    .forServers())
                    .broadcast(() -> System.out.println("Hello World from server!"));
        }
    }
}

