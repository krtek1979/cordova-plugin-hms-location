/*
Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.huawei.hms.location.cordova;

import android.util.Log;

import com.huawei.hms.location.backend.helpers.HMSBroadcastReceiver;
import com.huawei.hms.location.cordova.helpers.CordovaUtils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;


public class HMSLocationKit extends CordovaPlugin {
    private final static String TAG = HMSLocationKit.class.getSimpleName();

    public void pluginInitialize() {
        super.pluginInitialize();
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        Log.d(TAG, action + " is called.");
        if (action.equals("init")) {
            Log.d(TAG, "init called.");
            HMSBroadcastReceiver.init(cordova.getActivity(), (eventName, value) -> CordovaUtils.sendEvent(this, eventName, value));
            callbackContext.success();
        }
        return false;
    }
}
