package com.provesoft.resource.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class SystemHelpers {

    private SystemHelpers() {}

    public static String getCurrentDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        return sdf.format(new Date());
    }
}
