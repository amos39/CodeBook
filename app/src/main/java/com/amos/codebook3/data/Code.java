package com.amos.codebook3.data;

public class Code {
    //数据库错误类型
    //SQLiteDatabase内部执行出现错误
    public static final int INNER_ERROR_IN_SQLITEDATABASE_CLASS=1000;
    //DBHelper中的方法触发异常
    public static final int EXCEPTION_OCCUR_IN_DBHELPER_METHOD=1100;

    //DBHelper 获取写连接异常
    public static final int WRITE_LINK_ERROR=1200;
    //DBHelper 获取读连接异常
    public static final int READ_LINK_ERROR=1300;

    //无结果受影响
    public static final int NO_DATA_AFFECTED = 2000;
    //查询空结果
    public static final int EMPTY_RESULT=2100;

    //删除成功
    public static final int DELETE_SUCCESS=2001;
    //查询成功
    public static final int QUERY_SUCCESS=2002;
    //修改成功
    public static final int UPDATE_SUCCESS=2003;
    //增加成功
    public static final int ADD_SUCCESS=2004;

}
