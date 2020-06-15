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
        Log.d("Endpoint tests", "GitHub endpoint test completed");
    }

    /** Tests whether the GitHub endpoint is able to work successfully. **/
    private void testGitHubSuccess() {
        // Test public repository (Stable)
        GitHubEndpoint stableEndpoint = new GitHubEndpoint("aau-test/public-stable-only", false) {
            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                // Check if version and download link matches
                if (version.equals("1.1.0") && downloadLink.equals("https://github.com/aau-test/" +
                        "public-stable-only/releases/download/1.1.0/success-1.1.0.apk")) {
                    Log.d("GitHubEndpointTest", "Stable test succeeded");
                } else {
                    throw new IllegalStateException(String.format("Stable test got version %s and " +
                                    "download link %s instead of the expected values",
                            version, downloadLink));
                }
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
        };
        stableEndpoint.setCurrentVersion("1.1.1", false);
        stableEndpoint.setRequestQueue(queue);
        stableEndpoint.update();
        // Test public repository (Pre-release)
        GitHubEndpoint preReleaseEndpoint = new GitHubEndpoint("aau-test/public-prerelease-only", true) {
            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                // Check if version and download link matches
                if (version.equals("1.1.0") && downloadLink.equals("https://github.com/aau-test/" +
                        "public-prerelease-only/releases/download/1.1.0/success-1.1.0.apk")) {
                    Log.d("GitHubEndpointTest", "Test 2 succeeded");
                } else {
                    throw new IllegalStateException(String.format("Test 2 got version %s and " +
                                    "download link %s instead of the expected values",
                            version, downloadLink));
                }
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
        };
        preReleaseEndpoint.setCurrentVersion("1.1.3", false);
        preReleaseEndpoint.setRequestQueue(queue);
        preReleaseEndpoint.update();
        // Test public repository (Combined)
        GitHubEndpoint combinedEndpoint = new GitHubEndpoint("aau-test/public-combined", true) {
            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                // Check if version and download link matches
                if (version.equals("1.1.0-a") && downloadLink.equals("https://github.com/aau-test" +
                        "/public-combined/releases/download/1.1.0-a/success-1.1.0.apk")) {
                    Log.d("GitHubEndpointTest", "Test 4 succeeded");
                } else {
                    throw new IllegalStateException(String.format("Test 4 got version %s and " +
                                    "download link %s instead of the expected values",
                            version, downloadLink));
                }
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
        };
        combinedEndpoint.setCurrentVersion("1.0.0", false);
        combinedEndpoint.setRequestQueue(queue);
        combinedEndpoint.update();
    }

    /** Tests whether the GitHub endpoint is able to work successfully for private repositories. **/
    private void testGitHubPrivate() {
        // Test private repository (Combined)
        GitHubEndpoint endpoint = new GitHubEndpoint("aau-test/private-combined", false, authKey) {
            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                // Check if version and download link matches
                if (version.equals("1.1.0") && downloadLink.equals("https://github.com/aau-test/" +
                        "private-combined/releases/download/1.1.0/success-1.1.0.apk")) {
                    Log.d("GitHubEndpointTest", "Private test succeeded");
                } else {
                    throw new IllegalStateException(String.format("Private test got version %s and " +
                                    "download link %s instead of the expected values",
                            version, downloadLink));
                }
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
        };
        endpoint.setCurrentVersion("1.0.1", false);
        endpoint.setRequestQueue(queue);
        endpoint.update();
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
                if (error instanceof VolleyError) Log.d("GitHubEndpointTest", "Request failed as expected");
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
