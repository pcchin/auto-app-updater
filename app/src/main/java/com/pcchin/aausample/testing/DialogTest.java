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

/** Tests if dialogs are able to be shown successfully.
 * The tests can't be put in androidTest as listeners are needed to be used
 * or visual interactions are needed. **/
public class DialogTest {
    private Context context;

    /** Default constructor. Starts all the test functions. **/
    public DialogTest(Context context) {
        this.context = context;
        showProgressBarDialog();
        showUpdaterDialog();
    }

    /** Tests if the ProgressBarDialog is able to be shown successfully. **/
    public void showProgressBarDialog() {

    }

    /** Tests if the UpdaterDialog is able to be shown successfully. **/
    public void showUpdaterDialog() {
        // Without Learn More button
        // With Learn More button
        // With custom update message
        // With release notes instead of update messages
    }
}
