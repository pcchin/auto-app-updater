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
import com.pcchin.auto_app_updater.endpoint.custom.JSONObjectEndpoint;

import org.json.JSONException;

/** Tests to see if the JSON Object endpoint performs as expected.
 * The test endpoint is at https://my-json-server.typicode.com/aau-test/json-test.
 * All the functions need to go through logcat to be sure whether they actually ran correctly. **/
public class JSONObjectEndpointTest {
    private RequestQueue queue;

    /** Default constructor. Starts all the test functions. **/
    public JSONObjectEndpointTest(Context context) {
        this.queue = Volley.newRequestQueue(context);
        testObjectSuccess();
        testObjectNoAttributes();
        testObjectNoRequiredAttributes();
        testObjectReturnArray();
        Log.d("Endpoint tests", "JSON Object endpoint test completed");
    }

    /** Test whether the JSON Object endpoint is able to work successfully. **/
    public void testObjectSuccess() {
        // Test with only the version and download (/posts/1)
        JSONObjectEndpoint incrementalEndpoint = new JSONObjectEndpoint("https://my-json-server.typicode.com/aau-test" +
                "/json-test/posts/1", "version", "download") {
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
                    Log.d("JSONObjectEndpointTest", "Test 1 succeeded");
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
        incrementalEndpoint.setRequestQueue(queue);
        incrementalEndpoint.setCurrentVersion(3);
        incrementalEndpoint.update();
        // Test with additional objects (/posts/2)
        JSONObjectEndpoint decimalEndpoint = new JSONObjectEndpoint("https://my-json-server.typicode.com/aau-test" +
                "/json-test/posts/2", "vers", "dl") {
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

            // The correct one
            @Override
            public void onSuccess(float version, @NonNull String downloadLink) {
                // Check if version and download link matches
                // The number that is compared to should also be float
                if (version == (float) 10.441 && downloadLink.equals("https://jsonplaceholder.typicode.com/posts/2")) {
                    Log.d("JSONObjectEndpointTest", "Test 2 succeeded");
                } else {
                    throw new IllegalStateException(String.format("Test 2 got version %s and " +
                            "download link %s instead of the expected values", version, downloadLink));
                }
            }

            @Override
            public void onSuccess(float version, @NonNull String downloadLink, String learnMoreLink) {
                throw new IllegalStateException("onSuccess (float w/h learn more) is called");
            }
        };
        decimalEndpoint.setRequestQueue(queue);
        decimalEndpoint.setCurrentVersion((float) 3.14159);
        decimalEndpoint.update();
        // Test with the version, download and learn more attribute (/posts/3)
        JSONObjectEndpoint differenceEndpoint = new JSONObjectEndpoint("https://my-json-server.typicode.com/aau-test" +
                "/json-test/posts/3", "v", "d", "lm") {
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink) {
                throw new IllegalStateException("onSuccess (String) is called");
            }

            // The correct one
            @Override
            public void onSuccess(@NonNull String version, @NonNull String downloadLink, String learnMoreLink) {
                // Check if version and download link matches
                if (version.equals("v2.0.1") && downloadLink.equals("https://jsonplaceholder.typicode.com/posts/3")
                    && learnMoreLink.equals("https://jsonplaceholder.typicode.com/posts/3/comments")) {
                    Log.d("JSONObjectEndpointTest", "Test 3 succeeded");
                } else {
                    throw new IllegalStateException(String.format("Test 3 got version %s, " +
                            "download link %s and learn more link %s instead of the expected values",
                            version, downloadLink, learnMoreLink));
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
        differenceEndpoint.setRequestQueue(queue);
        differenceEndpoint.setCurrentVersion("v2.0.0");
        differenceEndpoint.update();
    }

    /** Test whether the JSON Object endpoint would fail if there are no required
     * attributes in the object. (/posts/4) **/
    public void testObjectNoAttributes() {
        JSONObjectEndpoint endpoint = getCommonEndpoint(4);
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("1.0.0");
        endpoint.update();
    }

    /** Test whether the JSON Object endpoint would fail if not all of the required attributes
     * are not met in the object. (/posts/5) **/
    public void testObjectNoRequiredAttributes() {
        JSONObjectEndpoint endpoint = getCommonEndpoint(5);
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("1.1.1-alpha");
        endpoint.update();
    }

    /** Gets the common JSONObjectEndpoint that is used by noAttributes
     * and noRequiredAttributes. **/
    @NonNull
    private JSONObjectEndpoint getCommonEndpoint(int postCount) {
        return new JSONObjectEndpoint(String.format("https://my-json-server.typicode.com/aau-test" +
                "/json-test/posts/%s", postCount), "version", "download") {
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
                if (error instanceof JSONException) Log.d("JSONObjectEndpointTest", "Request failed as expected");
                else throw new IllegalStateException(error);
            }
        };
    }

    /** Test whether the JSON Object endpoint would fail if a JSON Array is returned instead.
     * (/posts) **/
    public void testObjectReturnArray() {
        JSONObjectEndpoint endpoint = new JSONObjectEndpoint("https://my-json-server.typicode.com/aau-test" +
                "/json-test/posts", "version", "download") {
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
                if (error instanceof VolleyError) Log.d("JSONObjectEndpointTest", "Request from testObjectReturnArray failed as expected");
                else throw new IllegalStateException(error);
            }
        };
        endpoint.setRequestQueue(queue);
        endpoint.setCurrentVersion("1A");
        endpoint.update();
    }
}
