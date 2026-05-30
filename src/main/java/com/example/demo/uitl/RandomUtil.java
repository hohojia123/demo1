package com.example.demo.uitl;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomUtil {
  private Random random=   new Random();
    public synchronized int nextInt(int start, int end) {
        // +1:包括右界值
        return random.nextInt(end - start) + start + 1;
    }

}
