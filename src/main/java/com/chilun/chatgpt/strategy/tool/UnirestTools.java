package com.chilun.chatgpt.strategy.tool;

import io.github.asleepyfish.util.OpenAiUtils;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @auther 齿轮
 * @create 2023-11-02-16:53
 */
public class UnirestTools {
    public static String proxy_host = "127.0.0.1";
    public static int proxy_port = 33210;


    public static String sendGPTRequest2(String content) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.out.println("发送请求：" + content);
        OpenAiUtils.createStreamChatCompletion(content, byteArrayOutputStream);
        String s = byteArrayOutputStream.toString("UTF-8");
        byteArrayOutputStream.close();
        System.out.println("生成响应：" + s);
        return s;
    }

    public static HttpResponse<String> sendGPTRequest(String token, String body) {
        System.out.println("UnirestTools：---------------开始发送GPT请求：" + body);
        Unirest.config().reset();
        Unirest.config().proxy(proxy_host, proxy_port);       //设置代理
        Unirest.config().socketTimeout(120000);
        Unirest.config().connectTimeout(120000);
        HttpResponse<String> response = Unirest.post("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Host", "api.openai.com")
                .header("Connection", "keep-alive")
                .body(body)
                .asString();
        System.out.println("UnirestTools：--------------获得响应：" + response.getBody());
        return response;
    }

    public static HttpResponse<String> sendWYYXRequest(String token, String body) {
        HttpResponse<String> response = Unirest.post("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant?access_token=" + token)
                .header("Content-Type", "application/json")
                .body(body)
                .asString();
        System.out.println("UnirestTools：-------------请求：" + body + "----------------\n--------------获得响应：" + response.getBody());
        return response;
    }

    public static List<HttpResponse<String>> sendWYYXRequests(String token, String body, int num, int maxTimes) {
        List<HttpResponse<String>> ResponseList = new ArrayList<>();
        int times = 0;  //已重试次数
        for (int i = 0; i < num; i++) {
            System.out.println("UnirestTools：----------------第" + i + "次生成响应");
            try {
                ResponseList.add(sendWYYXRequest(token, body));
            } catch (Exception e) {
                e.printStackTrace();
                if ((times++) <= maxTimes) { //重试机制，最多重试maxTimes次
                    i--;
                } else {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        return ResponseList;
    }
}
