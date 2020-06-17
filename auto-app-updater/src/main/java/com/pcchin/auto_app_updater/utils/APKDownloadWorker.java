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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/** The foreground worker that downloads and installs the updated APK.
 * Input data to this Worker should be inserted through setInputData(Data data).
 * The data that should be present includes:
 * CONTENT_PROVIDER (String): The content provider from the app that is used to open the APK file.
 * DOWNLOAD_URL (String): The URL of the APK that will be downloaded.
 * DOWNLOAD_PATH (String): The path that the APK will be downloaded to.
 * HEADER_KEYS (String[]): The keys used for the headers in the request that is used to download the file.
 * HEADER_VALUES (String[]): The values for the keys in HEADER_KEYS. Should be the same length as HEADER_KEYS.
 * NOTIF_TITLE (String): The title of the notification that will be shown.
 * NOTIF_ICON (int): The icon that will be used when displaying the notification.
 * NOTIF_CHANNEL (String): The channel that is used to display the notification.
 * NOTIF_MSG (String): The message of the notification that will be shown.
 * SHOW_APP_ICON (boolean): Whether to show the logo of the app when updating, defaults to false. **/
public class APKDownloadWorker extends Worker {
    public static final String APK_DOWNLOAD_WORKER = "APKDownloadWorker";
    private static final String FILE_ERROR = "File Error";

    public static final String CONTENT_PROVIDER = "contentProvider";
    public static final String DOWNLOAD_URL = "downloadUrl";
    public static final String DOWNLOAD_PATH = "downloadPath";
    public static final String HEADER_KEYS = "headerKeys";
    public static final String HEADER_VALUES = "headerValues";
    public static final String NOTIF_TITLE = "notifTitle";
    public static final String NOTIF_MSG = "notifMsg";
    public static final String NOTIF_ICON = "notifIcon";
    public static final String NOTIF_CHANNEL = "notifChannel";
    public static final String SHOW_NOTIF_ICON = "showNotifIcon";

    private Context context;

    // Notification variables
    private boolean showNotifIcon;
    private int notifIcon;
    private String notifTitle;
    private String notifMsg;
    private String notifChannel;

    // Download variables
    private String downloadUrl;
    private String downloadPath;
    private String contentProvider;
    private HashMap<String, String> downloadHeaders;
    private String[] headerKeys;
    private String[] headerValues;

    //****** Start of constructors ******//

    /** Creates the download worker instance. **/
    public APKDownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    //****** Start of custom functions ******//

    /** Checks that all the required input data is present before running the request. **/
    @NonNull
    @Override
    public Result doWork() {
        // Check if the required values are present within the input data
        contentProvider = getInputData().getString(CONTENT_PROVIDER);
        downloadUrl = getInputData().getString(DOWNLOAD_URL);
        downloadPath = getInputData().getString(DOWNLOAD_PATH);
        headerKeys = getInputData().getStringArray(HEADER_KEYS);
        headerValues = getInputData().getStringArray(HEADER_VALUES);
        notifTitle = getInputData().getString(NOTIF_TITLE);
        notifMsg = getInputData().getString(NOTIF_MSG);
        notifIcon = getInputData().getInt(NOTIF_ICON, 0);
        notifChannel = getInputData().getString(NOTIF_CHANNEL);
        showNotifIcon = getInputData().getBoolean(SHOW_NOTIF_ICON, false);
        if (!UpdaterFunctions.isConnected(context) || inputDataIsNull()) return Result.failure();
        // Populate the headers
        downloadHeaders = new HashMap<>();
        for (int i = 0; i < headerKeys.length; i++) {
            downloadHeaders.put(headerKeys[i], headerValues[i]);
        }
        // Shows the notification
        showNotif();
        return runDownloader();
    }

    /** Show the notification that will be displayed on top of the screen.
     * The channel ID is also created if it is not present. **/
    private void showNotif() {
        // Creates the channel ID if not present
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel updateChannel = new NotificationChannel(notifChannel,
                    notifChannel, NotificationManager.IMPORTANCE_DEFAULT);
            if (manager != null) {
                manager.createNotificationChannel(updateChannel);
            }
        }
        // Creates the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getPackageName());
        if (showNotifIcon) builder.setSmallIcon(notifIcon);
        builder.setContentTitle(notifTitle);
        builder.setContentText(notifMsg);
        builder.setTicker(notifTitle);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.setChannelId(notifChannel);
        setForegroundAsync(new ForegroundInfo(new Random().nextInt(), builder.build()));
    }

    //****** Start of custom functions ******//

    /** Checks if whether any of the required input data is not present or incorrect. **/
    private boolean inputDataIsNull() {
        return contentProvider == null || downloadUrl == null || downloadPath == null
                || headerKeys == null || headerValues == null
                || (headerKeys.length != headerValues.length) || notifTitle == null
                || notifMsg == null || notifChannel == null;
    }

    /** Runs the downloader and installer for the APK.
     * Returns Result.success() if the downloader and installer is able to start successfully,
     * and Result.retry() if the download fails due to a network error (VolleyError),
     * and Result.failure() for all other errors. **/
    @NonNull
    private Result runDownloader() {
        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<byte[]> future = RequestFuture.newFuture();
        FileDownloadRequest request = new FileDownloadRequest(downloadUrl, future, future, downloadHeaders);
        queue.add(request);

        try {
            byte[] apkFile = future.get();
            createApk(apkFile);
            return Result.success();
        } catch (InterruptedException e) {
            Log.w(APK_DOWNLOAD_WORKER, "Error: Volley download request interrupted with error");
            e.printStackTrace();
            return Result.retry();
        } catch (ExecutionException e) {
            Log.w(APK_DOWNLOAD_WORKER, "Error: Volley download request failed in middle of operation with error");
            e.printStackTrace();
            return Result.retry();
        } catch (IOException e) {
            Log.w(APK_DOWNLOAD_WORKER, String.format("%s: An IOException occurred at %s, stack trace is", FILE_ERROR, downloadPath));
            e.printStackTrace();
            showToast(FILE_ERROR);
            return Result.failure();
        }
    }

    /** Creates and install the APK for the app.
     * @param response The binary representation of the APK. **/
    private void createApk(byte[] response) throws IOException {
        if (response != null) {
            File outputFile = new File(downloadPath);
            if (outputFile.createNewFile()) {
                writeApk(response, outputFile);
            } else {
                String errorString = String.format("File %s cannot be created.", downloadPath);
                Log.w(APK_DOWNLOAD_WORKER, errorString);
                showToast("File Error");
                throw new IOException(errorString);
            }
        } else {
            throw new IOException("Response received from download request cannot be null.");
        }
    }

    /** Writes the downloaded APK into the file.
     * @param response The binary representation of the APK.
     * @param outputFile The output file for the APK. **/
    private void writeApk(byte[] response, @NonNull File outputFile) throws IOException {
        SharedPreferences sharedPref = context.getSharedPreferences("com.pcchin.auto_app_updater", Context.MODE_PRIVATE);
        Set<String> apkList = UpdaterFunctions.getApkStringSet(sharedPref);
        apkList.add(outputFile.getAbsolutePath());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("previousApkList", apkList);
        editor.apply();
        // Write output file with buffer
        InputStream input = new ByteArrayInputStream(response);
        BufferedOutputStream output = null;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            output = new BufferedOutputStream(new FileOutputStream(outputFile));
            byte[] data = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            installApp(outputFile);
        } finally {
            if (output != null) output.close();
        }
    }

    /** Installs the new app from the given file path.
     * @param outputFile The APK file that was just downloaded. **/
    private void installApp(File outputFile) {
        showToast("Updating app...");
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(FileProvider.getUriForFile(context,
                contentProvider, outputFile), "application/vnd.android.package-archive");
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(installIntent);
    }

    /** Shows a short toast with the given message.
     * @param msg The message that will be displayed in the toast. **/
    private void showToast(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
