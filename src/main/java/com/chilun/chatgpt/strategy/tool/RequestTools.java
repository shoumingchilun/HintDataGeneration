package com.chilun.chatgpt.strategy.tool;

/**
 * @auther 齿轮
 * @create 2023-11-02-16:31
 */
public class RequestTools {
    /*
     * 创建文言一心模型所需请求的RequestBody部分。
     * 要求：temperature和top_p必须有一个为空，content不为空。
     * */
    public static String createWYYXRequestBody(Double temperature, Double top_p, String content) {
        return " { " +
                (top_p == null ? "" : "\"top_p\": " + top_p + " , ") +
                (temperature == null ? "" : "\"temperature\": " + temperature + " , ") +
                "\"messages\": [ " +
                " { " +
                "\"role\": \"user\", " +
                "\"content\": \"" + content + "\"" +
                " }" +
                " ] " +
                " } ";
    }

    /*
     * 创建GPT3.5模型所需请求的RequestBody部分。
     * 要求：temperature和top_p必须有一个为空，content不为空。
     * */
    public static String createGPT3_5RequestBody(Double temperature, Double top_p, String content) {
        return "{" +
                "    \"model\": \"gpt-3.5-turbo\"," +
                (top_p == null ? "" : "\"top_p\": " + top_p + ",") +
                (temperature == null ? "" : "\"temperature\": " + temperature + " , ") +
                "    \"messages\": [" +
                "        {" +
                "            \"role\": \"user\"," +
                "            \"content\": \"" + content + "\"" +
                "        }" +
                "    ]" +
                "}";
    }

    public static String createGPT4RequestBody(Double temperature, Double top_p, String content) {
        return "{" +
                "    \"model\": \"gpt-4\"," +
                (top_p == null ? "" : "\"top_p\": " + top_p + ",") +
                (temperature == null ? "" : "\"temperature\": " + temperature + " , ") +
                "    \"messages\": [" +
                "        {" +
                "            \"role\": \"user\"," +
                "            \"content\": \"" + content + "\"" +
                "        }" +
                "    ]" +
                "}";
    }
}
