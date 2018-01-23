package roc.cjm.bleconnect.activitys.cmds.databases;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import roc.cjm.bleconnect.activitys.cmds.entry.Command;
import roc.cjm.bleconnect.activitys.cmds.entry.Table;
import roc.cjm.bleconnect.databases.BaseDatabase;
import roc.cjm.bleconnect.databases.DbCommand;
import roc.cjm.bleconnect.databases.DbHelper;

/**
 * Created by Marcos on 2017/10/23.
 */

public class DbMcCommand extends BaseDatabase {

    public static String DEFAULT_TABLE = "default_table";

    private DbHelper helper;
    private static DbMcCommand instance;

    public static String COLUMN_PROFILE = "profile";
    public static String COLUMN_TYPE = "type";
    public static String COLUMN_CMD = "cmd";
    public static String COLUMN_REMARK = "remark";
    public static String COLUMN_DEFAULT = "defaultcmd";
    public static String COLUMN_INDEX = "mcindex";

    private Table table;

    public void createTable(Table tableName) {
        this.table = tableName;
        String CREATE_SQL = "create table if not exists '" + table.getTableName() +"'(" +
                "'" + COLUMN_CMD + "' blob not null ,"  +
                "'" + COLUMN_DEFAULT + "' blob not null," +
                "'" + COLUMN_PROFILE + "' integer," +
                "'" + COLUMN_REMARK + "' varchar(50)," +
                "'" + COLUMN_TYPE + "' integer," +
                "'" + COLUMN_INDEX +"' integer," +
                "primary key ('" + COLUMN_PROFILE+"','" + COLUMN_TYPE + "','" + COLUMN_INDEX +"')" +
                ")";
        helper.execSql(CREATE_SQL);
        DbTable.getInstance().replace(table);
    }

    public void setTable(Table table) {
        this.table = table;
    }

    private DbMcCommand() {
        helper = DbHelper.getInstance();
    }

    public static DbMcCommand getInstance() {
        if(instance == null) {
            synchronized (DbCommand.class) {
                if(instance == null) {
                    instance = new DbMcCommand();
                }
            }
        }
        return instance;
    }

    public ContentValues initWithCommand(Command cmd) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CMD, cmd.getCommand());
        values.put(COLUMN_PROFILE, cmd.getProfileType());
        values.put(COLUMN_TYPE, cmd.getType());
        values.put(COLUMN_REMARK, cmd.getRemark());
        values.put(COLUMN_INDEX, cmd.getIndex());
        values.put(COLUMN_DEFAULT, cmd.getDefaultCmd());
        return values;
    }


    public long insert(Command command) {
        return helper.insert(table.getTableName(), initWithCommand(command));
    }

    public void insert(List<Command> list) {
        helper.beginTransaction();
        try {
            for (int i=0;i<list.size(); i++) {
               insert(list.get(i));
            }
            helper.setTransactionSuccessful();
        }finally {
            helper.endTransaction();
        }
    }

    public void update(Command cmd, String wherearg, String[] args) {
        helper.update(table.getTableName() , initWithCommand(cmd), wherearg, args);
    }

    public void delete(String where, String[] args) {
        helper.delete(table.getTableName(), where, args);
    }

    public void replace(Command command) {
        helper.replace(table.getTableName(), null, initWithCommand(command));
    }

    public void replace(List<Command> list) {
        helper.beginTransaction();
        try {
            for (int i=0;i<list.size(); i++) {
                replace(list.get(i));
            }
            helper.setTransactionSuccessful();
        }finally {
            helper.endTransaction();
        }
    }

    public List<Command> query(String[] columns, String selection,
                      String[] selectionArgs, String groupBy, String orderby) {
        Cursor cursor = helper.query(table.getTableName(), columns, selection, selectionArgs, groupBy, orderby);
        List<Command> list = new ArrayList<>();
        if(cursor != null && cursor.getCount() >0 ) {
            while (cursor.moveToNext()) {
                int profile = cursor.getInt(cursor.getColumnIndex(COLUMN_PROFILE));
                int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                int index = cursor.getInt(cursor.getColumnIndex(COLUMN_INDEX));
                byte[] cmd = cursor.getBlob(cursor.getColumnIndex(COLUMN_CMD));
                byte[] defaultcmd = cursor.getBlob(cursor.getColumnIndex(COLUMN_DEFAULT));
                String remark = cursor.getString(cursor.getColumnIndex(COLUMN_REMARK));
                list.add(new Command(profile, type, index, defaultcmd, cmd , remark));
            }
        }
        if(cursor != null) {
            cursor.close();
            cursor = null;
        }
        return list;
    }
}
