package com.provesoft.resource.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class contains misc helper functions
 */
public final class SystemHelpers {

    private SystemHelpers() {}

    /**
     * Method gets current date and formats it as January 12, 2015
     * @return Date as String
     */
    public static String getCurrentDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        return sdf.format(new Date());
    }
}
