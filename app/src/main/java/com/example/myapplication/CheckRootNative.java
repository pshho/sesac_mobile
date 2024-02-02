package com.example.myapplication;

import com.example.myapplication.SLog;

public class CheckRootNative {
    private static boolean libraryLoaded = false;

    public native int checkForRoot(Object[] objArr);

    public native int setLogDebugMessages(boolean z);

    static {
        try {
            System.loadLibrary("tool-checker");
            libraryLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            SLog.e(e);
        }
    }

    public boolean wasNativeLibraryLoaded() {
        return libraryLoaded;
    }

}
