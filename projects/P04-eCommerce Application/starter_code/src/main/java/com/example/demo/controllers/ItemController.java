package com.example.demo.controllers;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;

@RestController
@RequestMapping("/api/item")
public class ItemController {
	Logger log = LoggerFactory.getLogger(ItemController.class);

	//@Autowired
	private ItemRepository itemRepository;

	//@Autowired
	private Receiver receiver;

	//@Autowired
	private Args args;

	public ItemController()
	{

	}

	public ItemController(ItemRepository itemRepository, Receiver receiver, Args args)
	{
		this.itemRepository = itemRepository;
		this.receiver = receiver;
		this.args = args;
	}

	@GetMapping
	public ResponseEntity<List<Item>> getItems() {
		try {
			log.info("Retrieving all the items in the system.");
			logToSplunk("items_retrieval","INFO", "",
					"Retrieving all the items in the system.");

			return ResponseEntity.ok(itemRepository.findAll());
		}
		catch (Exception e) {
			log.error("There was error retrieving items in the system. " +
					"Process failed with exception:{}.", e.getMessage());
			logToSplunk("items_retrieval","ERROR", "500",
					"There was error retrieving items in the system. " +
							"Process failed with exception:"+e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Item> getItemById(@PathVariable Long id) {
		try {
			log.info("Retrieving item with id:{} from the system",id);
			logToSplunk("items_retrieval_by_id","INFO", "",
					"Retrieving item with id:"+id+" from the system.");

			Optional<Item> item = itemRepository.findById(id);

			log.debug("Item with id:{} found in the system:{}", id, item.isPresent());
			logToSplunk("items_retrieval_by_id","DEBUG", "",
					"Item with id:"+id+" found in the system:"+item.isPresent());

			if(item.isPresent())
			{
				log.info("Retrieved item with id:{} successfully from the system",id);
				logToSplunk("items_retrieval_by_id","INFO", "200",
						"Retrieved item with id:"+ id + " successfully from the system.");
			}
			else
			{
				log.error("Item with id:{} not found in the system",id);
				logToSplunk("items_retrieval_by_id","ERROR", "400",
						"Item with id:"+id+ " not found in the system.");
			}

			return ResponseEntity.of(item);
		}
		catch (Exception e) {
			log.error("There was error retrieving item with id:{} in the system. " +
							"Process failed with exception:{}.",
					id,e.getMessage());
			logToSplunk("items_retrieval_by_id","ERROR", "500",
					"There was error retrieving item with id:"+id+
							" in the system. Process failed with exception:"+e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/name/{name}")
	public ResponseEntity<List<Item>> getItemsByName(@PathVariable String name) {
		try
		{
			log.info("Retrieving items with name:{} from the system",name);
			logToSplunk("items_retrieval_by_name","INFO","", "Retrieving items with name:"+ name +
					" from the system.");

			List<Item> items = itemRepository.findByName(name);

			log.debug("Item with name:{} found in the system:{}", name, !items.isEmpty());
			logToSplunk("items_retrieval_by_name","DEBUG","", "Item with name:"+name+" found in the system:"
					+!items.isEmpty());

			if(!items.isEmpty())
			{
				log.info("Retrieved items with name:{} from the system",name);
				logToSplunk("items_retrieval_by_name","INFO","200", "Retrieved items with name:"+name+" from the system.");
			}
			else
			{
				log.error("Items with name:{} not found in the system.",name);
				logToSplunk("items_retrieval_by_name","ERROR","400", "Items with name:"+name+" not found in the system.");
			}

			return items == null || items.isEmpty() ? ResponseEntity.notFound().build()
					: ResponseEntity.ok(items);
		}
		catch (Exception e) {
			log.error("There was error retrieving items with name:{} from the system. " +
							"Process failed with exception:{}.",
					name,e.getMessage());
			logToSplunk("items_retrieval_by_name","ERROR","500",
					"There was error retrieving items with name:"+name+
					" from the system."
					+" Process failed with exception:"+e.getMessage());

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
