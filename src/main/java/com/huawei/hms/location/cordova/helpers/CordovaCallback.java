/**
 * Copyright 2020 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.location.cordova.helpers;

import com.huawei.hms.location.backend.interfaces.HMSCallback;

import org.apache.cordova.CallbackContext;
import org.json.JSONObject;

public class CordovaCallback implements HMSCallback {
    private CallbackContext callbackContext;

    public static CordovaCallback fromCallbackContext(CallbackContext callbackContext) {
        return new CordovaCallback(callbackContext);
    }

    private CordovaCallback(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void success() {
        callbackContext.success();
    }

    @Override
    public void success(JSONObject json) {
        callbackContext.success(json);
    }

    @Override
    public void error(JSONObject json) {
        callbackContext.success(json);
    }
}
