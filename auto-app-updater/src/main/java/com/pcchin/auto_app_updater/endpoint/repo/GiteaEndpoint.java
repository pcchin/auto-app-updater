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

package com.pcchin.auto_app_updater.endpoint.repo;

import android.util.Log;

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

/** Sets the Gitea endpoint for the app.
 * The tag_name of the release will be used as the newer version
 * and the first APK file in that release will be the assets.
 * If the update type is UpdateType.INCREMENTAL, tag_name must be an integer.
 * If the update type is UpdateType.DECIMAL_INCREMENTAL, tag_name must be a valid number.**/
public class GiteaEndpoint extends Endpoint {
    private String repoPath;
    private String apiPath;
    private boolean includePreReleases;
    private GiteaAuth authMethod;
    private String authString;
    private String userAgent = Endpoint.USER_AGENT;

    /** Methods of authenticating with the Gitea API.
     * NONE: No authentication
     * TOKEN: Authorization via an API key token
     * OAUTH2: Access token that is obtained from Gitea's OAuth2 provider **/
    public enum GiteaAuth {
        NONE,
        TOKEN,
        OAUTH2
    }

    //****** Start of constructors ******//

    /** Default constructor with the repo path and whether to include pre-releases specified.
     * The API path is assumed to be https://try.gitea.io and no tokens will be used.
     * @param repoPath The path for the repository in the form of user/repo.
     * @param includePreReleases Whether to include pre releases in the version check. **/
    public GiteaEndpoint(String repoPath, boolean includePreReleases) {
        this(repoPath, includePreReleases, "https://try.gitea.io");
    }

    /** Default constructor with the repo path and whether to include pre-releases specified.
     * No tokens will be used.
     * @param repoPath The path for the repository in the form of user/repo.
     * @param includePreReleases Whether to include pre releases in the version check.
     * @param apiPath The path to access the API (Include https:// and without / at the end). **/
    public GiteaEndpoint(String repoPath, boolean includePreReleases, String apiPath) {
        this(repoPath, includePreReleases, apiPath, GiteaAuth.NONE, null);
    }

    /** Default constructor with the repo path and whether to include pre-releases specified.
     * The API path is assumed to be api.github.com and no OAuth2 token will be used.
     * @param repoPath The path for the repository in the form of user/repo.
     * @param includePreReleases Whether to include pre releases in the version check.
     * @param apiPath The path to access the API (Include https:// and without / at the end).
     * @param authMethod The method which is used to authorize the app.
     * @param authString The access token / oAuth2 token to access the repo. (Only use this if you can ensure that your token would not be leaked) **/
    public GiteaEndpoint(String repoPath, boolean includePreReleases, String apiPath, GiteaAuth authMethod, String authString) {
        super();
        this.repoPath = repoPath;
        this.includePreReleases = includePreReleases;
        this.apiPath = apiPath;
        this.authMethod = authMethod;
        this.authString = authString;
    }

    //****** Start of overridden functions ******//

    /** Gets all the releases from /api/v1/repos/.../releases. **/
    @Override
    public Request<?> getRequest() {
        return new JsonArrayRequest(Request.Method.GET, String.format("%s/api/v1/repos/%s/releases",
                apiPath, repoPath), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    parseReleaseList(response);
                } catch (JSONException e) {
                    Log.w("GiteaEndpoint", "Unable to get attributes from JSON response, stack trace is");
                    e.printStackTrace();
                    onFailure(e);
                } catch (NumberFormatException e) {
                    Log.w("GiteaEndpoint", "Unable to parse version tag to either an int or a float, stack trace is");
                    e.printStackTrace();
                    onFailure(e);
                } catch (IllegalStateException e) {
                    Log.w("GiteaEndpoint", String.format("%s", e.getMessage()));
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
                return getGiteaHeaders();
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(@NonNull NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };
    }

    /** Parses the release list to get the latest non-draft release.
     * If a pre-release version is requested but there are no pre-releases,
     * the latest stable version would be used instead. **/
    public void parseReleaseList(@NonNull JSONArray response) throws JSONException {
        Integer firstPreRelease = null;
        Integer firstStableRelease = null; // Fallback if there are no pre releases
        JSONObject targetObject;
        for (int i = 0; i < response.length(); i++) {
            JSONObject currentRelease = response.getJSONObject(i);
            boolean isPreRelease = currentRelease.getBoolean("prerelease"),
                    isDraft = currentRelease.getBoolean("draft");
            if (!isDraft) {
                if (isPreRelease && firstPreRelease == null) {
                    firstPreRelease = i;
                } else if (!isPreRelease && firstStableRelease == null) {
                    firstStableRelease = i;
                }
            }
        }
        if (firstStableRelease == null) throw new IllegalStateException("No stable releases found in Gitea release list!");
        if (firstPreRelease == null) firstPreRelease = firstStableRelease;
        if (includePreReleases) {
            targetObject = response.getJSONObject(firstPreRelease);
        } else {
            targetObject = response.getJSONObject(firstStableRelease);
        }
        parseRelease(targetObject);
    }

    /** Parses a specific release to get the version and download info. **/
    public void parseRelease(@NonNull JSONObject response) throws JSONException, NumberFormatException, IllegalStateException {
        String versionTag = response.getString("tag_name");
        String downloadLink = null;
        JSONArray assetsList = response.getJSONArray("assets");
        for (int i = 0; i < assetsList.length(); i++) {
            JSONObject currentObject = assetsList.getJSONObject(i);
            String assetName = currentObject.getString("name");
            if (assetName.endsWith(".apk")) {
                downloadLink = currentObject.getString("browser_download_url");
            }
        }
        if (downloadLink == null) throw new IllegalStateException("Asset download link not found in GitHub release!");
        if (super.updateType == AutoAppUpdater.UpdateType.DIFFERENCE) onSuccess(versionTag, downloadLink);
        else if (super.updateType == AutoAppUpdater.UpdateType.INCREMENTAL) onSuccess(Integer.parseInt(versionTag), downloadLink);
        else onSuccess(Float.parseFloat(versionTag), downloadLink);
    }

    //****** Start of custom functions ******/

    /** Gets the Gitea headers needed for the requests. **/
    @NonNull
    private Map<String, String> getGiteaHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (authMethod == GiteaAuth.TOKEN) headers.put("Authorization", String.format("token %s", authString));
        else if (authMethod == GiteaAuth.OAUTH2) headers.put("Authorization", String.format("bearer %s", authString));
        headers.put("User-agent", userAgent);
        return headers;
    }

    /** Sets the user agent for the request. Defaults to Endpoint.USER_AGENT. **/
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
