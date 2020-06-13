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
import com.pcchin.auto_app_updater.endpoint.custom.JSONArrayEndpoint;

import org.json.JSONException;

/** Tests to see if the JSON Array endpoint performs as expected.
 * The test endpoint is at https://my-json-server.typicode.com/aau-test/json-test.
 * The tests can't be put in androidTest as listeners are needed to be used. **/
public class JSONArrayEndpointTest {
    private RequestQueue queue;

    /** Default constructor. Starts all the test functions. **/
    public JSONArrayEndpointTest(Context context) {
        this.queue = Volley.newRequestQueue(context);
        testArraySuccess();
        testEmptyArray();
        testArrayNoRequiredAttributes();
        testArrayReturnObject();
        Log.d("Endpoint tests", "JSON Array endpoint test completed");
    }

    /** Test whether the JSON Array endpoint is able to work successfully. **/
    public void testArraySuccess() {
        // Test with only the required objects (/posts)
        JSONArrayEndpoint normalEndpoint = new JSONArrayEndpoint(
                "https://my-json-server.typicode.com/aau-test/json-test/posts",
                "version", "download") {
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                throw new IllegalStateException("onSuccess (String) is called");
            }

            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink, String learnMoreLink) {
                throw new IllegalStateException("onSuccess (String w/h learn more) is called");
            }

            // The correct one
            @Override
            public void onSuccess(int version, @NonNull String downloadLink) {
                // Check if results match
                if (version == 4 && downloadLink.equals("https://jsonplaceholder.typicode.com/posts/1")) {
                    Log.d("JSONArrayEndpointTest", "Test 1 succeeded");
                } else {
                    throw new IllegalStateException(String.format("Test 1 got version %s and " +
                            "download link %s instead of the expected values", version, downloadLink));
                }
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
        normalEndpoint.setCurrentVersion(5);
        normalEndpoint.setRequestQueue(queue);
        normalEndpoint.update();
        // Test with the learn more attribute (/comments)
        JSONArrayEndpoint learnMoreEndpoint = new JSONArrayEndpoint(
                "https://my-json-server.typicode.com/aau-test/json-test/comments",
                "vs", "dl", "learn more") {
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                throw new IllegalStateException("onSuccess (String) is called");
            }

            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink, String learnMoreLink) {
                // Check if version and download link matches
                // The number that is compared to should also be float
                if (version.equals("v5.4.67-a") && downloadLink.equals("https://jsonplaceholder.typicode.com/profile")
                    && learnMoreLink.equals("https://jsonplaceholder.typicode.com/posts/1")) {
                    Log.d("JSONArrayEndpointTest", "Test 2 succeeded");
                } else {
                    throw new IllegalStateException(String.format("Test 2 got version %s and " +
                            "download link %s instead of the expected values", version, downloadLink));
                }
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
        learnMoreEndpoint.setCurrentVersion("v0", false);
        learnMoreEndpoint.setRequestQueue(queue);
        learnMoreEndpoint.update();
    }

    /** Test whether the JSON Array endpoint would fail if there are no objects in the array. (/todos) **/
    public void testEmptyArray() {
        JSONArrayEndpoint endpoint = getJsonExceptionEndpoint("/todos");
        endpoint.setCurrentVersion((float) 2.71828);
        endpoint.setRequestQueue(queue);
        endpoint.update();
    }

    /** Test whether the JSON Array endpoint would fail if not all of the required attributes
     * are not met in the object. (/albums)  **/
    public void testArrayNoRequiredAttributes() {
        JSONArrayEndpoint endpoint = getJsonExceptionEndpoint("/albums");
        endpoint.setCurrentVersion((float) 2.71828);
        endpoint.setRequestQueue(queue);
        endpoint.update();
    }

    /** Gets the JSON Array endpoint which is expected to fail with a JSONException. **/
    @NonNull
    private JSONArrayEndpoint getJsonExceptionEndpoint(String path) {
        return new JSONArrayEndpoint(String.format("https://my-json-server.typicode.com/" +
                "aau-test/json-test%s", path), "version", "download") {
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
                if (error instanceof JSONException) Log.d("JSONArrayEndpointTest", "Request failed as expected");
                else throw new IllegalStateException(error);
            }
        };
    }

    /** Test whether the JSON Array endpoint would fail if a JSON Object is returned instead. (/posts/1) **/
    public void testArrayReturnObject() {
        JSONArrayEndpoint endpoint = new JSONArrayEndpoint(
                "https://my-json-server.typicode.com/aau-test/json-test/posts/1",
                "version", "download") {
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
                if (error instanceof VolleyError) Log.d("JSONArrayEndpointTest", "Request failed as expected");
                else throw new IllegalStateException(error);
            }
        };
        endpoint.setCurrentVersion((float) 5.607);
        endpoint.setRequestQueue(queue);
        endpoint.update();
    }
}
