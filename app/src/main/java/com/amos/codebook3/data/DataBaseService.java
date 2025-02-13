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
import android.content.Context;

import com.amos.codebook3.domain.DataObject;
import com.amos.codebook3.domain.Result;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class DataBaseService {

    //插入操作
    public static String insert(Context context, DataObject data){
        DBHelper dbHelper=DBHelper.getInstance(context);
        if(dbHelper == null){
            return "数据库初始化失败";
        }
        int result=dbHelper.insert(data);

        if(result == Code.ADD_SUCCESS) return "添加成功";
        else if(result == Code.WRITE_LINK_ERROR) return "数据库写连接打开失败";
        else if(result == Code.INNER_ERROR_IN_SQLITEDATABASE_CLASS) return "SQLiteDataBase内部函数执行错误";
        else if(result == Code.EXCEPTION_OCCUR_IN_DBHELPER_METHOD) return "DBHelper方法执行出现异常";
        else return "unknow";
    }

    public static String update(Context context,DataObject data){
        DBHelper dbHelper=DBHelper.getInstance(context);
        if(dbHelper == null){
            return "数据库初始化失败";
        }
        int result=dbHelper.update(data);

        if(result == Code.UPDATE_SUCCESS) return "更新成功";
        else if(result == Code.WRITE_LINK_ERROR) return "数据库写连接打开失败";
        else if(result == Code.NO_DATA_AFFECTED) return "没有数据被影响";
        else if(result == Code.EXCEPTION_OCCUR_IN_DBHELPER_METHOD) return "DBHelper方法执行出现异常";
        else return "unknow";
    }

    public static String delete(Context context,DataObject data){
        DBHelper dbHelper=DBHelper.getInstance(context);
        if(dbHelper == null){
            return "数据库初始化失败";
        }
        int result=dbHelper.delete(data);

        if(result == Code.DELETE_SUCCESS) return "删除成功";
        else if(result == Code.WRITE_LINK_ERROR) return "数据库写连接打开失败";
        else if(result == Code.NO_DATA_AFFECTED) return "没有数据被影响";
        else if(result == Code.EXCEPTION_OCCUR_IN_DBHELPER_METHOD) return "DBHelper方法执行出现异常";
        else return "unknow";
    }

    public static Result<List<DataObject>> getAllData(Context context) {
        List<DataObject> list;
        DBHelper dbHelper = DBHelper.getInstance(context);
        if(dbHelper == null){
            return new Result<>("数据库初始化失败",null);
        }
        try{
            list=dbHelper.getAllData();
        }catch (Exception e){
            e.printStackTrace();
            return new Result("查询发生异常",null);
        }
        return new Result(null,list);
    }

    public static Result<List<DataObject>> searchData(Context context,String query, String sortBy){
        //过滤非法字符
       Set<String> set = new HashSet<>();
       List<DataObject> list;
       DBHelper dbHelper=DBHelper.getInstance(context);
        if(dbHelper == null){
            return new Result<>("数据库初始化失败",null);
        }
        set.add(DBHelper.COLUMN_USERNAME);
        set.add(DBHelper.COLUMN_URL);
        set.add(DBHelper.COLUMN_DESCRIPTION);

        if(!set.contains(sortBy)){
            return new Result("非法查询字符",null);
        }

        try{
            list=dbHelper.searchData(query,sortBy);
        }catch (Exception e){
            e.printStackTrace();
            return new Result("查询发生异常",null);
        }
        return new Result(null,list);
    }

    //更新密钥
    public static Result<String> renewDataBase(Context context,String newKey){
        int result = MultiDBManager.renewDataBase(context,newKey);
        if(result == 1){
            //成功
            return new Result<>("更改成功",null);
        }
        if(result == 0){
            return new Result<>("更改失败",null);
        }
        if(result == -10 || result == -1){
            return new Result<>("发生致命错误",null);
        }
        else{
            return new Result<>("unknow",null);
        }
    }

    //删除数据库并创建一个空数据库
    public synchronized static String deleteAndResetDataBase(Context context, String databaseName){
        File databaseFile =null;
        try{
            databaseFile= context.getDatabasePath(databaseName);
        }catch (Exception e){
            e.printStackTrace();
            return "数据库名错误";
        }

        if(!databaseFile.exists()){

        }
        boolean result=MultiDBManager.deleteSpecificDatabase(context,databaseName);
        if(result){

                return Code.STR_RESET_SUCCESS;
        }else{
            return "重置失败";
        }

    }

}
