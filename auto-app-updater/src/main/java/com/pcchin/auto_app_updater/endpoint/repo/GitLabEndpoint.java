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

/** Sets the GitLab endpoint for the app.
 * The tag_name of the release will be used as the newer version
 * and the first APK file in that release will be the assets.
 * If the update type is UpdateType.INCREMENTAL, tag_name must be an integer.
 * If the update type is UpdateType.DECIMAL_INCREMENTAL, tag_name must be a valid number.
 * Only the latest release will be taken into account.
 * The release info and learn more link would not be shown unless the boolean values
 *  for them are set in the corresponding UpdateDialog. **/
public class GitLabEndpoint extends Endpoint {
    private int projectId;
    private String apiPath;
    private GitLabAuth authMethod;
    private String authString;
    private String userAgent = Endpoint.USER_AGENT;

    /** Methods of authenticating with the GitLab API. **/
    public enum GitLabAuth {
        /** No authentication. **/
        NONE,
        /** OAuth2 token. **/
        OAUTH2,
        /** Personal / project access token. **/
        PRIVATE_TOKEN
    }

    //****** Start of constructors ******//

    /** Default constructor with the repo path and whether to include pre-releases specified.
     * The API path is assumed to be https://gitlab.com and no tokens will be used.
     * @param projectId The ID for the repository required. **/
    public GitLabEndpoint(int projectId) {
        this(projectId, "https://gitlab.com");
    }

    /** Default constructor with the repo path and whether to include pre-releases specified.
     * No tokens will be used.
     * @param projectId The ID for the repository required.
     * @param apiPath The path to access the API (Include https:// and without / at the end). **/
    public GitLabEndpoint(int projectId, String apiPath) {
        this(projectId, apiPath, GitLabAuth.NONE, null);
    }

    /** Default constructor with the repo path and whether to include pre-releases specified.
     * @param projectId The ID for the repository required.
     * @param apiPath The path to access the API (Include https:// and without / at the end).
     * @param authMethod The method which is used to authorize the app.
     * @param authString The access token / oAuth2 token to access the repo. (Only use this if you can ensure that your token would not be leaked) **/
    public GitLabEndpoint(int projectId, String apiPath, GitLabAuth authMethod, String authString) {
        super();
        this.projectId = projectId;
        this.apiPath = apiPath;
        this.authMethod = authMethod;
        this.authString = authString;
    }

    //****** Start of overridden functions ******//

    /** Gets all the releases from /api/v1/repos/.../releases. **/
    @Override
    public Request<?> getRequest() {
        return new JsonArrayRequest(Request.Method.GET, String.format("%s/api/v4/repos/%s/releases",
                apiPath, projectId), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    parseReleaseList(response);
                } catch (JSONException e) {
                    Log.w("GitLabEndpoint", "Unable to get attributes from JSON response, stack trace is");
                    e.printStackTrace();
                    onFailure(e);
                } catch (NumberFormatException e) {
                    Log.w("GitLabEndpoint", "Unable to parse version tag to either an int or a float, stack trace is");
                    e.printStackTrace();
                    onFailure(e);
                } catch (IllegalStateException e) {
                    Log.w("GitLabEndpoint", String.format("%s", e.getMessage()));
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
                return getGitLabHeaders();
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(@NonNull NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };
    }

    /** Parses the release list to get the latest non-draft release.
     * If a pre-release version is requested but there are no pre-releases,
     * the latest stable version would be used instead.
     * @param response The response returned from the GitLab request. **/
    public void parseReleaseList(@NonNull JSONArray response) throws JSONException, NumberFormatException, IllegalStateException {
        JSONObject targetObject = null;
        if (response.length() > 0) targetObject = response.getJSONObject(0);
        if (targetObject == null) throw new IllegalStateException("No releases found in GitLab release list!");
        String versionTag = targetObject.getString("tag_name");
        String downloadLink = null;
        JSONObject assets = targetObject.getJSONObject("assets");
        JSONArray linksList = assets.getJSONArray("links");
        for (int i = 0; i < linksList.length(); i++) {
            JSONObject currentObject = linksList.getJSONObject(i);
            String linkName = currentObject.getString("name");
            if (linkName.endsWith(".apk")) {
                downloadLink = currentObject.getString("url");
                break;
            }
        }
        if (downloadLink == null) throw new IllegalStateException("Asset download link not found in GitHub release!");
        if (super.updateType == AutoAppUpdater.UpdateType.DIFFERENCE) onSuccess(versionTag, downloadLink);
        else if (super.updateType == AutoAppUpdater.UpdateType.INCREMENTAL) onSuccess(Integer.parseInt(versionTag), downloadLink);
        else onSuccess(Float.parseFloat(versionTag), downloadLink);
    }

    //****** Start of custom functions ******//

    /** Gets the GitLab headers needed for the requests. **/
    @NonNull
    private Map<String, String> getGitLabHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (authMethod == GitLabAuth.PRIVATE_TOKEN) headers.put("Private-Token", String.format("%s", authString));
        else if (authMethod == GitLabAuth.OAUTH2) headers.put("Authorization", String.format("Bearer %s", authString));
        headers.put("User-agent", userAgent);
        return headers;
    }

    /** Sets the user agent for the request. Defaults to Endpoint.USER_AGENT.
     * @param userAgent The user agent used to send the request. **/
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
