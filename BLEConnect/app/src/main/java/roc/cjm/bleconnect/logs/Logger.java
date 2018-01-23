package roc.cjm.bleconnect.logs;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

/**
 * Created by Administrator on 2017/8/28.
 */

public class Logger {

    public static int LOG_V_C = 0xff000000;
    public static int LOG_D_C = 0xff000000;
    public static int LOG_I_C = 0xff007f00;
    public static int LOG_W_C = 0xff00007f;
    public static int LOG_E_C = 0xff7f0000;

    public final static int LOG_V = 0;
    public final static int LOG_D = 1;
    public final static int LOG_I = 2;
    public final static int LOG_W = 3;
    public final static int LOG_E = 4;


    private int type;
    private StringBuilder log;

    public static SpannableString i(StringBuilder builder) {
        return initSpannableString(builder, LOG_I_C);
    }

    public static SpannableString v(StringBuilder builder) {
        return initSpannableString(builder, LOG_V_C);
    }

    public static SpannableString d(StringBuilder builder) {
        return initSpannableString(builder, LOG_D_C);
    }

    public static SpannableString w(StringBuilder builder) {
        return initSpannableString(builder, LOG_W_C);
    }

    public static SpannableString e(StringBuilder builder) {
        return initSpannableString(builder, LOG_E_C);
    }

    private static SpannableString initSpannableString(StringBuilder builder, int color) {
        SpannableString spannableString = new SpannableString(builder.toString());
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
        spannableString.setSpan(colorSpan, 0 , builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public static SpannableString log(StringBuilder builder, int level) {
        switch (level) {
            case LOG_V:
                return v(builder);
            case LOG_D:
                return d(builder);
            case LOG_I:
                return i(builder);
            case LOG_W:
                return w(builder);
            case LOG_E:
                return e(builder);
        }
        return null;
    }

}
