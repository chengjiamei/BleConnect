package roc.cjm.bleconnect.databases;

import android.database.Cursor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/26.
 */

public class BaseDatabase {

    public DbHelper helper;

    public float[] sum(String tableName, String[] columns, String selection, String[] selectionArgs) {
        if (columns != null) {
            String[] aliasName = new String[columns.length];
            for (int i=0;i<columns.length;i++){
                aliasName[i] = "sum"+i;
                columns[i] = columns[i]+" as "+aliasName[i];
            }
            Cursor cursor = helper.query(tableName, columns, selection, selectionArgs, null);
            float[] tp = new float[columns.length];
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                for (int i = 0; i < columns.length; i++) {
                    tp[i] = cursor.getFloat(cursor.getColumnIndex(aliasName[i]));
                }
            }
            return tp;
        }
        return null;
    }

    public String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    public int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    public float getFloat(Cursor cursor, String columnName) {
        return cursor.getFloat(cursor.getColumnIndex(columnName));
    }

    public long getLong(Cursor cursor, String columName) {
        return cursor.getLong(cursor.getColumnIndex(columName));
    }

    /**
     * @param obj
     * @return
     */
    public static byte[] toByteArray(Serializable obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /**
     * 从字节数组获取对象
     * @EditTime 2007-8-13 上午11:46:34
     */
    public static Object getObjectFromBytes(byte[] objBytes) {
        if (objBytes == null || objBytes.length == 0) {
            return null;
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(objBytes);
        ObjectInputStream oi = null;
        try {
            oi = new ObjectInputStream(bi);
            return oi.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return oi;
    }
}
