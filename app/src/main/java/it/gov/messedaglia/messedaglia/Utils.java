package it.gov.messedaglia.messedaglia;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Calendar;

public class Utils {

    public static int dpToPx (float dp){
        return (int) (Resources.getSystem().getDisplayMetrics().densityDpi*dp/DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String intervalToString (long interval) {
        if (interval < 0) return "mai";

        if (interval < 60 * 1000) return "ora";
        interval /= 60 * 1000;
        if (interval < 60) return interval+" minut"+ (interval == 1 ? 'o' : 'i') +" fa";
        interval /= 60;
        if (interval < 24) return interval+" or"+ (interval == 1 ? 'a' : 'e') +" fa";
        interval /= 24;
        return interval+" giorn"+ (interval == 1 ? 'o' : 'i') +" fa";
    }


}
