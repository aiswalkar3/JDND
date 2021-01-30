package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerTest {
    private UserController userController;
    private UserRepository userRepo = mock(UserRepository.class);
    private CartRepository cartRepo = mock(CartRepository.class);
    private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);

    @Before
    public void setup()
    {
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepo);
        TestUtils.injectObjects(userController, "cartRepository", cartRepo);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", encoder);
    }

    /*
    * Test to check if the user is created successfully.
    * */
    @Test
    public void createUserHappyPath() throws Exception {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test");
        createUserRequest.setPassword("testPassword");
        createUserRequest.setConfirmPassword("testPassword");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        User u = response.getBody();
        assertNotNull(u);
        assertEquals(0, u.getId());
        assertEquals("test", u.getUsername());
        assertEquals("thisIsHashed", u.getPassword());
    }

    /*
     * Test to check if the user is created successfully and is retrieved when queried by id successfully.
     * */
    @Test
    public void findByIdHappyPath() throws Exception
    {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test");
        createUserRequest.setPassword("testPassword");
        createUserRequest.setConfirmPassword("testPassword");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);
        assertEquals(200, response.getStatusCodeValue());
        User u = response.getBody();

        when(userRepo.findById(u.getId())).thenReturn(Optional.of(u));

        ResponseEntity<User> retrieved = userController.findById(u.getId());
        assertEquals(200, response.getStatusCodeValue());
        User retrievedUser = retrieved.getBody();
        assertEquals(u.getId(), retrievedUser.getId());
        assertEquals(u.getUsername(), retrievedUser.getUsername());
    }

    /*
     * Test to check if the user is created successfully and is retrieved when queried by username successfully.
     * */
    @Test
    public void findByUserNameHappyPath() throws Exception {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test-user");
        createUserRequest.setPassword("testPassword");
        createUserRequest.setConfirmPassword("testPassword");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);
        assertEquals(200, response.getStatusCodeValue());
        User user = response.getBody();

        when(userRepo.findByUsername(user.getUsername())).thenReturn(user);

        ResponseEntity<User> retrieved = userController.findByUserName(user.getUsername());
        assertEquals(200, response.getStatusCodeValue());
        User retrievedUser = retrieved.getBody();
        assertEquals(user.getId(), retrievedUser.getId());
        assertEquals(user.getUsername(), retrievedUser.getUsername());
    }

    /*
     * Test to check if the user is not created when the confirm password is not equal to password.
     * */
    @Test
    public void createUserErrorPathPasswordNotEqualsConfirmPassword() throws JSONException {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test");
        createUserRequest.setPassword("testPassword");
        createUserRequest.setConfirmPassword("test");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);
        assertEquals(400, response.getStatusCodeValue());
    }

    /*
     * Test to check if the user is not created when the username length is less than 7.
     * */
    @Test
    public void createUserErrorPathPasswordLengthLessThanRequired() throws JSONException {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test-user");
        createUserRequest.setPassword("test");
        createUserRequest.setConfirmPassword("test");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);
        assertEquals(400, response.getStatusCodeValue());
    }
}
