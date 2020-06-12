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

package com.pcchin.auto_app_updater.utils;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Map;

/** Request format used by Volley to download a binary file. **/
public class FileDownloadRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> requestResponse;
    private final Map<String, String> dlParams;

    /** Default constructor. **/
    protected FileDownloadRequest(String mUrl, Response.Listener<byte[]> listener,
                                  Response.ErrorListener errorListener, Map<String, String> params) {
        super(Method.GET, mUrl, errorListener);
        setShouldCache(false);
        requestResponse = listener;
        dlParams = params;
    }

    /** Returns the params. Nothing to see here. **/
    @Override
    protected Map<String, String> getParams() {
        return dlParams;
    }

    /** Delivers the response, nothing to see here. **/
    @Override
    protected void deliverResponse(byte[] response) {
        requestResponse.onResponse(response);
    }

    /** Pass on the response data. Nothing to see here. **/
    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success( response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}
