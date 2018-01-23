package roc.cjm.bleconnect.utils;

import android.content.Context;

/**
 * Created by Administrator on 2017/8/28.
 */

public class Util {

    public static boolean isEmptyString(String str) {
        if(str == null || str.equals("")){
            return true;
        }
        return false;
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) (dpValue * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
