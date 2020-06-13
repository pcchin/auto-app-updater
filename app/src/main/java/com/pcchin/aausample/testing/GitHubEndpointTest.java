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

/** Tests to see if the GitHub endpoint performs as expected.
 * The tests can't be put in androidTest as listeners are needed to be used. **/
public class GitHubEndpointTest {
    private Context context;
    private String authKey;

    /** Default constructor. Starts all the test functions. **/
    public GitHubEndpointTest(Context context, String authKey) {
        this.context = context;
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

    }

    /** Test whether the GitHub endpoint will fail when there is only drafts in the repository. **/
    private void testGitHubDraftsOnly() {

    }

    /** Test whether the GitHub endpoint will fail when there is insufficient to access the repository. **/
    private void testGitHubNoPermission() {

    }
}
