package com.example.authenticatorpoc.helpers;

import android.util.Log;

import com.example.authenticatorpoc.constants.Consts;

public final class Logger {

    // When DEV_MODE boolean is set to true, prints debug logging
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
