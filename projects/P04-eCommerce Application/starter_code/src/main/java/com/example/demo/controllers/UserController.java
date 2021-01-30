package com.example.demo.controllers;

import com.splunk.Args;
import com.splunk.Receiver;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {
	Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	Receiver receiver;

	@Autowired
	Args args;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		try {
			log.info("Finding user with id:{}",id);

			logToSplunk("user_retrieval_by_id", "INFO", "",
					"Finding user with id:" +id);

			Optional<User> user = userRepository.findById(id);

			log.debug("User with id:{} found in the system:{}", id, user.isPresent());
			logToSplunk("user_retrieval_by_id", "DEBUG", "",
					"User with id:"+id + " found in the system:" +user.isPresent());

			if(user.isPresent())
			{
				log.info("User with id:"+id+" found in the system.");
				logToSplunk("user_retrieval_by_id", "INFO", "200",
						"User with id:"+id+" found in the system.");
			}
			else
			{
				log.error("User with id:"+id+" not found in the system.");
				logToSplunk("user_retrieval_by_id", "ERROR", "400",
						"User with id:"+id+" not found in the system.");
			}

			return ResponseEntity.of(user);
		}
		catch (Exception e) {
			e.printStackTrace();
			log.error("There was error retrieving user with id:{}. Process failed with exception:{}.",
					id,e.getMessage());

			logToSplunk("user_retrieval_by_id", "ERROR", "500",
					"There was error retrieving user with id:"+id+". " +
							"Process failed with exception:"+e.getMessage());

			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		try
		{
			log.info("Finding user with username:{}"+username);

			logToSplunk("user_retrieval_by_username", "INFO", "",
					"Finding user with username:" +username);

			User user = userRepository.findByUsername(username);

			log.debug("User with username:{} found in the system:{}", username, user != null);
			logToSplunk("user_retrieval_by_username", "DEBUG", "",
					"User with username:"
							+username+" found in the system:"+(user != null));

			if(user != null)
			{
				log.info("User with username:"+username+ " found in the system.");
				logToSplunk("user_retrieval_by_username", "INFO", "200",
						"User with username:" +username+" found in the system.");
			}
			else
			{
				log.error("User with username:"+username+" not found in the system.");
				logToSplunk("user_retrieval_by_username", "ERROR", "400",
						"User with username:"+username+" not found in the system.");
			}

			return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
		}
		catch (Exception e) {
			e.printStackTrace();
			log.error("There was error retrieving user with username:{}. Process failed with exception:{}.",
					username,e.getMessage());
			logToSplunk("user_retrieval_by_username", "ERROR", "500",
					"There was error retrieving user with username:"+username+
							". Process failed with exception"+e.getMessage());

			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) throws JSONException {
		try {
			log.info("Creating user with username:{}",createUserRequest.getUsername());

			logToSplunk("user_creation", "INFO", "",
					"Creating user with username:" +createUserRequest.getUsername());

			User user = new User();
			user.setUsername(createUserRequest.getUsername());

			log.info("User name set with {}.", createUserRequest.getUsername());

			logToSplunk("user_creation", "INFO", "",
					"User name set with:" +createUserRequest.getUsername());

			Cart cart = new Cart();
			cartRepository.save(cart);
			user.setCart(cart);

			log.debug("User name password with length:{}.", createUserRequest.getPassword().length());

			logToSplunk("user_creation", "DEBUG", "",
					"User name password with length:"
							+createUserRequest.getPassword().length());

			log.debug("User name logged in password not equals confirm password:{}.",
					!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword()));

			logToSplunk("user_creation", "DEBUG", "",
					"User name logged in password not equals confirm password:"
					+!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword()));

			if (createUserRequest.getPassword().length() < 7 ||
					!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword())) {
				log.error("Password validation failed for user:{}", createUserRequest.getUsername());

				logToSplunk("user_creation", "ERROR", "400",
						"Password validation failed for user:"
								+createUserRequest.getUsername());

				return ResponseEntity.badRequest().build();
			}

			user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));

			userRepository.save(user);

			log.info("User with user name {} created successfully.", createUserRequest.getUsername());

			logToSplunk("user_creation", "INFO", "200",
					"User with user name: " + createUserRequest.getUsername()
					+ " was created successfully.");

			return ResponseEntity.ok(user);
		}
		catch (Exception e)
		{
			e.printStackTrace();

			log.error("There was error creating user with username:{}. Process failed with exception:{}.",
					createUserRequest.getUsername(),e.getMessage());

			logToSplunk("user_creation", "INFO", "500",
					"There was error creating user with username:" + createUserRequest.getUsername()
							+ ". Process failed with exception: "+e.getMessage());

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
