package com.example.demo.config;

import com.splunk.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;

@Configuration
public class SplunkConfig {
    @Value("${splunk.username}")
    private String username;

    @Value("${splunk.password}")
    private String password;

    @Value("${splunk.host}")
    private String host;

    @Value("${splunk.port}")
    private Integer port;

    @Bean
    public Service splunkService()
    {
        HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);
        ServiceArgs loginArgs = new ServiceArgs();
        loginArgs.setUsername(username);
        loginArgs.setPassword(password);
        loginArgs.setHost(host);
        loginArgs.setPort(port);

        Service service = Service.connect(loginArgs);

        IndexCollection myIndexes = service.getIndexes();

        if(!myIndexes.containsKey("user_creation")) {
            myIndexes.create("user_creation");
        }

        if(!myIndexes.containsKey("user_retrieval_by_id")) {
            myIndexes.create("user_retrieval_by_id");
        }

        if(!myIndexes.containsKey("user_retrieval_by_username")) {
            myIndexes.create("user_retrieval_by_username");
        }

        if(!myIndexes.containsKey("order_creation")) {
            myIndexes.create("order_creation");
        }

        if(!myIndexes.containsKey("order_retrieval_by_username")) {
            myIndexes.create("order_retrieval_by_username");
        }

        if(!myIndexes.containsKey("add_item_to_cart")) {
            myIndexes.create("add_item_to_cart");
        }

        if(!myIndexes.containsKey("remove_item_from_cart")) {
            myIndexes.create("remove_item_from_cart");
        }

        if(!myIndexes.containsKey("items_retrieval")) {
            myIndexes.create("items_retrieval");
        }

        if(!myIndexes.containsKey("items_retrieval_by_id")) {
            myIndexes.create("items_retrieval_by_id");
        }

        if(!myIndexes.containsKey("items_retrieval_by_name")) {
            myIndexes.create("items_retrieval_by_name");
        }

        return service;
    }

    @Bean
    public Receiver receiver() throws IOException {
        Receiver receiver =  splunkService().getReceiver();
        return receiver;
    }

    @Bean
    public Args args()
    {
        Args logArgs = new Args();
        logArgs.put("source", "e-commerce application");
        logArgs.put("sourcetype","_json");
        return logArgs;
    }

}
