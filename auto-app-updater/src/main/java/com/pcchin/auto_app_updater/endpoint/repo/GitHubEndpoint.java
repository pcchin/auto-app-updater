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
import com.android.volley.toolbox.JsonObjectRequest;
import com.pcchin.auto_app_updater.AutoAppUpdater;
import com.pcchin.auto_app_updater.endpoint.Endpoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/** Sets the GitHub endpoint for the app.
 * The tag_name of the release will be used as the newer version
 * and the first APK file in that release will be the assets.
 * If the update type is UpdateType.INCREMENTAL, tag_name must be an integer.
 * If the update type is UpdateType.DECIMAL_INCREMENTAL, tag_name must be a valid number.
 * The release info and learn more link would not be shown unless the boolean values
 * for them are set in the corresponding UpdateDialog.**/
public class GitHubEndpoint extends Endpoint {
    private String repoPath;
    private String apiPath;
    private boolean isPrerelease;
    private String oAuthToken;
    private String userAgent = Endpoint.USER_AGENT;

    //****** Start of constructors ******//

    /** Default constructor with the repo path and whether to include pre-releases specified.
     * The API path is assumed to be https://api.github.com and no OAuth2 token will be used.
     * @param repoPath The path for the repository in the form of user/repo.
     * @param isPrerelease Whether to include pre releases in the version check. **/
    public GitHubEndpoint(String repoPath, boolean isPrerelease) {
        this(repoPath, isPrerelease, null);
    }

    /** Default constructor with the repo path and whether to include pre-releases specified.
     * @param repoPath The path for the repository in the form of user/repo.
     * @param isPrerelease Whether to include pre releases in the version check.
     * @param oAuthToken The oAuth2 token to access the repo (Only use this if you can ensure that your token would not be leaked), can be null. **/
    public GitHubEndpoint(String repoPath, boolean isPrerelease, String oAuthToken) {
        this(repoPath, isPrerelease, oAuthToken, "https://api.github.com");
    }

    /** Default constructor with the repo path and whether to include pre-releases specified.
     * @param repoPath The path for the repository in the form of user/repo.
     * @param isPrerelease Whether to include pre releases in the version check.
     * @param oAuthToken The oAuth2 token to access the repo (Only use this if you can ensure that your token would not be leaked), can be null.
     * @param apiPath The path to access the API (Include https:// and without / at the end). **/
    public GitHubEndpoint(String repoPath, boolean isPrerelease, String oAuthToken, String apiPath) {
        super();
        this.repoPath = repoPath;
        this.isPrerelease = isPrerelease;
        this.oAuthToken = oAuthToken;
        this.apiPath = apiPath;
    }

    //****** Start of overridden functions ******//

    /** Get the request needed to get the latest APK. If the version list includes pre releases,
     * a JsonArrayRequest would be returned. Otherwise, a JsonObjectRequest would be returned. **/
    @Override
    public Request<?> getRequest() {
        if (isPrerelease) {
            return getPreReleaseRequest();
        } else {
            return getStableRequest();
        }
    }

    //****** Start of custom functions ******//

    /** Gets the pre releases from /repos/.../releases. **/
    @NonNull
    private JsonArrayRequest getPreReleaseRequest() {
        return new JsonArrayRequest(Request.Method.GET, String.format("%s/repos/%s/releases",
                apiPath, repoPath), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    parseReleaseList(response);
                } catch (JSONException e) {
                    Log.w("GitHubEndpoint", "Unable to get attributes from JSON response, stack trace is");
                    e.printStackTrace();
                    onFailure(e);
                } catch (NumberFormatException e) {
                    Log.w("GitHubEndpoint", "Unable to parse version tag to either an int or a float, stack trace is");
                    e.printStackTrace();
                    onFailure(e);
                } catch (IllegalStateException e) {
                    Log.w("GitHubEndpoint", String.format("%s", e.getMessage()));
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
                return getGitHubHeaders();
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(@NonNull NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };
    }

    /** Parses the releases list to get the latest pre release.
     * and if there are no releases, the function would throw an IllegalStateException.
     * @param response The response received from the request. **/
    private void parseReleaseList(@NonNull JSONArray response) throws JSONException, NumberFormatException, IllegalStateException {
        JSONObject targetObject = null;
        for (int i = 0; i < response.length(); i++) {
            JSONObject currentRelease = response.getJSONObject(i);
            boolean isPreRelease = currentRelease.getBoolean("prerelease"),
                    isDraft = currentRelease.getBoolean("draft");
            if (!isDraft && isPreRelease) {
                targetObject = currentRelease;
                break;
            }
        }
        if (targetObject == null) {
            throw new IllegalStateException("Asset download link not found in GitHub release!");
        } else {
            parseRelease(targetObject);
        }
    }

    /** Gets the latest stable release from /repos/.../releases/latest. **/
    @NonNull
    private JsonObjectRequest getStableRequest() {
        return new JsonObjectRequest(String.format("%s/repos/%s/releases/latest", apiPath, repoPath),
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    parseRelease(response);
                } catch (JSONException e) {
                    Log.w("GitHubEndpoint", "Unable to get attributes from JSON response, stack trace is");
                    e.printStackTrace();
                    onFailure(e);
                } catch (NumberFormatException e) {
                    Log.w("GitHubEndpoint", "Unable to parse version tag to either an int or a float, stack trace is");
                    e.printStackTrace();
                    onFailure(e);
                } catch (IllegalStateException e) {
                    Log.w("GitHubEndpoint", String.format("%s", e.getMessage()));
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
                return getGitHubHeaders();
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(@NonNull NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };
    }

    /** Parses a specific release to get the version and download info.
     * @param response The JSON object for a specific GitHub release. **/
    private void parseRelease(@NonNull JSONObject response) throws JSONException,
            NumberFormatException, IllegalStateException {
        String versionTag = response.getString("tag_name"), downloadLink = null;
        JSONArray assetsList = response.getJSONArray("assets");
        for (int i = 0; i < assetsList.length(); i++) {
            JSONObject currentObject = assetsList.getJSONObject(i);
            String assetType = currentObject.getString("content_type");
            if (assetType.equals("application/vnd.android.package-archive")) {
                downloadLink = currentObject.getString("browser_download_url");
                break;
            }
        }
        if (oAuthToken != null)  updateDialog.setAuth("Authorization", String.format("token %s", oAuthToken));
        updateDialog.setReleaseInfo(response.getString("body"));
        updateDialog.setLearnMoreUrl(response.getString("html_url"));
        if (downloadLink == null) throw new IllegalStateException("Asset download link not found in GitHub release!");
        if (super.updateType == AutoAppUpdater.UpdateType.DECIMAL_INCREMENTAL) onSuccess(Float.parseFloat(versionTag), downloadLink);
        else if (super.updateType == AutoAppUpdater.UpdateType.INCREMENTAL) onSuccess(Integer.parseInt(versionTag), downloadLink);
        else onSuccess(versionTag, downloadLink);
    }

    /** Gets the GitHub headers needed for the requests. **/
    @NonNull
    private Map<String, String> getGitHubHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (oAuthToken != null)  headers.put("Authorization", String.format("token %s", oAuthToken));
        headers.put("User-agent", userAgent);
        headers.put("Accept", "application/vnd.github.v3.full+json");
        return headers;
    }

    /** Sets the user agent for the request. Defaults to Endpoint.USER_AGENT.
     * @param userAgent The user agent used to send the request. **/
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
