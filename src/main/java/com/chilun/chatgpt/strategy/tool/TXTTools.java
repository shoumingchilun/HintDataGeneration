package com.chilun.chatgpt.strategy.tool;

import com.alibaba.fastjson.JSON;
import com.chilun.chatgpt.pojo.Entry;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @auther 齿轮
 * @create 2023-11-04-21:19
 */
public class TXTTools {
    public static String saveInTXTFile(List<Entry> list, String FUllFileName) {
        PrintStream ps = null;
        try {
            File file = new File(FUllFileName);
            file.createNewFile();
            ps = new PrintStream(new FileOutputStream(file, false));//覆写
            for (int i = 0; i < list.size(); i++) {
                ps.println(list.get(i).getQuestion());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "TXT持久化失败" + e.getMessage();
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "JSON持久化完成";
    }

    public static List<Entry> getListFromTXTFile(String FUllFileName) {
        List<Entry> list = new ArrayList<>();
        Scanner scanner = null;
        try {
            File file = new File(FUllFileName);
            scanner = new Scanner(new FileInputStream(file));
            while (scanner.hasNext()) {
                String question = scanner.nextLine();
                list.add(new Entry(question, null));
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null)
                scanner.close();
        }
        return list;
    }
}
