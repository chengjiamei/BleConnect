package roc.cjm.bleconnect.utils;

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
}
