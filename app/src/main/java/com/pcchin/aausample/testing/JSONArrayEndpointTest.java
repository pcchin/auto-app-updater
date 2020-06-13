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

/** Tests to see if the JSON Array endpoint performs as expected. **/
public class JSONArrayEndpointTest {
    private Context context;

    /** Default constructor. Starts all the test functions. **/
    public JSONArrayEndpointTest(Context context) {
        this.context = context;
        testArraySuccess();
        testArrayNoAttributes();
        testArrayNoRequiredAttributes();
        testArrayReturnObject();
    }

    /** Test whether the JSON Array endpoint is able to work successfully. **/
    public void testArraySuccess() {
        // Test with only the required objects
        // Test with additional objects
        // Test with the learn more attribute
        // Test with single object in array
    }

    /** Test whether the JSON Array endpoint would fail if there are no attributes in the object. **/
    public void testArrayNoAttributes() {

    }

    /** Test whether the JSON Array endpoint would fail if the required attributes are not met in the object. **/
    public void testArrayNoRequiredAttributes() {

    }

    /** Test whether the JSON Array endpoint would fail if a JSON Object is returned instead. **/
    public void testArrayReturnObject() {

    }
}
