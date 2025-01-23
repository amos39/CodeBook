package com.amos.codebook3.data;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import com.amos.codebook3.domain.DataObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库帮助类，使用 SQLCipher 实现加密，继承 SQLiteOpenHelper。
 */
public class DBHelper extends SQLiteOpenHelper {
    // 数据库名称
    private static String DATABASE_NAME;
    // 数据库版本号
    private static int DATABASE_VERSION = -1;

    // 数据表名称
    public static final String TABLE_DATA = "data";
    // 数据表字段名称
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_DESCRIPTION = "description";

    // DBHelper单例实例
    private static DBHelper dbHelper = null;

    // 数据库密钥
    private static String DATABASE_KEY; // 修改为实际密钥

    // 创建数据表的 SQL 语句
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_DATA + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_URL + " TEXT NOT NULL, "
            + COLUMN_USERNAME + " TEXT NOT NULL, "
            + COLUMN_PASSWORD + " TEXT NOT NULL, "
            + COLUMN_DESCRIPTION + " TEXT NOT NULL);";

    /**
     * 构造方法，初始化数据库帮助类
     *
     * @param context 上下文对象
     */
    private DBHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void setDatabaseName(String databaseName) {
        DATABASE_NAME = databaseName;
    }

    public static void setDatabaseVersion(int databaseVersion) {
        DATABASE_VERSION = databaseVersion;
    }

    public static void setDatabaseKey(String databaseKey) {
        DATABASE_KEY = databaseKey;
    }

    /**
     * 获取 DBHelper 的单例实例，确保只有一个数据库帮助类实例存在。
     *
     * @param context 上下文对象
     * @return DBHelper 的实例
     */
    public static DBHelper getInstance(Context context) {
//        初始化DBHelper
        MultiDBManager.initDBHelper(context);

        if(DATABASE_NAME == null || DATABASE_VERSION == -1 || DATABASE_KEY == null){
            Log.e("DBHelper.getInstance","DBHelper init error!");
            return null;
        }

        if (dbHelper == null) {
            SQLiteDatabase.loadLibs(context); // 加载 SQLCipher 库
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        onCreate(db);
    }

    /**
     * 获取只读数据库实例。
     *
     * @return SQLiteDatabase 只读数据库对象
     */
    private SQLiteDatabase getReadDatabase() {
        return this.getReadableDatabase(DATABASE_KEY);
    }

    /**
     * 获取可写数据库实例。
     *
     * @return SQLiteDatabase 可写数据库对象
     */
    private SQLiteDatabase getWriteDatabase() {
        return this.getWritableDatabase(DATABASE_KEY);
    }

    /**
     * 插入数据对象到数据库。
     *
     * @param data 数据对象
     * @return 状态码（成功或失败）
     */
    public synchronized int insert(DataObject data) {
        SQLiteDatabase WDB = null;

        try {
            WDB = getWriteDatabase(); // 获取可写数据库实例
        } catch (Exception e) {
            return Code.WRITE_LINK_ERROR; // 写数据库失败
        }

        try {
            WDB.beginTransaction(); // 开始事务

            // 准备插入数据
            ContentValues values = new ContentValues();
            values.put(COLUMN_URL, data.url);
            values.put(COLUMN_USERNAME, data.username);
            values.put(COLUMN_PASSWORD, data.password);
            values.put(COLUMN_DESCRIPTION, data.description);

            // 执行插入操作
            long insertId = WDB.insert(TABLE_DATA, null, values);
            if (insertId != -1) {
                data.id = (int) insertId;  // 设置数据对象的 ID
                WDB.setTransactionSuccessful(); // 标记事务成功
                return Code.ADD_SUCCESS; // 插入成功
            } else {
                return Code.INNER_ERROR_IN_SQLITEDATABASE_CLASS; // 插入失败
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Code.EXCEPTION_OCCUR_IN_DBHELPER_METHOD; // 插入过程中发生异常
        } finally {
            if (WDB != null) {
                WDB.endTransaction(); // 结束事务
                WDB.close(); // 关闭数据库连接
            }
        }
    }

    /**
     * 更新数据库中的数据对象。
     *
     * @param data 数据对象
     * @return 状态码（成功或失败）
     */
    public synchronized int update(DataObject data) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_URL, data.url);
        values.put(COLUMN_USERNAME, data.username);
        values.put(COLUMN_PASSWORD, data.password);
        values.put(COLUMN_DESCRIPTION, data.description);

        SQLiteDatabase WDB = null;

        try {
            WDB = getWriteDatabase(); // 获取可写数据库实例
        } catch (Exception e) {
            return Code.WRITE_LINK_ERROR; // 写数据库失败
        }

        try {
            WDB.beginTransaction(); // 开始事务
            int rowsUpdated = WDB.update(TABLE_DATA, values,
                    COLUMN_ID + " = ?", new String[]{String.valueOf(data.id)});
            if (rowsUpdated > 0) {
                WDB.setTransactionSuccessful(); // 标记事务成功
                return Code.UPDATE_SUCCESS; // 更新成功
            } else {
                return Code.NO_DATA_AFFECTED; // 没有数据被更新
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Code.EXCEPTION_OCCUR_IN_DBHELPER_METHOD; // 更新过程中发生异常
        } finally {
            if (WDB != null) {
                WDB.endTransaction(); // 结束事务
                WDB.close(); // 关闭数据库连接
            }
        }
    }

    /**
     * 从数据库中删除指定的数据对象。
     *
     * @param data 数据对象
     * @return 状态码（成功或失败）
     */
    public synchronized int delete(DataObject data) {
        SQLiteDatabase WDB = null;

        try {
            WDB = getWriteDatabase(); // 获取可写数据库实例
        } catch (Exception e) {
            return Code.WRITE_LINK_ERROR; // 写数据库失败
        }

        try {
            WDB.beginTransaction(); // 开始事务
            int rowsDeleted = WDB.delete(TABLE_DATA,
                    COLUMN_ID + " = ?", new String[]{String.valueOf(data.id)});
            if (rowsDeleted > 0) {
                WDB.setTransactionSuccessful(); // 标记事务成功
                return Code.DELETE_SUCCESS; // 删除成功
            } else {
                return Code.NO_DATA_AFFECTED; // 没有数据被删除
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Code.EXCEPTION_OCCUR_IN_DBHELPER_METHOD; // 删除过程中发生异常
        } finally {
            if (WDB != null) {
                WDB.endTransaction(); // 结束事务
                WDB.close(); // 关闭数据库连接
            }
        }
    }

    /**
     * 获取数据库中的所有数据对象。
     *
     * @return 数据对象列表
     */
    public List<DataObject> getAllData() {
        SQLiteDatabase RDB = getReadDatabase();
        List<DataObject> dataList = new ArrayList<>();
        Cursor cursor = RDB.query(TABLE_DATA,
                null, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DataObject data = cursorToData(cursor); // 将游标转换为数据对象
            dataList.add(data);
            cursor.moveToNext();
        }
        cursor.close();
        return dataList;
    }

    /**
     * 根据关键词搜索数据对象。
     *
     * @param query  搜索关键词
     * @param sortBy 排序字段
     * @return 数据对象列表
     */
    public List<DataObject> searchData(String query, String sortBy) {
        SQLiteDatabase RDB = getReadDatabase();
        String selection = COLUMN_URL + " LIKE ? OR " +
                COLUMN_USERNAME + " LIKE ? OR " +
                COLUMN_DESCRIPTION + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"};
        String orderBy = sortBy + " COLLATE NOCASE ASC";

        List<DataObject> dataList = new ArrayList<>();
        Cursor cursor = RDB.query(TABLE_DATA,
                null, selection, selectionArgs, null, null, orderBy);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DataObject data = cursorToData(cursor); // 将游标转换为数据对象
            dataList.add(data);
            cursor.moveToNext();
        }
        cursor.close();
        return dataList;
    }

    /**
     * 将游标转换为数据对象。
     *
     * @param cursor 游标
     * @return 数据对象
     */
    private DataObject cursorToData(Cursor cursor) {
        DataObject data = new DataObject();
        data.id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        data.url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL));
        data.username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
        data.password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
        data.description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
        return data;
    }

}
