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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.fragment.app.DialogFragment;

import java.util.HashMap;

/** The dialog that will be shown when a newer version of the app is found,
 * prompting the user to update the app. **/
public class UpdaterDialog extends DialogFragment {
    private boolean rotatable = true;
    private Dialog dialog;
    private APKDownloader downloader;

    private String currentVersion;
    private String newVersion;
    private String downloadUrl;
    private String authParam;
    private String authString;
    private String updateMessage = "A newer version of the app is available. Would you like to update to " +
                    "the latest version?";
    private String releaseInfo = "";
    private String learnMoreUrl = "about:blank";
    private String title = "Update App";
    private boolean showReleaseInfo = false;
    private boolean showLearnMore = false;

    //****** Start of constructors ******//

    /** Default constructor, should not be used outside of this library. **/
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public UpdaterDialog() {
        setRetainInstance(true);
    }

    /** Default constructor.
     * @param context The context that is used to create the APK Downloader instance.
     * @param contentProvider The content provider that will be used to open the APK file. **/
    public UpdaterDialog(Context context, String contentProvider) {
        setRetainInstance(true);
        downloader = new APKDownloader(context, contentProvider);
    }

    //****** Start of overridden functions ******//

    /** Creates the AlertDialog that will be displayed by the app.
     * @param savedInstanceState The previously saved app info. **/
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
     * The dialog should not be overwritten here, but instead in createDialog.
     * @param savedInstanceState The previously saved app info. **/
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (dialog == null) {
            super.setShowsDialog(false);
        }
        return dialog;
    }

    //****** Start of getters and setters ******//

    /** Creates the AlertDialog that is used to build the alert.
     * Override this function to insert your own AlertDialog. **/
    public Dialog createDialog() {
       AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle(title);
       if (showReleaseInfo) {
           builder.setMessage(releaseInfo);
       } else {
           builder.setMessage(updateMessage.replaceAll(TemplateRegex.RELEASE_INFO, releaseInfo)
                   .replaceAll(TemplateRegex.DOWNLOAD_URL, downloadUrl)
                   .replaceAll(TemplateRegex.LEARN_MORE_URL, learnMoreUrl)
                   .replaceAll(TemplateRegex.CURRENT_VERSION, currentVersion)
                   .replaceAll(TemplateRegex.NEW_VERSION, newVersion));
       }
       final HashMap<String, String> dlParams = new HashMap<>();
       if (authParam != null && authString != null) dlParams.put(authParam, authString);
       if (showLearnMore) builder.setNeutralButton("Learn More", null);
       builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               dialog.dismiss();
               downloader.setDownloadParams(dlParams);
               downloader.setDownloadUrl(downloadUrl);
               downloader.start();
           }
       });
       builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               dialog.dismiss();
           }
       });
       AlertDialog dialog = builder.create();
       if (showLearnMore) {
           dialog.setOnShowListener(new DialogInterface.OnShowListener() {
               @Override
               public void onShow(DialogInterface dialog1) {
                   // Sets the 'Learn More' buttons here so that the dialog would not be dismissed on that button click
                   ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(learnMoreUrl));
                           startActivity(intent);
                       }
                   });
               }
           });
       }
       return dialog;
    }

    /** Sets whether the fragment is rotatable. The default is set to true.
     * @param rotatable Whether the fragment is rotatable. **/
    public void setRotatable(boolean rotatable) {
        this.rotatable = rotatable;
    }

    /** Sets the title for the Updater Dialog. Defaults to 'Update App'.
     * @param title The title for the updater dialog. **/
    public void setTitle(String title) {
        this.title = title;
    }

    /** Sets the update message for the app. Certain templates can be used.
     * The possible templates that can be used are found in UpdateDialog.Template.
     * @param updateMessage The update message for the app. **/
    public void setUpdateMessage(String updateMessage) {
        this.updateMessage = updateMessage;
    }

    /** Sets the URL that is used to download the APK.
     * @param downloadUrl The URL used to download the APK. **/
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /** Set whether to show the release info of the app. If the release info is not set,
     * nothing would be displayed as the message.
     * @param showReleaseInfo Whether to show the release info of the app. **/
    public void setShowReleaseInfo(boolean showReleaseInfo) {
        this.showReleaseInfo = showReleaseInfo;
    }

    /** Sets the release info of the app. Even if this is set, it will not be shown unless
     * setShowReleaseInfo is called.
     * @param releaseInfo The release info of the app. **/
    public void setReleaseInfo(String releaseInfo) {
        this.releaseInfo = releaseInfo;
    }

    /** Set whether the 'Learn More' button is displayed. If the URL to 'Learn More' is not set,
     * the URL would redirect to 'about:blank'.
     * @param showLearnMore Whether to display the 'Learn More' button. **/
    public void setShowLearnMore(boolean showLearnMore) {
        this.showLearnMore = showLearnMore;
    }

    /** Sets the Learn More URL of the app. Even if this is set, it will not be shown unless
     * setShowLearnMore is called.
     * @param url The Learn More URL of the app. **/
    public void setLearnMoreUrl(String url) {
        this.learnMoreUrl = url;
    }

    /** Sets the current version of the app. This is only used as the value for Template.CURRENT_VERSION.
     * @param currentVersion The current version of the app. **/
    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    /** Sets the newer version of the app. This is only used as the value for Template.NEW_VERSION.
     * @param newVersion The newer version of the app. **/
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

    /** Sets the downloader that will be used to download the APK.
     * @param downloader The APK downloader. **/
    public void setDownloader(APKDownloader downloader) {
        this.downloader = downloader;
    }

    /** The message templates that will be replaced with specific values in the message section of the dialog. **/
    public static class Template {
        public static final String RELEASE_INFO = "${releaseInfo}";
        public static final String DOWNLOAD_URL = "${downloadUrl}";
        public static final String LEARN_MORE_URL = "${learnMoreUrl}";
        public static final String CURRENT_VERSION = "${currentVersion}";
        public static final String NEW_VERSION = "${newVersion}";
    }

    /** The regex for message templates that will be replaced with specific values in the message section of the dialog.
     * This should not be used outside this library. **/
    @SuppressWarnings("RegExpRedundantEscape") // Escape is actually necessary, false positive
    static class TemplateRegex {
        public static final String RELEASE_INFO = "\\$\\{releaseInfo\\}";
        public static final String DOWNLOAD_URL = "\\$\\{downloadUrl\\}";
        public static final String LEARN_MORE_URL = "\\$\\{learnMoreUrl\\}";
        public static final String CURRENT_VERSION = "\\$\\{currentVersion\\}";
        public static final String NEW_VERSION = "\\$\\{newVersion\\}";
    }
}
