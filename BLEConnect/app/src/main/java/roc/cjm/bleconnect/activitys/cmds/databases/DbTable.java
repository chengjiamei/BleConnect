package roc.cjm.bleconnect.activitys.cmds.databases;

/**
 * Created by Marcos on 2017/10/24.
 */

import android.app.ActionBar;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import roc.cjm.bleconnect.activitys.cmds.entry.Table;
import roc.cjm.bleconnect.databases.DbHelper;

/**
 * 保存表名和id
 */
public class DbTable {
    public static String TABLE_NAME = "tables";

    public static String COLUMN_ID = "tid";
    public static String COLUMN_PROFILE = "profile";
    public static String COLUMN_NAME = "name";
    public static String COLUMN_REMARK = "remark";
    public static String COLUMN_TIME = "dateString";

    public static String CREATE_SQL = "create table if not exists '"+ TABLE_NAME+"'("+
            "'" + COLUMN_ID + "' integer identity," +
            "'" + COLUMN_PROFILE + "' integer," +
            "'" + COLUMN_NAME + "' varchar(10) not null," +
            "'" + COLUMN_REMARK +"' varchar(100) not null," +
            "'" +COLUMN_TIME + "' long," +
            "primary key ('"+ COLUMN_ID+"')" +
            ")";


    private DbHelper helper;
    private volatile static DbTable instance;

    private DbTable() {
        helper = DbHelper.getInstance();
    }

    public static DbTable getInstance() {
        if(instance == null) {
            synchronized (DbTable.class) {
                if(instance == null) {
                    instance = new DbTable();
                }
            }
        }
        return instance;
    }

    public List<Table> findAll(String where, String[] whereArgs) {
        Cursor cursor = helper.query(TABLE_NAME, null, where, whereArgs, COLUMN_TIME+" ASC");
        List<Table> list = new ArrayList<>();
        if(cursor != null && cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                long time = cursor.getLong(cursor.getColumnIndex(COLUMN_TIME));
                int profiletype = cursor.getInt(cursor.getColumnIndex(COLUMN_PROFILE));
                String remark = cursor.getString(cursor.getColumnIndex(COLUMN_REMARK));
                Table table = new Table( profiletype, name, remark, time);
                list.add(table);
            }
        }
        if(cursor != null) {
            cursor.close();
        }
        return list;
    }

    private ContentValues initWithTable(Table table) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, table.getTableName());
        values.put(COLUMN_PROFILE, table.getProfileType());
        values.put(COLUMN_TIME, table.getDateTime());
        values.put(COLUMN_REMARK, table.getTableMark());
        return values;
    }

    public long insert(Table table) {
        return helper.insert(TABLE_NAME, initWithTable(table));
    }

    public void insert(List<Table> list) {
        helper.beginTransaction();
        try {
            if (list != null && list.size()>0) {
                for (int i=0;i<list.size();i++) {
                    insert(list.get(i));
                }
                helper.setTransactionSuccessful();
            }
        }finally {
            helper.endTransaction();
        }
    }

    public void replace(Table table) {
        helper.replace(TABLE_NAME, null, initWithTable(table));
    }

    public void replace(List<Table> list) {
        helper.beginTransaction();
        try {
            if (list != null && list.size()>0) {
                for (int i=0;i<list.size();i++) {
                    replace(list.get(i));
                }
                helper.setTransactionSuccessful();
            }
        }finally {
            helper.endTransaction();
        }
    }

    public void delete(String where, String[] whereargs) {
        helper.delete(TABLE_NAME , where ,whereargs);
    }

    public void delete(Table table) {
        helper.delete(TABLE_NAME , COLUMN_PROFILE + "=? and " + COLUMN_NAME +"=?" ,new String[]{table.getProfileType() +"", table.getTableName()});
    }
}
