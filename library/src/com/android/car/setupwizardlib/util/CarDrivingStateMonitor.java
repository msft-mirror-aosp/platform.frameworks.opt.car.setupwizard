/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.car.setupwizardlib.util;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.drivingstate.CarUxRestrictions;
import android.car.drivingstate.CarUxRestrictionsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import java.util.HashSet;
import java.util.Set;

/**
 * Monitor that listens for changes in the driving state so that it can trigger an exit of the
 * setup wizard when {@link CarUxRestrictions.UX_RESTRICTIONS_NO_SETUP}
 * is active.
 */
public class CarDrivingStateMonitor implements
        CarUxRestrictionsManager.OnUxRestrictionsChangedListener {

    private static final String TAG = "CarDrivingStateMonitor";
    private static final long DISCONNECT_DELAY_MS = 700;

    private static CarDrivingStateMonitor sCarDrivingStateMonitor;

    private Car mCar;
    private CarUxRestrictionsManager mRestrictionsManager;
    private Set<DrivingStateChangeListener> mDrivingStateChangeListeners = new HashSet<>();
    // Need to track the number of times the monitor is started so a single stopMonitor call does
    // not override them all.
    private int mMonitorStartedCount;
    private final Context mContext;
    @VisibleForTesting
    final Handler mHandler = new Handler();
    @VisibleForTesting
    final Runnable mDisconnectRunnable = this::disconnectCarMonitor;

    private CarDrivingStateMonitor(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Returns the singleton instance of CarDrivingStateMonitor.
     */
    public static CarDrivingStateMonitor get(Context context) {
        if (sCarDrivingStateMonitor == null) {
            sCarDrivingStateMonitor = new CarDrivingStateMonitor(context);
        }
        return sCarDrivingStateMonitor;
    }

    /**
     * Starts the monitor listening to driving state changes.
     */
    public void startMonitor() {
        mHandler.removeCallbacks(mDisconnectRunnable);
        mMonitorStartedCount++;
        if (mCar != null) {
            if (mCar.isConnected()) {
                try {
                    onUxRestrictionsChanged(mRestrictionsManager.getCurrentCarUxRestrictions());
                } catch (CarNotConnectedException e) {
                    Log.e(TAG, "Car not connected", e);
                }
            } else {
                try {
                    mCar.connect();
                } catch (IllegalStateException e) {
                    // Connection failure - already connected or connecting.
                    Log.e(TAG, "Failure connecting to Car object.", e);
                }
            }
            return;
        }
        mCar = Car.createCar(mContext, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
                    mRestrictionsManager = (CarUxRestrictionsManager)
                            mCar.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE);
                    if (mRestrictionsManager == null) {
                        Log.e(TAG, "Unable to get CarUxRestrictionsManager");
                        return;
                    }
                    mRestrictionsManager.registerListener(CarDrivingStateMonitor.this);
                    onUxRestrictionsChanged(mRestrictionsManager.getCurrentCarUxRestrictions());
                } catch (CarNotConnectedException e) {
                    Log.e(TAG, "Car not connected", e);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                try {
                    if (mRestrictionsManager != null) {
                        mRestrictionsManager.unregisterListener();
                        mRestrictionsManager = null;
                    }
                } catch (CarNotConnectedException e) {
                    Log.e(TAG, "Car not connected", e);
                }
            }
        });
        try {
            mCar.connect();
        } catch (IllegalStateException e) {
            // Connection failure - already connected or connecting.
            Log.e(TAG, "Failure connecting to Car object.", e);
        }
    }

    /**
     * Stops the monitor from listening for driving state changes. This will only occur after a
     * set delay so that calling stop/start in quick succession doesn't actually need to reconnect
     * to the service repeatedly. This monitor also maintains parity between started and stopped so
     * 2 started calls requires two stop calls to stop.
     */
    public void stopMonitor() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Scheduling driving monitor timeout");
        }
        mHandler.removeCallbacks(mDisconnectRunnable);
        mMonitorStartedCount--;
        if (mMonitorStartedCount == 0) {
            mHandler.postDelayed(mDisconnectRunnable, DISCONNECT_DELAY_MS);
        }
    }

    private void disconnectCarMonitor() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Timeout finished, disconnecting Car Monitor");
        }
        if (mMonitorStartedCount > 0) {
            return;
        }
        try {
            if (mRestrictionsManager != null) {
                mRestrictionsManager.unregisterListener();
                mRestrictionsManager = null;
            }
        } catch (CarNotConnectedException e) {
            Log.e(TAG, "Car not connected for unregistering listener", e);
        }

        if (mCar == null || !mCar.isConnected()) {
            return;
        }

        try {
            mCar.disconnect();
        } catch (IllegalStateException e) {
            // Connection failure - already disconnected or disconnecting.
            Log.e(TAG, "Failure disconnecting from Car object", e);
        }

    }

    private boolean isSetupRestricted(CarUxRestrictions restrictionInfo) {
        return (restrictionInfo.getActiveRestrictions()
                & CarUxRestrictions.UX_RESTRICTIONS_NO_SETUP) != 0;
    }

    /**
     * Adds the given listener to be notified if there is a driving exit triggered.
     */
    public void addDrivingExitListener(DrivingStateChangeListener drivingExitListener) {
        mDrivingStateChangeListeners.add(drivingExitListener);
    }

    /**
     * Removes this listener for driving exits.
     */
    public void removeDrivingExitListener(DrivingStateChangeListener drivingExitListener) {
        mDrivingStateChangeListeners.remove(drivingExitListener);
    }

    @Override
    public void onUxRestrictionsChanged(CarUxRestrictions restrictionInfo) {
        // Check if setup restriction is active.
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "onUxRestrictionsChanged");
        }
        if (isSetupRestricted(restrictionInfo)) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Triggering driving exit on listeners");
            }
            for (DrivingStateChangeListener drivingStateChangeListener :
                    mDrivingStateChangeListeners) {
                drivingStateChangeListener.onDrivingExitTriggered();
            }
        }
    }

    /**
     * Resets the car driving state monitor. This is only for use in testing.
     */
    @VisibleForTesting
    public static void reset() {
        sCarDrivingStateMonitor = null;
    }

    /**
     * Interface to be implemented by components that handle the exit logic for setup wizard.
     */
    public interface DrivingStateChangeListener {

        /**
         * Method that will be called by {@link CarDrivingStateMonitor} when the driving state
         * changes to {@link CarUxRestrictions.UX_RESTRICTIONS_NO_SETUP}.
         */
        void onDrivingExitTriggered();
    }
}
