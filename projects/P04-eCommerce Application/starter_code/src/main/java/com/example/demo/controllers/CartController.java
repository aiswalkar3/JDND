package com.example.demo.controllers;

import java.util.Optional;
import java.util.stream.IntStream;

import com.splunk.Args;
import com.splunk.Receiver;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;

@RestController
@RequestMapping("/api/cart")
public class CartController {
	Logger log = LoggerFactory.getLogger(CartController.class);

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private ItemRepository itemRepository;

	//@Autowired
	//Receiver receiver;

	@Autowired
	Args args;

	@PostMapping("/addToCart")
	public ResponseEntity<Cart> addTocart(@RequestBody ModifyCartRequest request) {
		try {
			log.info("Adding item:{}, quantity:{} to user:{} cart.",request.getItemId(),
					request.getQuantity(),request.getUsername());

			logToSplunk("add_item_to_cart","INFO", "",
					"Adding item:"+request.getItemId() +" quantity:"+request.getQuantity() +
							" to user:"+request.getUsername() + " cart.");

			User user = userRepository.findByUsername(request.getUsername());

			log.debug("user with username:{} exists in the system:{}", request.getUsername(), !(user == null));
			logToSplunk("add_item_to_cart","DEBUG", "",
					"User with username:"+request.getUsername()
							+" exists in the system:"+!(user == null));

			if (user == null) {
				log.error("user with username:{} does not exist in the system.", request.getUsername());
				logToSplunk("add_item_to_cart","ERROR", "400",
						"user with username:"
								+request.getUsername()+" does not exist in the system.");

				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}

			Optional<Item> item = itemRepository.findById(request.getItemId());

			log.debug("item with id:{} exists in the system:{}", request.getItemId(), item.isPresent());
			logToSplunk("add_item_to_cart","DEBUG", "",
					"item with id:"+request.getItemId() +" exists in the system:"+item.isPresent());

			if (!item.isPresent()) {
				log.error("item with id:{} does not exist in the system.", request.getItemId());
				logToSplunk("add_item_to_cart","ERROR", "400",
						"item with id:"+request.getItemId()
								+" does not exist in the system.");

				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}

			Cart cart = user.getCart();
			IntStream.range(0, request.getQuantity())
					.forEach(i -> cart.addItem(item.get()));
			cartRepository.save(cart);

			log.info("item with name:{}, quantity:{} was added to the user:{} cart.", item.get().getName(),
					request.getQuantity(), request.getUsername());

			logToSplunk("add_item_to_cart","INFO", "200",
					"item with name:"+item.get().getName() + " quantity:"+
							+ request.getQuantity() + " was added to the user:"+request.getUsername()+" cart.");

			return ResponseEntity.ok(cart);
		}
		catch(Exception e)
		{
			log.error("There was error adding item:{} to user:{} cart. Process failed with exception:{}.",
					request.getItemId(),request.getUsername(), e.getMessage());
			logToSplunk("add_item_to_cart","ERROR", "500",
					"There was error adding item:"+request.getItemId()
							+" to user:"+request.getUsername()+" cart. Process failed with exception:"+e.getMessage());

			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/removeFromCart")
	public ResponseEntity<Cart> removeFromcart(@RequestBody ModifyCartRequest request) {
		try {
			log.info("Removing item:{}, quantity:{} from user:{} cart.",request.getItemId(),
					request.getQuantity(),request.getUsername());
			logToSplunk("remove_item_from_cart","INFO", "",
					"Removing item:"+request.getItemId()
							+" quantity:"+request.getQuantity()+" from user:"+request.getUsername()+" cart.");

			User user = userRepository.findByUsername(request.getUsername());

			log.debug("user with username:{} exists in the system:{}", request.getUsername(), !(user == null));
			logToSplunk("remove_item_from_cart","DEBUG", "",
					"user with username:"+request.getUsername()
							+" exists in the system:"+!(user == null));

			if (user == null) {
				log.error("user with username:{} does not exist in the system.", request.getUsername());
				logToSplunk("remove_item_from_cart","ERROR", "400",
						"user with username:"
								+request.getUsername()+" does not exist in the system.");

				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}

			Optional<Item> item = itemRepository.findById(request.getItemId());

			log.debug("item with id:{} exists in the system:{}", request.getItemId(), item.isPresent());
			logToSplunk("remove_item_from_cart","DEBUG", "",
					"item with id:"+request.getItemId()
							+" exists in the system:"+item.isPresent());

			if (!item.isPresent()) {
				log.error("item with id:{} does not exist in the system.", request.getItemId());
				logToSplunk("remove_item_from_cart","ERROR", "400",
						"item with id:"+request.getItemId()
								+" does not exist in the system.");

				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}

			Cart cart = user.getCart();
			IntStream.range(0, request.getQuantity())
					.forEach(i -> cart.removeItem(item.get()));
			cartRepository.save(cart);

			log.info("item with name:{}, quantity:{} was removed from the user:{} cart.", item.get().getName(),
					request.getQuantity(), request.getUsername());
			logToSplunk("remove_item_from_cart","INFO", "200",
					"item with name:"+item.get().getName()
							+" quantity:"+request.getQuantity()+" was removed from the user:"+request.getUsername()
							+" cart.");

			return ResponseEntity.ok(cart);
		}
		catch(Exception e) {
			log.error("There was error adding item:{} to user:{} cart. Process failed with exception:{}.",
					request.getItemId(),request.getUsername(), e.getMessage());
			logToSplunk("remove_item_from_cart","ERROR", "500",
					"There was error adding item:"+request.getItemId()
					+" to user:"+request.getUsername()+" cart. Process failed with exception:"
					+e.getMessage());

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
