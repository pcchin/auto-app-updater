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

package com.pcchin.auto_app_updater;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.fragment.app.FragmentManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.pcchin.auto_app_updater.endpoint.Endpoint;
import com.pcchin.auto_app_updater.utils.APKDownloader;
import com.pcchin.auto_app_updater.utils.UpdaterDialog;
import com.pcchin.auto_app_updater.utils.UpdaterFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/** An updater that checks for updates to the app.
 * This is the class that should be started first when running the updater. **/
public class AutoAppUpdater {
    private Context context;
    private ErrorListener listener;

    private int updateInterval; // The update interval for the app (In seconds).
    private List<Endpoint> endpointList; // All the possible endpoints for updating the app.

    /** The type of update checks that will be performed. **/
    public enum UpdateType {
        /** The semantic versioning from http://semver.org/. **/
        SEMANTIC,
        /** As long as the version provided differs from the current version, a version update would be needed. **/
        DIFFERENCE,
        /** The version provided will be a number, and if the number is larger than the current one,
         * the app would need to be updated. **/
        INCREMENTAL,
        /** The version provided will be a decimal number, and if the number is larger than the current one,
         * the app would need to be updated. **/
        DECIMAL_INCREMENTAL
    }

    /** The constructor for the class, only used by the builder.
     * @param context The context used by the app. **/
    private AutoAppUpdater(Context context) {
        this.context = context;
        deletePreviousAPKs();
    }

    /** Delete the previous APKs that are downloaded from the app. **/
    private void deletePreviousAPKs() {
        SharedPreferences sharedPref = context.getSharedPreferences("com.pcchin.auto_app_updater", Context.MODE_PRIVATE);
        Set<String> previousApkList = UpdaterFunctions.getApkStringSet(sharedPref);
        for (String previousApk: previousApkList) {
            if (new File(previousApk).delete()) {
                previousApkList.remove(previousApk);
            }
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("previousApkList", null);
        editor.apply();
    }

    /** Starts the update checking process. **/
    public void run() {
        if (UpdaterFunctions.isConnected(context)) {
            SharedPreferences sharedPref = context.getSharedPreferences("com.pcchin.auto_app_updater", Context.MODE_PRIVATE);
            long lastRunTime = sharedPref.getLong("lastRunTime", 0);
            long currentTime = new Date().getTime();
            if (((currentTime - lastRunTime) / 1000) >= updateInterval) {
                // Update the shared preferences
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong("lastRunTime", currentTime);
                editor.apply();

                if (endpointList.size() > 0) {
                    endpointList.get(0).update();
                }
            }
        }
    }

    /** Function that is called when all of the endpoints fail.
     * Use an AutoAppUpdater.ErrorListener to handle the error. **/
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void onFailure(final Exception e) {
        if (this.listener == null) {
            throw new IllegalStateException("AutoAppUpdater.ErrorListener cannot be null!");
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailure(e);
                }
            });
        }
    }

    /** The builder class for creating the AutoAppUpdater.
     * The order of method calls should be
     * setUpdateType -> setCurrentVersion -> setUpdateDialog (Optional) -> setDownloader (Optional)
     * -> addEndpoint / addEndpoints.
     * The full lifecycle can be found in the Wiki of the repository.**/
    public static class Builder {
        private Context bContext;
        private ErrorListener bListener;
        private FragmentManager bFragmentManager;
        private String bFragmentTag; // The tag of the fragment that would be shown, defaults, to "AutoAppUpdater".

        private String bContentProvider; // The content provider that will open the APK file needed to install it.
        private UpdateType bUpdateType; // The update type of the app, defaults to UpdateType.DIFFERENCE.
        private int bUpdateInterval; // The interval between updating the app (In seconds), defaults to 86400 (One day).
        private List<Endpoint> bEndpointList = new ArrayList<>();
        private UpdaterDialog bUpdateDialog; // Defaults to UpdaterDialog without any additional arguments.
        private RequestQueue bQueue;

        // Current version
        private String bCurrentVersionStr;
        private Integer bCurrentVersionInt;
        private Float bCurrentVersionDecimal;

        /** The default constructor for the builder.
         * The default values for variables are set here.
         * @param context The context that will be used by the updater.
         * @param manager The Fragment manager that will be used to display the update dialog.
         * @param contentProvider The content provider that will be used to open the downloaded APK file. (e.g. com.pcchin.aausample.ContentProvider) **/
        public Builder(@NonNull Context context, @NonNull FragmentManager manager, String contentProvider) {
            this(context, manager, "AutoAppUpdater", contentProvider);
        }

        /** The default constructor for the builder.
         * The default values for variables are set here.
         * @param context The context that will be used by the updater.
         * @param manager The Fragment manager that will be used to display the update dialog.
         * @param tag The tag that will be used to display the update dialog.
         * @param contentProvider The content provider that will be used to open the downloaded APK file. (e.g. com.pcchin.aausample.ContentProvider)**/
        public Builder(@NonNull Context context, @NonNull FragmentManager manager, String tag, String contentProvider) {
            this.bContext = context;
            this.bUpdateType = UpdateType.DIFFERENCE;
            this.bFragmentTag = tag;
            this.bUpdateInterval = 60 * 60 * 24;
            this.bUpdateDialog = new UpdaterDialog(context, contentProvider);
            this.bFragmentManager = manager;
            this.bQueue = Volley.newRequestQueue(context);
            this.bContentProvider = contentProvider;
            this.bListener = new ErrorListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.w("AutoAppUpdater", String.format("Attempt to update app failed " +
                            "with error %s, stack trace is ", e.getMessage()));
                    e.printStackTrace();
                    Toast.makeText(bContext, "Network Error", Toast.LENGTH_SHORT).show();
                }
            };
        }

        /** Sets the update type of the updater.
         * If a type is not set, it is assumed to be UpdateType.DIFFERENCE.
         * @param type The update type that will be used. **/
        public Builder setUpdateType(UpdateType type) {
            this.bUpdateType = type;
            return this;
        }

        /** Sets the update interval of the updater.
         * The update interval will not be checked if it is run within that period.
         * If the update interval is not set, it is assumed to be 86400 seconds. (One day)
         * @param interval The update interval for the app (In seconds). **/
        public Builder setUpdateInterval(int interval) {
            if (interval < 0) {
                throw new IllegalArgumentException(String.format("Interval must be above or equal to 0, got %s", interval));
            } else {
                this.bUpdateInterval = interval;
                return this;
            }
        }

        /** Sets the current version of the app.
         * This should be used in conjunction with UpdateType.DIFFERENCE / UpdateType.
         * and should be used after setUpdateType is called.
         * @param version The current version (as a String) of the app. **/
        public Builder setCurrentVersion(@NonNull String version) {
            if (bUpdateType != UpdateType.DIFFERENCE && bUpdateType != UpdateType.SEMANTIC) {
                throw new IllegalStateException(String.format("Incorrect update type set, expected" +
                        " UpdateType.DIFFERENCE but got %s", bUpdateType));
            }
            this.bCurrentVersionStr = version;
            return this;
        }

        /** Sets the current version of the app.
         * This should be used in conjunction with UpdateType.INCREMENTAL
         * and should be used after setUpdateType is called.
         * @param version The current version (as an integer) of the app.**/
        public Builder setCurrentVersion(int version) {
            if (bUpdateType != UpdateType.INCREMENTAL) {
                throw new IllegalStateException(String.format("Incorrect update type set, expected" +
                        " UpdateType.INCREMENTAL but got %s", bUpdateType));
            }
            this.bCurrentVersionInt = version;
            return this;
        }

        /** Sets the current version of the app.
         * This should be used in conjunction with UpdateType.DECIMAL_INCREMENTAL
         * and should be used after setUpdateType is called.
         * @param version The current version (as a float) of the app.**/
        public Builder setCurrentVersion(float version) {
            if (bUpdateType != UpdateType.DECIMAL_INCREMENTAL) {
                throw new IllegalStateException(String.format("Incorrect update type set, expected" +
                        " UpdateType.DECIMAL_INCREMENTAL but got %s", bUpdateType));
            }
            this.bCurrentVersionDecimal = version;
            return this;
        }

        /** Adds an endpoint to the app.
         * This should be used after setCurrentVersion is called.
         * @param endpoint The update endpoint to be added. **/
        public Builder addEndpoint(@NonNull Endpoint endpoint) {
            checkEndpointRequirements();
            setEndpointProperties(endpoint);
            bEndpointList.add(endpoint);
            return this;
        }

        /** Sets the endpoints for the app, takes in a list containing multiple endpoints.
         * The endpoints at the start of the list will be executed first.
         * @param endpoints A list of update endpoints that will be added. **/
        public Builder addEndpointList(@NonNull List<Endpoint> endpoints) {
            checkEndpointRequirements();
            for (Endpoint endpoint: endpoints) {
                setEndpointProperties(endpoint);
                bEndpointList.add(endpoint);
            }
            return this;
        }

        /** Sets the endpoints for the app, takes in a list containing multiple endpoints.
         * The endpoints at the start of the list will be executed first.
         * @param endpoints An array of update endpoints that will be added.**/
        public Builder addEndpointList(@NonNull Endpoint[] endpoints) {
            return addEndpoints(endpoints);
        }

        /** Sets the endpoints for the app, takes in multiple arguments.
         * The endpoints at the start of the arguments list will be executed first.
         * @param endpoints The update endpoints that will be added. **/
        public Builder addEndpoints(@NonNull Endpoint... endpoints) {
            checkEndpointRequirements();
            for (Endpoint endpoint: endpoints) {
                setEndpointProperties(endpoint);
                bEndpointList.add(endpoint);
            }
            return this;
        }

        /** Sets the update dialog for the updater, defaults to UpdaterDialog without any additional arguments.
         * This should be called before addEndpoint or addEndpoints call as they rely on this dialog.
         * @param dialog The dialog that will be shown when a newer version is found.
         *               If you wish to use a custom dialog, you would need to extend UpdaterDialog to do so. **/
        public Builder setUpdateDialog(@NonNull UpdaterDialog dialog) {
            this.bUpdateDialog = dialog;
            return this;
        }

        /** Sets the downloader that will be used in the update dialog to download the APK.
         * @param downloader The APK downloader that will be downloading the APK. **/
        public Builder setDownloader(APKDownloader downloader) {
            if (this.bUpdateDialog == null) throw new IllegalStateException("Updater dialog cannot be null!");
            this.bUpdateDialog.setDownloader(downloader);
            return this;
        }

        /** Sets the error listener which deals with the error thrown by the endpoint.
         * @param errorListener The error listener that handles the error. **/
        public Builder setErrorListener(ErrorListener errorListener) {
            this.bListener = errorListener;
            return this;
        }

        /** Sets the properties of the endpoint before passing them on to the app updater.
         * @param endpoint The endpoint that will be added to the app updater. **/
        private void setEndpointProperties(@NonNull Endpoint endpoint) {
            endpoint.setContentProvider(bContext, bContentProvider);
            endpoint.setUpdateDialog(bUpdateDialog, bFragmentManager, bFragmentTag);
            if (bUpdateType == UpdateType.SEMANTIC) {
                endpoint.setCurrentVersion(bCurrentVersionStr, true);
            } else if (bUpdateType == UpdateType.DIFFERENCE) {
                endpoint.setCurrentVersion(bCurrentVersionStr, false);
            } else if (bUpdateType == UpdateType.INCREMENTAL) {
                endpoint.setCurrentVersion(bCurrentVersionInt);
            } else {
                endpoint.setCurrentVersion(bCurrentVersionDecimal);
            }
            endpoint.setRequestQueue(bQueue);
        }

        /** Check whether the requirements are met for the endpoint. **/
        private void checkEndpointRequirements() {
            if (bUpdateType == null) {
                throw new IllegalStateException("Update type should be set before endpoints can be added.");
            } else if (bCurrentVersionStr == null && bCurrentVersionInt == null && bCurrentVersionDecimal == null) {
                throw new IllegalStateException("setCurrentVersion should be called before endpoints can be added.");
            } else if (bUpdateDialog == null) {
                throw new IllegalStateException("Update dialog should be provided before endpoints can be added.");
            }
        }

        /** Creates the Auto App Updater based on the parameters given. **/
        public AutoAppUpdater build() {
            AutoAppUpdater updater = new AutoAppUpdater(bContext);
            updater.endpointList = this.bEndpointList;
            for (int i = updater.endpointList.size() - 1; i > 0; i--) {
                updater.endpointList.get(i - 1).setBackupEndpoint(updater.endpointList.get(i));
            }
            updater.updateInterval = this.bUpdateInterval;
            updater.listener = bListener;
            return updater;
        }
    }

    /** The class that handles the error returned by the endpoint. **/
    public abstract static class ErrorListener {
        /** The function that is called when the error is returned by the endpoint.
         * @param e The exception that is thrown by the endpoint. **/
        public abstract void onFailure(Exception e);
    }
}
