package com.chilun.chatgpt.strategy.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chilun.chatgpt.pojo.Entry;
import kong.unirest.HttpResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * @auther 齿轮
 * @create 2023-11-02-16:39
 */
public class JSONTools {
    public static String getWYYXResultFromResponse(HttpResponse<String> response) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if (!jsonObject.containsKey("result")) {
            System.out.println("------------文言一心原响应中无法提取result");
            throw new Exception("------------文言一心原响应中无法提取result：" + response.getBody());
        }
        return jsonObject.getString("result");
    }

    public static String getGPTResultFromResponse(HttpResponse<String> response) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if (!jsonObject.containsKey("choices")) {
            System.out.println("------------GPT3_5原响应中无法提取choices");
            throw new Exception("------------GPT3_5原响应中无法提取choices：" + response.getBody());
        }
        return jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
    }

    /*
     * 创建.json文件
     * 要求：FUllFileName包含完整路径+文件名+后缀
     * */
    public static String saveInJsonFile(List<Entry> list, String FUllFileName) {
        PrintStream ps = null;
        try {
            File file = new File(FUllFileName);
            file.createNewFile();
            ps = new PrintStream(new FileOutputStream(file));
            ps.print(JSON.toJSONString(list));
        } catch (IOException e) {
            e.printStackTrace();
            return "JSON持久化失败" + e.getMessage();
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "JSON持久化完成";
    }
}
