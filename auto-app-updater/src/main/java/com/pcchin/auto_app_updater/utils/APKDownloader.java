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
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.pcchin.auto_app_updater.dialogs.ProgressBarDialog;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** The downloader that downloads the updated APK of the app. **/
public class APKDownloader {
    private static final String APK_DOWNLOADER = "APKDownloader";
    private static final String FILE_ERROR = "File Error";

    private Context context;
    private FragmentManager manager;

    private boolean showDownloadDialog; // Whether to show the Downloading... dialog
    private DialogFragment downloadingDialog; // The Downloading... dialog
    private String downloadUrl;
    private String dlPath;
    private String contentProvider;
    private Map<String, String> dlParams;


    //****** Start of constructors ******//

    /** Creates a request to download the request path.
     * The default download path is set to be UpdaterFunctions.getInternalDownloadDir + "/.download.apk".
     * It is assumed that no special headers is required to get the file.
     * @param context The context needed to start the installer.
     * @param manager The manager that would be used to show the download dialog.
     * @param downloadUrl The download URL needed to start the app.
     * @param contentProvider The content provider needed to open the APK file. **/
    public APKDownloader(Context context, FragmentManager manager, String downloadUrl, String contentProvider) {
        this(context, manager, downloadUrl, UpdaterFunctions.generateValidFile(String.format("%s%s",
                UpdaterFunctions.getInternalDownloadDir(context), ".download"), ".apk"), contentProvider);
    }

    /** Creates a request to download the request path.
     * It is assumed that no special headers is required to get the file.
     * @param context The context needed to start the installer.
     * @param manager The manager that would be used to show the download dialog.
     * @param downloadUrl The download URL needed to start the app.
     * @param dlPath The absolute download path for the downloaded APK.
     * @param contentProvider The content provider needed to open the APK file. **/
    public APKDownloader(Context context, FragmentManager manager, String downloadUrl, String dlPath, String contentProvider) {
        this(context, manager, downloadUrl, dlPath, contentProvider, new HashMap<String, String>());
    }

    /** Creates a request to download the request path with the given headers.
     * The default download path is set to be UpdaterFunctions.getInternalDownloadDir + "/.download.apk".
     * @param context The context needed to start the installer.
     * @param manager The manager that would be used to show the download dialog.
     * @param downloadUrl The download URL needed to start the app.
     * @param contentProvider The content provider needed to open the APK file.
     * @param dlParams The header parameters in the GET request used to download the app. **/
    public APKDownloader(Context context, FragmentManager manager, String downloadUrl,
                         String contentProvider, Map<String, String> dlParams) {
        this(context, manager, downloadUrl, contentProvider, UpdaterFunctions.generateValidFile(String.format("%s%s",
                UpdaterFunctions.getInternalDownloadDir(context), ".download"), ".apk"), dlParams);
    }

    /** Creates a request to download the request path with the given headers.
     * @param context The context needed to start the installer.
     * @param manager The manager that would be used to show the download dialog.
     * @param downloadUrl The download URL needed to start the app.
     * @param dlPath The absolute download path for the downloaded APK.
     * @param dlParams The header parameters in the GET request used to download the app. **/
    public APKDownloader(Context context, FragmentManager manager, String downloadUrl, String contentProvider,
                         String dlPath, Map<String, String> dlParams) {
        this.context = context;
        this.manager = manager;
        this.downloadUrl = downloadUrl;
        this.contentProvider = contentProvider;
        this.dlPath = dlPath;
        this.dlParams = dlParams;
        this.showDownloadDialog = true;
    }

    //****** Start of getters and setters ******//

    /** Sets whether to show the Downloading... dialog, defaults to true. **/
    public void setShowDownloadDialog(boolean showDialog) {
        this.showDownloadDialog = showDialog;
    }

    //****** Start of custom functions ******//

    /** Starts the download and installation process for the APK. **/
    public void start() {
        RequestQueue queue = Volley.newRequestQueue(context);
        Response.Listener<byte[]> response = getResponseListener(downloadUrl, dlPath);
        Response.ErrorListener errorListener = getResponseErrorListener(downloadUrl);
        Request<byte[]> request = createDownloadRequest(downloadUrl, response, errorListener, dlParams);
        if (showDownloadDialog) {
            downloadingDialog = getDownloadDialog(request);
            downloadingDialog.show(manager, "Downloading");
        }
        queue.add(request);
    }

    /** Gets the listener that is called when the file is successfully downloaded.
     * Override this function if you wish to install the file manually. **/
    public Response.Listener<byte[]> getResponseListener(final String downloadUrl, final String dlPath) {
        return new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] response) {
                Log.d(APK_DOWNLOADER, String.format("Downloading from %s", downloadUrl));
                if (downloadingDialog != null) downloadingDialog.dismiss();
                tryCreateApk(response, dlPath);
            }
        };
    }

    /** The try / catch blocks for createApk. **/
    private void tryCreateApk(byte[] response, String dlPath) {
        try {
            createApk(response, dlPath);
        } catch (FileNotFoundException e) {
            Log.w(APK_DOWNLOADER, String.format("%s: File %s not found, stack trace is", FILE_ERROR, dlPath));
            e.printStackTrace();
            Toast.makeText(context, FILE_ERROR, Toast.LENGTH_SHORT).show();
        } catch (IOException e2) {
            Log.w(APK_DOWNLOADER, String.format("%s: An IOException occurred at %s, stack trace is", FILE_ERROR, dlPath));
            e2.printStackTrace();
            Toast.makeText(context, FILE_ERROR, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.w(APK_DOWNLOADER, "Error: Volley download request failed in middle of operation with error");
            e.printStackTrace();
            Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
        }
    }

    /** Creates and install the APK for the app. **/
    private void createApk(byte[] response, String dlPath) throws IOException {
        if (response != null) {
            File outputFile = new File(dlPath);
            if (outputFile.createNewFile()) {
                writeApk(response, outputFile);
            } else {
                Log.w(APK_DOWNLOADER, String.format("File %s cannot be created.", dlPath));
                Toast.makeText(context, FILE_ERROR, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Writes the downloaded APK into the file. **/
    private void writeApk(byte[] response, @NonNull File outputFile) throws IOException {
        SharedPreferences sharedPref = context.getSharedPreferences("com.pcchin.auto_app_updater", Context.MODE_PRIVATE);
        Set<String> apkList = UpdaterFunctions.getStringSet(sharedPref);
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

    /** Installs the new app from the given file path. **/
    private void installApp(File outputFile) {
        Toast.makeText(context, "Updating file...", Toast.LENGTH_SHORT).show();
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(FileProvider.getUriForFile(context,
                contentProvider, outputFile), "application/vnd.android.package-archive");
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(installIntent);
    }

    /** Gets the listener that is called when an error occurs during the process of downloading the file.
     * Override this function if you wish to install the file manually. **/
    @NonNull
    public Response.ErrorListener getResponseErrorListener(final String downloadUrl) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w(APK_DOWNLOADER, String.format(
                        "Unable to download from URL %s, error is %s, stack trace is",
                        downloadUrl, error.getMessage()));
                error.printStackTrace();
            }
        };
    }

    /** Gets the Downloading... dialog.
     * Override this function if you wish to display your own custom Downloading... dialog **/
    @SuppressWarnings("unused")
    public DialogFragment getDownloadDialog(Request<byte[]> request) {
        return new ProgressBarDialog();
    }

    /** Creates the Volley request required to download the URL.
     * Override this function to set your own download request based on the download URL given. **/
    public Request<byte[]> createDownloadRequest(String downloadUrl, Response.Listener<byte[]> response,
                                                 Response.ErrorListener errorListener, Map<String, String> params) {
        return new FileDownloadRequest(downloadUrl, response, errorListener, params);
    }
}
