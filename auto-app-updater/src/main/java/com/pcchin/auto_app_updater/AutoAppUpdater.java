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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.pcchin.auto_app_updater.endpoint.Endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An updater that checks for updates to the app. **/
public class AutoAppUpdater {
    private Context context;
    private FragmentManager manager;
    private String fragmentTag;

    private UPDATE_TYPE updateType;
    private int updateInterval; // The update interval for the app (In seconds).
    private List<Endpoint> endpointList; // All the possible endpoints for updating the app.
    private UpdaterDialog updateDialog; // The dialog that is shown when the update is called.
    private RequestQueue queue; // The request queue to send the requests

    private String currentVersionStr; // The current version for the app. (UPDATE_TYPE.DIFFERENCE)
    private int currentVersionInt; // The current version for the app. (UPDATE_TYPE.INCREMENTAL)
    private float currentVersionDecimal; // The current version for the app. (UPDATE_TYPE.DECIMAL_INCREMENTAL)

    // The type of update checks that will be performed
    public enum UPDATE_TYPE {
        // As long as the version provided differs from the current version, a version update would be needed.
        DIFFERENCE,
        // The version provided will be a number, and if the number is larger than the current one,
        // the app would need to be updated
        INCREMENTAL,
        // Same thing as incremental, but for decimal numbers
        DECIMAL_INCREMENTAL
    }

    /** The constructor for the class, only used by the builder. **/
    private AutoAppUpdater(Context context, FragmentManager manager, String fragmentTag) {
        this.context = context;
        this.manager = manager;
        this.fragmentTag = fragmentTag;
    }

    /** Starts the update checking process. **/
    public void run() {
        if (endpointList.size() > 0) {
            endpointList.get(0).update();
        }
    }

    /** The builder class for creating the AutoAppUpdater.
     * The order of method calls should be
     * setUpdateType -> setCurrentVersion -> setNotifChannel (If needed) -> setUpdateDialog -> addEndpoint / addEndpoints. **/
    public static class Builder {
        private Context bContext;
        private FragmentManager bFragmentManager;
        private String bFragmentTag; // The tag of the fragment that would be shown, defaults, to "AutoAppUpdater".

        private UPDATE_TYPE bUpdateType; // The update type of the app, defaults to UPDATE_TYPE.DIFFERENCE.
        private int bUpdateInterval; // The interval between updating the app (In seconds), defaults to 86400 (One day).
        private List<Endpoint> bEndpointList = new ArrayList<>();
        private UpdaterDialog bUpdateDialog; // Defaults to UpdaterDialog without any additional arguments.
        private RequestQueue bQueue;

        // Variables that are only present inside the builder
        private boolean bShowNotif;
        private String bNotifChannel;

        // Current version
        private String bCurrentVersionStr;
        private Integer bCurrentVersionInt;
        private Float bCurrentVersionDecimal;

        /** The default constructor for the builder.
         * The default values for variables are set here. **/
        public Builder(@NonNull Context context, @NonNull FragmentManager manager) {
            this(context, manager, "AutoAppUpdater");
        }

        /** The default constructor for the builder.
         * The default values for variables are set here. **/
        public Builder(@NonNull Context context, @NonNull FragmentManager manager, String tag) {
            this.bContext = context;
            this.bUpdateType = UPDATE_TYPE.DIFFERENCE;
            this.bFragmentTag = tag;
            this.bUpdateInterval = 60 * 60 * 24;
            this.bUpdateDialog = new UpdaterDialog();
            this.bFragmentManager = manager;
            this.bQueue = Volley.newRequestQueue(context);
            this.bShowNotif = true;
        }

        /** Sets the update type of the updater.
         * If a type is not set, it is assumed to be UPDATE_TYPE.DIFFERENCE. **/
        public Builder setUpdateType(UPDATE_TYPE type) {
            this.bUpdateType = type;
            return this;
        }

        /** Sets the update interval of the updater.
         * The update interval will not be checked if it is run within that period.
         * If the update interval is not set, it is assumed to be 86400 seconds. (One day) **/
        public Builder setUpdateInterval(int interval) {
            if (interval < 0) {
                throw new IllegalArgumentException(String.format("Interval must be above or equal to 0, got %s", interval));
            } else {
                this.bUpdateInterval = interval;
                return this;
            }
        }

        /** Sets the current version of the app.
         * This should be used in conjunction with UPDATE_TYPE.DIFFERENCE
         * and should be used after setUpdateType is called. **/
        public Builder setCurrentVersion(@NonNull String version) {
            if (bUpdateType != UPDATE_TYPE.DIFFERENCE) {
                throw new IllegalStateException(String.format("Incorrect update type set, expected" +
                        " UPDATE_TYPE.DIFFERENCE but got %s", bUpdateType));
            }
            this.bCurrentVersionStr = version;
            return this;
        }

        /** Sets the current version of the app.
         * This should be used in conjunction with UPDATE_TYPE.INCREMENTAL
         * and should be used after setUpdateType is called. **/
        public Builder setCurrentVersion(int version) {
            if (bUpdateType != UPDATE_TYPE.INCREMENTAL) {
                throw new IllegalStateException(String.format("Incorrect update type set, expected" +
                        " UPDATE_TYPE.INCREMENTAL but got %s", bUpdateType));
            }
            this.bCurrentVersionInt = version;
            return this;
        }

        /** Sets the current version of the app.
         * This should be used in conjunction with UPDATE_TYPE.DECIMAL_INCREMENTAL
         * and should be used after setUpdateType is called. **/
        public Builder setCurrentVersion(float version) {
            if (bUpdateType != UPDATE_TYPE.DECIMAL_INCREMENTAL) {
                throw new IllegalStateException(String.format("Incorrect update type set, expected" +
                        " UPDATE_TYPE.DECIMAL_INCREMENTAL but got %s", bUpdateType));
            }
            this.bCurrentVersionDecimal = version;
            return this;
        }

        /** Adds an endpoint to the app.
         * This should be used after setCurrentVersion is called. **/
        public Builder addEndpoint(@NonNull Endpoint endpoint) {
            checkEndpointRequirements();
            setEndpointProperties(endpoint);
            bEndpointList.add(endpoint);
            return this;
        }

        /** Sets the endpoints for the app, takes in a list containing multiple endpoints.
         * The endpoints at the start of the list will be executed first. **/
        public Builder addEndpointList(@NonNull List<Endpoint> endpoints) {
            checkEndpointRequirements();
            for (Endpoint endpoint: endpoints) {
                setEndpointProperties(endpoint);
                bEndpointList.add(endpoint);
            }
            return this;
        }

        /** Sets the endpoints for the app, takes in a list containing multiple endpoints.
         * The endpoints at the start of the list will be executed first. **/
        public Builder addEndpointList(@NonNull Endpoint[] endpoints) {
            return addEndpoints(endpoints);
        }

        /** Sets the endpoints for the app, takes in multiple arguments.
         * The endpoints at the start of the arguments list will be executed first. **/
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
         * If you wish to use a custom dialog, you would need to overwrite UpdaterDialog to do so. **/
        public Builder setUpdateDialog(@NonNull UpdaterDialog dialog) {
            if (bShowNotif) {
                dialog.setNotification(getNotification());
            }
            this.bUpdateDialog = dialog;
            return this;
        }

        /** Sets whether to show notifications when an update is needed.  **/
        public Builder setShowNotif(boolean showNotif) {
            this.bShowNotif = showNotif;
            return this;
        }

        /** Sets the notification channel that would be used to display notifications,
         * defaults to "App Update". This feature is only available for
         * Build.VERSION_CODES.O and above. Defaults to 'App Update'. **/
        public Builder setNotifChannel(String channel) {
            this.bNotifChannel = channel;
            return this;
        }

        /** Gets the notification that will be shown if the app is updated.
         * Override this function if you wish to display a custom notification. **/
        public Notification getNotification() {
            Intent intent = new Intent(bContext, bContext.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(bContext, 0, intent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(bContext, bContext.getPackageName())
                    .setContentTitle(UpdaterFunctions.getApplicationName(bContext))
                    .setContentText("Update App")
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setLights(Color.BLUE, 2000, 0)
                    .setVibrate(new long[]{0, 250, 250, 250, 250})
                    .setAutoCancel(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.setChannelId(bNotifChannel);
            return builder.build();
        }

        /** Sets the properties of the endpoint. **/
        private void setEndpointProperties(@NonNull Endpoint endpoint) {
            endpoint.setUpdateDialog(bUpdateDialog, bFragmentManager, bFragmentTag);
            if (bUpdateType == UPDATE_TYPE.DIFFERENCE) {
                endpoint.setCurrentVersion(bCurrentVersionStr);
            } else if (bUpdateType == UPDATE_TYPE.INCREMENTAL) {
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

        /** Sets up the chain of endpoints which depend on one another. **/
        private void initEndpointList() {
            Collections.reverse(this.bEndpointList);
            for (int i = 0; i < this.bEndpointList.size(); i++) {
                if (i + 1 < this.bEndpointList.size()) {
                    this.bEndpointList.get(i).setBackupEndpoint(this.bEndpointList.get(i + 1));
                }
            }
            Collections.reverse(this.bEndpointList);
        }

        /** Throws an IllegalArgumentException that the current version of the app is not set. **/
        private void throwUnsetCurrentVersion() {
            throw new IllegalArgumentException("Current version of app not set. Is setCurrentVersion called?");
        }

        /** Creates the Auto App Updater based on the parameters given. **/
        public AutoAppUpdater create() {
            AutoAppUpdater updater = new AutoAppUpdater(bContext, bFragmentManager, bFragmentTag);
            updater.updateType = this.bUpdateType;
            initEndpointList();
            updater.endpointList = this.bEndpointList;
            updater.updateInterval = this.bUpdateInterval;
            if (bUpdateType == UPDATE_TYPE.DIFFERENCE) {
                if (bCurrentVersionStr == null) throwUnsetCurrentVersion();
                updater.currentVersionStr = bCurrentVersionStr;
            } else if (bUpdateType == UPDATE_TYPE.INCREMENTAL) {
                if (bCurrentVersionInt == null) throwUnsetCurrentVersion();
                updater.currentVersionDecimal = bCurrentVersionInt;
            } else {
                if (bCurrentVersionDecimal == null) throwUnsetCurrentVersion();
                updater.currentVersionDecimal = bCurrentVersionDecimal;
            }
            updater.updateDialog = bUpdateDialog;
            return updater;
        }
    }
}
