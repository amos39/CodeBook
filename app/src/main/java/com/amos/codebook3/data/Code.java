/*
   ______    ___   ______   ________  ______      ___      ___   ___  ____
 .' ___  | .'   `.|_   _ `.|_   __  ||_   _ \   .'   `.  .'   `.|_  ||_  _|
/ .'   \_|/  .-.  \ | | `. \ | |_ \_|  | |_) | /  .-.  \/  .-.  \ | |_/ /
| |       | |   | | | |  | | |  _| _   |  __'. | |   | || |   | | |  __'.
\ `.___.'\\  `-'  /_| |_.' /_| |__/ | _| |__) |\  `-'  /\  `-'  /_| |  \ \_
 `.____ .' `.___.'|______.'|________||_______/  `.___.'  `.___.'|____||____|

    https://github.com/amos39/CodeBook

*/
package com.amos.codebook3.data;

public class Code {
    //数据库错误类型
    //SQLiteDatabase内部执行出现错误
    public static final int INNER_ERROR_IN_SQLITEDATABASE_CLASS=1001;
    //DBHelper中的方法触发异常
    public static final int EXCEPTION_OCCUR_IN_DBHELPER_METHOD=1002;

    //DBHelper 获取写连接异常
    public static final int WRITE_LINK_ERROR=1003;
    //DBHelper 获取读连接异常
    public static final int READ_LINK_ERROR=1004;

    //无结果受影响
    public static final int NO_DATA_AFFECTED = 1005;
    //查询空结果
    public static final int EMPTY_RESULT=1006;

    //删除成功
    public static final int DELETE_SUCCESS=1007;
    //查询成功
    public static final int QUERY_SUCCESS=1008;
    //修改成功
    public static final int UPDATE_SUCCESS=1009;
    //增加成功
    public static final int ADD_SUCCESS=1010;

    //数据库重置成功
    public static final int DATABASE_DELETE_SUCCESS=1011;

    //数据库重置失败
    public static final int DATABASE_DELETE_FAILED=1012;

    //删除成功的String
    public static final String STR_DELETE_SUCCESS="删除成功";
    //重置成功的String
    public static final String STR_RESET_SUCCESS = "重置成功";
}
