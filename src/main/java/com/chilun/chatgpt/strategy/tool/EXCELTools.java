package com.chilun.chatgpt.strategy.tool;

import com.alibaba.excel.EasyExcel;
import com.chilun.chatgpt.pojo.Entry;

import java.util.List;

/**
 * @auther 齿轮
 * @create 2023-11-02-16:34
 */
public class EXCELTools {
    /*
     * 创建EXCEL文件
     * 要求：FUllFileName包含完整路径+文件名+后缀
     * */
    public static String saveInExcelFile(List<Entry> list, String FUllFileName) {
        try {
            EasyExcel.write(FUllFileName, Entry.class).sheet().doWrite(list);
        } catch (Exception e) {
            e.printStackTrace();
            return "EXCEL持久化失败" + e.getMessage();
        }
        return "EXCEL持久化完成";
    }
}
