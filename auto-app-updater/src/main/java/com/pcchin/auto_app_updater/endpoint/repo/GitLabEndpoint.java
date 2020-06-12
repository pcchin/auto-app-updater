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

import com.android.volley.Request;
import com.pcchin.auto_app_updater.endpoint.Endpoint;

/** Sets the GitLab endpoint for the app. **/
public class GitLabEndpoint extends Endpoint {
    /** Methods of authenticating with the GitLab API.
     * None: No authentication
     * OAUTH2: OAuth2 token
     * PRIVATE_TOKEN: Personal / project access tokens **/
    public enum GitLabAuth {
        NONE,
        OAUTH2,
        PRIVATE_TOKEN
    }

    public GitLabEndpoint() {
        super();
    }

    @Override
    public Request<?> getRequest() {
        return null;
    }
}
