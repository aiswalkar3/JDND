package com.example.demo;

import com.splunk.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;

@EnableJpaRepositories("com.example.demo.model.persistence.repositories")
@EntityScan("com.example.demo.model.persistence")
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class SareetaApplication {

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

	/*
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
	public Args logArgs()
	{
		Args logArgs = new Args();
		logArgs.put("source", "e-commerce application");
		logArgs.put("sourcetype","_json");
		return logArgs;
	}
	*/

	public static void main(String[] args) {
		SpringApplication.run(SareetaApplication.class, args);
	}

}
