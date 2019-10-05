package it.gov.messedaglia.messedaglia;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Utils {

    public static int dpToPx (float dp){
        return (int) (Resources.getSystem().getDisplayMetrics().densityDpi*dp/DisplayMetrics.DENSITY_DEFAULT);
    }

}
