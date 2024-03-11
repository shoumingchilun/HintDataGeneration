package com.chilun.chatgpt;

import io.github.asleepyfish.annotation.EnableChatGPT;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @auther 齿轮
 * @create 2023-10-31-21:44
 */
@SpringBootApplication
@EnableChatGPT
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}