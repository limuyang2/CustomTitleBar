package top.limuyang2.customtitlebar.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenHelper {

    /**
     * 屏幕密度
     */
    private static float sDensity = 0f;

    static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    private static float getDensity(Context context) {
        if (sDensity == 0f) {
            sDensity = getDisplayMetrics(context).density;
        }
        return sDensity;
    }

    /**
     * 单位转换: dp  px
     * @param context 上下文
     * @param dp dp
     * @return px
     */
    public static int dp2px(Context context, int dp) {
        return (int) (getDensity(context) * dp + 0.5);
    }

    /**
     * 单位转换: sp  px
     *
     * @param sp sp
     * @return px
     */
    public static int sp2px(Context context, int sp) {
        return (int) (getFontDensity(context) * sp + 0.5);
    }

    private static float getFontDensity(Context context) {
        return context.getResources().getDisplayMetrics().scaledDensity;
    }
}
