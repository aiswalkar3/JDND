package com.example.demo.model.persistence;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import com.example.demo.controllers.OrderController;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.splunk.Args;
import com.splunk.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Entity
@Table(name = "cart")
public class Cart {
	@Transient
	Logger log = LoggerFactory.getLogger(Cart.class);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty
	@Column
	private Long id;
	
	@ManyToMany
	@JsonProperty
	@Column
    private List<Item> items;
	
	@OneToOne(mappedBy = "cart")
	@JsonProperty
    private User user;
	
	@Column
	@JsonProperty
	private BigDecimal total;
	
	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	public void addItem(Item item) {
		try {
			log.info("Adding item:{} to cart for user:{}.", item.getName(), this.user.getUsername());

			log.debug("Is cart empty:{} for user:{}", (items == null || items.isEmpty()), this.user.getUsername());

			if (items == null) {
				items = new ArrayList<>();
			}

			items.add(item);

			log.debug("Is cart total price 0:{} for user:{}", total == null, this.user.getUsername());

			if (total == null) {
				total = new BigDecimal(0);
			}

			total = total.add(item.getPrice());

			log.info("After adding item:{} to cart for user:{}, the total price is:{}."
					, item.getName(), this.user.getUsername(), total);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error("There was an error adding item:{} to cart for user:{}. Process failed with exception:{}"
					, item.getName(),this.user.getUsername(),e.getMessage());
		}
	}
	
	public void removeItem(Item item) {
		try {
			log.info("Removing item:{} from cart for user:{}.", item.getName(), this.user.getUsername());

			log.debug("Is cart empty:{} for user:{}", (items == null || items.isEmpty()), this.user.getUsername());

			if (items == null) {
				items = new ArrayList<>();
			}

			items.remove(item);

			log.debug("Is cart total price 0:{} for user:{}", (total == null || total == BigDecimal.valueOf(0)),
					this.user.getUsername());

			if (total == null) {
				total = new BigDecimal(0);
			}

			total = total.subtract(item.getPrice());

			log.info("After removing item:{} from cart for user:{}, the total price is:{}."
					, item.getName(), this.user.getUsername(), total);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error("There was an error removing item:{} from cart for user:{}. Process failed with exception:{}"
					, item.getName(),this.user.getUsername(),e.getMessage());
		}
	}
}
