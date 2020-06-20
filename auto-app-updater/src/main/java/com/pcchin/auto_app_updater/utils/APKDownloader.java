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
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** The wrapper class that pass along the required arguments to the APKDownloadWorker. **/
public class APKDownloader {
    private Context context;

    private int notifIcon;
    private int maxRetryCount;
    private String downloadUrl;
    private String downloadPath;
    private String contentProvider;
    private String notifTitle;
    private String notifMsg;
    private String notifChannel;
    private Map<String, String> downloadParams;

    //****** Start of constructors ******//

    /** Creates a request to download the request path.
     * The default download path is set to be UpdaterFunctions.getInternalDownloadDir + "/.download.apk".
     * It is assumed that no special headers is required to get the file.
     * @param context The context needed to start the installer.
     * @param contentProvider The content provider needed to open the APK file. **/
    public APKDownloader(Context context, String contentProvider) {
        this.context = context;
        this.contentProvider = contentProvider;
        this.downloadPath = UpdaterFunctions.generateValidFile(String.format("%s%s",
                UpdaterFunctions.getInternalDownloadDir(context), ".download"));
        this.downloadParams = new HashMap<>();
        this.maxRetryCount = 5;
        this.notifTitle = UpdaterFunctions.getApplicationName(context);
        this.notifMsg = "Updating app";
        this.notifIcon = android.R.drawable.stat_sys_download;
        this.notifChannel = "Update App";
    }

    //****** Start of custom functions ******//

    /** Starts the APKDownloadWorker that is used to download and install the APK. **/
    public void start() {
        Data inputData = getInputData();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();
        WorkRequest request = new OneTimeWorkRequest.Builder(APKDownloadWorker.class)
                .setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .setConstraints(constraints).setInputData(inputData).build();
        final Operation operation = WorkManager.getInstance(context).enqueue(request);
        operation.getResult().addListener(new Runnable() {
            @Override
            public void run() {
                Operation.State currentState = operation.getState().getValue();
                if (currentState instanceof Operation.State.SUCCESS) {
                    onSuccess();
                } else {
                    onFailure(new IllegalStateException(String.format("APKDownloadWorker returned state %s", currentState)));
                }
            }
        }, Executors.newSingleThreadExecutor());
    }

    /** Gets the input data that will be passed on to the download worker. **/
    @NonNull
    private Data getInputData() {
        String[] keyArray = downloadParams.keySet().toArray(new String[0]),
                valuesArray = downloadParams.values().toArray(new String[0]);
        return new Data.Builder()
                .putString(APKDownloadWorker.CONTENT_PROVIDER, contentProvider)
                .putString(APKDownloadWorker.DOWNLOAD_URL, downloadUrl)
                .putString(APKDownloadWorker.DOWNLOAD_PATH, downloadPath)
                .putStringArray(APKDownloadWorker.HEADER_KEYS, keyArray)
                .putStringArray(APKDownloadWorker.HEADER_VALUES, valuesArray)
                .putInt(APKDownloadWorker.MAX_RETRY, maxRetryCount)
                .putString(APKDownloadWorker.NOTIF_TITLE, notifTitle)
                .putString(APKDownloadWorker.NOTIF_MSG, notifMsg)
                .putInt(APKDownloadWorker.NOTIF_ICON, notifIcon)
                .putString(APKDownloadWorker.NOTIF_CHANNEL, notifChannel)
                .build();
    }

    //****** Start of functions that can be overridden ******//

    /** Function that is called if the worker is able to run successfully.
     * Override this function if you wish to handle it manually. **/
    public void onSuccess() {
        // Stub function
    }

    /** Function that is called if the worker is unable to run successfully.
     * Override this function if you wish to handle it manually.
     * @param e The exception that causes the failure to occur.**/
    public void onFailure(@NonNull Exception e) {
        Log.w("APKDownloader", String.format("Attempt to run downloader failed with " +
                "exception %s, stack trace is", e.getMessage()));
        e.printStackTrace();
    }

    //****** Start of getters and setters ******//

    /** Sets the title of the notification that will be displayed when the worker is running,
     * defaults to the app name.
     * @param notifTitle The title of the notification. **/
    public void setNotifTitle(String notifTitle) {
        this.notifTitle = notifTitle;
    }

    /** Sets the message of the notification that will be displayed when the worker is running,
     * defaults to the message.
     * @param notifMsg The message of the notification. **/
    public void setNotifMsg(String notifMsg) {
        this.notifMsg = notifMsg;
    }

    /** Sets the notification icon that will be shown when the worker is running.
     * If this function is not called, a placeholder icon would be shown in the notification.
     * @param notifIcon The icon that would be shown in the notification. **/
    public void setNotifIcon(int notifIcon) {
        this.notifIcon = notifIcon;
    }

    /** Sets the notification channel that is used to display the notification for the worker,
     * defaults to 'Update App'.
     * @param notifChannel The notification channel that will be used by the worker to display the notification. **/
    @RequiresApi(Build.VERSION_CODES.O)
    public void setNotifChannel(String notifChannel) {
        this.notifChannel = notifChannel;
    }

    /** Sets the URL for the APK file that will be downloaded.
     * @param downloadUrl The URL that will be used to download the APK file. **/
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /** Sets the headers used in the download request.
     * @param downloadParams The headers used in the download request. **/
    public void setDownloadParams(Map<String, String> downloadParams) {
        this.downloadParams = downloadParams;
    }

    /** Sets the maximum amount of times the downloader will retry before failing.
     * @param maxRetryCount The maximum retry attempts that will be tried by the downloader. **/
    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    /** Sets the download path for the APK.
     * @param downloadPath The download path of the APK. **/
    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }
}
