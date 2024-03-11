package com.chilun.chatgpt.strategy.tool;

import com.alibaba.fastjson.JSON;
import com.chilun.chatgpt.pojo.Dialogue;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther 齿轮
 * @create 2023-11-03-20:16
 */
public class DialogueTools {
    public static MyRequest request = new MyRequest("gpt-4", 1.4, null, new ArrayList<Dialogue>());

    public static String sendGPTSuperQuestionRequest_GetGPTResultFromResponse(String token, String content) throws Exception {
        request.messages.add(new Dialogue("user", content));
        System.out.println("---------------开始发送GPT连续问题请求：" + JSON.toJSONString(request));
        Unirest.config().reset();
        Unirest.config().proxy(UnirestTools.proxy_host, UnirestTools.proxy_port);       //设置代理
        Unirest.config().socketTimeout(180000);
        Unirest.config().connectTimeout(180000);
        HttpResponse<String> response = Unirest.post("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Host", "api.openai.com")
                .header("Connection", "keep-alive")
                .body(JSON.toJSONString(request))
                .asString();
        System.out.println("--------------获得响应：" + response.getBody());
        String response1 = JSONTools.getGPTResultFromResponse(response);
        Dialogue assistant = new Dialogue("assistant", response1);
        request.messages.add(assistant);
        return response1;
    }

    public static HttpResponse<String> sendGPTSuperQuestionRequest_GetGPTResultFromResponse2(String token, String content) throws Exception {
        request.messages.add(new Dialogue("user", content));
        System.out.println("---------------开始发送GPT连续问题请求：" + JSON.toJSONString(request));
        Unirest.config().reset();
        Unirest.config().proxy(UnirestTools.proxy_host, UnirestTools.proxy_port);       //设置代理
        Unirest.config().socketTimeout(180000);
        Unirest.config().connectTimeout(180000);
        HttpResponse<String> response = Unirest.post("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Host", "api.openai.com")
                .header("Connection", "keep-alive")
                .body(JSON.toJSONString(request))
                .asString();
        System.out.println("--------------获得响应：" + response.getBody());
        String response1 = JSONTools.getGPTResultFromResponse(response);
        Dialogue assistant = new Dialogue("assistant", response1);
        request.messages.add(assistant);
        return response;
    }

    public static void clearContext() {
        request.messages.clear();
    }


}


class MyRequest {
    public String model;
    public Double temperature;
    public Double top_p;
    public List<Dialogue> messages;

    public MyRequest(String model, Double temperature, Double top_p, List<Dialogue> messages) {
        this.model = model;
        this.temperature = temperature;
        this.top_p = top_p;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTop_p() {
        return top_p;
    }

    public void setTop_p(Double top_p) {
        this.top_p = top_p;
    }

    public List<Dialogue> getMessages() {
        return messages;
    }

    public void setMessages(List<Dialogue> messages) {
        this.messages = messages;
    }

    public MyRequest() {
    }
}