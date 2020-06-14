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

package com.pcchin.aausample.testing;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.pcchin.auto_app_updater.endpoint.repo.GitHubEndpoint;

/** Tests to see if the GitHub endpoint performs as expected.
 * The tests can't be put in androidTest as listeners are needed to be used. **/
public class GitHubEndpointTest {
    private RequestQueue queue;
    private String authKey;

    /** Default constructor. Starts all the test functions. **/
    public GitHubEndpointTest(Context context, String authKey) {
        this.queue = Volley.newRequestQueue(context);
        this.authKey = authKey;
        testGitHubSuccess();
        if (this.authKey != null) testGitHubPrivate();
        testGitHubNoReleases();
        testGitHubDraftsOnly();
        testGitHubNoPermission();
    }

    /** Tests whether the GitHub endpoint is able to work successfully. **/
    private void testGitHubSuccess() {
        // Test public repository (Stable)
        // Test public repository (Pre-release)
        // Test public repository (Stable + Pre-release)
        // Test public repository (Combined)
    }

    /** Tests whether the GitHub endpoint is able to work successfully for private repositories. **/
    private void testGitHubPrivate() {
        // Test private repository (Combined)
    }

    /** Tests whether the GitHub endpoint will fail when there is no releases in the repository. **/
    private void testGitHubNoReleases() {
        GitHubEndpoint endpoint = getCommonEndpoint("aau-test/public-no-releases");
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("2.0.0", true);
        endpoint.update();
    }

    /** Test whether the GitHub endpoint will fail when there is only drafts in the repository. **/
    private void testGitHubDraftsOnly() {
        GitHubEndpoint endpoint = getCommonEndpoint("aau-test/public-draft-only");
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("2.0.1", false);
        endpoint.update();
    }

    /** Gets the common endpoint used by testGitHubNoReleases and testGitHubDraftsOnly. **/
    @NonNull
    private GitHubEndpoint getCommonEndpoint(String repoPath) {
        return new GitHubEndpoint(repoPath, false) {
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                throw new IllegalStateException("onSuccess (String) is called");
            }

            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink, String learnMoreLink) {
                throw new IllegalStateException("onSuccess (String w/h learn more) is called");
            }

            @Override
            public void onSuccess(int version, @NonNull String downloadLink) {
                throw new IllegalStateException("onSuccess (int) is called");
            }

            @Override
            public void onSuccess(int version, @NonNull String downloadLink, String learnMoreLink) {
                throw new IllegalStateException("onSuccess (int w/h learn more) is called");
            }

            @Override
            public void onSuccess(float version, @NonNull String downloadLink) {
                throw new IllegalStateException("onSuccess (float) is called");
            }

            @Override
            public void onSuccess(float version, @NonNull String downloadLink, String learnMoreLink) {
                throw new IllegalStateException("onSuccess (float w/h learn more) is called");
            }

            @Override
            public void onFailure(@NonNull Exception error) {
                if (error instanceof IllegalStateException) Log.d("GitHubEndpointTest", "Request failed as expected");
                else throw new IllegalStateException(error);
            }
        };
    }

    /** Test whether the GitHub endpoint will fail when there is insufficient to access the repository. **/
    private void testGitHubNoPermission() {
        GitHubEndpoint endpoint = new GitHubEndpoint("aau-test/public-draft-only", false) {
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                throw new IllegalStateException("onSuccess (String) is called");
            }

            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink, String learnMoreLink) {
                throw new IllegalStateException("onSuccess (String w/h learn more) is called");
            }

            @Override
            public void onSuccess(int version, @NonNull String downloadLink) {
                throw new IllegalStateException("onSuccess (int) is called");
            }

            @Override
            public void onSuccess(int version, @NonNull String downloadLink, String learnMoreLink) {
                throw new IllegalStateException("onSuccess (int w/h learn more) is called");
            }

            @Override
            public void onSuccess(float version, @NonNull String downloadLink) {
                throw new IllegalStateException("onSuccess (float) is called");
            }

            @Override
            public void onSuccess(float version, @NonNull String downloadLink, String learnMoreLink) {
                throw new IllegalStateException("onSuccess (float w/h learn more) is called");
            }

            @Override
            public void onFailure(@NonNull Exception error) {
                if (error instanceof VolleyError) Log.d("GitHubEndpointTest", "Request failed as expected");
                else throw new IllegalStateException(error);
            }
        };
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion(3);
        endpoint.update();
    }
}
