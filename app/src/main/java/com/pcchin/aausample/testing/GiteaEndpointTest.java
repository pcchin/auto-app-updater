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
import com.pcchin.auto_app_updater.endpoint.repo.GiteaEndpoint;

/** Tests to see if the Gitea endpoint performs as expected.
 * The tests can't be put in androidTest as listeners are needed to be used. **/
public class GiteaEndpointTest {
    private RequestQueue queue;
    private String token;
    private String oAuth2;

    /** Default constructor. Starts all the test functions. **/
    public GiteaEndpointTest(Context context, String token, String oAuth2) {
        this.queue = Volley.newRequestQueue(context);
        this.token = token;
        this.oAuth2 = oAuth2;
        testGiteaSuccess();
        if (this.token != null) testGiteaToken();
        if (this.oAuth2 != null) testGiteaOAuth();
        testGiteaNoReleases();
        testGiteaDraftOnly();
        testGiteaNoPermission();
        Log.d("Endpoint tests", "Gitea endpoint test completed");
    }

    /** Tests whether the Gitea endpoint is able to work successfully. **/
    private void testGiteaSuccess() {
        // Test public repository (Stable)
        GiteaEndpoint stableEndpoint = new GiteaEndpoint("aau-test/public-stable-only",
                false, "https://git.pcchin.com") {
            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                // Check if version and download link matches
                if (version.equals("1.1.0") && downloadLink.equals(
                        "https://git.pcchin.com/attachments/16013117-3a20-4b12-9e87-4f89486df2a3")) {
                    Log.d("GiteaEndpointTest", "Stable test succeeded");
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
        stableEndpoint.setRequestQueue(queue);
        stableEndpoint.setCurrentVersion("1.0.0-1", false);
        stableEndpoint.update();
        // Test public repository (Pre-release)
        GiteaEndpoint preReleaseEndpoint = new GiteaEndpoint("aau-test/public-prerelease-only",
                true, "https://git.pcchin.com") {
            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                // Check if version and download link matches
                if (version.equals("1.1.0") && downloadLink.equals(
                        "https://git.pcchin.com/attachments/65cc4240-6ec2-4987-8333-8f9e8b6085c8")) {
                    Log.d("GiteaEndpointTest", "Pre-release test succeeded");
                } else {
                    throw new IllegalStateException(String.format("Pre-release test got version %s and " +
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
        preReleaseEndpoint.setRequestQueue(queue);
        preReleaseEndpoint.setCurrentVersion("1.0.0-a", false);
        preReleaseEndpoint.update();
        // Test public repository (Combined)
        GiteaEndpoint combinedEndpoint = new GiteaEndpoint("aau-test/public-combined",
                true, "https://git.pcchin.com") {
            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                // Check if version and download link matches
                if (version.equals("1.1.1-a") && downloadLink.equals(
                        "https://git.pcchin.com/attachments/18de4dd1-951d-41ed-8adc-5b92b9a65072")) {
                    Log.d("GiteaEndpointTest", "Combined test succeeded");
                } else {
                    throw new IllegalStateException(String.format("Combined test got version %s and " +
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
        combinedEndpoint.setRequestQueue(queue);
        combinedEndpoint.setCurrentVersion("1.0.0-3", true);
        combinedEndpoint.update();
    }

    /** Tests whether the Gitea endpoint is able to work successfully on a private repository with the given token. **/
    private void testGiteaToken() {
        GiteaEndpoint endpoint = getPrivateEndpoint(GiteaEndpoint.GiteaAuth.TOKEN, token);
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("1.0.0", false);
        endpoint.update();
    }

    /** Tests whether the Gitea endpoint is able to work successfully on a private repository with the given OAuth2 token. **/
    private void testGiteaOAuth() {
        GiteaEndpoint endpoint = getPrivateEndpoint(GiteaEndpoint.GiteaAuth.OAUTH2, oAuth2);
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("1.0.0", false);
        endpoint.update();
    }

    /** Tests whether the Gitea endpoint will fail when there is no releases in the repository. **/
    private void testGiteaNoReleases() {
        GiteaEndpoint endpoint = getIllegalStateEndpoint("aau-test/public-no-releases", "testGiteaNoReleases");
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("1.1.1");
        endpoint.update();
    }

    /** Test whether the Gitea endpoint will fail when there is only drafts in the repository. **/
    private void testGiteaDraftOnly() {
        GiteaEndpoint endpoint = getIllegalStateEndpoint("aau-test/public-draft-only", "testGiteaDraftOnly");
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("1.11"); // Cannot be other type as the results are still parsed
        endpoint.update();
    }

    /** Test whether the Gitea endpoint will fail when there is insufficient to access the repository. **/
    private void testGiteaNoPermission() {
        GiteaEndpoint endpoint = new GiteaEndpoint("aau-test/private-combined", false, "https://git.pcchin.com") {
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
                if (error instanceof VolleyError) Log.d("GiteaEndpointTest",
                        "Request from testGiteaNoPermission failed as expected");
                else throw new IllegalStateException(error);
            }
        };
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion(4);
        endpoint.update();
    }

    /** Gets the GiteaEndpoint that is used to access the private repository. **/
    @NonNull
    private GiteaEndpoint getPrivateEndpoint(GiteaEndpoint.GiteaAuth authMode, String authString) {
        return new GiteaEndpoint("aau-test/private-combined",
                false, "https://git.pcchin.com", authMode, authString) {
            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                // Check if version and download link matches
                if (version.equals("1.1.0") && downloadLink.equals(
                        "https://git.pcchin.com/attachments/daf9abcc-76f4-4bed-904e-3f498457aa71")) {
                    Log.d("GiteaEndpointTest", String.format("%s test succeeded", authMode.toString()));
                } else {
                    throw new IllegalStateException(String.format("%s test got version %s and " +
                                    "download link %s instead of the expected values",
                            authMode.toString(), version, downloadLink));
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

    /** Returns the GiteaEndpoint that is expected to fail due to an IllegalStateException. **/
    @NonNull
    private GiteaEndpoint getIllegalStateEndpoint(String repoPath, String methodName) {
        return new GiteaEndpoint(repoPath, false, "https://git.pcchin.com") {
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
                if (error instanceof IllegalStateException) Log.d("GiteaEndpointTest", String.format("Request from %s failed as expected", methodName));
                else throw new IllegalStateException(error);
            }
        };
    }
}
