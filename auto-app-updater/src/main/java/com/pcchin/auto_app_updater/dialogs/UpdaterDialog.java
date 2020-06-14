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

package com.pcchin.auto_app_updater.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.pcchin.auto_app_updater.utils.APKDownloader;

import java.util.HashMap;

public class UpdaterDialog extends DialogFragment {
    private boolean rotatable = true;
    private Dialog dialog;

    private String contentProvider;
    private String currentVersion;
    private String newVersion;
    private String downloadLink;
    private String authParam;
    private String authString;
    private String updateMessage = "A newer version of the app is available. Would you like to update to the latest version?";
    private boolean showReleaseInfo = false;
    private String releaseInfo = "";
    private boolean showLearnMore = false;
    private boolean showDownloadingDialog = true;
    private String learnMoreUrl = "about:blank";
    private String title = "Update App";

    //****** Start of constructors ******//

    /** Default constructor, should not be used. **/
    public UpdaterDialog() {
        setRetainInstance(true);
    }

    /** Default constructor. **/
    public UpdaterDialog(String contentProvider) {
        setRetainInstance(true);
        this.contentProvider = contentProvider;
    }

    //****** Start of overridden functions ******//

    /** Creates the AlertDialog that will be displayed by the app. **/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dialog = createDialog();
    }

    /** Dismiss the dialog if the last one is still showing and the dialog is not rotatable. **/
    @Override
    public void onStart() {
        if (!rotatable && dialog != null && dialog.isShowing()) {
            dismiss();
        }
        super.onStart();
    }

    /** Returns the set dialog.
     * The dialog should not be overwritten here, but instead in createDialog. **/
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (dialog == null) {
            super.setShowsDialog(false);
        }
        return dialog;
    }

    //****** Start of getters and setters ******//

    /** Creates the AlertDialog that is used to create the alert.
     * Override this function to insert your own AlertDialog. **/
    public Dialog createDialog() {
       AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle(title);
       if (showReleaseInfo) {
           builder.setMessage(releaseInfo);
       } else {
           builder.setMessage(updateMessage.replaceAll(Template.RELEASE_INFO, releaseInfo)
                   .replaceAll(Template.DOWNLOAD_URL, downloadLink)
                   .replaceAll(Template.LEARN_MORE_URL, learnMoreUrl)
                   .replaceAll(Template.CURRENT_VERSION, currentVersion)
                   .replaceAll(Template.NEW_VERSION, newVersion));
       }
       if (showLearnMore) {
           // Set the neutral button to open an external URL when clicked
           builder.setNeutralButton("Learn More", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(learnMoreUrl));
                   startActivity(intent);
               }
           });
       }

        final HashMap<String, String> dlParams = new HashMap<>();
       if (authParam != null && authString != null) dlParams.put(authParam, authString);
       builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               dialog.dismiss();
               APKDownloader downloader = new APKDownloader(requireContext(), getFragmentManager(),
                       downloadLink, contentProvider, dlParams);
               downloader.setShowDownloadDialog(showDownloadingDialog);
               downloader.start();
           }
       });
       builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               dialog.dismiss();
           }
       });
       return builder.create();
    }

    /** Sets whether the fragment is rotatable. The default is set to true. **/
    public void setRotatable(boolean rotatable) {
        this.rotatable = rotatable;
    }

    /** Sets the title for the Updater Dialog. Defaults to 'Update App'. **/
    public void setTitle(String title) {
        this.title = title;
    }

    /** Sets the download link for the app.
     * This function does not need to be called manually. **/
    public void setDownloadLink(String link) {
        this.downloadLink = link;
    }

    /** Sets whether the Downloading... dialog would be shown. **/
    public void setShowDownloadingDialog(boolean showDialog) {
        this.showDownloadingDialog = showDialog;
    }

    /** Sets the update message for the app. Certain templates can be used.
     * The possible templates that can be used are found in MessageTemplate.class. **/
    public void setUpdateMessage(String updateMessage) {
        this.updateMessage = updateMessage;
    }

    /** Set whether to show the release info of the app. If the release info is not set,
     * nothing would be displayed as the message. **/
    public void setShowReleaseInfo(boolean showReleaseInfo) {
        this.showReleaseInfo = showReleaseInfo;
    }

    /** Sets the release info of the app. Even if this is set, it will not be shown unless
     * setShowReleaseInfo is called. **/
    public void setReleaseInfo(String releaseInfo) {
        this.releaseInfo = releaseInfo;
    }

    /** Set whether the 'Learn More' button is displayed. If the URL to 'Learn More' is not set,
     * the URL would redirect to 'about:blank'. **/
    public void setShowLearnMore(boolean showLearnMore) {
        this.showLearnMore = showLearnMore;
    }

    /** Sets the Learn More URL of the app. Even if this is set, it will not be shown unless
     * setShowLearnMore is called. **/
    public void setLearnMoreUrl(String url) {
        this.learnMoreUrl = url;
    }

    /** Sets the current version of the app. This is only used as the value for Template.CURRENT_VERSION. **/
    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    /** Sets the newer version of the app. This is only used as the value for Template.NEW_VERSION. **/
    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    /** Sets the authorization required to access the APK that will be downloaded.
     * @param authParam The header name of the authorization (eg. Private-Token / Authorization)
     * @param authString The value that will be sent along with the header name. **/
    public void setAuth(String authParam, String authString) {
        this.authParam = authParam;
        this.authString = authString;
    }

    /** Message templates that will be replaced with specific values in the message section of the dialog. **/
    public static class Template {
        public static final String RELEASE_INFO = "\\$\\{releaseInfo}";
        public static final String DOWNLOAD_URL = "\\$\\{downloadUrl}";
        public static final String LEARN_MORE_URL = "\\$\\{learnMoreUrl}";
        public static final String CURRENT_VERSION = "\\$\\{currentVersion}";
        public static final String NEW_VERSION = "\\$\\{newVersion}";
    }
}
