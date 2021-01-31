package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import com.splunk.Args;
import com.splunk.Receiver;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CartControllerTest {
    private CartController cartController;
    private UserController userController;
    private UserRepository userRepo = mock(UserRepository.class);
    private CartRepository cartRepo = mock(CartRepository.class);
    private ItemRepository itemRepo = mock(ItemRepository.class);
    private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
    private Receiver receiverUser = mock(Receiver.class);
    private Receiver receiverCart = mock(Receiver.class);
    private Args args = mock(Args.class);

    @Before
    public void setup()
    {
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepo);
        TestUtils.injectObjects(userController, "cartRepository", cartRepo);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", encoder);
        TestUtils.injectObjects(userController, "receiver", receiverUser);
        TestUtils.injectObjects(userController, "args", args);

        cartController = new CartController();
        TestUtils.injectObjects(cartController, "userRepository", userRepo);
        TestUtils.injectObjects(cartController, "cartRepository", cartRepo);
        TestUtils.injectObjects(cartController, "itemRepository", itemRepo);
        TestUtils.injectObjects(cartController, "receiver", receiverCart);
        TestUtils.injectObjects(cartController, "args", args);
    }

    @Test
    public void addToCartHappyPath() throws JSONException {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test1");
        createUserRequest.setPassword("testPassword");
        createUserRequest.setConfirmPassword("testPassword");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        User user = response.getBody();
        assertNotNull(user);

        int itemId = 1;
        int itemQuantity = 5;

        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("test1");
        request.setItemId(itemId);
        request.setQuantity(itemQuantity);

        Item item = new Item();
        item.setDescription("A widget that is round");
        item.setId(1L);
        item.setName("Round Widget");
        item.setPrice(BigDecimal.valueOf(2.99));

        List<Item> itemList = new ArrayList<>();

        for(int i=0; i<itemQuantity; i++)
        {
            itemList.add(item);
        }

        when(userRepo.findByUsername("test1")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> cartResponse = cartController.addTocart(request);
        Cart cart = cartResponse.getBody();
        assertEquals(200, cartResponse.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(14.95), cart.getTotal());
        assertEquals(itemList,cart.getItems());
    }

    @Test
    public void removeFromCartHappyPath() throws JSONException {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test1");
        createUserRequest.setPassword("testPassword");
        createUserRequest.setConfirmPassword("testPassword");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        User user = response.getBody();
        assertNotNull(user);

        int itemId = 1;
        int itemQuantity = 5;

        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("test1");
        request.setItemId(itemId);
        request.setQuantity(itemQuantity);

        Item item = new Item();
        item.setDescription("A widget that is round");
        item.setId(1L);
        item.setName("Round Widget");
        item.setPrice(BigDecimal.valueOf(2.99));

        List<Item> itemList = new ArrayList<>();

        for(int i=0; i<itemQuantity; i++)
        {
            itemList.add(item);
        }

        when(userRepo.findByUsername("test1")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> cartResponse = cartController.addTocart(request);
        Cart cart = cartResponse.getBody();
        assertEquals(200, cartResponse.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(14.95), cart.getTotal());
        assertEquals(itemList,cart.getItems());

        user.setCart(cart);

        int removeItemQuantity = 2;
        ModifyCartRequest removeRequest = new ModifyCartRequest();
        removeRequest.setUsername("test1");
        removeRequest.setItemId(itemId);
        removeRequest.setQuantity(removeItemQuantity);

        when(userRepo.findByUsername("test1")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        cartResponse = cartController.removeFromcart(removeRequest);
        cart = cartResponse.getBody();
        assertEquals(200, cartResponse.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(8.97), cart.getTotal());
        itemList.remove(itemList.size()-1);
        itemList.remove(itemList.size()-1);
        assertEquals(itemList,cart.getItems());
    }

    @Test
    public void addToCartErrorPathUserNotFound() throws JSONException {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test1");
        createUserRequest.setPassword("testPassword");
        createUserRequest.setConfirmPassword("testPassword");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        User user = response.getBody();
        assertNotNull(user);

        int itemId = 1;
        int itemQuantity = 5;

        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("test2");
        request.setItemId(itemId);
        request.setQuantity(itemQuantity);

        Item item = new Item();
        item.setDescription("A widget that is round");
        item.setId(1L);
        item.setName("Round Widget");
        item.setPrice(BigDecimal.valueOf(2.99));

        when(userRepo.findByUsername("test1")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> cartResponse = cartController.addTocart(request);
        assertEquals(404, cartResponse.getStatusCodeValue());
    }

    @Test
    public void addToCartErrorPathItemNotFound() throws JSONException {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test1");
        createUserRequest.setPassword("testPassword");
        createUserRequest.setConfirmPassword("testPassword");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        User user = response.getBody();
        assertNotNull(user);

        int itemId = 2;
        int itemQuantity = 5;

        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("test1");
        request.setItemId(itemId);
        request.setQuantity(itemQuantity);

        Item item = new Item();
        item.setDescription("A widget that is round");
        item.setId(1L);
        item.setName("Round Widget");
        item.setPrice(BigDecimal.valueOf(2.99));

        when(userRepo.findByUsername("test1")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> cartResponse = cartController.addTocart(request);
        assertEquals(404, cartResponse.getStatusCodeValue());
    }

    @Test
    public void removeFromCartErrorPathUserNotFound() throws JSONException {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test1");
        createUserRequest.setPassword("testPassword");
        createUserRequest.setConfirmPassword("testPassword");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        User user = response.getBody();
        assertNotNull(user);

        int itemId = 1;
        int itemQuantity = 5;

        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("test1");
        request.setItemId(itemId);
        request.setQuantity(itemQuantity);

        Item item = new Item();
        item.setDescription("A widget that is round");
        item.setId(1L);
        item.setName("Round Widget");
        item.setPrice(BigDecimal.valueOf(2.99));

        List<Item> itemList = new ArrayList<>();

        for(int i=0; i<itemQuantity; i++)
        {
            itemList.add(item);
        }

        when(userRepo.findByUsername("test1")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> cartResponse = cartController.addTocart(request);
        Cart cart = cartResponse.getBody();
        assertEquals(200, cartResponse.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(14.95), cart.getTotal());
        assertEquals(itemList,cart.getItems());

        user.setCart(cart);

        int removeItemQuantity = 2;
        ModifyCartRequest removeRequest = new ModifyCartRequest();
        removeRequest.setUsername("test2");
        removeRequest.setItemId(itemId);
        removeRequest.setQuantity(removeItemQuantity);

        when(userRepo.findByUsername("test1")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        cartResponse = cartController.removeFromcart(removeRequest);
        assertEquals(404, cartResponse.getStatusCodeValue());
    }

    @Test
    public void removeFromCartErrorPathItemNotFound() throws JSONException {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test1");
        createUserRequest.setPassword("testPassword");
        createUserRequest.setConfirmPassword("testPassword");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        User user = response.getBody();
        assertNotNull(user);

        long itemId = 1;
        int itemQuantity = 5;

        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("test1");
        request.setItemId(itemId);
        request.setQuantity(itemQuantity);

        Item item = new Item();
        item.setDescription("A widget that is round");
        item.setId(itemId);
        item.setName("Round Widget");
        item.setPrice(BigDecimal.valueOf(2.99));

        List<Item> itemList = new ArrayList<>();

        for(int i=0; i<itemQuantity; i++)
        {
            itemList.add(item);
        }

        when(userRepo.findByUsername("test1")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> cartResponse = cartController.addTocart(request);
        Cart cart = cartResponse.getBody();
        assertEquals(200, cartResponse.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(14.95), cart.getTotal());
        assertEquals(itemList,cart.getItems());

        user.setCart(cart);

        int removeItemQuantity = 2;
        ModifyCartRequest removeRequest = new ModifyCartRequest();
        removeRequest.setUsername("test1");

        long errorItemId = 2;
        removeRequest.setItemId(errorItemId);
        removeRequest.setQuantity(removeItemQuantity);

        when(userRepo.findByUsername("test1")).thenReturn(user);
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));

        cartResponse = cartController.removeFromcart(removeRequest);
        assertEquals(404, cartResponse.getStatusCodeValue());

    }
}
