package com.example.demo.controllers;

import java.util.List;

import com.splunk.Args;
import com.splunk.Receiver;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;

@RestController
@RequestMapping("/api/order")
public class OrderController {
	Logger log = LoggerFactory.getLogger(OrderController.class);
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	Receiver receiver;

	@Autowired
	Args args;
	
	@PostMapping("/submit/{username}")
	public ResponseEntity<UserOrder> submit(@PathVariable String username) {
		try {
			log.info("User with username:{} submitted the order.",username);
			logToSplunk("order_creation","INFO", "",
					"User with username:"+username+ " submitted the order.");

			User user = userRepository.findByUsername(username);

			log.debug("User with username:{} found in the system:{}", username, user != null);
			logToSplunk("order_creation","DEBUG", "",
					"User with username:"+username+ " found in the system:"+(user != null));

			if (user == null) {
				log.error("User with username:{} not found in the system.",username);
				logToSplunk("order_creation","ERROR", "400",
						"User with username:"+username +" not found in the system.");

				return ResponseEntity.notFound().build();
			}

			UserOrder order = UserOrder.createFromCart(user.getCart());

			orderRepository.save(order);

			log.info("Order:{} of the user:{} successfully submitted.",order.getId(),username);
			logToSplunk("order_creation","INFO", "200",
					"Order:"+order.getId()+" of the user:"+username
							+" successfully submitted.");

			return ResponseEntity.ok(order);
		}
		catch (Exception e) {
			e.printStackTrace();
			log.error("There was an error submitting user order for user:{}. Process failed with exception:{}.",
					username,e.getMessage());
			logToSplunk("order_creation","ERROR", "500",
					"There was an error submitting user order for user:"
							+username +" Process failed with exception:"+e.getMessage());

			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/history/{username}")
	public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) {
		try
		{
			log.info("Retrieving orders for user:{}.",username);
			logToSplunk("order_retrieval_by_username","INFO", "",
					"Retrieving orders for user:"+username);

			User user = userRepository.findByUsername(username);

			log.debug("User with username:{} found in the system:{}", username, user != null);
			logToSplunk("order_retrieval_by_username","DEBUG", "",
					"User with username:"+username+" found in the system:" +(user != null));

			if(user == null) {
				log.error("User with username:{} not found in the system.",username);
				logToSplunk("order_retrieval_by_username","ERROR", "400",
						"User with username:"+username +" not found in the system.");

				return ResponseEntity.notFound().build();
			}

			log.info("Orders retrieved for user:{}",username);
			logToSplunk("order_retrieval_by_username","INFO", "200",
					"Orders retrieved for user:"+username);

			return ResponseEntity.ok(orderRepository.findByUser(user));
		}
		catch (Exception e) {
			log.error("There was error retrieving orders for user:{}. Process failed with exception:{}.",
					username,e.getMessage());
			logToSplunk("order_retrieval_by_username","ERROR", "500",
					"There was error retrieving orders for user:"
							+username+" Process failed with exception:"+e.getMessage());

			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void logToSplunk(String index, String logLevel, String responseCode, String message)
	{
		try {
			JSONObject jsonObject = new JSONObject();
			if (logLevel != null && !logLevel.isEmpty())
				jsonObject.put("logLevel", logLevel);
			if (responseCode != null && !responseCode.isEmpty())
				jsonObject.put("responseCode", responseCode);
			if (message != null && !message.isEmpty())
				jsonObject.put("message", message);

			receiver.log(index, args, jsonObject.toString());
		}
		catch(JSONException exception)
		{
			System.out.println("Exception occurred while creating json object.");
			exception.printStackTrace();
		}
		catch (Exception e)
		{
			System.out.println("Exception occurred while creating json object.");
			e.printStackTrace();
		}
	}
}
