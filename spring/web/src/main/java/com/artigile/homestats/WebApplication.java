package com.artigile.homestats;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@EnableCircuitBreaker
@EnableDiscoveryClient
@EnableBinding(Source.class)
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}

@RestController
@RequestMapping("/test")
class TestAppRestController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Source source;

    @RequestMapping(method = RequestMethod.POST, value = "call1")
    public void testWrite(@RequestBody String test) {
        Message<String> msg = MessageBuilder.withPayload(test).build();
        this.source.output().send(msg);
    }

    public String defaultResult() {
        return "this is default";
    }

    @HystrixCommand(fallbackMethod = "defaultResult")
    @RequestMapping(method = RequestMethod.GET, value = "call")
    public String getValues() {

        ResponseEntity<String> response =
                this.restTemplate.exchange("http://ivannaroom/message", HttpMethod.GET, null, String.class);
        return response.getBody();
    }
}