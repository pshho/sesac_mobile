package com.example.myapplication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import com.example.myapplication.SLog;
import com.example.myapplication.Const;
import com.example.myapplication.CheckRootNative;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class CheckRoot {
    private boolean loggingEnabled = true;
    private final Context mContext;

    public CheckRoot(Context context) {
        this.mContext = context;
    }

    public boolean isRooted() {
        return detectRootManagementApps() || detectPotentiallyDangerousApps() || checkForBinary("su") || checkForDangerousProps() || checkForRWPaths() || detectTestKeys() || checkSuExists() || checkForRootNative() || checkForMagiskBinary();
    }

    @Deprecated
    public boolean isRootedWithoutBusyBoxCheck() {
        return isRooted();
    }

    public boolean isRootedWithBusyBoxCheck() {
        return detectRootManagementApps() || detectPotentiallyDangerousApps() || checkForBinary("su") || checkForBinary("busybox") || checkForDangerousProps() || checkForRWPaths() || detectTestKeys() || checkSuExists() || checkForRootNative() || checkForMagiskBinary();
    }

    public boolean detectTestKeys() {
        String str = Build.TAGS;
        return str != null && str.contains("test-keys");
    }

    public boolean detectRootManagementApps() {
        return detectRootManagementApps(null);
    }

    public boolean detectRootManagementApps(String[] strArr) {
        ArrayList arrayList = new ArrayList(Arrays.asList(Const.knownRootAppsPackages));
        if (strArr != null && strArr.length > 0) {
            arrayList.addAll(Arrays.asList(strArr));
        }
        return isAnyPackageFromListInstalled(arrayList);
    }

    public boolean detectPotentiallyDangerousApps() {
        return detectPotentiallyDangerousApps(null);
    }

    public boolean detectPotentiallyDangerousApps(String[] strArr) {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(Arrays.asList(Const.knownDangerousAppsPackages));
        if (strArr != null && strArr.length > 0) {
            arrayList.addAll(Arrays.asList(strArr));
        }
        return isAnyPackageFromListInstalled(arrayList);
    }

    public boolean detectRootCloakingApps() {
        return detectRootCloakingApps(null) || (canLoadNativeLibrary() && !checkForNativeLibraryReadAccess());
    }

    public boolean detectRootCloakingApps(String[] strArr) {
        ArrayList arrayList = new ArrayList(Arrays.asList(Const.knownRootCloakingPackages));
        if (strArr != null && strArr.length > 0) {
            arrayList.addAll(Arrays.asList(strArr));
        }
        return isAnyPackageFromListInstalled(arrayList);
    }

    public boolean checkForSuBinary() {
        return checkForBinary("su");
    }

    public boolean checkForMagiskBinary() {
        return checkForBinary("magisk");
    }

    public boolean checkForBusyBoxBinary() {
        return checkForBinary("busybox");
    }

    public boolean checkForBinary(String str) {
        String[] paths;
        boolean z = false;
        for (String str2 : Const.getPaths()) {
            String str3 = str2 + str;
            if (new File(str2, str).exists()) {
                SLog.v(str3 + " binary detected!");
                z = true;
            }
        }
        return z;
    }

    public void setLogging(boolean z) {
        this.loggingEnabled = z;
        SLog.LOGGING_LEVEL = z ? 5 : 0;
    }

    private String[] propsReader() {
        try {
            InputStream inputStream = Runtime.getRuntime().exec("getprop").getInputStream();
            if (inputStream == null) {
                return null;
            }
            return new Scanner(inputStream).useDelimiter("\\A").next().split("\n");
        } catch (IOException | NoSuchElementException e) {
            SLog.e(e);
            return null;
        }
    }

    private String[] mountReader() {
        try {
            InputStream inputStream = Runtime.getRuntime().exec("mount").getInputStream();
            if (inputStream == null) {
                return null;
            }
            return new Scanner(inputStream).useDelimiter("\\A").next().split("\n");
        } catch (IOException | NoSuchElementException e) {
            SLog.e(e);
            return null;
        }
    }

    private boolean isAnyPackageFromListInstalled(List<String> list) {
        PackageManager packageManager = this.mContext.getPackageManager();
        boolean z = false;
        for (String str : list) {
            try {
                packageManager.getPackageInfo(str, 0);
                SLog.e(str + " ROOT management app detected!");
                z = true;
            } catch (PackageManager.NameNotFoundException unused) {
            }
        }
        return z;
    }

    public boolean checkForDangerousProps() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("ro.debuggable", "1");
        hashMap.put("ro.secure", "0");
        String[] propsReader = propsReader();
        if (propsReader == null) {
            return false;
        }
        boolean z = false;
        for (String str : propsReader) {
            for (String str2 : hashMap.keySet()) {
                if (str.contains(str2)) {
                    String str3 = "[" + ((String) hashMap.get(str2)) + "]";
                    if (str.contains(str3)) {
                        SLog.v(str2 + " = " + str3 + " detected!");
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public boolean checkForRWPaths() {
        String str;
        String str2;
        String[] strArr;
        String[] mountReader = mountReader();
        if (mountReader == null) {
            return false;
        }
        int i = Build.VERSION.SDK_INT;
        int length = mountReader.length;
        int i2 = 0;
        boolean z = false;
        while (i2 < length) {
            String str3 = mountReader[i2];
            String[] split = str3.split(" ");
            int i3 = 23;
            if ((i <= 23 && split.length < 4) || (i > 23 && split.length < 6)) {
                SLog.e("Error formatting mount line: " + str3);
            } else {
                if (i > 23) {
                    str = split[2];
                    str2 = split[5];
                } else {
                    str = split[1];
                    str2 = split[3];
                }
                String[] strArr2 = Const.pathsThatShouldNotBeWritable;
                int length2 = strArr2.length;
                int i4 = 0;
                while (i4 < length2) {
                    String str4 = strArr2[i4];
                    if (str.equalsIgnoreCase(str4)) {
//                        if (Build.VERSION.SDK_INT > i3) {
//                            str2 = str2.replace("(", com.example.myapplication.BuildConfig.FLAVOR).replace(")", com.example.myapplication.BuildConfig.FLAVOR);
//                        }
                        String[] split2 = str2.split(",");
                        int length3 = split2.length;
                        int i5 = 0;
                        while (i5 < length3) {
                            strArr = mountReader;
                            if (split2[i5].equalsIgnoreCase("rw")) {
                                SLog.v(str4 + " path is mounted with rw permissions! " + str3);
                                z = true;
                                break;
                            }
                            i5++;
                            mountReader = strArr;
                        }
                    }
                    strArr = mountReader;
                    i4++;
                    mountReader = strArr;
                    i3 = 23;
                }
            }
            i2++;
            mountReader = mountReader;
        }
        return z;
    }

    public boolean checkSuExists() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"which", "su"});
            boolean z = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine() != null;
            if (process != null) {
                process.destroy();
            }
            return z;
        } catch (Throwable unused) {
            if (process != null) {
                process.destroy();
            }
            return false;
        }
    }

    public boolean checkForNativeLibraryReadAccess() {
        try {
            new CheckRootNative().setLogDebugMessages(this.loggingEnabled);
            return true;
        } catch (UnsatisfiedLinkError unused) {
            return false;
        }
    }

    public boolean canLoadNativeLibrary() {
        return new CheckRootNative().wasNativeLibraryLoaded();
    }

    public boolean checkForRootNative() {
        if (!canLoadNativeLibrary()) {
            SLog.e("We could not load the native library to test for root");
            return false;
        }
        String[] paths = Const.getPaths();
        int length = paths.length;
        String[] strArr = new String[length];
        for (int i = 0; i < length; i++) {
            strArr[i] = paths[i] + "su";
        }
        CheckRootNative rootBeerNative = new CheckRootNative();
        try {
            rootBeerNative.setLogDebugMessages(this.loggingEnabled);
            return rootBeerNative.checkForRoot(strArr) > 0;
        } catch (UnsatisfiedLinkError unused) {
            return false;
        }
    }
}
