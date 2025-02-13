package com.amos.codebook3.domain;

import java.io.Serializable;
/**
 * 用户保存的信息对象
 * */
public class DataObject implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public transient int id=-1;
    //网址
    public String url = null;
    //用户名
    public String username = null;
    //密码
    public String password=null;
    //描述
    public String description=null;

    @Override
    public String toString() {
        return "DataObject{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public DataObject() {
    }

    public DataObject(String password){
        this.password = password;
    }
    public DataObject(String username,String password){
        this.username=username;
        this.password=password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDescription() {
        return description;
    }
}
