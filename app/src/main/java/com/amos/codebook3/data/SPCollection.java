package com.amos.codebook3.data;

//存储的SharedPreference信息
public class SPCollection {
    //FN:fileName文件名
    //KN:KeyName:键名
    public static final String FN_DATABASE="DataBaseInfo";

    public static final String KN_DB_NAME="NAME";
    public static final String KN_DB_VERSION="VERSION";
    public static final String KN_DB_KEY="KEY";
    public static final String KN_DB_COUNT="COUNT";

    //密钥保存状态
    //   -1：软件首次启动  0：用户未保存密钥 1：用户已保存密钥
    public static final String KN_DB_KEY_IS_SAVED="KEY_IS_SAVED";

}
