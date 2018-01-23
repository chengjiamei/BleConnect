package roc.cjm.bleconnect.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import roc.cjm.bleconnect.MyApp;
import roc.cjm.bleconnect.activitys.cmds.databases.DbMcCommand;
import roc.cjm.bleconnect.activitys.cmds.databases.DbTable;

/**
 * Created by Administrator on 2017/8/26.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static String dbName = "commanddb.db";
    private static DbHelper sIntances;
    private static int version = 2;
    private SQLiteDatabase db;

    public static DbHelper getInstance() {
        if (sIntances == null) {
            synchronized (DbHelper.class) {
                if (sIntances == null) {
                    sIntances = new DbHelper(MyApp.getInstance().getApplicationContext());
                }
            }
        }
        return sIntances;
    }

    private DbHelper(Context context) {
        super(context, dbName, null, version);
    }

    public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        db.execSQL(DbCommand.CREATE_SQL);
        db.execSQL(DbTable.CREATE_SQL);
    }

    public void replace(String table, String nullColumnHack, ContentValues initialValues){
        SQLiteDatabase db = getWritableDatabase();
        db.replace(table,nullColumnHack, initialValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.db = db;
        db.execSQL(DbCommand.CREATE_SQL);
        db.execSQL(DbTable.CREATE_SQL);
    }

    public long insert(String table, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(table, null, values);
    }

    public int delete(String tablename, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(tablename, whereClause, whereArgs);
    }

    public void update(String table, ContentValues values, String whereClause,
                       String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(table, values, whereClause, whereArgs);
    }

    public Cursor query(String tablename, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String orderby) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(tablename, columns, selection, selectionArgs, groupBy,
                null, orderby, null);
    }

    public Cursor query(String tablename, String[] columns, String selection,
                        String[] selectionArgs, String orderby) {
        return query(tablename, columns, selection, selectionArgs, null,
                orderby);
    }

    public void execSql(String sql){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
    }

    public void beginTransaction(){
        this.db = getWritableDatabase();
        db.beginTransaction();
    }

    public void endTransaction(){
        this.db = getWritableDatabase();
        db.endTransaction();
    }

    public void setTransactionSuccessful(){
        this.db = getWritableDatabase();
        db.setTransactionSuccessful();
    }
}
