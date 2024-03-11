package com.chilun.chatgpt.pojo;

import com.alibaba.excel.annotation.ExcelProperty;

import java.util.Objects;

/**
 * @auther 齿轮
 * @create 2023-11-01-10:43
 */
public class Entry {
    @ExcelProperty("问题")
    private String question;
    @ExcelProperty("回答")
    private String answer;

    @Override
    public String toString() {
        return "Entry{" +
                "question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                '}';
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Entry() {
    }

    public Entry(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entry)) return false;
        Entry entry = (Entry) o;
        return Objects.equals(question, entry.question);
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, answer);
    }
}
