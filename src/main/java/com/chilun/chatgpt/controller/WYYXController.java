package com.chilun.chatgpt.controller;

import com.chilun.chatgpt.pojo.Entry;
import com.chilun.chatgpt.strategy.WYYXStrategy;
import com.chilun.chatgpt.strategy.GPTStrategy;
import com.chilun.chatgpt.strategy.tool.GPT3_5MultiThreadTools;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @auther 齿轮
 * @create 2023-10-31-21:47
 */
@RestController
public class WYYXController {
    public static String catalogue = "C:\\Users\\齿轮\\Desktop\\得能数据生成\\";
    public static String MODEL = "文言一心";    //使用模型
    public static String access_token1 = "#脱敏";
    public static String access_token2 = "#脱敏";

    public static Function<String, String> FORMAT_PRIMAL_ANSWER1 = s -> s.replace(" ", "")
            .replace("\n\n", "\n")
            .replace("*", "")
            .replace("总结：", "")
            .replace("【", "").replace("】", "")
            .replace("小标题", "")
            .replace("“", "").replace("”", "");

    public static Predicate<String> JUDGE_SUITABLE_QUESTION1 = s -> s.length() >= 2 && s.length() <= 100 && !s.contains("xxx") && !s.contains("以下");

    @GetMapping("/test")
    public String test() {
        GPTStrategy strategy = new GPTStrategy(catalogue,
                "开放式_6类",
                "GPT3_5",
                "军事",
                "#脱敏",
                "GPT3_5_开放式_6类_",
                "请提供10个开放式提问，与" + "$AMBIT$" + "相关，" +
                        "问题为：“请列出你认为最重要的xxx，并给出每类的定义和示例。”，查看上下文以保证请勿重复问题" +
                        "举例：问题1：请列出你认为最重要的军事战略，并给出每类的定义和示例。",
                s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("定义"),
                s -> {
                    if (s.contains("：")) {
                        return s.substring(s.indexOf("：") + 1);
                    } else if (s.contains(":")) {
                        return s.substring(s.indexOf(":") + 1);
                    } else if (s.contains(".")) {
                        return s.substring(s.indexOf(".") + 1);
                    } else {
                        return s;
                    }
                },
                question -> question +
                        "    回答格式（总分总结构）：" +
                        "我认为最重要的xxx包括：..." +
                        "1. 【小标题】：...   " +
                        "2. 【小标题】：...  " +
                        "....",
                s -> s.length() >= 80,
                s -> s);
        GPT3_5MultiThreadTools.BeginProduceQuestion(15, strategy);
        GPT3_5MultiThreadTools.BeginConsumeQuestion(5);
        GPT3_5MultiThreadTools.BeginSave();

//        List<Entry> list = strategy.GENERATE_FORMATTED_QUESTION_COLLECTION(2);
//        strategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
//        strategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
        return "开始多线程处理";
    }

//    @GetMapping("/gpt/open1")
//    public String GptOpen1(@RequestParam int num){
//        String QUESTION_TYPE = "开放式_1类";  //问答类型
//        String GENERATE_QUESTION_PROMPTS =
//                "请提供10个开放式提问，与" + "$AMBIT$" + "相关，" +
//                        "问题为：“对于xxx问题，您有什么建议或者解决方案吗？”，" +
//                        "举例：问题1：对于提高军队战斗力的训练方法，您有什么建议或者解决方案吗？";
//        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
//        Function<String, String> FORMAT_PRIMAL_QUESTION = s -> {
//            s = s.substring(0, s.indexOf("？") + 1);
//            if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
//                return s.substring(s.indexOf("：") + 1);
//            } else if (s.contains(".")) {
//                return s.substring(s.indexOf(".") + 1);
//            } else return s;
//        };
//        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
//                question +
//                        "    回答格式（总分总结构）：" +
//                        "我建议/认为我们可以....   " +
//                        "首先....   " +
//                        "其次....   " +
//                        "....";
//        Predicate<String> JUDGE_SUITABLE_ANSWER = s -> s.length() >= 80;
//        Function<String, String> FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER1;
//
//        GPT3_5Strategy strategy = new GPT3_5Strategy(catalogue,
//                QUESTION_TYPE,
//                "GPT3_5",
//                "军事",
//                "#脱敏",
//                "GPT3_5_" + QUESTION_TYPE,
//                GENERATE_QUESTION_PROMPTS,
//                JUDGE_SUITABLE_QUESTION,
//                s -> {
//                    if (s.contains("：")) {
//                        return s.substring(s.indexOf("：") + 1);
//                    } else if (s.contains(":")) {
//                        return s.substring(s.indexOf(":") + 1);
//                    } else if (s.contains(".")) {
//                        return s.substring(s.indexOf(".") + 1);
//                    } else {
//                        return s;
//                    }
//                },
//                question -> question +
//                        "    回答格式（总分总结构）：" +
//                        "我认为最重要的xxx包括：..." +
//                        "1. 【小标题】：...   " +
//                        "2. 【小标题】：...  " +
//                        "....",
//                s -> s.length() >= 80,
//                s -> s);
//        List<Entry> list = strategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
//        strategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
//        return strategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
//    }

    @GetMapping("/open1")
    public String open1(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "开放式_1类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";

        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与" + "$AMBIT$" + "相关，" +
                        "问题为：“对于xxx问题，您有什么建议或者解决方案吗？”，" +
                        "举例：问题1：对于提高军队战斗力的训练方法，您有什么建议或者解决方案吗？" +
                        "问题2：对于改善军事装备维护和保养效率的措施，您有什么建议或者解决方案吗？" +
                        "问题3：对于减少军事行动中的人员伤亡的措施，您有什么建议或者解决方案吗？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> FORMAT_PRIMAL_QUESTION = s -> {
            s = s.substring(0, s.indexOf("？") + 1);
            if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
                return s.substring(s.indexOf("：") + 1);
            } else if (s.contains(".")) {
                return s.substring(s.indexOf(".") + 1);
            } else return s;
        };
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答格式（总分总结构）：" +
                        "我建议/认为我们可以....   " +
                        "首先....   " +
                        "其次....   " +
                        "....";
        Predicate<String> JUDGE_SUITABLE_ANSWER = s -> s.length() >= 80;
        Function<String, String> FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER1;

        WYYXStrategy commonStrategy = new WYYXStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token1, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER, FORMAT_PRIMAL_ANSWER);
        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open2")
    public String open2(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "开放式_2类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";

        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与" + "$AMBIT$" + "相关，" +
                        "问题为：“请谈谈你对XXX的看法。”，" +
                        "举例：问题1：请谈谈你对军队士气对战斗力影响的看法。" +
                        "问题2：请谈谈你对战争中民众安全保障的看法。" +
                        "问题3：请谈谈你对军事干预国家内政对国际秩序稳定性的看法。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("请谈谈");
        Function<String, String> FORMAT_PRIMAL_QUESTION = s -> {
            s = s.substring(0, s.indexOf("。") + 1);
            if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
                return s.substring(s.indexOf("：") + 1);
            } else if (s.contains(".")) {
                return s.substring(s.indexOf(".") + 1);
            } else return s;
        };
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答格式（总分总结构）：" +
                        "“我认为”......." +
                        "1. 【小标题】：内容....   " +
                        "2. 【小标题】：内容....   " +
                        "....";
        Predicate<String> JUDGE_SUITABLE_ANSWER = s -> s.length() >= 80;
        Function<String, String> FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER1;

        WYYXStrategy commonStrategy = new WYYXStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token2, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER, FORMAT_PRIMAL_ANSWER);
        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open3")
    public String open3(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "开放式_3类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";

        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与" + "$AMBIT$" + "相关，" +
                        "问题为：“如何理解xxx？”，" +
                        "举例：问题1：如何理解战争法律框架对保护平民权益的意义？" +
                        "问题2：如何理解军事干预国家内政对国际秩序的影响？" +
                        "问题3：如何理解军队多样化组成对提升部队凝聚力的意义？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> FORMAT_PRIMAL_QUESTION = s -> {
            s = s.substring(0, s.indexOf("？") + 1);
            if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
                return s.substring(s.indexOf("：") + 1);
            } else if (s.contains(".")) {
                return s.substring(s.indexOf(".") + 1);
            } else return s;
        };
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答格式（总分总结构）：xxx是....，可以从以下几个方面进行理解：" +
                        "1. 【小标题】：....   " +
                        " 2. 【小标题】：....  " +
                        ".... ";
        Predicate<String> JUDGE_SUITABLE_ANSWER = s -> s.length() >= 80;
        Function<String, String> FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER1;

        WYYXStrategy commonStrategy = new WYYXStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token1, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER, FORMAT_PRIMAL_ANSWER);
        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open4")
    public String open4(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "开放式_4类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";

        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与" + "$AMBIT$" + "相关，" +
                        "问题为：“如何评价xxx？”，" +
                        "举例：问题1：如何评价军事训练对部队战斗力提升的效果？" +
                        "问题2：如何评价军队装备现代化对国防实力的提升效果？" +
                        "问题3：如何评价军事干预国家内政对国际关系稳定性的影响？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> FORMAT_PRIMAL_QUESTION = s -> {
            s = s.substring(0, s.indexOf("？") + 1);
            if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
                return s.substring(s.indexOf("：") + 1);
            } else if (s.contains(".")) {
                return s.substring(s.indexOf(".") + 1);
            } else return s;
        };
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答格式（总分总结构）：" +
                        "评价.......有很多方面   " +
                        "1. 【小标题】：...   " +
                        "2. 【小标题】：...  " +
                        "....";
        Predicate<String> JUDGE_SUITABLE_ANSWER = s -> s.length() >= 80;
        Function<String, String> FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER1;

        WYYXStrategy commonStrategy = new WYYXStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token2, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER, FORMAT_PRIMAL_ANSWER);
        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open5")
    public String open5(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "开放式_5类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";

        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与" + "$AMBIT$" + "相关，" +
                        "问题为：“你认为xxx？”，" +
                        "举例：问题1：你认为军事训练对士兵战斗素质的提高有何重要作用？" +
                        "问题2：你认为军事装备现代化对提高国防实力有何重要意义？" +
                        "问题3：你认为战争法律框架对保护平民权益起到了怎样的作用？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> FORMAT_PRIMAL_QUESTION = s -> {
            s = s.substring(0, s.indexOf("？") + 1);
            if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
                return s.substring(s.indexOf("：") + 1);
            } else if (s.contains(".")) {
                return s.substring(s.indexOf(".") + 1);
            } else return s;
        };
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答格式（总分总结构）：.......有以下几点" +
                        "1. 【小标题】：...   " +
                        "2. 【小标题】：...  " +
                        "....";
        Predicate<String> JUDGE_SUITABLE_ANSWER = s -> s.length() >= 80;
        Function<String, String> FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER1;

        WYYXStrategy commonStrategy = new WYYXStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token1, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER, FORMAT_PRIMAL_ANSWER);
        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open6")
    public String open6(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "开放式_6类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";

        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与" + "$AMBIT$" + "相关，" +
                        "问题为：“请列出你认为最重要的xxx，并给出每类的定义和示例。”，" +
                        "举例：问题1：请列出你认为最重要的军事战略，并给出每类的定义和示例。" +
                        "问题2：请列出你认为最重要的军事指挥原则，并给出每类的定义和示例。" +
                        "问题3：请列出你认为最重要的军事科技发展，并给出每类的定义和示例。";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && (s.contains("？") || s.contains("定义") && s.contains("示例"));
        Function<String, String> FORMAT_PRIMAL_QUESTION = s -> {
            s = s.substring(0, s.indexOf("？") + 1);
            if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
                return s.substring(s.indexOf("：") + 1);
            } else if (s.contains(".")) {
                return s.substring(s.indexOf(".") + 1);
            } else return s;
        };
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答格式（总分总结构）：" +
                        "我认为最重要的xxx包括：..." +
                        "1. 【小标题】：...   " +
                        "2. 【小标题】：...  " +
                        "....";
        Predicate<String> JUDGE_SUITABLE_ANSWER = s -> s.length() >= 80;
        Function<String, String> FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER1;

        WYYXStrategy commonStrategy = new WYYXStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token2, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER, FORMAT_PRIMAL_ANSWER);
        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/open7")
    public String open7(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "开放式_7类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";

        String GENERATE_QUESTION_PROMPTS =
                "请提供10个开放式提问，与" + "$AMBIT$" + "相关，" +
                        "问题为：“关于xxx你有什么看法？”，" +
                        "举例：问题1：关于xxx你有什么看法？" +
                        "问题2：关于军事训练对部队战斗力的影响你有什么看法？" +
                        "问题3：关于军事科技发展对战争格局的影响你有什么看法？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> FORMAT_PRIMAL_QUESTION = s -> {
            s = s.substring(0, s.indexOf("？") + 1);
            if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
                return s.substring(s.indexOf("：") + 1);
            } else if (s.contains(".")) {
                return s.substring(s.indexOf(".") + 1);
            } else return s;
        };
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答格式（总分总结构）：" +
                        "总体看法.......，分点阐述：" +
                        "1. 【小标题】：...   " +
                        "2. 【小标题】：...  " +
                        "总结：...";
        Predicate<String> JUDGE_SUITABLE_ANSWER = s -> s.length() >= 80;
        Function<String, String> FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER1;

        WYYXStrategy commonStrategy = new WYYXStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token1, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER, FORMAT_PRIMAL_ANSWER);
        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/inference1")
    public String inference1(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "推理分析式_1类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";

        String GENERATE_QUESTION_PROMPTS =
                "请提供10个推理分析式提问，与" + "$AMBIT$" + "相关，" +
                        "问题为：“xxx为什么能xxx？”，" +
                        "举例：问题1：军事情报收集与分析为什么能够提高作战决策的准确性和及时性？" +
                        "问题2：军事科技发展为什么能够改变战争格局和提高战争效率？" +
                        "问题3：战争中民众保护措施为什么能够减少战争对平民的伤害和损失？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> FORMAT_PRIMAL_QUESTION = s -> {
            s = s.substring(0, s.indexOf("？") + 1);
            if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
                return s.substring(s.indexOf("：") + 1);
            } else if (s.contains(".")) {
                return s.substring(s.indexOf(".") + 1);
            } else return s;
        };
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答格式（总分总结构）：" +
                        "xxx能xxx有多个方面的原因：" +
                        "1. 【小标题】：...   " +
                        "2. 【小标题】：...  " +
                        "....";
        Predicate<String> JUDGE_SUITABLE_ANSWER = s -> s.length() >= 80;
        Function<String, String> FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER1;

        WYYXStrategy commonStrategy = new WYYXStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token2, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER, FORMAT_PRIMAL_ANSWER);
        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/inference2")
    public String inference2(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "推理分析式_2类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";

        String GENERATE_QUESTION_PROMPTS =
                "请提供10个推理分析式提问，与" + "$AMBIT$" + "相关，" +
                        "问题为：“为什么xxx？”，" +
                        "举例：问题1：为什么军事训练对提高士兵的战斗力和适应能力至关重要？" +
                        "问题2：为什么军事科技发展能够改变战争的方式和手段？" +
                        "问题3：为什么军队士气的提升能够直接影响战场上的斗志和作战效果？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> FORMAT_PRIMAL_QUESTION = s -> {
            s = s.substring(0, s.indexOf("？") + 1);
            if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
                return s.substring(s.indexOf("：") + 1);
            } else if (s.contains(".")) {
                return s.substring(s.indexOf(".") + 1);
            } else return s;
        };
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答格式（总分总结构）：" +
                        "xxx是...，它...，" +
                        "首先....   " +
                        "其次....   " +
                        "....";
        Predicate<String> JUDGE_SUITABLE_ANSWER = s -> s.length() >= 80;
        Function<String, String> FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER1;

        WYYXStrategy commonStrategy = new WYYXStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token1, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER, FORMAT_PRIMAL_ANSWER);
        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }

    @GetMapping("/inference3")
    public String inference3(@RequestParam int num, String AMBIT) {
        String QUESTION_TYPE = "推理分析式_3类";  //问答类型
        String FILENAME_PREFIX = MODEL + "_" + QUESTION_TYPE + "_";

        String GENERATE_QUESTION_PROMPTS =
                "请提供10个推理分析式提问，与" + "$AMBIT$" + "相关，" +
                        "问题为：“.......是为什么？”。要求问题以“是为什么？”结尾。" +
                        "举例：问题1：军队采用分散式部署战术是为什么？" +
                        "问题2：采用无人机进行侦察和攻击是为什么？" +
                        "问题3：军事演习对于应对突发情况至关重要是为什么？";
        Predicate<String> JUDGE_SUITABLE_QUESTION = s -> JUDGE_SUITABLE_QUESTION1.test(s) && s.contains("？");
        Function<String, String> FORMAT_PRIMAL_QUESTION = s -> {
            s = s.substring(0, s.indexOf("？") + 1);
            if (s.substring(0, 5).contains("问题") && s.substring(0, 7).contains("：")) {
                return s.substring(s.indexOf("：") + 1);
            } else if (s.contains(".")) {
                return s.substring(s.indexOf(".") + 1);
            } else return s;
        };
        Function<String, String> GENERATE_QUESTION_PROMPTS_FUNCTION = question -> //生成回答的提问词
                question +
                        "    回答格式（总分总结构）：" +
                        "........有以下几个原因：" +
                        "1. 【小标题】：...   " +
                        "2. 【小标题】：...  " +
                        "....";
        Predicate<String> JUDGE_SUITABLE_ANSWER = s -> s.length() >= 80;
        Function<String, String> FORMAT_PRIMAL_ANSWER = FORMAT_PRIMAL_ANSWER1;

        WYYXStrategy commonStrategy = new WYYXStrategy(catalogue, QUESTION_TYPE, MODEL, AMBIT, access_token2, FILENAME_PREFIX,
                GENERATE_QUESTION_PROMPTS, JUDGE_SUITABLE_QUESTION, FORMAT_PRIMAL_QUESTION,
                GENERATE_QUESTION_PROMPTS_FUNCTION, JUDGE_SUITABLE_ANSWER, FORMAT_PRIMAL_ANSWER);
        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
    }
//
//    @GetMapping("/test")
//    public String test(@RequestParam int num) {
//        CommonStrategy commonStrategy = new CommonStrategy();
//        commonStrategy.AMBIT = "军事";
//        commonStrategy.access_token = "#脱敏";
//        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
//        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
//        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
//    }
//
//    @GetMapping("/test2")
//    public String test2(@RequestParam int num) {
//        CommonStrategy commonStrategy = new CommonStrategy();
//        commonStrategy.AMBIT = "政治、党史";
//        commonStrategy.access_token = "#脱敏";
//        List<Entry> list = commonStrategy.GENERATE_FORMATTED_QUESTION_COLLECTION(num);
//        commonStrategy.GENERATE_FORMATTED_ANSWER_COLLECTION(list);
//        return commonStrategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(list);
//    }


//    public static String catalogue = "C:\\Users\\齿轮\\Desktop\\得能数据生成\\";
//    public static String access_token = "#脱敏";
//    public static String QUESTION_TYPE = "假设式_6类";
//    public static String METHOD = "文言一心";
//    public static String GENERATE_QUESTION_PROMPTS = "请提供10个假设式问题，与政治、党史相关，问题为：我们为什么不试试xxx。问题必须“为什么不试试xxx”结尾，提问的格式：问题1：我们为什么不试试xxx？问题2：我们为什么不试试xxx？问题3：我们为什么不试试xxx？......";
//    public static Function<String, String> ANSWER_QUESTION_FUNCTION = question -> createBody(1.0, null, question +
//            "回答问题的格式为：" +
//            "因为....，" +
//            "分：1...2...3.......；" +
//            "总结：...。");
//    public static Function<String, String> OPTIMIZE_QUESTION_FUNCTION = question -> question.contains("？") ? question.substring(0, question.indexOf("？") + 1) : question.substring(0, question.indexOf("?") + 1);
//    public static Function<String, String> OPTIMIZE_ANSWER_FUNCTION = answer -> answer.substring(answer.indexOf("因为")).replace("总起：", "").replace("分：", "").replace("回答：", "").replace("总结：", "").replace("\n\n", "\n");
//
//    @GetMapping("/begin")
//    public String begin(@RequestParam int num) throws ParseException {//参数为生成文件名、提问次数@RequestParam String DataFrom,@RequestParam int num
//        //常量空间
//        JSONObject jsonObject = null;                       //问题存储空间
//        ArrayList<Entry> list = new ArrayList<>();      //内存空间
//        Exception exception = null;                             //异常空间
//
//        //向list中存储生成的所有问题
//        for (int i = 0; i < num; i++) {
//            System.out.println("--------------------开始第" + i + "次生成问题----------------------");
//            try {
//                jsonObject = generateQuestion(createBody(null, 1.0, GENERATE_QUESTION_PROMPTS));
//            } catch (Exception e) {
//                return METHOD + "生成问题失败：" + e.getMessage();
//            }
//            System.out.println("--------------------第" + i + "次生成问题成功----------------------");
//            System.out.println("--------------------开始第" + i + "次格式化问题--------------------");
//            try {
//                formatQuestion(jsonObject, list);//格式化问题，存入内存
//            } catch (Exception e) {
//                if (e.getMessage().equals(METHOD + "生成问题无效")) {
//                    System.out.println("--------------------开始第" + i + "次格式化失败--------------------");
//                    continue;
//                }
//                return "格式化问题失败：" + e.getMessage();
//            }
//            System.out.println("--------------------开始第" + i + "次格式化成功--------------------");
//        }
//
//        System.out.println("--------------------开始处理问题--------------------");
//        try {
//            DealQuestion(list);//处理问题并在list的entry中填入解答
//        } catch (Exception e) {
//            exception = e;                  //持久化后再返回错误
//        }
//        if (exception != null) {
//            System.out.println("--------------------处理问题失败--------------------");
//        } else {
//            System.out.println("--------------------处理问题成功--------------------");
//        }
//        System.out.println("--------------------开始持久化--------------------");
//        String save = null;
//        String FileName = METHOD + "_" + QUESTION_TYPE + "_" + System.currentTimeMillis() + "_约包含" + num * 10 + "条数据";
//        try {
//            save = saveInExcelFile(list, FileName);
//        } catch (Exception e) {
//            return exception == null ? "持久化失败：" + e.getMessage() : "持久化失败：" + e.getMessage() + "  生成回答失败" + exception.getMessage();
//        }
//        System.out.println("--------------------持久化完成--------------------");
//        return exception == null ? save : "  生成回答失败" + exception.getMessage();

//    }
//
//    private JSONObject generateQuestion(String Body) {
//        HttpResponse<String> response = Unirest.post("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant?access_token=" + access_token)
//                .header("Content-Type", "application/json")
//                .body(Body)
//                .asString();
//        System.out.println("-----------------生成问题响应：" + response.getBody());
//        return JSONObject.parseObject(response.getBody());
//    }
//
//    private void formatQuestion(JSONObject jsonObject, ArrayList<Entry> list) throws Exception {
//        boolean usable = false;
//        if (jsonObject.containsKey("result")) {
//            Object result = jsonObject.get("result");
//            String s = result.toString();
//            Scanner scanner = new Scanner(s);
//            while (scanner.hasNext()) {
//                String s1 = scanner.nextLine();
//                if (s1.length() > 100) {
//                    System.out.println("----------------格式化问题：" + s1 + "_不通过_原因：过长--------------");
//                } else if (s1.contains("我们为什么") && (s1.contains("?") || s1.contains("？"))) {
//                    list.add(new Entry(OPTIMIZE_QUESTION_FUNCTION.apply(s1.substring(s1.indexOf("我们为什么"))), null));
//                    usable = true;
//                } else if (s1.contains("为什么") && (s1.contains("?") || s1.contains("？"))) {
//                    list.add(new Entry(OPTIMIZE_QUESTION_FUNCTION.apply(s1.substring(s1.indexOf("为什么"))), null));
//                    usable = true;
//                } else if (s1.contains("假设") && (s1.contains("?") || s1.contains("？"))) {
//                    list.add(new Entry(OPTIMIZE_QUESTION_FUNCTION.apply(s1.substring(s1.indexOf("假设"))), null));
//                    usable = true;
//                } else if (s1.contains("如果") && (s1.contains("?") || s1.contains("？"))) {
//                    list.add(new Entry(OPTIMIZE_QUESTION_FUNCTION.apply(s1.substring(s1.indexOf("如果"))), null));
//                    usable = true;
//                } else if (s1.contains("假如") && (s1.contains("?") || s1.contains("？"))) {
//                    list.add(new Entry(OPTIMIZE_QUESTION_FUNCTION.apply(s1.substring(s1.indexOf("假如"))), null));
//                    usable = true;
//                } else {
//                    System.out.println("----------------格式化问题：" + s1 + "_不通过_原因：未检测到关键词--------------");
//                }
//            }
//        } else {
//            throw new Exception(METHOD + "生成问题格式无法解析");
//        }
//        if (!usable) {
//            throw new Exception(METHOD + "生成问题无效");
//        }
//    }
//
//    private void DealQuestion(ArrayList<Entry> list) {
//        int index = 0;
//        int sum = 0;
////        int times = 0;
//        while (index < list.size()) {
//            Entry entry = list.get(index++);
//            try {
//                if (entry.getAnswer() == null) {
//                    HttpResponse<String> response = Unirest.post("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant?access_token=" + access_token)
//                            .header("Content-Type", "application/json")
//                            //                        .body("{\r\n    \"temperature\": 1,\r\n    \"messages\": [\r\n        {\r\n            \"role\": \"user\",\r\n            \"content\": \"" + entry.getQuestion() + "  回答格式（严格遵守）：总起（包含明确的答复）：...  分点：1...2...3...... 总结：...\"\r\n        }\r\n    ]\r\n}")
//                            .body(ANSWER_QUESTION_FUNCTION.apply(entry.getQuestion()))
//                            .asString();
//                    JSONObject jsonObject = JSONObject.parseObject(response.getBody());
//                    System.out.println("------------------第" + index + "个问题的响应\n" +
//                            response.getBody());
//                    String result = OPTIMIZE_ANSWER_FUNCTION.apply(jsonObject.get("result").toString());
////                    if (result.length() <= 150) {
////                        index--;
////                        if (times++ >= 2) {
////                            index++;
////                            times = 0;
////                        }
////                    }
//                    if (result.contains("我无法提供此类信息") || result.contains("很抱歉")) {
//                        System.out.println("----------------检测回答：" + result + "_不通过_原因：无效回答--------------");
//                        list.remove(--index);
//                        continue;
//                    }
//                    entry.setAnswer(result);
//                }
//            } catch (Exception e) {
//                if (sum++ >= 10) {//重试机制，10次失败则取消生成
//                    throw e;
//                }
//                index--;//重新生成
//            }
//        }
//    }
//
//    private String saveInExcelFile(ArrayList<Entry> list, String fileName) {
//        String excelFilePath = catalogue + fileName + ".xlsx";
//        EasyExcel.write(excelFilePath, Entry.class).sheet().doWrite(list);
//        return "持久化完成";
//    }
//
//
//    private static String createBody(Double temperature, Double top_p, String content) {
//        String s = " { " +
//                (top_p == null ? "" : "\"top_p\": " + top_p + " , ") +
//                (temperature == null ? "" : "\"temperature\": " + temperature + " , ") +
//                "\"messages\": [ " +
//                " { " +
//                "\"role\": \"user\", " +
//                "\"content\": \"" + content + "\"" +
//                " }" +
//                " ] " +
//                " } ";
//        return s;
//    }
}
