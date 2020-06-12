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

package com.pcchin.auto_app_updater.endpoint;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.pcchin.auto_app_updater.dialogs.UpdaterDialog;

/** The endpoint used to get the updater service. **/
public abstract class Endpoint {
    // The endpoint that will be called if this endpoint fails.
    protected Endpoint backupEndpoint;
    protected UpdaterDialog updateDialog;
    protected FragmentManager manager;
    protected String tag;
    protected RequestQueue queue;

    // Current version
    protected String currentVersionStr;
    protected int currentVersionInt;
    protected float currentVersionDecimal;

    /** The constructor for the endpoint. Should not be used. **/
    public Endpoint() {
        this.updateDialog = new UpdaterDialog();
    }

    /** Sets the backup endpoint of the app.
     * The endpoint can be null if no more backup endpoints are found.
     * This function does not need to be called manually. **/
    public void setBackupEndpoint(Endpoint backupEndpoint) {
        this.backupEndpoint = backupEndpoint;
    }

    /** Sets the current version of the app from within AutoAppUpdater.
     * This function does not need to be called manually. **/
    public void setUpdateDialog(UpdaterDialog dialog, FragmentManager manager, String tag) {
        this.updateDialog = dialog;
        this.manager = manager;
        this.tag = tag;
    }

    /** Sets the current version of the app from within AutoAppUpdater.
     * This function does not need to be called manually. **/
    public void setCurrentVersion(String version) {
        this.currentVersionStr = version;
    }

    /** Sets the current version of the app from within AutoAppUpdater.
     * This function does not need to be called manually. **/
    public void setCurrentVersion(int version) {
        this.currentVersionInt = version;
    }

    /** Sets the current version of the app from within AutoAppUpdater.
     * This function does not need to be called manually. **/
    public void setCurrentVersion(float version) {
        this.currentVersionDecimal = version;
    }

    /** Sets the content provider which is required to get the files.
     * This function does not needed to be called manually. **/
    public void setContentProvider(String provider) {
        this.updateDialog = new UpdaterDialog(provider);
    }

    /** Sets the current queue for the request.
     * This function does not need to be called manually. **/
    public void setRequestQueue(RequestQueue queue) {
        this.queue = queue;
    }

    /** Fetches the endpoint requested.
     * onSuccess would be called if the new version info can be successfully retrieved
     * and onFailure if it fails. **/
    public void update() {
        queue.add(getRequest());
    }

    /** Gets the Volley request for the current endpoint.
     * onFailure can be thrown from here if the request fails. **/
    public abstract Request<?> getRequest();

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UPDATE_TYPE.DIFFERENCE. **/
    public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
        if (!version.equals(currentVersionStr)) {
            updateDialog.setCurrentVersion(currentVersionStr);
            updateDialog.setNewVersion(version);
            updateApp(downloadLink);
        }
    }

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UPDATE_TYPE.INCREMENTAL. **/
    public void onSuccess(int version, @NonNull String downloadLink) {
        if (version > currentVersionInt) {
            updateDialog.setCurrentVersion(String.valueOf(version));
            updateDialog.setNewVersion(String.valueOf(version));
            updateApp(downloadLink);
        }
    }

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UPDATE_TYPE.INCREMENTAL. **/
    public void onSuccess(float version, @NonNull String downloadLink) {
        if (version > currentVersionDecimal) {
            updateDialog.setCurrentVersion(String.valueOf(version));
            updateDialog.setNewVersion(String.valueOf(version));
            updateApp(downloadLink);
        }
    }

    /** Displays the AlertDialog and push notification for updating the app. **/
    private void updateApp(String downloadLink) {
        updateDialog.setDownloadLink(downloadLink);
        updateDialog.show(manager, tag);
    }

    /** The function that is called if the endpoint fails.
     * Override this function if you wish to handle the error yourself,
     * and call super.onFail for it to automatically fall back to the subsequent endpoints.
     * If there is no more backup endpoints, the error would be thrown. **/
    public void onFailure(Exception error) throws Exception {
        if (this.backupEndpoint == null) throw error;
        this.backupEndpoint.update();
    }
}
