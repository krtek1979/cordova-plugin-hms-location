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

package com.huawei.hms.location.backend.providers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.huawei.hms.location.ActivityConversionInfo;
import com.huawei.hms.location.ActivityConversionRequest;
import com.huawei.hms.location.ActivityIdentification;
import com.huawei.hms.location.ActivityIdentificationData;
import com.huawei.hms.location.ActivityIdentificationService;
import com.huawei.hms.location.backend.helpers.Constants;
import com.huawei.hms.location.backend.helpers.Exceptions;
import com.huawei.hms.location.backend.helpers.HMSBroadcastReceiver;
import com.huawei.hms.location.backend.helpers.Pair;
import com.huawei.hms.location.backend.interfaces.HMSCallback;
import com.huawei.hms.location.backend.interfaces.HMSProvider;
import com.huawei.hms.location.backend.utils.ActivityUtils;
import com.huawei.hms.location.backend.utils.PermissionUtils;
import com.huawei.hms.location.backend.utils.PlatformUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.huawei.hms.location.backend.helpers.Exceptions.ERR_NO_EXISTENT_REQUEST_ID;

public class ActivityIdentificationProvider extends HMSProvider {
    private static String TAG = ActivityIdentificationProvider.class.getSimpleName();
    private HMSCallback permissionResultCallback;
    private ActivityIdentificationService activityService;

    private Map<Integer, PendingIntent> requests;
    private int mRequestCode = 0;


    public ActivityIdentificationProvider(Context ctx) {
        super(ctx);

        this.activityService = ActivityIdentification.getService(getContext());
        this.requests = new HashMap<>();
    }

    @Override
    public JSONObject getConstants() throws JSONException {
        final JSONObject activityConstants = new JSONObject();
        activityConstants.put("VEHICLE", ActivityIdentificationData.VEHICLE);
        activityConstants.put("BIKE", ActivityIdentificationData.BIKE);
        activityConstants.put("FOOT", ActivityIdentificationData.FOOT);
        activityConstants.put("RUNNING", ActivityIdentificationData.RUNNING);
        activityConstants.put("STILL", ActivityIdentificationData.STILL);
        activityConstants.put("TILTING", ActivityIdentificationData.TILTING);
        activityConstants.put("OTHERS", ActivityIdentificationData.OTHERS);
        activityConstants.put("WALKING", ActivityIdentificationData.WALKING);

        final JSONObject eventConstants = new JSONObject();
        eventConstants.put("ACTIVITY_CONVERSION_RESULT", Constants.Event.ACTIVITY_CONVERSION_RESULT.getVal());
        eventConstants.put("ACTIVITY_IDENTIFICATION_RESULT", Constants.Event.ACTIVITY_IDENTIFICATION_RESULT.getVal());

        final JSONObject activityConversionConstants = new JSONObject();
        activityConversionConstants.put("ENTER_ACTIVITY_CONVERSION", ActivityConversionInfo.ENTER_ACTIVITY_CONVERSION);
        activityConversionConstants.put("EXIT_ACTIVITY_CONVERSION", ActivityConversionInfo.EXIT_ACTIVITY_CONVERSION);

        final JSONObject constants = new JSONObject();
        constants.put("Activities", activityConstants);
        constants.put("Events", eventConstants);
        constants.put("ActivityConversions", activityConversionConstants);

        return constants;
    }

    // @ExposedMethod
    public void createActivityConversionUpdates(JSONArray activityConversionRequestArray, final HMSCallback callback) {
        Log.i(TAG, "createActivityConversionUpdates start");

        ActivityConversionRequest request = ActivityUtils.FROM_JSON_ARRAY_TO_ACTIVITY_CONVERSION_REQUEST.map(activityConversionRequestArray);

        final Pair<Integer, PendingIntent> intentData = buildPendingIntent(HMSBroadcastReceiver.ACTION_PROCESS_CONVERSION);
        activityService.createActivityConversionUpdates(request, intentData.get1())
            .addOnSuccessListener(PlatformUtils.successListener(callback, PlatformUtils.keyValPair("requestCode", intentData.get0())))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));

        Log.i(TAG, "createActivityConversionUpdates end");
    }

    // @ExposedMethod
    public void createActivityIdentificationUpdates(double intervalMillis, final HMSCallback callback) {
        Log.i(TAG, "createActivityIdentificationUpdates start");

        final Pair<Integer, PendingIntent> intentData = buildPendingIntent(HMSBroadcastReceiver.ACTION_PROCESS_IDENTIFICATION);

        activityService.createActivityIdentificationUpdates((long) intervalMillis, intentData.get1())
            .addOnSuccessListener(PlatformUtils.successListener(callback, PlatformUtils.keyValPair("requestCode", intentData.get0())))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));

        Log.i(TAG, "createActivityIdentificationUpdates end");
    }

    // @ExposedMethod
    public void deleteActivityConversionUpdates(final int requestCode, final HMSCallback callback) {
        Log.i(TAG, "deleteActivityConversionUpdates start");
        if (!requests.containsKey(requestCode)) {
            callback.error(Exceptions.toErrorJSON(ERR_NO_EXISTENT_REQUEST_ID));
        }

        activityService.deleteActivityConversionUpdates(requests.get(requestCode))
            .addOnSuccessListener(PlatformUtils.voidSuccessListener(callback))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));

        Log.i(TAG, "deleteActivityConversionUpdates end");
    }

    // @ExposedMethod
    public void deleteActivityIdentificationUpdates(final int requestCode, final HMSCallback callback) {
        Log.i(TAG, "deleteActivityIdentificationUpdates start");

        if (!requests.containsKey(requestCode)) {
            callback.error(Exceptions.toErrorJSON(ERR_NO_EXISTENT_REQUEST_ID));
        }

        activityService.deleteActivityIdentificationUpdates(requests.get(requestCode))
            .addOnSuccessListener(PlatformUtils.voidSuccessListener(callback))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));

        Log.i(TAG, "deleteActivityIdentificationUpdates end");
    }

    // @ExposedMethod
    public void requestPermission(final HMSCallback callback) {
        PermissionUtils.requestActivityRecognitionPermission(this);
        permissionResultCallback = callback;
    }

    // @ExposedMethod
    public void hasPermission(final HMSCallback callback) {
        boolean result = PermissionUtils.hasActivityRecognitionPermission(this);
        callback.success(PlatformUtils.keyValPair("hasPermission", result));
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        JSONObject json = PermissionUtils.HANDLE_PERMISSION_REQUEST_RESULT.map(requestCode, permissions, grantResults);
        if (permissionResultCallback != null) {
            permissionResultCallback.success(json);
        } else {
            Log.w(TAG, "onRequestPermissionResult() :: null callback");
        }
    }

    private Pair<Integer, PendingIntent> buildPendingIntent(String action) {
        Log.d(TAG, "buildPendingIntent start");
        Intent intent = new Intent();
        intent.setPackage(getActivity().getApplicationContext().getPackageName());
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), mRequestCode++, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.requests.put(mRequestCode, pendingIntent);
        return Pair.create(mRequestCode, pendingIntent);
    }
}
