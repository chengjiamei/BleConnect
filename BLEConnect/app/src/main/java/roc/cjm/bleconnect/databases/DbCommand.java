package roc.cjm.bleconnect.databases;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import roc.cjm.bleconnect.services.entry.Command;
import roc.cjm.bleconnect.services.entry.CommandList;

/**
 * Created by Administrator on 2017/8/26.
 */

public class DbCommand extends BaseDatabase {

    public static String TABLE_NAME = "command";
    public static String COLUMN_TIME = "column_time";
    public static String COLUMN_MARK = "column_mark";
    public static String COLUMN_COMMANDS = "column_commands";

    private DbHelper helper;
    private static DbCommand instance;

    public static String CREATE_SQL = "create table  if not exists '"+TABLE_NAME+"' ("+
            "'" + COLUMN_COMMANDS + "' blod not null," +
            "'" + COLUMN_MARK + "' varchar(100)," +
            "'" + COLUMN_TIME + "' long"+
            ");";

    private DbCommand() {
        helper = DbHelper.getInstance();
    }

    public static DbCommand getInstance() {
        if(instance == null) {
            synchronized (DbCommand.class) {
                if(instance == null) {
                    instance = new DbCommand();
                }
            }
        }
        return instance;
    }

    public void update(ContentValues values, String whereCause, String[] whereArgs) {
        helper.update(TABLE_NAME, values, whereCause, whereArgs);
    }

    public void delete(String whereCause, String[] whereArgs) {
        helper.delete(TABLE_NAME, whereCause, whereArgs);
    }

    public Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String orderBy) {
        return helper.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, orderBy);
    }

    /**String mac,String dateString, int stepNum, int sleepState
     *
     *  COLUMN_DATE COLUMN_MAC is PRIMARY KEY
     **/
    public List<CommandList> findAll(String selection, String[] selectionArgs, String orderby) {
        Cursor cursor = helper.query(TABLE_NAME, null, selection, selectionArgs, orderby);

        List<CommandList> list = new ArrayList<>();
        if (cursor != null) {
            int count = cursor.getCount();
            try {
                while (cursor.moveToNext()) {
                    //long createTime, String remark, List<Command> commandList
                    CommandList historySport = new CommandList(getLong(cursor, COLUMN_TIME), getString(cursor, COLUMN_MARK),
                            (ArrayList<Command>) getObjectFromBytes(cursor.getBlob(cursor.getColumnIndex(COLUMN_COMMANDS))));
                    list.add(historySport);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    public CommandList findFirst(String selection,String[] selectionArgs){
        Cursor cursor = helper.query(TABLE_NAME,null,selection,selectionArgs,null,null);
        if(cursor != null && cursor.getCount()>0){
            if(cursor.moveToFirst()){
                //String mac,String dateString, int stepNum, int sleepState
                CommandList historySport = new CommandList(getLong(cursor, COLUMN_TIME), getString(cursor, COLUMN_MARK),
                        (ArrayList<Command>) getObjectFromBytes(cursor.getBlob(cursor.getColumnIndex(COLUMN_COMMANDS))));
                cursor.close();
                cursor = null;
                return historySport;
            }
        }
        if(cursor != null){
            cursor.close();
            cursor = null;
        }
        return null;
    }

    public void saveOrUpdate(List<CommandList> list) {
        if (list != null && list.size() > 0) {
            helper.beginTransaction();
            try {
                for (int i = 0; i < list.size(); i++) {
                    CommandList historySport = list.get(i);
                    saveOrUpdate(historySport);
                }
                helper.setTransactionSuccessful();
            }finally {
                helper.endTransaction();
            }
        }
    }

    public void saveOrUpdate(CommandList commandList) {
        helper.replace(TABLE_NAME, null ,contentWithDevice(commandList));
    }

    private ContentValues contentWithDevice(CommandList commandList) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMMANDS, toByteArray(commandList.getCommandList()));
        values.put(COLUMN_MARK, commandList.getRemark());
        values.put(COLUMN_TIME, commandList.getCreateTime());
        return values;
    }

    private long insert(CommandList historySport) {
        ContentValues values = contentWithDevice(historySport);
        return helper.insert(TABLE_NAME, values);
    }

    private void insert(List<CommandList> list) {
        if (list != null) {
            helper.beginTransaction();
            try {
                for (int i = 0; i < list.size(); i++) {
                    insert(list.get(i));
                }
                helper.setTransactionSuccessful();
            } finally {
                helper.endTransaction();
            }
        }
    }

    public void beginTransaction(){
        helper.beginTransaction();
    }

    public void endTransaction(){
        helper.endTransaction();
    }

    public void setTransactionSuccessful(){
        helper.setTransactionSuccessful();
    }


}
