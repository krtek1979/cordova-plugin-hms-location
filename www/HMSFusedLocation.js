"use strict";
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
Object.defineProperty(exports, "__esModule", { value: true });
exports.Events = exports.PriorityConstants = exports.hasPermission = exports.requestPermission = exports.removeLocationUpdates = exports.requestLocationUpdates = exports.setMockMode = exports.setMockLocation = exports.getLocationAvailability = exports.getLastLocationWithAddress = exports.checkLocationSettings = exports.flushLocations = exports.getLastLocation = exports.init = void 0;
const utils_1 = require("./utils");
function init() {
    return run('init');
}
exports.init = init;
function getLastLocation() {
    return run('getLastLocation');
}
exports.getLastLocation = getLastLocation;
function flushLocations() {
    return run('flushLocations');
}
exports.flushLocations = flushLocations;
function checkLocationSettings(request) {
    return run('checkLocationSettings', [request]);
}
exports.checkLocationSettings = checkLocationSettings;
function getLastLocationWithAddress(request) {
    return run('getLastLocationWithAddress', [request]);
}
exports.getLastLocationWithAddress = getLastLocationWithAddress;
function getLocationAvailability() {
    return run('getLocationAvailability');
}
exports.getLocationAvailability = getLocationAvailability;
function setMockLocation(latLng) {
    return run('setMockLocation', [latLng]);
}
exports.setMockLocation = setMockLocation;
function setMockMode(mode) {
    return run('setMockMode', [mode]);
}
exports.setMockMode = setMockMode;
function requestLocationUpdates(request) {
    return run('requestLocationUpdates', [request]);
}
exports.requestLocationUpdates = requestLocationUpdates;
function removeLocationUpdates(id) {
    return run('removeLocationUpdates', [id]);
}
exports.removeLocationUpdates = removeLocationUpdates;
function requestPermission() {
    return run('requestPermission');
}
exports.requestPermission = requestPermission;
function hasPermission() {
    return run('hasPermission');
}
exports.hasPermission = hasPermission;
//
// Helpers
//
function run(funcName, args = []) {
    return utils_1.asyncExec('HMSFusedLocation', funcName, args);
}
//
// Constants
//
var PriorityConstants;
(function (PriorityConstants) {
    PriorityConstants[PriorityConstants["PRIORITY_BALANCED_POWER_ACCURACY"] = 102] = "PRIORITY_BALANCED_POWER_ACCURACY";
    PriorityConstants[PriorityConstants["PRIORITY_HIGH_ACCURACY"] = 100] = "PRIORITY_HIGH_ACCURACY";
    PriorityConstants[PriorityConstants["PRIORITY_LOW_POWER"] = 104] = "PRIORITY_LOW_POWER";
    PriorityConstants[PriorityConstants["PRIORITY_NO_POWER"] = 105] = "PRIORITY_NO_POWER";
})(PriorityConstants = exports.PriorityConstants || (exports.PriorityConstants = {}));
var Events;
(function (Events) {
    Events["SCANNING_RESULT"] = "onScanningResult";
})(Events = exports.Events || (exports.Events = {}));
//# sourceMappingURL=HMSFusedLocation.js.map