package roc.cjm.bleconnect;

import android.app.Application;

import roc.cjm.bleconnect.activitys.cmds.McConfig;

/**
 * Created by Carmy Cheng on 2017/8/23.
 */

public class MyApp extends Application {

    private static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        McConfig.getInstance();
    }

    public static MyApp getInstance(){
        return instance;
    }
}
