package com.example.authenticatorpoc.helpers;

import android.util.Log;

public final class Logger {

    public static void debug(String aTag, String aLine) {
        if (Consts.DEV_MODE) {
            StackTraceElement[] e = Thread.currentThread().getStackTrace();
            String filename = e[3].getFileName();
            int linenumber = e[3].getLineNumber();
            String functionCall = e[3].getMethodName();
            Log.d(aTag, aLine +
                    "\n" + filename +
                    ":" + linenumber +
                    ", " + functionCall);
        }
    }
}
