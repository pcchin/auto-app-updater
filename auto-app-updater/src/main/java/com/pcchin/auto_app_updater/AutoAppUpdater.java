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

package com.pcchin.auto_app_updater;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pcchin.auto_app_updater.endpoint.Endpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoAppUpdater {
    private Context context;
    private UPDATE_TYPE updateType;
    private List<Endpoint> endpointList; // All the possible endpoints for updating the app.
    private int updateInterval; // The update interval for the app (In seconds).

    private String currentVersionStr; // The current version for the app. (UPDATE_TYPE.DIFFERENCE)
    private int currentVersionInt; // The current version for the app. (UPDATE_TYPE.INCREMENTAL)
    private float currentVersionDecimal; // The current version for the app. (UPDATE_TYPE.DECIMAL_INCREMENTAL)

    // The type of update checks that will be performed
    public enum UPDATE_TYPE {
        // As long as the version provided differs from the current version, a version update would be needed.
        DIFFERENCE,
        // The version provided will be a number, and if the number is larger than the current one,
        // the app would need to be updated
        INCREMENTAL,
        // Same thing as incremental, but for decimal numbers
        DECIMAL_INCREMENTAL
    }

    /** The constructor for the class, only used by the builder. **/
    private AutoAppUpdater(Context context) {
        this.context = context;
    }

    /** The builder class for creating the AutoAppUpdater. **/
    public static class Builder {
        private Context bContext;
        private UPDATE_TYPE bUpdateType; // The update type of the app, defaults to UPDATE_TYPE.DIFFERENCE.
        private int bUpdateInterval; // The interval between updating the app (In seconds), defaults to 86400 (One day).
        private List<Endpoint> bEndpointList = new ArrayList<>();

        private String bCurrentVersionStr;
        private Integer bCurrentVersionInt;
        private Float bCurrentVersionDecimal;

        /** The default constructor for the builder.
         * The default values for variables are set here. **/
        public Builder(@NonNull Context context) {
            this.bContext = context;
            this.bUpdateType = UPDATE_TYPE.DIFFERENCE;
            this.bUpdateInterval = 60 * 60 * 24;
        }

        /** Sets the update type of the updater.
         * If a type is not set, it is assumed to be UPDATE_TYPE.DIFFERENCE. **/
        public Builder setUpdateType(UPDATE_TYPE type) {
            this.bUpdateType = type;
            return this;
        }

        /** Sets the update interval of the updater.
         * The update interval will not be checked if it is run within that period.
         * If the update interval is not set, it is assumed to be 86400 seconds. (One day) **/
        public Builder setUpdateInterval(int interval) {
            if (interval < 0) {
                throw new IllegalArgumentException(String.format("Interval must be above or equal to 0, got %s", interval));
            } else {
                this.bUpdateInterval = interval;
                return this;
            }
        }

        /** Sets the current version of the app.
         * This should be used in conjunction with UPDATE_TYPE.DIFFERENCE
         * and should be used after setUpdateType is called. **/
        public Builder setCurrentVersion(String version) {
            if (bUpdateType != UPDATE_TYPE.DIFFERENCE) {
                throw new IllegalStateException(String.format("Incorrect update type set, expected" +
                        " UPDATE_TYPE.DIFFERENCE but got %s", bUpdateType));
            }
            this.bCurrentVersionStr = version;
            return this;
        }

        /** Sets the current version of the app.
         * This should be used in conjunction with UPDATE_TYPE.INCREMENTAL
         * and should be used after setUpdateType is called. **/
        public Builder setCurrentVersion(int version) {
            if (bUpdateType != UPDATE_TYPE.INCREMENTAL) {
                throw new IllegalStateException(String.format("Incorrect update type set, expected" +
                        " UPDATE_TYPE.INCREMENTAL but got %s", bUpdateType));
            }
            this.bCurrentVersionInt = version;
            return this;
        }

        /** Sets the current version of the app.
         * This should be used in conjunction with UPDATE_TYPE.DECIMAL_INCREMENTAL
         * and should be used after setUpdateType is called. **/
        public Builder setCurrentVersion(float version) {
            if (bUpdateType != UPDATE_TYPE.DECIMAL_INCREMENTAL) {
                throw new IllegalStateException(String.format("Incorrect update type set, expected" +
                        " UPDATE_TYPE.DECIMAL_INCREMENTAL but got %s", bUpdateType));
            }
            this.bCurrentVersionDecimal = version;
            return this;
        }

        /** Adds an endpoint to the app. This should not be used in conjunction with setEndpoints as
         * setEndpoints will overwrite any previously stored endpoints. **/
        public Builder addEndpoint(Endpoint endpoint) {
            bEndpointList.add(endpoint);
            return this;
        }

        /** Sets the endpoints for the app, takes in a list containing multiple endpoints.
         * The endpoints at the start of the list will be executed first. **/
        public Builder setEndpoints(List<Endpoint> endpoints) {
            this.bEndpointList = endpoints;
            return this;
        }

        /** Sets the endpoints for the app, takes in multiple arguments.
         * The endpoints at the start of the arguments list will be executed first. **/
        public Builder setEndpoints(Endpoint... endpoints) {
            this.bEndpointList = Arrays.asList(endpoints);
            return this;
        }

        /** Throws an IllegalArgumentException that the current version of the app is not set. **/
        private void throwUnsetCurrentVersion() {
            throw new IllegalArgumentException("Current version of app not set. Is setCurrentVersion called?");
        }

        /** Creates the Auto App Updater based on the parameters given. **/
        public AutoAppUpdater create() {
            AutoAppUpdater updater = new AutoAppUpdater(bContext);
            updater.updateType = this.bUpdateType;
            updater.endpointList = this.bEndpointList;
            updater.updateInterval = this.bUpdateInterval;
            if (updater.updateType == UPDATE_TYPE.DIFFERENCE) {
                if (bCurrentVersionStr == null) throwUnsetCurrentVersion();
                updater.currentVersionStr = bCurrentVersionStr;
            } else if (bUpdateType == UPDATE_TYPE.INCREMENTAL) {
                if (bCurrentVersionInt == null) throwUnsetCurrentVersion();
                updater.currentVersionDecimal = bCurrentVersionInt;
            } else {
                if (bCurrentVersionDecimal == null) throwUnsetCurrentVersion();
                updater.currentVersionDecimal = bCurrentVersionDecimal;
            }
            return updater;
        }
    }
}
