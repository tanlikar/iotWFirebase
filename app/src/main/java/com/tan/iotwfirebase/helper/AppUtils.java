package com.tan.iotwfirebase.helper;

import android.os.Build;

/**
 * Created by tanli on 1/8/2018.
 */

public class AppUtils {

    /**
     * @return true If device has Android Marshmallow or above version
     */
    public static boolean hasM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
