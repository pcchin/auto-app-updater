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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

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
        testGiteaDraftsOnly();
        testGiteaNoPermission();
    }

    /** Tests whether the Gitea endpoint is able to work successfully. **/
    private void testGiteaSuccess() {
        // Test public repository (Stable)
        // Test public repository (Pre-release)
        // Test public repository (Stable + Pre-release)
        // Test public repository (Combined)
        // Test private repository (Combined)
    }

    /** Tests whether the Gitea endpoint is able to work successfully on a private repository with the given token. **/
    private void testGiteaToken() {

    }

    /** Tests whether the Gitea endpoint is able to work successfully on a private repository with the given OAuth2 token. **/
    private void testGiteaOAuth() {

    }

    /** Tests whether the Gitea endpoint will fail when there is no releases in the repository. **/
    private void testGiteaNoReleases() {

    }

    /** Test whether the Gitea endpoint will fail when there is only drafts in the repository. **/
    private void testGiteaDraftsOnly() {

    }

    /** Test whether the Gitea endpoint will fail when there is insufficient to access the repository. **/
    private void testGiteaNoPermission() {

    }
}
