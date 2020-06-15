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
import com.pcchin.auto_app_updater.endpoint.repo.GitLabEndpoint;

/** Tests to see if the GitLab endpoint performs as expected.
 * The tests can't be put in androidTest as listeners are needed to be used. **/
public class GitLabEndpointTest {
    private RequestQueue queue;
    private String token;
    private String oAuth2;

    /** Default constructor. Starts all the test functions. **/
    public GitLabEndpointTest(Context context, String token, String oAuth2) {
        this.queue = Volley.newRequestQueue(context);
        this.token = token;
        this.oAuth2 = oAuth2;
        testGitLabSuccess();
        if (this.token != null) testGitLabToken();
        if (this.oAuth2 != null) testGitLabOAuth();
        testGitLabNoReleases();
        testGitLabNoPermission();
    }

    /** Tests whether the GitLab endpoint is able to work successfully. **/
    private void testGitLabSuccess() {
        GitLabEndpoint endpoint = new GitLabEndpoint(19360565) {
            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                // Check if version and download link matches
                if (version.equals("1.1.0") && downloadLink.equals(
                        // Yep, its GitHub for some reason
                        "https://github.com/uploads/77cac8a21c9d6fc907d5da6272fd49e2/success-1.1.0.apk")) {
                    Log.d("GiLabEndpointTest", "Default test succeeded");
                } else {
                    throw new IllegalStateException(String.format("Default test got version %s and " +
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
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("1.0.0");
        endpoint.update();
    }

    /** Tests whether the GitLab endpoint is able to work successfully on a private repository with the given token. **/
    private void testGitLabToken() {
        GitLabEndpoint endpoint = getPrivateEndpoint(GitLabEndpoint.GitLabAuth.PRIVATE_TOKEN, token);
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("1.0.1");
        endpoint.update();
    }

    /** Tests whether the GitLab endpoint is able to work successfully on a private repository with the given OAuth2 token. **/
    private void testGitLabOAuth() {
        GitLabEndpoint endpoint = getPrivateEndpoint(GitLabEndpoint.GitLabAuth.OAUTH2, oAuth2);
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("1.0.0");
        endpoint.update();
    }

    /** Tests whether the GitLab endpoint will fail when there is no releases in the repository. **/
    private void testGitLabNoReleases() {
        GitLabEndpoint endpoint = new GitLabEndpoint(19360336) {
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
                if (error instanceof IllegalStateException) Log.d("GitLabEndpointTest", "Request testGitLabNoReleases failed as expected");
                else throw new IllegalStateException(error);
            }
        };
        endpoint.setCurrentVersion(2);
        endpoint.setRequestQueue(queue);
        endpoint.update();
    }

    /** Test whether the GitLab endpoint will fail when there is insufficient to access the repository. **/
    private void testGitLabNoPermission() {
        GitLabEndpoint endpoint = new GitLabEndpoint(19360906) {
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
                if (error instanceof VolleyError) Log.d("GitLabEndpointTest", "Request testGitLabNoPermission failed as expected");
                else throw new IllegalStateException(error);
            }
        };
        endpoint.setCurrentVersion((float) 2.01);
        endpoint.setRequestQueue(queue);
        endpoint.update();
    }

    /** Gets the endpoint required to access the private GitLab repository. **/
    @NonNull
    private GitLabEndpoint getPrivateEndpoint(GitLabEndpoint.GitLabAuth authMethod, String authString) {
        return new GitLabEndpoint(19360906, authMethod, authString) {
            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                // Check if version and download link matches
                if (version.equals("1.1.0") && downloadLink.equals(
                        "https://gitlab.com/aau-test/private-combined/uploads/9b5f40bfba447e93ab8fde7ff98412e7/success-1.1.0.apk")) {
                    Log.d("GiLabEndpointTest", "Private test succeeded");
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
    }
}
