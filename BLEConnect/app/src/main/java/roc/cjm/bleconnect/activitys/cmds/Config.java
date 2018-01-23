package roc.cjm.bleconnect.activitys.cmds;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Marcos on 2017/10/23.
 */

public class Config {

    public static final int PROFILE_NORMAL = 0x00;
    public static final int PROFILE_MC = 0x01;

    private static String CONFIG_PATH = "CONFIG_PATH";
    public static String KEY_PROFILE = "profile";
    public static String KEY_TABLEID = "tableid";
    public static String KEY_TABLENAME = "tablename";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static void init(Context ctx) {
        if(sharedPreferences == null) {
            sharedPreferences = ctx.getApplicationContext().getSharedPreferences(CONFIG_PATH, Context.MODE_PRIVATE);
        }
        if(editor == null) {
            editor = sharedPreferences.edit();
        }
    }

    public static String getString(Context ctx, String key, String defValue) {
        init(ctx);
        return sharedPreferences.getString(key, defValue);
    }

    public static int getInt(Context ctx, String key, int defValue) {
        init(ctx);
        return sharedPreferences.getInt(key, defValue);
    }

    public static long getLong(Context ctx, String key, long defValue) {
        init(ctx);
        return sharedPreferences.getLong(key, defValue);
    }

    public static Boolean getBoolean(Context ctx, String key, boolean defValue) {
        init(ctx);
        return sharedPreferences.getBoolean(key, defValue);
    }

    public static void putInt(Context ctx, String key, int defValue) {
        init(ctx);
        editor.putInt(key, defValue).commit();
    }

    public static void putLong(Context ctx, String key, long defValue) {
        init(ctx);
        editor.putLong(key, defValue).commit();
    }

    public static void putBoolean(Context ctx, String key, boolean defValue) {
        init(ctx);
        editor.putBoolean(key, defValue).commit();
    }

    public static void putString(Context ctx, String key, String defValue) {
        init(ctx);
        editor.putString(key, defValue).commit();
    }

}
