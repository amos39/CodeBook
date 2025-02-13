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
import android.content.SharedPreferences;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

//用户更改数据库密钥时使用
public class MultiDBManager {
    //将共享参数中的配置注入到DBHelper中
    public static void initDBHelper(Context context){
        // 获取指定名称的 SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPCollection.FN_DATABASE, Context.MODE_PRIVATE);
//        注入到DBHelper中
        DBHelper.setDatabaseName(sharedPreferences.getString(SPCollection.KN_DB_NAME,null));
        DBHelper.setDatabaseVersion(sharedPreferences.getInt(SPCollection.KN_DB_VERSION,-1));
        DBHelper.setDatabaseKey(sharedPreferences.getString(SPCollection.KN_DB_KEY,null));
    }

    // 更新数据库(变更密钥)
    /**
     * @return {发生异常但对数据库无更改：0，发生致命且损害原来数据库：-10，更改成功 1，发生错误但未影响到数据库：-1}
     *
     */
    public static synchronized int renewDataBase(Context context, String newKey) {
        if (newKey == null) {
            Log.e("DataBaseRenew", "empty newKey");
            return 0;
        }

        SharedPreferences preferences = context.getSharedPreferences(SPCollection.FN_DATABASE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // 当前的数据库信息
        String oldName = preferences.getString(SPCollection.KN_DB_NAME, null);
        String oldKey = preferences.getString(SPCollection.KN_DB_KEY, null);
        int count = preferences.getInt(SPCollection.KN_DB_COUNT, -1);

        if (oldName == null || oldKey == null || count == -1) {
            Log.e("DataBaseRenew", "error database info");
            return 0;
        }

        String databasePath = context.getDatabasePath(oldName).getAbsolutePath();
        SQLiteDatabase db = null;

        try {
            // 打开数据库
            db = SQLiteDatabase.openOrCreateDatabase(databasePath, oldKey, null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DataBaseRenew", "cannot open oldDataBase");
            return 0;
        }
        //关闭数据库无法更改密钥
//        //关闭数据库
//        try {
//            if (db != null && db.isOpen()) {
//                db.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("DataBaseRenew", "cannot close oldDataBase");
//            return 0;
//        }

        try {
            // 更改密钥
            db.rawExecSQL("PRAGMA rekey = '" + newKey + "';");

            // 验证新密钥是否有效
            SQLiteDatabase dbNew = SQLiteDatabase.openOrCreateDatabase(databasePath, newKey, null);
            dbNew.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                //使用oldKey连接是否正常
                SQLiteDatabase dbNew = SQLiteDatabase.openOrCreateDatabase(databasePath, oldKey, null);
                dbNew.close();
                return 0;
            } catch (Exception e2) {
                e.printStackTrace();
                Log.e("DataBaseRenew", "DATABASE DEAD!!!");
                return -10;
            }
        }
        try {
            // 写入新的密钥到共享参数
            editor.putString(SPCollection.KN_DB_KEY, newKey);
            editor.apply(); // 确保提交成功
            //重新注入修改后的值到DBHelper
           //
            Log.i("DataBaseRenew", "Database key changed successfully!");
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("DataBaseRenew", "new Key Not Write In");
            return -1;

        }
    }
//删除数据库
    public synchronized static boolean deleteSpecificDatabase(Context context,String databaseName) {
        return context.deleteDatabase(databaseName);
    }
}
