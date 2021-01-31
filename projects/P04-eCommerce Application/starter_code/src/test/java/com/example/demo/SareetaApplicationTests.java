package com.example.demo;

import com.example.demo.controllers.CartControllerTest;
import com.example.demo.controllers.ItemControllerTest;
import com.example.demo.controllers.OrderControllerTest;
import com.example.demo.controllers.UserControllerTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/*
@RunWith(SpringRunner.class)
@SpringBootTest
*/
@RunWith(Suite.class)
@SpringBootTest
@Suite.SuiteClasses({CartControllerTest.class, ItemControllerTest.class, OrderControllerTest.class, UserControllerTest.class})
public class SareetaApplicationTests {
	/*
	@Test
	public void contextLoads() {
	}
	*/

}
