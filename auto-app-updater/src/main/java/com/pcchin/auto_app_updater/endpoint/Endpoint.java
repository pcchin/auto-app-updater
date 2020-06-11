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

package com.pcchin.auto_app_updater.endpoint;

/** The endpoint used to get the updater service. **/
public abstract class Endpoint {
    // The endpoint that will be called if this endpoint fails.
    private Endpoint backupEndpoint;

    /** The constructor for the endpoint.
     * The backup endpoint can be null if it fails.
     * @param backupEndpoint The endpoint that will be called if this endpoint fails. **/
    public Endpoint(Endpoint backupEndpoint) {
        this.backupEndpoint = backupEndpoint;
    }

    /** Fetches the endpoint requested.
     * onSuccess would be called if the new version info can be successfully retrieved
     * and onFailure if it fails. **/
    public abstract void update();

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UPDATE_TYPE.DIFFERENCE. **/
    public abstract void onSuccess(String version);

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UPDATE_TYPE.INCREMENTAL. **/
    public abstract void onSuccess(int version);

    /** The function that is called if the latest version is able to be successfully retrieved.
     * This function would only be called if the update type is UPDATE_TYPE.INCREMENTAL. **/
    public abstract void onSuccess(float version);

    /** The function that is called if the endpoint fails.
     * Override this function if you wish to handle the error yourself,
     * and call super.onFail for it to automatically fall back to the subsequent endpoints.
     * If there is no more backup endpoints, the error would be thrown.
     * This method is called when there is an error from Volley.**/
    public void onFailure(Exception error) throws Exception {
        if (this.backupEndpoint == null) throw error;
        this.backupEndpoint.update();
    }
}
