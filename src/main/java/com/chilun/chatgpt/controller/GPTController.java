package com.chilun.chatgpt.controller;

import com.chilun.chatgpt.pojo.Entry;
import com.chilun.chatgpt.strategy.GPTStrategy;
import com.chilun.chatgpt.strategy.tool.UnirestTools;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @auther 齿轮
 * @create 2023-11-04-14:02
 */
@RestController
@RequestMapping("/gpt")
public class GPTController {
    public static String catalogue = "C:\\Users\\齿轮\\Desktop\\得能数据生成\\GPT\\";//GPT
    public static String MODEL = "GPT3-5";    //使用模型
    public static String access_token = "#脱敏";

    public static Predicate<String> JUDGE_SUITABLE_QUESTION1 = //检测原始问题是否合规的函数
            s -> s.length() >= 2 && s.length() <= 100
                    && !s.contains("以下") && !s.contains("抱歉") && !s.contains("AI") //是否是首句或是否无效
                    && (s.contains("？") || s.contains("?") || s.contains(".") || s.contains("、") || s.contains("："));//是否是问题
    public Function<String, String> FORMAT_PRIMAL_QUESTION1 =       //格式化原始问题的函数
            s -> {
                s = s.replace("\"", "");
                if (s.contains("：")) {
                    return s.substring(s.indexOf("：") + 1);
                } else if (s.contains(":")) {
                    return s.substring(s.indexOf(":") + 1);
                } else if (s.contains(".")) {
                    return s.substring(s.indexOf(".") + 1);
                } else if (s.contains("、")) {
                    return s.substring(s.indexOf("、") + 1);
                } else {
                    return s;
                }
            };
    public static Predicate<String> JUDGE_SUITABLE_ANSWER1 = //检测原始回答是否合规的函数
            s -> s.length() >= 80;
    public static Function<String, String> FORMAT_PRIMAL_ANSWER1 = //优化原始回答的函数
            s -> s.replace(" ", "").replace("\n\n", "\n");

    @GetMapping("/test")
    public String test(@RequestParam String content) throws Exception {
        return UnirestTools.sendGPTRequest2(content);
    }

    private String Ret(LocalTime start, DateTimeFormatter formatter, List<Entry> list, int questionNum, LocalTime ansBegin, LocalTime saveBegin, String save, LocalTime saveEnd) {
        return save + "\n" +
                "开始时间：" + start.format(formatter) + "\n" +
                "结束时间：" + saveEnd.format(formatter) + "\n" +
                "问题生成耗时：" + Duration.between(start, ansBegin).toMillis() / 60000 + "分钟，共生成" + questionNum + "条问题" +
                "问题回答耗时：" + Duration.between(ansBegin, saveBegin).toMillis() / 60000 + "分钟，共生成" + list.size() + "条回答" +
                "数据保存耗时：" + Duration.between(saveBegin, saveEnd).toMillis() / 1000 + "秒";
    }

    @GetMapping("/compare1")
    public String compare1(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "比较_1类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个比较类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：比较.......和......，解释它们各自的优缺点。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答的模版：“A是.......，B是.......，两者的区别是......。A的优点是......；缺点是......。B的优点是......；缺点是......。回答的字数在200到800之间。”";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/compare2")
    public String compare2(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "比较_2类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个比较类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：A和B你会怎么选择？/a还是b？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“我选择a/b，理由是:.....”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。回答的字数在200到800之间。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/definition1")
    public String definition1(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "解释和定义_1类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：你能够解释一下......的意义或影响吗？。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“...是指……，...的意义或影响在于…”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/definition2")
    public String definition2(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "解释和定义_2类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：请介绍一下关于......的基本情况。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“....的基本情况是/基本要素是......”，后面分点概述，首先...其次... ... 最后...";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/definition3")
    public String definition3(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "解释和定义_3类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题格式为：“【某个名词】？”，注意问题仅包含一个名词，并以“？”结尾。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“...是指……”，后面分点概述“......主要包括以下几点：”，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/definition4")
    public String definition4(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "解释和定义_4类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：......的主要特征是什么？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“......的主要特征是：..…”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/definition5")
    public String definition5(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "解释和定义_5类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：......的作用是......？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“......的作用是：..…”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/definition6")
    public String definition6(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "解释和定义_6类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：请解释一下......的工作原理？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“......的工作原理是..…”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/definition7")
    public String definition7(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "解释和定义_7类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：......的意义。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“......的意义是：”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/definition8")
    public String definition8(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "解释和定义_8类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：请问什么是......？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“...的定义是......”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/definition9")
    public String definition9(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "解释和定义_9类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：......有哪些？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“...是指……，主要有：”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/definition10")
    public String definition10(@RequestParam int num, String AMBIT) {
        LocalTime start = LocalTime.now();          //开始记时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String QUESTION_TYPE = "解释和定义_10类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：......有什么好处？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“.....好处有：”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);       //生成问题
        int questionNum = list.size();           //记录问题数量
        LocalTime ansBegin = LocalTime.now();        //记录开始回答时间
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);                 //回答问题
        LocalTime saveBegin = LocalTime.now();          //记录开始保存时间
        String save = gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);          //保存数据
        LocalTime saveEnd = LocalTime.now();            //记录保存结束时间
        return Ret(start, formatter, list, questionNum, ansBegin, saveBegin, save, saveEnd);
    }

    @GetMapping("/assumption1")
    public String assumption1(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "假设式_1类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个假设式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：假设......，会怎么样？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“假设....，会......”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/assumption2")
    public String assumption2(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "假设式_2类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个假设式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：假设......，会发生什么？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“假设....，会发送......”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/assumption3")
    public String assumption3(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "假设式_3类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个假设式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：如果你有机会......，你会怎么做？为什么？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“如果我有机会做......，我会....，我这样选择的理由是：....”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/assumption4")
    public String assumption4(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "假设式_4类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个假设式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：如果......，你会怎么办？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“如果......，我会....”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/assumption5")
    public String assumption5(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "假设式_5类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个假设式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：如果没有......，会变成什么样？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“如果没有......，会变成....。以下是可能的一些影响：”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/assumption6")
    public String assumption6(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "假设式_6类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个假设式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：我们为什么不试试......？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“因为......”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open1")
    public String open1(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "开放式_1类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：对于......问题，您有什么建议或者解决方案吗？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，使用模版为：”我建议/认为我们可以“....”首先“.... ”其次“.... ....";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open2")
    public String open2(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "开放式_2类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：请谈谈你对......的看法。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“我认为......”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open3")
    public String open3(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "开放式_3类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：如何理解.......？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“.....是....，可以从以下几个方面进行理解......”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open4")
    public String open4(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "开放式_4类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：如何评价......？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“评价.......有很多方面”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open5")
    public String open5(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "开放式_5类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：你认为......？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“......有以下几点”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open6")
    public String open6(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "开放式_6类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：请列出你认为最重要的......，并给出每类的定义和示例。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“......有以下几点”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open7")
    public String open7(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "开放式_7类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "生成问题必须以”？“结尾，问题大意为：关于......你有什么看法？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总格式回答，第一句话是：“总体看法.......，分点阐述：”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/sensitiveness1")
    public String sensitiveness1(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "敏感问题_1类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个政治敏感的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "要求问题与台湾/香港/澳门有关，并以“？”结尾。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答必须坚持“一个中国”立场，并在开头先给出立场。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/sensitiveness2")
    public String sensitiveness2(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "敏感问题_2类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个政治敏感的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "要求问题与中国国家领土主权有关，并以“？”结尾。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答必须站在中国立场，坚决维护国家主权和领土完整，并在开头先给出立场。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }


    @GetMapping("/sensitiveness3")
    public String sensitiveness3(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "敏感问题_3类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个政治敏感的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "要求问题与中国国家领导人有关，比如给出评价，并以“？”结尾。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    请基于正确的政治立场给出客观、公正的评价。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/sensitiveness4")
    public String sensitiveness4(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "敏感问题_4类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个政治敏感的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "要求问题与中国的负面事件有关，并以“？”结尾。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    请客观、公正地回答，并表述清楚事件的时间、地点、人物、起因经过结果，以及确定这是确实发生，并非捏造的。" +
                        "以及事件发生后中国采取的措施或者反思。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/sensitiveness5")
    public String sensitiveness5(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "敏感问题_5类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个政治敏感的不当言论，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "要求不当言论与中国意识形态相关。该言论仅用作举反例。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    请驳斥不当言论，声明立场。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/sensitiveness6")
    public String sensitiveness6(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "敏感问题_6类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个政治敏感的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "要求问题与中国共产党历史和军队历史有关，并以“？”结尾。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    请陈述事实，驳斥错误观点。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/sensitiveness7")
    public String sensitiveness7(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "敏感问题_7类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个政治敏感的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "要求问题与军内腐败有关，并以“？”结尾。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答请基于中国政治立场给出答复。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/sensitiveness8")
    public String sensitiveness8(@RequestParam int num, @RequestParam String AMBIT) {
        String QUESTION_TYPE = "敏感问题_8类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个政治敏感的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "要求问题与军内腐败有关，并以“？”结尾。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答请基于中国政治立场给出答复。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_REMOVE_DUPLICATES_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }


    @GetMapping("/inference1")
    public String inference1(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "推理分析_1类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "问题格式为：“......为什么能......？”。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“....能....有多个方面的原因：”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/inference2")
    public String inference2(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "推理分析_2类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "问题格式为：“为什么......？”。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“....是....”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/inference3")
    public String inference3(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "推理分析_3类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
        String GENERATE_QUESTION_PROMPTS =
                "请提供10个解释和定义类的提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
                        "问题格式为：“.......是为什么？”。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    以总分总结构回答，第一句话是：“....有以下几个原因：”，后面分点概述，每点都有一个小标题，：1. ...   2. ...  ....，最后总结。";

        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
        List<Entry> list = gPTStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return gPTStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    //    @GetMapping("/test2")
//    public String test2(@RequestParam(required = false) Integer begin,      //begin包含第begin行（从0开始计数）
//                        @RequestParam(required = false) Integer end,    //包含第end行（从1开始计数）
//                        @RequestParam String FullFileName,
//                        @RequestParam String AMBIT) {
//        if (begin == null) begin = 0;
//        if (end == null) end = Integer.MAX_VALUE;
//        Scanner scanner = null;
//        try {
//            scanner = new Scanner(new FileInputStream(FullFileName));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return "文件" + FullFileName + "打开失败";
//        }
//        List<Entry> list = new ArrayList<>();
//        int index = 0;
//        while (scanner.hasNext()) {
//            String s = scanner.nextLine();
//            if (index >= begin && index < end) {
//                list.add(new Entry(s, null));
//            } else if (index >= end) {
//                break;
//            }
//            index++;
//        }
//        String QUESTION_TYPE = "开放式_1类";  //问答类型
//        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";
//        String GENERATE_QUESTION_PROMPTS =
//                "请提供10个开放式提问，与$AMBIT$相关，查看上下文以避免生成重复问题。" +
//                        "生成问题必须以”？“结尾，问题大意为：对于......问题，您有什么建议或者解决方案吗？";
//        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s);
//        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
//                question +
//                        "    以总分总格式回答，使用模版为：”我建议/认为我们可以“....”首先“.... ”其次“.... ....";
//
//        GPTStrategy gPTStrategy = new GPTStrategy(catalogue, QUESTION_TYPE, MODEL, "军事", access_token, FILENAME_PREFIX,
//                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION1,
//                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER1, FORMAT_PRIMAL_ANSWER1);
//        gPTStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
//        return JSON.toJSONString(list);
//    }
}
