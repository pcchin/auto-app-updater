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

package com.pcchin.aausample;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pcchin.aausample.testing.GitHubEndpointTest;
import com.pcchin.aausample.testing.GitLabEndpointTest;
import com.pcchin.aausample.testing.GiteaEndpointTest;
import com.pcchin.aausample.testing.JSONArrayEndpointTest;
import com.pcchin.aausample.testing.JSONObjectEndpointTest;
import com.pcchin.auto_app_updater.utils.UpdaterFunctions;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (UpdaterFunctions.isConnected(MainActivity.this)) {
            findViewById(R.id.testEndpoints).setOnClickListener(view -> {
                Toast.makeText(MainActivity.this, "View results through Logcat", Toast.LENGTH_SHORT).show();
                // The auth keys and tokens here are just for testing, no need to insert your own
                new Thread(() -> new GiteaEndpointTest(MainActivity.this)).start();
                new Thread(() -> new GitHubEndpointTest(MainActivity.this, null)).start();
                new Thread(() -> new GitLabEndpointTest(MainActivity.this, null, null)).start();
                new Thread(() -> new JSONArrayEndpointTest(MainActivity.this)).start();
                new Thread(() -> new JSONObjectEndpointTest(MainActivity.this)).start();
            });
        } else {
            findViewById(R.id.testEndpoints).setOnClickListener( view -> Toast.makeText(
                    MainActivity.this, "Internet connection is required for this " +
                            "operation", Toast.LENGTH_SHORT).show());
        }
    }
}