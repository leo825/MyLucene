package com.leo.solr;

import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;

/**
 * Created by LX on 2017/9/4.
 */
public class Article {
    // 文章id
    @Field
    public String id;
    // 文章分类id
    public String categoryId;
    // 作者id
    public String authorId;
    // 文章标题
    @Field
    public String name;
    // 文章内容
    @Field
    public String content;
    // 发布时间
    @Field
    public Date createTime;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}