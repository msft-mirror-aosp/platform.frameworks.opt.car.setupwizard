/*
 * Copyright (C) 2020 The Android Open Source Project
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

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.core.util.Preconditions;

import java.lang.ref.WeakReference;

/**
 * Immersive mode helper handles the immersive mode for a given window.
 * Internally it applies the flag specified at
 * https://developer.android.com/training/system-ui/immersive#EnableFullscreen and sets
 * window navigation and status bar colors to transparent when applying immersive mode.
 * Disabling immersive mode restores the previous colors.
 */
public class ImmersiveModeHelper {

    private static final String TAG = ImmersiveModeHelper.class.getSimpleName();

    private WeakReference<Window> mWindowWeakRef;
    private int mNavigationBarColor;
    private int mStatusBarColor;

    /**
     * Constructor.
     *
     * @param window to apply immersive mode to.
     */
    public ImmersiveModeHelper(Window window) {
        Preconditions.checkNotNull(window);
        this.mWindowWeakRef = new WeakReference<>(window);
        this.mNavigationBarColor = window.getNavigationBarColor();
        this.mStatusBarColor = window.getStatusBarColor();
    }

    /**
     * Enables immersive mode hiding system UI. This function also sets navigation and status bar
     * colors as transparent.
     */
    public void enable() {
        Window window = mWindowWeakRef.get();
        if (window == null) {
            Log.w(TAG, "Can't enable immersive move. Window reference is lost.");
            return;
        }

        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Workaround for the issue, StatusBar background hides the buttons (b/154227638).
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * Disables immersive mode hiding system UI and restores the previous colors.
     */
    public void disable() {
        Window window = mWindowWeakRef.get();
        if (window == null) {
            Log.w(TAG, "Can't disable immersive move. Window reference is lost.");
            return;
        }

        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Restores the original window colors.
        window.setNavigationBarColor(mNavigationBarColor);
        window.setStatusBarColor(mStatusBarColor);
    }
}
