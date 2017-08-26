package com.leo.demo;

/**
 * 索引的实体类
 *
 * Created by LX on 2017/8/25.
 */
public class IndexObject {
    /**
     * 关键字的id **/
    private String id;

    /**
     * 关键字的名称 **/
    private String name;

    /**
     * 关键字的内容 **/
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
