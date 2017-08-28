package roc.cjm.bleconnect.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;

import roc.cjm.bleconnect.MyApp;


/**
 * @content 获取设备的相关信息
 * @author ShuLQ
 */
public class DeviceConfiger {

    public static float sDensity;
    public static float sFontScale;
    public static int sWidth;
    public static int sHeight;
    public static Context sContext;
    public static String sDeviceId;

    static {
        init();
    }

    /**
     * @content 获取设备的屏幕尺寸和屏幕密度
     */
    public static void init() {
        sContext = MyApp.getInstance();
        DisplayMetrics dm = sContext.getResources().getDisplayMetrics();
        sDensity = dm.density;
        sWidth = dm.widthPixels;
        sHeight = dm.heightPixels;
        sFontScale = dm.scaledDensity;
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取
     * 
     * @return
     */
    public static int getAPPVersion() {
        PackageManager manager = sContext.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(sContext.getPackageName(), 0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 屏幕像素宽
     * 
     * @return
     */
    public static int getScreenWidth() {
        return sWidth;
    }

    /**
     * 屏幕像素高
     * 
     * @return
     */
    public static int getScreenHeight() {
        return sHeight;
    }

    /**
     * 屏幕密度
     * 
     * @return
     */
    public static float getScreenDensity() {
        return sDensity;
    }

    /**
     * 
     * @return
     */
    public static float getFontScale() {
        return sFontScale;
    }

    /**
     * 获取屏幕方向： 1 、横屏 ORIENTATION_LANDSCAPE 2 、竖屏 ORIENTATION_PORTRAIT
     * 
     * @return
     */
    public static int getScreenOrientation() {
        Configuration config = sContext.getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏
            return 1;
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏
            return 2;
        }
        return 0;
    }

    /**
     * @content dp转px
     * @param dpValue
     *            dp值
     * @return
     */
    public static int dp2px(float dpValue) {
        return (int) (dpValue * sDensity + 0.5f);
    }

    /**
     * @content px转dp
     * @param pxValue
     *            px值
     * @return
     */
    public static int px2dp(float pxValue) {
        return (int) (pxValue / sDensity + 0.5f);
    }

    /**
     * @content px转换为sp，保证文字大小不变
     * @author ShuLQ
     * @param pxValue
     * @return
     */
    public static int px2sp(float pxValue) {

        return (int) (pxValue / sFontScale + 0.5f);
    }

    public static int sp2Dp(float sp){
        return (int) ((sp - 0.5f)*sFontScale);
    }

    /**
     * @content dp转换为sp，保证文字大小不变
     * @author ShuLQ
     * @param dpValue
     * @return
     */
    public static int dp2sp(float dpValue) {
        return px2sp(dp2px(dpValue));
    }


    /**
     * @content 获得语言编码
     * @return
     */
    public static String getLanguage() {
        return sContext.getResources().getConfiguration().locale.getLanguage();
    }

    /**
     * @content 获得语言编码
     * @return
     */
    public static String getCountry() {
        return sContext.getResources().getConfiguration().locale.getCountry();
    }

    /**
     * @content 获取android的版本
     * @author ShuLQ
     * @return
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * @content 获取联网类型
     * @author ShuLQ
     * @return
     */
    public static String getNetType() {
        ConnectivityManager connectionManager = (ConnectivityManager) sContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            return networkInfo.getTypeName();
        }
        return null;
    }

    /**
     * 获取联网类型 2G 3G 4G WIFI UNKNOWN NONE
     * 
     * @return
     */
    public static String getNetTypeInChina() {
        ConnectivityManager cm = (ConnectivityManager) sContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return "WIFI";
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {
                case 1:
                case 2:
                case 4:
                    return "2G";
                case 3:
                case 5:
                case 6:
                case 8:
                case 12:
                case 15:
                    return "3G";
                case 13:
                    return "4G";
                default:
                    return "UNKNOWN";
                }
            } else {
                return "UNKNOWN";
            }
        }
        return "NONE";
    }


    /**
     * 屏幕宽的比例对应的宽度
     */
    public static int getScreenWPcr(float pcr) {
        return (int) (sWidth * pcr);
    }
}
