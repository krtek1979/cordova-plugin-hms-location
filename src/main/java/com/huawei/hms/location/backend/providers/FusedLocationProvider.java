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

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.location.backend.helpers.Constants;
import com.huawei.hms.location.backend.helpers.Exceptions;
import com.huawei.hms.location.backend.helpers.LocationCallbackWithHandler;
import com.huawei.hms.location.backend.interfaces.HMSCallback;
import com.huawei.hms.location.backend.interfaces.HMSProvider;
import com.huawei.hms.location.backend.interfaces.ResultHandler;
import com.huawei.hms.location.backend.interfaces.TriMapper;
import com.huawei.hms.location.backend.utils.LocationUtils;
import com.huawei.hms.location.backend.utils.PermissionUtils;
import com.huawei.hms.location.backend.utils.PlatformUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.huawei.hms.location.backend.helpers.Exceptions.ERR_DUPLICATE_ID;
import static com.huawei.hms.location.backend.helpers.Exceptions.ERR_EMPTY_CALLBACK;

public class FusedLocationProvider extends HMSProvider implements ResultHandler {
    protected static final String TAG = FusedLocationProvider.class.getSimpleName();

    private HMSCallback permissionResultCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private HashMap<String, LocationCallback> locationCallbackMap;

    private SettingsClient settingsClient;

    public FusedLocationProvider(Context ctx) {
        super(ctx);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        settingsClient = LocationServices.getSettingsClient(getContext());
        locationCallbackMap = new HashMap<>();
    }

    @Override
    public JSONObject getConstants() throws JSONException {
        final JSONObject priorityConstants = new JSONObject();
        priorityConstants.put("PRIORITY_BALANCED_POWER_ACCURACY", LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        priorityConstants.put("PRIORITY_HIGH_ACCURACY", LocationRequest.PRIORITY_HIGH_ACCURACY);
        priorityConstants.put("PRIORITY_LOW_POWER", LocationRequest.PRIORITY_LOW_POWER);
        priorityConstants.put("PRIORITY_NO_POWER", LocationRequest.PRIORITY_NO_POWER);

        final JSONObject eventConstants = new JSONObject();
        eventConstants.put("SCANNING_RESULT", Constants.Event.SCANNING_RESULT.getVal());

        final JSONObject constants = new JSONObject();
        constants.put("PriorityConstants", priorityConstants);
        constants.put("Events", eventConstants);

        return constants;
    }

    // @ExposedMethod
    public void flushLocations(final HMSCallback callback) {
        Log.i(TAG, "flushLocations begin");

        fusedLocationProviderClient
            .flushLocations()
            .addOnSuccessListener(PlatformUtils.voidSuccessListener(callback))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));
    }

    // @ExposedMethod
    public void checkLocationSettings(final JSONObject locationRequestMap, final HMSCallback callback) {
        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        settingsClient
            .checkLocationSettings(LocationUtils.FROM_JSON_OBJECT_TO_LOCATION_SETTINGS_REQUEST.map(locationRequestMap))
            .addOnSuccessListener(PlatformUtils.successListener(callback, LocationUtils.FROM_LOCATION_SETTINGS_STATES_RESPONSE_TO_JSON_OBJECT))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));
    }

    // @ExposedMethod
    public void getLastLocation(final HMSCallback callback) {
        Log.i(TAG, "getLastLocation begin");

        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        fusedLocationProviderClient
            .getLastLocation()
            .addOnSuccessListener(PlatformUtils.successListener(callback, LocationUtils.FROM_LOCATION_TO_JSON_OBJECT))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));

        Log.i(TAG, "getLastLocation end");
    }

    // @ExposedMethod
    public void getLastLocationWithAddress(final JSONObject map, final HMSCallback callback) {
        Log.i(TAG, "getLastLocationWithAddress begin");

        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        fusedLocationProviderClient
            .getLastLocationWithAddress(LocationUtils.FROM_JSON_OBJECT_TO_LOCATION_REQUEST.map(map))
            .addOnSuccessListener(PlatformUtils.successListener(callback, LocationUtils.FROM_HW_LOCATION_TO_JSON_OBJECT))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));

        Log.i(TAG, "getLastLocationWithAddress end");
    }

    // @ExposedMethod
    public void getLocationAvailability(final HMSCallback callback) {
        Log.i(TAG, "getLocationAvailability begin");
        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        fusedLocationProviderClient
            .getLocationAvailability()
            .addOnSuccessListener(PlatformUtils.successListener(callback, LocationUtils.FROM_LOCATION_AVAILABILITY_TO_JSON_OBJECT))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));

        Log.i(TAG, "getLocationAvailability end");
    }

    // @ExposedMethod
    public void setMockLocation(JSONObject map, final HMSCallback callback) {
        Log.i(TAG, "setMockLocation begin");

        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        Location location = new Location("HMS-MOCK");
        location.setLongitude(map.optDouble("longitude"));
        location.setLatitude(map.optDouble("latitude"));

        fusedLocationProviderClient
            .setMockLocation(location)
            .addOnSuccessListener(PlatformUtils.voidSuccessListener(callback))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));

        Log.i(TAG, "setMockLocation end");
    }

    // @ExposedMethod
    public void setMockMode(final boolean shouldMock, final HMSCallback callback) {
        Log.i(TAG, "setMockMode -> shouldMock=" + shouldMock);

        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        fusedLocationProviderClient
            .setMockMode(shouldMock)
            .addOnSuccessListener(PlatformUtils.voidSuccessListener(callback))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));

        Log.i(TAG, "setMockMode end");
    }

    // @ExposedMethod
    public void requestLocationUpdates(final JSONObject json, final HMSCallback callback) {
        requestLocationUpdatesGeneric(fusedLocationProviderClient::requestLocationUpdates, json, callback);
    }

    // @ExposedMethod
    public void requestLocationUpdatesEx(final JSONObject json, final HMSCallback callback) {
        requestLocationUpdatesGeneric(fusedLocationProviderClient::requestLocationUpdatesEx, json, callback);
    }

    // @ExposedMethod
    public void removeLocationUpdates(final String id, final HMSCallback callback) {
        LocationCallback locationCallback = locationCallbackMap.get(id);

        if (locationCallback == null) {
            Log.i(TAG, "removeLocationUpdates callback is null");
            callback.error(Exceptions.toErrorJSON(ERR_EMPTY_CALLBACK));
            return;
        }

        fusedLocationProviderClient
            .removeLocationUpdates(locationCallback)
            .addOnSuccessListener(PlatformUtils.voidSuccessListener(callback))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));
    }

    // @ExposedMethod
    public void requestPermission(HMSCallback callback) {
        PermissionUtils.requestLocationPermission(this);
        permissionResultCallback = callback;
    }

    // @ExposedMethod
    public void hasPermission(final HMSCallback callback) {
        boolean result = PermissionUtils.hasLocationPermission(this);
        callback.success(PlatformUtils.keyValPair("hasPermission", result));
    }

    public void handleResult(Location location) {
        JSONObject params = LocationUtils.FROM_LOCATION_TO_JSON_OBJECT.map(location);
        getEventSender().send(Constants.Event.SCANNING_RESULT.getVal(), params);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        JSONObject json = PermissionUtils.HANDLE_PERMISSION_REQUEST_RESULT.map(requestCode, permissions, grantResults);
        if (permissionResultCallback != null) {
            permissionResultCallback.success(json);
        } else {
            Log.e(TAG, "onRequestPermissionResult() :: permissionResultCallback is null");
        }
    }

    private void requestLocationUpdatesGeneric(TriMapper<LocationRequest, LocationCallback, Looper, Task<Void>> requestMethod, final JSONObject json, final HMSCallback callback) {
        Log.i(TAG, "requestLocationUpdatesWithCallback start");

        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        final String id = json.optString("id");
        final LocationRequest locationRequest = LocationUtils.FROM_JSON_OBJECT_TO_LOCATION_REQUEST.map(json);

        if (locationCallbackMap.get(id) != null) {
            Log.i(TAG, "requestLocationUpdatesWithCallback: this callback id already exists");
            callback.error(Exceptions.toErrorJSON(ERR_DUPLICATE_ID));
            return;
        }

        // Create locationCallback
        LocationCallback locationCallback = new LocationCallbackWithHandler(this);
        locationCallbackMap.put(id, locationCallback);

        requestMethod.map(locationRequest, locationCallback, Looper.getMainLooper())
            .addOnSuccessListener(PlatformUtils.successListener(callback, PlatformUtils.keyValPair("requestCode", id)))
            .addOnFailureListener(PlatformUtils.failureListener(getActivity(), callback));

        Log.i(TAG, "call requestLocationUpdatesWithCallback success.");
    }
}
