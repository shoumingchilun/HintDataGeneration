package com.chilun.chatgpt.pojo;

/**
 * @auther 齿轮
 * @create 2023-11-03-20:14
 */
public class Dialogue {
    public String role;
    public String content;

    public Dialogue() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Dialogue(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
