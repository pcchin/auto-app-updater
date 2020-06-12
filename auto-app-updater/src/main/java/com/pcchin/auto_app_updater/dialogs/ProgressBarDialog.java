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

import android.app.Dialog;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/** A custom DialogFragment which simply displays a progress bar, and when cancelled would terminate the download process. **/
public class ProgressBarDialog extends DialogFragment {
    public Dialog dialog;
    private String title = "Downloading the latest version...";

    //****** Start of constructors ******//

    /** Default constructor. **/
    public ProgressBarDialog() {
        setRetainInstance(true);
    }

    //****** Start of overridden functions ******//

    /** Creates the AlertDialog that will be displayed by the app. **/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = createDialog();
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
        // Boolean used as it is possible for user to cancel the dialog before the download starts
        ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        AlertDialog downloadDialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(progressBar)
                .setPositiveButton(null, null)
                .create();
        downloadDialog.setCancelable(false);
        return downloadDialog;
    }

    /** Sets the title for the Updater Dialog. Defaults to 'Downloading the latest version...'. **/
    public void setTitle(String title) {
        this.title = title;
    }
}
