/*
 * Copyright 2020 PC Chin. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pcchin.auto_app_updater.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/** A utility class for functions that are used in the updater. **/
public class UpdaterFunctions {
    private UpdaterFunctions() {
        throw new IllegalStateException("Utility class!");
    }

    /** A static function that checks whether there is currently network connection for the app.
     * @return true if the device is connected to the internet and false if not.
     * @param context The context needed to get the connectivity service. **/
    public static boolean isConnected(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT < 23) {
            final NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null) {
                return ni.isConnected();
            }
        } else {
            final Network n = cm.getActiveNetwork();
            if (n != null) {
                final NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                if (nc != null) {
                    return nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                }
            }
        }
        return false;
    }

    /** Gets the application name of the app from the given context.
     * @param context The context of the current application. **/
    static String getApplicationName(@NonNull Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    /** Get the internal download directory of the app.
     * Falls back to the root directory if no such download directory could be found.
     * The path will always end in '/'.
     * @param context The context for the app. **/
    @NonNull
    static String getInternalDownloadDir(@NonNull Context context) {
        File downloadDirFile = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDirFile == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            downloadDirFile = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        }
        return downloadDirFile == null ? "/storage/emulated/0/" : downloadDirFile.getAbsolutePath() + "/";
    }

    /** Generates a valid file in the required directory.
     * If a file with the same name exists,
     * a file with incrementing number will be added to the file.
     * @param fullPathName The absolute path to the directory of the file including the file name. **/
    static String generateValidFile(String fullPathName) {
        String returnFile = fullPathName + ".apk";
        int i = 1;
        while (new File(returnFile).exists() && i < Integer.MAX_VALUE) {
            returnFile = fullPathName + "(" + i + ")" + ".apk";
            i++;
        }
        return returnFile;
    }

    /** Gets the set of Strings of the downloaded APKs.
     * Returns a new HashSet if no values are found.
     * This is required due to the following restriction:
     * https://medium.com/@anupamchugh/a-nightmare-with-shared-preferences-and-stringset-c53f39f1ef52
     * @param sharedPref The shared preferences that the string set is stored in.**/
    @NonNull
    public static Set<String> getApkStringSet(@NonNull SharedPreferences sharedPref) {
        Set<String> sharedPrefValues = sharedPref.getStringSet("previousApkList", null);
        if (sharedPrefValues == null) return new HashSet<>();
        else return new HashSet<>(sharedPrefValues);
    }
}
