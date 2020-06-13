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

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.pcchin.auto_app_updater.AutoAppUpdater;
import com.pcchin.auto_app_updater.BuildConfig;
import com.pcchin.auto_app_updater.dialogs.UpdaterDialog;

import de.skuzzle.semantic.Version;

/** The endpoint used to get the updater service.
 * Extend this class to create your own endpoints. **/
public abstract class Endpoint {
    /* Example user agent: "AutoAppUpdater/1.0.0 (...)" */
    @SuppressWarnings("ConstantConditions")
    public static final String USER_AGENT = System.getProperty("http.agent","")
            .replaceAll("^.+?/\\S+", String.format("AutoAppUpdater/%s", BuildConfig.VERSION_NAME));

    // The endpoint that will be called if this endpoint fails.
    protected Endpoint backupEndpoint;
    protected AutoAppUpdater.UpdateType updateType;
    protected UpdaterDialog updateDialog;
    protected FragmentManager manager;
    protected String tag;
    protected RequestQueue queue;

    // Current version
    protected String currentVersionStr;
    protected int currentVersionInt;
    protected float currentVersionDecimal;

    /** The constructor for the endpoint. This should only be used by child classes as super()
     * in their constructors. **/
    protected Endpoint() {
        this.updateDialog = new UpdaterDialog();
    }

    /** Sets the backup endpoint of the app.
     * This function does not need to be called manually as it is called within AutoAppUpdater.
     * @param backupEndpoint The next endpoint that would be called if this endpoint fails.
     *                       If no more backup endpoints are found, this can be null. **/
    public void setBackupEndpoint(Endpoint backupEndpoint) {
        this.backupEndpoint = backupEndpoint;
    }

    /** Sets the current version of the app from within AutoAppUpdater.
     * This function does not need to be called manually as it is called within AutoAppUpdater.
     * @param dialog The dialog that will be shown if a newer version of the app is found.
     * @param manager The Fragment manager that will be used to display the dialog.
     * @param tag The tag that will be used when displaying the dialog. **/
    public void setUpdateDialog(UpdaterDialog dialog, FragmentManager manager, String tag) {
        this.updateDialog = dialog;
        this.manager = manager;
        this.tag = tag;
    }

    /** Sets the current version of the app from within AutoAppUpdater.
     * This function does not need to be called manually.
     * @param version The current version of the app.
     * @param isSemantic Whether semantic version is used for the updater. **/
    public void setCurrentVersion(String version, boolean isSemantic) {
        if (isSemantic) {
            this.updateType = AutoAppUpdater.UpdateType.SEMANTIC;
        } else {
            this.updateType = AutoAppUpdater.UpdateType.DIFFERENCE;
        }
        this.currentVersionStr = version;
    }

    /** Sets the current version of the app from within AutoAppUpdater.
     * This function does not need to be called manually as it is called within AutoAppUpdater.
     * @param version The current integer version of the app. **/
    public void setCurrentVersion(int version) {
        this.updateType = AutoAppUpdater.UpdateType.INCREMENTAL;
        this.currentVersionInt = version;
    }

    /** Sets the current version of the app from within AutoAppUpdater.
     * This function does not need to be called manually as it is called within AutoAppUpdater.
     * @param version The current decimal version of the app. **/
    public void setCurrentVersion(float version) {
        this.updateType = AutoAppUpdater.UpdateType.DECIMAL_INCREMENTAL;
        this.currentVersionDecimal = version;
    }

    /** Sets the content provider which is required to get the files.
     * This function does not needed to be called manually as it is called within AutoAppUpdater.
     * @param provider The content provider that will be used to open the downloaded APK file. (e.g. com.pcchin.aausample.ContentProvider)**/
    public void setContentProvider(String provider) {
        this.updateDialog = new UpdaterDialog(provider);
    }

    /** Sets the current queue for the request.
     * This function does not need to be called manually as it is called within AutoAppUpdater.
     * @param queue The Volley request queue that will be used to run the request. **/
    public void setRequestQueue(RequestQueue queue) {
        this.queue = queue;
    }

    /** Fetches the endpoint requested.
     * onSuccess would be called if the new version info can be successfully retrieved
     * and onFailure if it fails.
     * This will not run if there is no internet connection available. **/
    public void update() {
        queue.add(getRequest());
    }

    /** Gets the Volley request for the current endpoint.
     * onFailure can be thrown from here if the request fails. **/
    public abstract Request<?> getRequest();

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UpdateType.DIFFERENCE or UpdateType.SEMANTIC.
     * @param version The latest version of the app.
     * @param downloadLink The download link for the APK. **/
    public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
        boolean isSemanticUpdate = false;
        try {
            isSemanticUpdate = updateType == AutoAppUpdater.UpdateType.SEMANTIC &&
                    Version.parseVersion(version).isGreaterThan(Version.parseVersion(currentVersionStr));
        } catch (Version.VersionFormatException | IllegalArgumentException e) {
            onFailure(e);
        }
        if (isSemanticUpdate || (updateType == AutoAppUpdater.UpdateType.DIFFERENCE && !version.equals(currentVersionStr))) {
            updateDialog.setCurrentVersion(currentVersionStr);
            updateDialog.setNewVersion(version);
            updateApp(downloadLink);
        }
    }

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UpdateType.DIFFERENCE or UpdateType.SEMANTIC.
     * The 'Learn More' button would be enabled.
     * @param version The latest version of the app.
     * @param downloadLink The download link for the APK.
     * @param learnMoreLink The link accessed by the user to learn more about the latest update. **/
    public void onSuccess(@NonNull String version, @NonNull String downloadLink, String learnMoreLink) {
        setUpdateDialogLearnMore(learnMoreLink);
        onSuccess(version, downloadLink);
    }

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UpdateType.INCREMENTAL.
     * @param version The latest version of the app.
     * @param downloadLink The download link for the APK.**/
    public void onSuccess(int version, @NonNull String downloadLink) {
        if (version > currentVersionInt) {
            updateDialog.setCurrentVersion(String.valueOf(version));
            updateDialog.setNewVersion(String.valueOf(version));
            updateApp(downloadLink);
        }
    }

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UpdateType.INCREMENTAL.
     * The 'Learn More' button would be enabled.
     * @param version The latest version of the app.
     * @param downloadLink The download link for the APK.
     * @param learnMoreLink The link accessed by the user to learn more about the latest update.**/
    public void onSuccess(int version, @NonNull String downloadLink, String learnMoreLink) {
        setUpdateDialogLearnMore(learnMoreLink);
        onSuccess(version, downloadLink);
    }

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UpdateType.INCREMENTAL.
     * @param version The latest version of the app.
     * @param downloadLink The download link for the APK.**/
    public void onSuccess(float version, @NonNull String downloadLink) {
        if (version > currentVersionDecimal) {
            updateDialog.setCurrentVersion(String.valueOf(version));
            updateDialog.setNewVersion(String.valueOf(version));
            updateApp(downloadLink);
        }
    }

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UpdateType.INCREMENTAL.
     * The 'Learn More' button would be enabled.
     * @param version The latest version of the app.
     * @param downloadLink The download link for the APK.
     * @param learnMoreLink The link accessed by the user to learn more about the latest update.**/
    public void onSuccess(float version, @NonNull String downloadLink, String learnMoreLink) {
        setUpdateDialogLearnMore(learnMoreLink);
        onSuccess(version, downloadLink);
    }

    /** Sets the 'Learn More' URL for the update dialog.
     * @param learnMoreLink The link accessed by the user to learn more about the latest update. **/
    private void setUpdateDialogLearnMore(String learnMoreLink) {
        updateDialog.setShowLearnMore(true);
        updateDialog.setLearnMoreUrl(learnMoreLink);
    }

    /** Displays the AlertDialog and push notification for updating the app.
     * @param downloadLink The download link for the APK. **/
    private void updateApp(String downloadLink) {
        updateDialog.setDownloadLink(downloadLink);
        updateDialog.show(manager, tag);
    }

    /** The function that is called if the endpoint fails.
     * Override this function if you wish to handle the error yourself,
     * and call super.onFail for it to automatically fall back to the subsequent endpoints.
     * If there is no more backup endpoints, the error would be thrown.
     * @param error The error that caused the endpoint to fail. **/
    public void onFailure(@NonNull Exception error) {
        if (this.backupEndpoint == null) {
            throw new IllegalStateException(error);
        } else {
            Log.w("AutoAppUpdater", String.format("Endpoint failed with error %s, stack trace is", error.getMessage()));
            error.printStackTrace();
            this.backupEndpoint.update();
        }
    }
}
