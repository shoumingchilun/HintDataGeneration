package com.chilun.chatgpt.strategy;

import com.chilun.chatgpt.pojo.Entry;
import com.chilun.chatgpt.strategy.tool.EXCELTools;
import com.chilun.chatgpt.strategy.tool.JSONTools;
import com.chilun.chatgpt.strategy.tool.UnirestTools;
import com.chilun.chatgpt.strategy.tool.RequestTools;
import kong.unirest.HttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @auther 齿轮
 * @create 2023-11-02-21:10
 */
public class WYYXStrategy {
    public String catalogue = "#脱敏";
    public String QUESTION_TYPE = "解释和定义_3类";  //问答类型
    public String MODEL = "文言一心";    //使用模型
    public String AMBIT = "军事";   //生成相关领域的问题
    public String access_token = "#脱敏";   //使用模型所需TOKEN
    public String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";   //最终文件名+路径为：
    // catalogue + FILENAME_PREFIX + System.currentTimeMillis() + "_约包含" + num * 10 + "条数据" + 后缀名如.json

    public String GENERATE_QUESTION_PROMPTS =    //生成问题的提问词
            "请提供10个解释和定义类问题，与" + "$AMBIT$" + "相关，问题为：【某个名词】?，问题仅由一个名词组成！" +
                    "举例：问题1：武装直升机？" +
                    "问题2：网络战争？" +
                    "问题3：歼灭战？";
    public Predicate<String> JUDGE_SUITABLE_QUESTION     //检测原始问题是否合规
            = s -> s.length() >= 2 && s.length() <= 100 &&
            (s.contains("？"));
    public Function<String, String> FORMAT_PRIMAL_QUESTION       //格式化原始问题
            = s -> {
        s = s.substring(0, s.indexOf("？") + 1);
        if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
            return s.substring(s.indexOf("：") + 1);
        } else if (s.contains(".")) {
            return s.substring(s.indexOf(".") + 1);
        } else return s;
    };

    public Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
            question +
                    "    回答格式（总分总结构）：" +
                    "我建议/认为我们可以....   " +
                    "首先....   " +
                    "其次....   " +
                    "....";
    public Predicate<String> JUDGE_SUITABLE_ANSWER     //检测原始回答是否合规
            = s -> s.length() >= 80;
    public Function<String, String> FORMAT_PRIMAL_ANSWER       //优化原始回答
            = s -> s.replace(" ", "")
            .replace("\n\n", "\n")
            .replace("*", "");

    public WYYXStrategy(String catalogue,
                        String QUESTION_TYPE,
                        String MODEL,
                        String AMBIT,
                        String access_token,
                        String FILENAME_PREFIX,
                        String GENERATE_QUESTION_PROMPTS,
                        Predicate<String> JUDGE_SUITABLE_QUESTION,
                        Function<String, String> FORMAT_PRIMAL_QUESTION,
                        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION,
                        Predicate<String> JUDGE_SUITABLE_ANSWER,
                        Function<String, String> FORMAT_PRIMAL_ANSWER) {
        this.catalogue = catalogue;
        this.QUESTION_TYPE = QUESTION_TYPE;
        this.MODEL = MODEL;
        this.AMBIT = AMBIT;
        this.access_token = access_token;
        this.FILENAME_PREFIX = FILENAME_PREFIX;
        this.GENERATE_QUESTION_PROMPTS = GENERATE_QUESTION_PROMPTS;
        this.JUDGE_SUITABLE_QUESTION = JUDGE_SUITABLE_QUESTION;
        this.FORMAT_PRIMAL_QUESTION = FORMAT_PRIMAL_QUESTION;
        this.GENERATE_QUESTION_PROMPTS_FUNCTION = GENERATE_QUESTION_PROMPTS_FUNCTION;
        this.JUDGE_SUITABLE_ANSWER = JUDGE_SUITABLE_ANSWER;
        this.FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER;
    }

    public WYYXStrategy() {
    }

    //生成数量为integer*10的规范问题集合
    public List<Entry> GENERATE_FORMATTED_QUESTION_COLLECTION(int num) {
        GENERATE_QUESTION_PROMPTS = GENERATE_QUESTION_PROMPTS.replace("$AMBIT$", AMBIT);
        //获得WYYX原始问题响应集合
        List<HttpResponse<String>> ResponseList = UnirestTools.sendWYYXRequests(access_token,
                RequestTools.createWYYXRequestBody(null, 1.0, GENERATE_QUESTION_PROMPTS),
                num,
                3);

        //开始格式化
        List<Entry> list = new ArrayList<>();
        for (HttpResponse<String> response : ResponseList) {
            try {
                ADD_FORMATTED_QUESTION_TO_LIST(response, list);
            } catch (Exception e) {
                System.out.println("---------------" + e.getMessage() + "原始响应：" + response.getBody());
                e.printStackTrace();
            }
        }
        return list;
    }

    //将原始响应解析添加到规范问题集合
    private void ADD_FORMATTED_QUESTION_TO_LIST(HttpResponse<String> response, List<Entry> list)
            throws Exception {
        int oldSize = list.size();
        String result = JSONTools.getWYYXResultFromResponse(response);
        Scanner scanner = new Scanner(result);
        while (scanner.hasNext()) {
            String s1 = scanner.nextLine();
            if (JUDGE_SUITABLE_QUESTION.test(s1)) {
                list.add(new Entry(FORMAT_PRIMAL_QUESTION.apply(s1), null));
            } else {
                System.out.println("----------------格式化问题：" + s1 + "_不通过_原因：过长或未检测到关键词--------------");
            }
        }
        if (list.size() == oldSize) {   //说明原始问题有异常，或检测函数有异常
            throw new Exception("------------原始问题有异常，或检测函数有异常，无可格式化数据：" + result);
        }
    }

    public void GENERATE_FORMATTED_ANSWER_COLLECTION(List<Entry> list) {//为规范问题集合提供规范解答，并去除无法解答问题
        for (int i = 0; i < list.size(); i++) {
            Entry entry = list.get(i);
            System.out.println("----------------正在处理第" + i + "个问题---------------");
            boolean hasError = false;
            String result = null;
            HttpResponse<String> response = null;
            if (entry.getAnswer() == null) {
                try {
                    response = UnirestTools.sendWYYXRequest(access_token,
                            RequestTools.createWYYXRequestBody(1.0, null,
                                    GENERATE_QUESTION_PROMPTS_FUNCTION.apply(entry.getQuestion())
                            )
                    );
                    result = JSONTools.getWYYXResultFromResponse(response);
                } catch (Exception e) {     //出现异常，记录并重试
                    hasError = true;
                    e.printStackTrace();
                }
                if (hasError || !JUDGE_SUITABLE_ANSWER.test(result)) {//出现问题，进行一次重试
                    hasError = false;
                    try {
                        response = UnirestTools.sendWYYXRequest(access_token,
                                RequestTools.createWYYXRequestBody(1.0, null,
                                        GENERATE_QUESTION_PROMPTS_FUNCTION.apply(entry.getQuestion())));
                        result = JSONTools.getWYYXResultFromResponse(response);
                    } catch (Exception e) {
                        hasError = true;
                        e.printStackTrace();
                    }
                }
                if (hasError || !JUDGE_SUITABLE_ANSWER.test(result)) {//再次出现问题，跳过
                    list.remove(entry);
                    i--;
                } else {
                    entry.setAnswer(FORMAT_PRIMAL_ANSWER.apply(result));
                }
            }
        }
    }

    public String SAVE_FINAL_QUESTION_ANSWER_COLLECTION(List<Entry> list) {//持久化最终集合
        return EXCELTools.saveInExcelFile(list,
                catalogue + FILENAME_PREFIX + System.currentTimeMillis() + "_包含" + list.size() + "条" + AMBIT + "类数据.xlsx");
    }
}
