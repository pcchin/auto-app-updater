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

package com.pcchin.aausample;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pcchin.aausample.testing.GitHubEndpointTest;
import com.pcchin.aausample.testing.GitLabEndpointTest;
import com.pcchin.aausample.testing.GiteaEndpointTest;
import com.pcchin.aausample.testing.JSONArrayEndpointTest;
import com.pcchin.aausample.testing.JSONObjectEndpointTest;
import com.pcchin.auto_app_updater.AutoAppUpdater;
import com.pcchin.auto_app_updater.endpoint.repo.GitHubEndpoint;
import com.pcchin.auto_app_updater.endpoint.repo.GitLabEndpoint;
import com.pcchin.auto_app_updater.utils.UpdaterDialog;
import com.pcchin.auto_app_updater.utils.UpdaterFunctions;

/** The main activity for the demo app. **/
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.run_github_update).setOnClickListener(view -> runGitHubUpdate());
        findViewById(R.id.run_github_gitlab_update).setOnClickListener(view -> runGitHubGitLabUpdate());
        setButtonListeners();
    }

    /** Example of how to build an updater based on a GitHub endpoint. **/
    private void runGitHubUpdate() {
        // The content provider that is set in your AndroidManifest.xml
        String contentProvider = getPackageName() + ".ContentProvider";
        // Set up a custom UpdaterDialog (With templates)
        UpdaterDialog dialog = new UpdaterDialog(MainActivity.this, contentProvider);
        dialog.setUpdateMessage("A new update is available!\nNew version: " +
                UpdaterDialog.Template.NEW_VERSION + "\nCurrent version: " +
                UpdaterDialog.Template.CURRENT_VERSION + "\nIf the installation fails, you can " +
                "download and install the APK manually at " + UpdaterDialog.Template.DOWNLOAD_URL);
        dialog.setShowLearnMore(true); // Normally it's false
        // Create the GitHub endpoint
        GitHubEndpoint endpoint = new GitHubEndpoint("aau-test/public-combined");
        // Create and run the updater
        // Ideally, this should be run behind the scenes, maybe when starting an Activity
        AutoAppUpdater updater = new AutoAppUpdater.Builder(MainActivity.this,
                getSupportFragmentManager(), contentProvider)
                .setUpdateType(AutoAppUpdater.UpdateType.SEMANTIC)
                .setCurrentVersion(BuildConfig.VERSION_NAME) // "1.0.0"
                .setUpdateDialog(dialog)
                .addEndpoint(endpoint)
                .setUpdateInterval(60) // Updates every 1 minute for example purposes
                .build();
        updater.run();
    }

    /** Example of how to build an updater based on multiple sources,
     * in this case, GitHub and GitLab. **/
    private void runGitHubGitLabUpdate() {
        // The content provider that is set in your AndroidManifest.xml
        String contentProvider = getPackageName() + ".ContentProvider";
        // Set up a custom UpdaterDialog (With templates)
        UpdaterDialog dialog = new UpdaterDialog(MainActivity.this, contentProvider);
        dialog.setShowReleaseInfo(true); // Only the release info will be shown in the message
        // This endpoint will fail as this is a private repository and the OAuth2 token is not provided
        GitHubEndpoint gitHubEndpoint = new GitHubEndpoint("aau-test/private-combined") {
            @Override
            public void onFailure(@NonNull Exception error) {
                Toast.makeText(MainActivity.this, "Unable to reach GitHub endpoint, trying GitLab endpoint", Toast.LENGTH_SHORT).show();
                super.onFailure(error); // Needed for the second endpoint to start
            }
        };
        // This endpoint is added as the second argument, so it will run if the first endpoint fails
        GitLabEndpoint gitLabEndpoint = new GitLabEndpoint(19360565);
        // Create and run the updater
        // Ideally, this should be run behind the scenes, maybe when starting an Activity
        AutoAppUpdater updater = new AutoAppUpdater.Builder(MainActivity.this,
                getSupportFragmentManager(), contentProvider)
                .setUpdateType(AutoAppUpdater.UpdateType.DIFFERENCE)
                .setCurrentVersion(BuildConfig.VERSION_NAME) // "1.0.0"
                .setUpdateDialog(dialog)
                .addEndpoints(gitHubEndpoint, gitLabEndpoint)
                .setUpdateInterval(60) // Updates every 1 minute for example purposes
                .build();
        updater.run();
    }

    /** Set the onClickListeners for the buttons that are not demoing the updater. **/
    private void setButtonListeners() {
        findViewById(R.id.show_update).setOnClickListener(view -> new UpdaterDialog().show(getSupportFragmentManager(), "Update"));
        if (UpdaterFunctions.isConnected(MainActivity.this)) {
            findViewById(R.id.testEndpoints).setOnClickListener(view -> {
                Toast.makeText(MainActivity.this, "View results through Logcat", Toast.LENGTH_SHORT).show();
                // The auth keys and tokens here are just for testing, no need to insert your own
                new Thread(() -> new GiteaEndpointTest(MainActivity.this, null, null)).start();
                new Thread(() -> new GitHubEndpointTest(MainActivity.this, null)).start();
                new Thread(() -> new GitLabEndpointTest(MainActivity.this, null, null)).start();
                new Thread(() -> new JSONArrayEndpointTest(MainActivity.this)).start();
                new Thread(() -> new JSONObjectEndpointTest(MainActivity.this)).start();
            });
        } else {
            findViewById(R.id.testEndpoints).setOnClickListener( view -> Toast.makeText(
                    MainActivity.this, "Internet connection is required for this " +
                            "operation", Toast.LENGTH_SHORT).show());
        }
    }
}