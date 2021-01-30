package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ItemControllerTest {
    private ItemController itemController;
    private ItemRepository itemRepo = mock(ItemRepository.class);;

    @Before
    public void setup()
    {
        itemController = new ItemController();
        TestUtils.injectObjects(itemController,"itemRepository", itemRepo);
    }

    @Test
    public void getItemsHappyPath()
    {
        List<Item> listItems = new ArrayList<>();

        Item item1 = new Item();
        item1.setDescription("A widget that is round");
        item1.setId(1L);
        item1.setName("Round Widget");
        item1.setPrice(BigDecimal.valueOf(2.99));

        Item item2 = new Item();
        item2.setDescription("A widget that is square");
        item2.setId(2L);
        item2.setName("Square Widget");
        item2.setPrice(BigDecimal.valueOf(1.99));

        listItems.add(item1);
        listItems.add(item2);

        when(itemRepo.findAll()).thenReturn(listItems);

        ResponseEntity<List<Item>> itemListResponse = itemController.getItems();
        List<Item> responseItemList = itemListResponse.getBody();
        assertEquals(200, itemListResponse.getStatusCodeValue());
        assertEquals(listItems, responseItemList);
        assertEquals(listItems.size(), responseItemList.size());
    }

    @Test
    public void getItemByIdHappyPath()
    {
        long itemId = 1;
        Item item1 = new Item();
        item1.setDescription("A widget that is round");
        item1.setId(itemId);
        item1.setName("Round Widget");
        item1.setPrice(BigDecimal.valueOf(2.99));

        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item1));
        ResponseEntity<Item> itemResponse = itemController.getItemById(itemId);
        Item actualItem = itemResponse.getBody();
        assertEquals(200, itemResponse.getStatusCodeValue());
        assertEquals(Long.valueOf(itemId), actualItem.getId());
        assertEquals(item1.getDescription(), actualItem.getDescription());
        assertEquals(item1.getName(), actualItem.getName());
        assertEquals(item1.getPrice(), actualItem.getPrice());
    }

    @Test
    public void getItemsByNameHappyPath()
    {
        long itemId = 1;
        String itemName = "Round Widget";
        Item item1 = new Item();
        item1.setDescription("A widget that is round");
        item1.setId(itemId);
        item1.setName(itemName);
        item1.setPrice(BigDecimal.valueOf(2.99));

        itemId = 2;
        Item item2 = new Item();
        item2.setDescription("A widget that is round and golden");
        item2.setId(itemId);
        item2.setName(itemName);
        item2.setPrice(BigDecimal.valueOf(3.99));

        when(itemRepo.findByName(itemName)).thenReturn(Arrays.asList(item1,item2));
        ResponseEntity<List<Item>> itemResponse = itemController.getItemsByName(itemName);
        assertEquals(200, itemResponse.getStatusCodeValue());
        List<Item> actualItemList = itemResponse.getBody();
        assertEquals(Arrays.asList(item1,item2),actualItemList);
        assertEquals(Arrays.asList(item1,item2).size(),actualItemList.size());
    }

    @Test
    public void getItemsByNameErrorPath()
    {
        long itemId = 1;
        String itemName = "Round Widget";
        Item item1 = new Item();
        item1.setDescription("A widget that is round");
        item1.setId(itemId);
        item1.setName(itemName);
        item1.setPrice(BigDecimal.valueOf(2.99));

        itemId = 2;
        Item item2 = new Item();
        item2.setDescription("A widget that is round and golden");
        item2.setId(itemId);
        item2.setName(itemName);
        item2.setPrice(BigDecimal.valueOf(3.99));

        when(itemRepo.findByName(itemName)).thenReturn(Arrays.asList(item1,item2));
        ResponseEntity<List<Item>> itemResponse = itemController.getItemsByName("Square Widget");
        assertEquals(404, itemResponse.getStatusCodeValue());
    }
}
