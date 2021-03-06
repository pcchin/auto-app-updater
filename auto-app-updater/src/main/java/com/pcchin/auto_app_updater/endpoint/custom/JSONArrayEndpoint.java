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

import androidx.annotation.NonNull;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.pcchin.auto_app_updater.AutoAppUpdater;
import com.pcchin.auto_app_updater.endpoint.Endpoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/** The endpoint which returns a JSON array.
 * It is assumed that each object in the array would contain the same attributes.
 * Only the first object of the array would be taken into account.
 * If an empty array is returned, onFailure would be called. **/
public class JSONArrayEndpoint extends Endpoint {
    private int method;
    private String requestUrl;
    private String versionAttribute;
    private String downloadUrlAttribute;
    private String learnMoreAttribute;
    private String releaseInfoAttribute;
    private String userAgent = Endpoint.USER_AGENT;
    private Map<String, String> headers;

    //****** Start of constructors ******//

    /** Default constructor with the request type specified.
     * The 'Learn More' button would not be shown.
     * The request method is assumed to be a GET request.
     * @param requestUrl The request URL.
     * @param versionAttribute The attribute which points to the version.
     * @param downloadUrlAttribute The attribute which points to the download URL.**/
    public JSONArrayEndpoint(String requestUrl, String versionAttribute, String downloadUrlAttribute) {
        this(requestUrl, versionAttribute, downloadUrlAttribute, null);
    }

    /** Default constructor with the request type specified.
     * The 'Learn More' button would be shown.
     * The request method is assumed to be a GET request.
     * @param requestUrl The request URL.
     * @param versionAttribute The attribute which points to the version.
     * @param downloadUrlAttribute The attribute which points to the download URL.
     * @param learnMoreAttribute The attribute pointing to the Learn More URL.**/
    public JSONArrayEndpoint(String requestUrl, String versionAttribute, String downloadUrlAttribute, String learnMoreAttribute) {
        this(requestUrl, versionAttribute, downloadUrlAttribute, learnMoreAttribute, null);
    }

    /** Default constructor with the request type specified.
     * The 'Learn More' button would be shown.
     *  @param requestUrl The request URL.
     *  @param versionAttribute The attribute which points to the version.
     *  @param downloadUrlAttribute The attribute which points to the download URL.
     *  @param learnMoreAttribute The attribute pointing to the Learn More URL.
     *  @param releaseInfoAttribute The attribute pointing to the release info. **/
    public JSONArrayEndpoint(String requestUrl, String versionAttribute, String downloadUrlAttribute,
                              String learnMoreAttribute, String releaseInfoAttribute) {
        this(Request.Method.GET, requestUrl, versionAttribute, downloadUrlAttribute,
                learnMoreAttribute, releaseInfoAttribute, new HashMap<String, String>());
    }

    /** Default constructor with the request type and headers specified.
     * The user agent should not be specified in the headers here and instead should be set in setUserAgent.
     * The 'Learn More' button would not be shown if learnMoreAttribute is null.
     * @param method The REST method used to sent the request.
     * @param requestUrl The request URL.
     * @param versionAttribute The attribute which points to the version.
     * @param downloadUrlAttribute The attribute which points to the download URL.
     * @param learnMoreAttribute The attribute pointing to the Learn More URL.
     * @param releaseInfoAttribute The attribute pointing to the release info.
     * @param headers The headers that are used when sending the request. **/
    public JSONArrayEndpoint(int method, String requestUrl, String versionAttribute, String downloadUrlAttribute,
                              String learnMoreAttribute, String releaseInfoAttribute, Map<String, String> headers) {
        super();
        this.method = method;
        this.requestUrl = requestUrl;
        this.versionAttribute = versionAttribute;
        this.downloadUrlAttribute = downloadUrlAttribute;
        this.learnMoreAttribute = learnMoreAttribute;
        this.releaseInfoAttribute = releaseInfoAttribute;
        this.headers = headers;
    }

    //****** Start of overridden functions ******//

    /** Gets the JsonArrayRequest required to run the app. **/
    @Override
    public Request<?> getRequest() {
        return new JsonArrayRequest(method, requestUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    parseResponse(response);
                } catch (JSONException e) {
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
            protected Response<JSONArray> parseNetworkResponse(@NonNull NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };
    }

    /** Parses the JSON Array response.
     * @param response The response received from the Volley request. **/
    private void parseResponse(@NonNull JSONArray response) throws JSONException {
        JSONObject firstObject = response.getJSONObject(0);
        String downloadUrl = firstObject.getString(downloadUrlAttribute);
        String learnMoreUrl = null;
        if (learnMoreAttribute != null) learnMoreUrl = firstObject.getString(learnMoreAttribute);
        if (releaseInfoAttribute != null) updateDialog.setReleaseInfo(releaseInfoAttribute);
        if (super.updateType == AutoAppUpdater.UpdateType.DIFFERENCE || super.updateType == AutoAppUpdater.UpdateType.SEMANTIC) {
            String version = firstObject.getString(versionAttribute);
            if (learnMoreUrl == null) onSuccess(version, downloadUrl);
            else onSuccess(version, downloadUrl, learnMoreUrl);
        } else if (super.updateType == AutoAppUpdater.UpdateType.INCREMENTAL) {
            int version = firstObject.getInt(versionAttribute);
            if (learnMoreUrl == null) onSuccess(version, downloadUrl);
            else onSuccess(version, downloadUrl, learnMoreUrl);
        } else {
            float version = (float) firstObject.getDouble(versionAttribute);
            if (learnMoreUrl == null) onSuccess(version, downloadUrl);
            else onSuccess(version, downloadUrl, learnMoreUrl);
        }
    }

    //****** Start of getters and setters ******//

    /** Sets the user agent for the request. Defaults to Endpoint.USER_AGENT.
     * @param userAgent The user agent used to send the request. **/
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
