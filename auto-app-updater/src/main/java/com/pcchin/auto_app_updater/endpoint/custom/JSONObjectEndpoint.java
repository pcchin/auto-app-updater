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

package com.pcchin.auto_app_updater.endpoint.custom;

import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.pcchin.auto_app_updater.AutoAppUpdater;
import com.pcchin.auto_app_updater.endpoint.Endpoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/** The endpoint which returns a JSON object.
 * The object is assumed to contain no further objects. **/
public class JSONObjectEndpoint extends Endpoint {
    private String requestUrl;
    private String versionAttribute;
    private String downloadUrlAttribute;
    private String learnMoreAttribute;
    private Map<String, String> headers;
    private String userAgent = Endpoint.USER_AGENT;

    //****** Start of constructors ******//

    /** Default constructor with the request type specified.
     * The 'Learn More' button would not be shown.
     * @param requestUrl The request URL.
     * @param versionAttribute The attribute which points to the version.
     * @param downloadUrlAttribute The attribute which points to the download URL. **/
    public JSONObjectEndpoint(String requestUrl, String versionAttribute, String downloadUrlAttribute) {
        this(requestUrl, versionAttribute, downloadUrlAttribute, null, new HashMap<String, String>());
    }

    /** Default constructor with the request type specified.
     * The 'Learn More' button would be shown.
     *  @param requestUrl The request URL.
     *  @param versionAttribute The attribute which points to the version.
     *  @param downloadUrlAttribute The attribute which points to the download URL.
     *  @param learnMoreAttribute The attribute pointing to the Learn More URL. **/
    public JSONObjectEndpoint(String requestUrl, String versionAttribute, String downloadUrlAttribute, String learnMoreAttribute) {
        this(requestUrl, versionAttribute, downloadUrlAttribute, learnMoreAttribute, new HashMap<String, String>());
    }

    /** Default constructor with the request type and headers specified.
     * The user agent should not be specified in the headers here and instead should be set in setUserAgent.
     * The 'Learn More' button would not be shown if learnMoreAttribute is null.
     * @param requestUrl The request URL.
     * @param versionAttribute The attribute which points to the version.
     * @param downloadUrlAttribute The attribute which points to the download URL.
     * @param learnMoreAttribute The attribute pointing to the Learn More URL.
     * @param headers The headers that will be sent in the GET request. The user agent needs to be set separately. **/
    public JSONObjectEndpoint(String requestUrl, String versionAttribute, String downloadUrlAttribute,
                              String learnMoreAttribute, Map<String, String> headers) {
        super();
        this.requestUrl = requestUrl;
        this.versionAttribute = versionAttribute;
        this.downloadUrlAttribute = downloadUrlAttribute;
        this.learnMoreAttribute = learnMoreAttribute;
        this.headers = headers;
    }

    //****** Start of overridden functions ******//

    /** Gets the JsonObjectRequest required to run the app. **/
    @Override
    public Request<?> getRequest() {
        return new JsonObjectRequest(requestUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    parseResponse(response);
                } catch (JSONException e) {
                    Log.w("JSONObjectEndpoint", "Unable to get attributes from JSON response, stack trace is");
                    e.printStackTrace();
                    onFailure(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Forwards the error on
                onFailure(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                headers.put("User-agent", userAgent);
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(@NonNull NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };
    }

    /** Parses the JSON Object response. **/
    private void parseResponse(@NonNull JSONObject response) throws JSONException {
        String downloadUrl = response.getString(downloadUrlAttribute);
        String learnMoreUrl = null;
        if (learnMoreAttribute != null) learnMoreUrl = response.getString(learnMoreAttribute);
        if (super.updateType == AutoAppUpdater.UPDATE_TYPE.DIFFERENCE) {
            String version = response.getString(versionAttribute);
            if (learnMoreUrl == null) onSuccess(version, downloadUrl);
            else onSuccess(version, downloadUrl, learnMoreUrl);
        } else if (super.updateType == AutoAppUpdater.UPDATE_TYPE.INCREMENTAL) {
            int version = response.getInt(versionAttribute);
            if (learnMoreUrl == null) onSuccess(version, downloadUrl);
            else onSuccess(version, downloadUrl, learnMoreUrl);
        } else {
            float version = (float) response.getDouble(versionAttribute);
            if (learnMoreUrl == null) onSuccess(version, downloadUrl);
            else onSuccess(version, downloadUrl, learnMoreUrl);
        }
    }

    //****** Start of getters and setters ******//

    /** Sets the user agent for the request. Defaults to Endpoint.USER_AGENT. **/
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
