package com.artigile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@EnableBinding(Sink.class)
@IntegrationComponentScan
@SpringBootApplication
public class RoomSensorsApplication {

    @Bean
    public RepositoryRestConfigurer repositoryRestConfigurer() {
        return new RepositoryRestConfigurerAdapter() {
            @Override
            public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
                config.exposeIdsFor(SensorData.class);
            }

        };
    }

    @Bean
    public CommandLineRunner commandLineRunner(final SensorRepository sensorRepository) {
        return strings -> {
            sensorRepository.findAll();
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(RoomSensorsApplication.class, args);
    }


}

@MessageEndpoint
class TestProcessor {
    @ServiceActivator(inputChannel = Sink.INPUT)
    public void acceptCall(String n) {
        System.out.println(n);
    }
}

@RefreshScope
@RestController
class MsgRestController {

    @Value("${message}")
    private String msg;

    @RequestMapping("/message")
    String message() {
        return this.msg;
    }


}
