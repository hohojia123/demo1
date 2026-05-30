package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private RedisTemplate redisTemplate;

	public void test() {
		redisTemplate.opsForValue().set("test_key", "test_value");
		System.out.println("写入完成");
	}

}
