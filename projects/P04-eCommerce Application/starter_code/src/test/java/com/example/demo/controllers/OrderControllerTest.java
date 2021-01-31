package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderControllerTest {
    private OrderController orderController;
    private UserController userController;
    private CartController cartController;
    private UserRepository userRepo = mock(UserRepository.class);
    private OrderRepository orderRepo = mock(OrderRepository.class);
    private CartRepository cartRepo = mock(CartRepository.class);
    private ItemRepository itemRepo = mock(ItemRepository.class);
    private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
    private Receiver receiver = mock(Receiver.class);
    private Args args = mock(Args.class);

    @Before
    public void setup()
    {
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepo);
        TestUtils.injectObjects(userController, "cartRepository", cartRepo);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", encoder);
        TestUtils.injectObjects(userController, "receiver", receiver);
        TestUtils.injectObjects(userController, "args", args);

        cartController = new CartController();
        TestUtils.injectObjects(cartController, "userRepository", userRepo);
        TestUtils.injectObjects(cartController, "cartRepository", cartRepo);
        TestUtils.injectObjects(cartController, "itemRepository", itemRepo);
        TestUtils.injectObjects(cartController, "receiver", receiver);
        TestUtils.injectObjects(cartController, "args", args);

        orderController = new OrderController();
        TestUtils.injectObjects(orderController, "userRepository", userRepo);
        TestUtils.injectObjects(orderController, "orderRepository", orderRepo);
        TestUtils.injectObjects(orderController, "receiver", receiver);
        TestUtils.injectObjects(orderController, "args", args);
    }

    @Test
    public void submit() throws JSONException {
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
        int itemQuantity = 6;

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
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> cartResponse = cartController.addTocart(request);
        Cart cart = cartResponse.getBody();
        assertEquals(200, cartResponse.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(17.94), cart.getTotal());
        assertEquals(itemList,cart.getItems());

        cart.setUser(user);
        user.setCart(cart);

        when(userRepo.findByUsername("test1")).thenReturn(user);
        final ResponseEntity<UserOrder> responseOrderSave = orderController.submit("test1");
        assertNotNull(responseOrderSave);
        assertEquals(200, responseOrderSave.getStatusCodeValue());
        UserOrder userOrder = responseOrderSave.getBody();
        assertEquals(itemList,userOrder.getItems());
        assertEquals(BigDecimal.valueOf(17.94),userOrder.getTotal());
        assertEquals(user,userOrder.getUser());
    }

    @Test
    public void getOrdersForUserTest() throws JSONException {
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
        int itemQuantity = 6;

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
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> cartResponse = cartController.addTocart(request);
        Cart cart = cartResponse.getBody();
        assertEquals(200, cartResponse.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(17.94), cart.getTotal());
        assertEquals(itemList,cart.getItems());

        cart.setUser(user);
        user.setCart(cart);

        when(userRepo.findByUsername("test1")).thenReturn(user);
        final ResponseEntity<UserOrder> responseOrderSave = orderController.submit("test1");
        assertNotNull(responseOrderSave);
        assertEquals(200, responseOrderSave.getStatusCodeValue());
        UserOrder userOrder = responseOrderSave.getBody();
        assertEquals(itemList,userOrder.getItems());
        assertEquals(BigDecimal.valueOf(17.94),userOrder.getTotal());
        assertEquals(user,userOrder.getUser());

        when(userRepo.findByUsername("test1")).thenReturn(user);
        when(orderRepo.findByUser(user)).thenReturn(Arrays.asList(userOrder));
        final ResponseEntity<List<UserOrder>> responseOrderGet = orderController.getOrdersForUser("test1");
        assertNotNull(responseOrderSave);
        assertEquals(200, responseOrderSave.getStatusCodeValue());
        List<UserOrder> userOrderList = responseOrderGet.getBody();
        UserOrder userOrderRetrieved = userOrderList.get(0);
        assertEquals(itemList,userOrderRetrieved.getItems());
        assertEquals(BigDecimal.valueOf(17.94),userOrderRetrieved.getTotal());
        assertEquals(user,userOrderRetrieved.getUser());
    }

    @Test
    public void submitIncorrectUsername() throws JSONException {
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
        int itemQuantity = 6;

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
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> cartResponse = cartController.addTocart(request);
        Cart cart = cartResponse.getBody();
        assertEquals(200, cartResponse.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(17.94), cart.getTotal());
        assertEquals(itemList,cart.getItems());

        cart.setUser(user);
        user.setCart(cart);

        when(userRepo.findByUsername("test1")).thenReturn(user);
        final ResponseEntity<UserOrder> responseOrderSave = orderController.submit("test2");
        assertEquals(404, responseOrderSave.getStatusCodeValue());
    }

    @Test
    public void getOrdersForUserTestIncorrectUsername() throws JSONException {
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
        int itemQuantity = 6;

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
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> cartResponse = cartController.addTocart(request);
        Cart cart = cartResponse.getBody();
        assertEquals(200, cartResponse.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(17.94), cart.getTotal());
        assertEquals(itemList,cart.getItems());

        cart.setUser(user);
        user.setCart(cart);

        when(userRepo.findByUsername("test1")).thenReturn(user);
        final ResponseEntity<List<UserOrder>> responseOrderSave = orderController.getOrdersForUser("test2");
        assertEquals(404, responseOrderSave.getStatusCodeValue());
    }
}
