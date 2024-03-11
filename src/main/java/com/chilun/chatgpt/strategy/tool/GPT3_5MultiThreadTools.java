package com.chilun.chatgpt.strategy.tool;

import com.chilun.chatgpt.pojo.Entry;
import com.chilun.chatgpt.strategy.GPTStrategy;
import kong.unirest.HttpResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @auther 齿轮
 * @create 2023-11-03-19:50
 */
public class GPT3_5MultiThreadTools {
    public static ReentrantLock lock = new ReentrantLock();
    public static int RequiredQuestion = 0;
    public static List<Entry> QuestionList = Collections.synchronizedList(new ArrayList<>());
    public static List<Entry> AnswerList = Collections.synchronizedList(new ArrayList<>());
    public static boolean endingQuestion = false;
    public static boolean beginPersistent = false;
    public static boolean[] isWorking;

    public static GPTStrategy strategy;

    public static void BeginProduceQuestion(int num, GPTStrategy strategy1) {
        strategy = strategy1;
        RequiredQuestion = num;
        QuestionProducer questionProducer = new QuestionProducer();
        Thread producer = new Thread(questionProducer);
        producer.start();
    }

    public static void BeginConsumeQuestion(int num) {
        isWorking = new boolean[num];
        for (int i = 0; i < num; i++) {
            isWorking[i] = false;
            AnswerProducer answerProducer = new AnswerProducer();
            Thread consumer = new Thread(answerProducer);
            consumer.setName(String.valueOf(i));
            consumer.start();
        }
    }

    public static void BeginSave() {
        Saver saver = new Saver();
        Thread saver1 = new Thread(saver);
        saver1.start();
    }
}

class QuestionProducer implements Runnable {
    @Override
    public void run() {
        GPTStrategy strategy = GPT3_5MultiThreadTools.strategy;
        while (true) {
            System.out.println("--生产者线程开始尝试生成问题");
            boolean start = false;

            //判断是否需要继续生成问题
            System.out.println("生产者" + Thread.currentThread().getName() + "等待锁：获得还需要问题数RequiredQuestion");
            GPT3_5MultiThreadTools.lock.lock();
            System.out.println("生产者" + Thread.currentThread().getName() + "获得锁");
            {
                if (GPT3_5MultiThreadTools.RequiredQuestion >= 0) {
                    System.out.println("RequiredQuestion：" + GPT3_5MultiThreadTools.RequiredQuestion);
                    start = true;
                }
            }
            System.out.println("生产者" + Thread.currentThread().getName() + "释放锁");
            GPT3_5MultiThreadTools.lock.unlock();


            if (start) {//需要
                try {
                    System.out.println("------开始生成问题");
                    String result = DialogueTools.sendGPTSuperQuestionRequest_GetGPTResultFromResponse(
                            strategy.access_token,
                            strategy.GENERATE_QUESTION_PROMPTS.replace("$AMBIT$", strategy.AMBIT)
                    );
                    System.out.println("------问题生成完成");
                    Scanner scanner = new Scanner(result);

                    while (scanner.hasNext()) {//格式化问题
                        String s1 = scanner.nextLine();
                        System.out.println("------开始格式化问题：" + s1);
                        if (strategy.JUDGE_SUITABLE_QUESTION.test(s1)) {
                            String apply = strategy.FORMAT_PRIMAL_QUESTION.apply(s1);
                            System.out.println("------开始添加问题：" + apply);
                            System.out.println("生产者" + Thread.currentThread().getName() + "添加问题");
                            {
                                GPT3_5MultiThreadTools.QuestionList.add(new Entry(apply, null));
                                System.out.println("------添加问题完成：" + apply);

                                System.out.println("生产者" + Thread.currentThread().getName() + "等待锁：更改RequiredQuestion，减一");
                                GPT3_5MultiThreadTools.lock.lock();
                                System.out.println("生产者" + Thread.currentThread().getName() + "获得锁");
                                GPT3_5MultiThreadTools.RequiredQuestion--;
                                System.out.println("-------RequiredQuestion更改结果" + GPT3_5MultiThreadTools.RequiredQuestion);
                                if (GPT3_5MultiThreadTools.RequiredQuestion <= 0)
                                    GPT3_5MultiThreadTools.endingQuestion = true;
                                System.out.println("生产者" + Thread.currentThread().getName() + "释放锁");
                                GPT3_5MultiThreadTools.lock.unlock();

                            }
                        } else {
                            System.out.println("----------------格式化问题：" + s1 + "_不通过_原因：过长或未检测到关键词--------------");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("--全部问题生成完成，生产者线程结束");
                return;
            }
        }
    }
}

class AnswerProducer implements Runnable {

    @Override
    public void run() {
        GPTStrategy strategy = GPT3_5MultiThreadTools.strategy;

        while (true) {
            boolean needWait = false;
            boolean needStop = false;

            System.out.println("消费者" + Thread.currentThread().getName() + "等待锁，用于判断能否获得问题并生产回答");
            GPT3_5MultiThreadTools.lock.lock();
            System.out.println("消费者" + Thread.currentThread().getName() + "获得锁");
            {
                if (GPT3_5MultiThreadTools.beginPersistent) {//开始持久化，不能生产回答
                    System.out.println("消费者" + Thread.currentThread().getName() + "释放锁：beginPersistent");
                    GPT3_5MultiThreadTools.lock.unlock();
                    System.out.println("消费者" + Thread.currentThread().getName() + "--已开始持久化，停止线程");
                    return;
                } else if (GPT3_5MultiThreadTools.endingQuestion && GPT3_5MultiThreadTools.QuestionList.size() <= 0) {//无剩余问题剩余可处理
                    System.out.println("消费者" + Thread.currentThread().getName() + "释放锁：问题已全部处理完毕");
                    GPT3_5MultiThreadTools.lock.unlock();
                    System.out.println("消费者" + Thread.currentThread().getName() + "--问题已处理完毕，停止线程");
                    return;
                }
            }
            System.out.println("消费者" + Thread.currentThread().getName() + "释放锁：判断能否获得问题完毕，让下者进行判断");
            GPT3_5MultiThreadTools.lock.unlock();

            //说明将来或现在有问题可处理
            Entry question = null;
            try {
                question = GPT3_5MultiThreadTools.QuestionList.remove(0);
            } catch (Exception e) {//说明没问题可处理，等待5s，重新进入循环进行判断
                System.out.println("暂无问题可处理");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                continue;
            }

            System.out.println("消费者" + Thread.currentThread().getName() + "等待锁，用于设置工作状态为true");
            GPT3_5MultiThreadTools.lock.lock();
            System.out.println("消费者" + Thread.currentThread().getName() + "获得锁");
            GPT3_5MultiThreadTools.isWorking[Integer.parseInt(Thread.currentThread().getName())] = true;
            System.out.println("消费者" + Thread.currentThread().getName() + "释放锁：设置完成");
            GPT3_5MultiThreadTools.lock.unlock();

            //开始处理问题
            System.out.println("消费者" + Thread.currentThread().getName() + "--开始尝试回答问题：" + question.getQuestion());
            String result = null;
            HttpResponse<String> response = null;
            for (int i = 0; i < 2; i++) {
                try {
                    response = UnirestTools.sendGPTRequest(strategy.access_token,
                            RequestTools.createGPT3_5RequestBody(null, null,
                                    strategy.GENERATE_QUESTION_PROMPTS_FUNCTION.apply(question.getQuestion())
                            )
                    );
                    result = JSONTools.getGPTResultFromResponse(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("消费者" + Thread.currentThread().getName() + "----生成回答失败，跳过" + (i + 1) + "次");
                    continue;
                }
                if (strategy.JUDGE_SUITABLE_ANSWER.test(result)) {//通过则存储
                    System.out.println("消费者" + Thread.currentThread().getName() + "----存储回答完成问题：" + question);
                    question.setAnswer(strategy.FORMAT_PRIMAL_ANSWER.apply(result));
                    GPT3_5MultiThreadTools.AnswerList.add(question);
                    break;
                }
            }

            System.out.println("消费者" + Thread.currentThread().getName() + "等待锁，用于设置工作状态为false");
            GPT3_5MultiThreadTools.lock.lock();
            System.out.println("消费者" + Thread.currentThread().getName() + "获得锁");
            GPT3_5MultiThreadTools.isWorking[Integer.parseInt(Thread.currentThread().getName())] = false;
            System.out.println("消费者" + Thread.currentThread().getName() + "释放锁：设置完成");
            GPT3_5MultiThreadTools.lock.unlock();
        }
    }
}


class Saver implements Runnable {
    @Override
    public void run() {
        while (true) {
            System.out.println("保存者" + Thread.currentThread().getName() + "等待锁，判断能否进入持久化");
            GPT3_5MultiThreadTools.lock.lock();
            System.out.println("保存者" + Thread.currentThread().getName() + "获得锁");
            boolean isWork = false;
            for (boolean b : GPT3_5MultiThreadTools.isWorking) {
                isWork = isWork || b;
            }
            if (GPT3_5MultiThreadTools.QuestionList.size() == 0 && GPT3_5MultiThreadTools.endingQuestion && !isWork) {
                GPT3_5MultiThreadTools.beginPersistent = true;
                break;
            }
            System.out.println("保存者" + Thread.currentThread().getName() + "释放锁");
            GPT3_5MultiThreadTools.lock.unlock();
            try {
                System.out.println("保存者" + Thread.currentThread().getName() + "--处理未完成，持久化线程暂停");
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("保存者" + Thread.currentThread().getName() + "等待锁，进行持久化");
        GPT3_5MultiThreadTools.lock.lock();
        System.out.println("保存者" + Thread.currentThread().getName() + "获得锁");
        {
            if (GPT3_5MultiThreadTools.beginPersistent) {
                System.out.println("保存者" + Thread.currentThread().getName() + "--处理均已完成，开始持久化");
                GPT3_5MultiThreadTools.strategy.SAVE_FINAL_QUESTION_ANSWER_COLLECTION(GPT3_5MultiThreadTools.AnswerList);
            }
        }
        System.out.println("保存者" + Thread.currentThread().getName() + "释放锁");
        GPT3_5MultiThreadTools.lock.unlock();
    }
}