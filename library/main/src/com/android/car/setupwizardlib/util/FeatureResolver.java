/*
 * Copyright (C) 2021 The Android Open Source Project
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

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to resolve feature enablement and versions
 */
public class FeatureResolver {

    private static final String TAG = FeatureResolver.class.getSimpleName();
    private static volatile FeatureResolver sInstance = null;

    static final String AUTHORITY =
            CarSetupWizardUiUtils.SETUP_WIZARD_PACKAGE + ".feature_management";
    static final String GET_FEATURE_VERSION_METHOD = "getFeatureVersion";
    static final String GET_FEATURE_ENABLEMENT_METHOD = "getFeatureEnablement";
    static final String SPLIT_NAV_LAYOUT_FEATURE = "split_nav_layout";
    static final String VALUE = "value";

    private Context mContext;

    private Map<String, Bundle> mResultMap = new HashMap<>();

    /**
     * Factory method to get an instance
     */
    public static FeatureResolver get(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (FeatureResolver.class) {
                if (sInstance == null) {
                    sInstance = new FeatureResolver(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * Returns whether the alternative layout feature is enabled
     */
    public boolean isSplitNavLayoutFeatureEnabled() {
        Bundle bundle;
        String bundleKey = SPLIT_NAV_LAYOUT_FEATURE.concat(GET_FEATURE_ENABLEMENT_METHOD);
        if (mResultMap.containsKey(bundleKey)) {
            bundle = mResultMap.get(bundleKey);
        } else {
            bundle = getFeatureBundle(SPLIT_NAV_LAYOUT_FEATURE, GET_FEATURE_ENABLEMENT_METHOD);
            mResultMap.put(bundleKey, bundle);
        }
        boolean isSplitNavLayoutFeatureEnabled = bundle != null
                && bundle.getBoolean(VALUE, false);
        Log.v(TAG, String.format("isSplitNavLayoutEnabled: %s", isSplitNavLayoutFeatureEnabled));
        return isSplitNavLayoutFeatureEnabled;
    }

    /**
     * Returns the enabled version number of split-nav layout
     */
    public int getSplitNavLayoutFeatureVersion() {
        Bundle bundle;
        String bundleKey = SPLIT_NAV_LAYOUT_FEATURE.concat(GET_FEATURE_VERSION_METHOD);
        if (mResultMap.containsKey(bundleKey)) {
            bundle = mResultMap.get(bundleKey);
        } else {
            bundle = getFeatureBundle(SPLIT_NAV_LAYOUT_FEATURE, GET_FEATURE_VERSION_METHOD);
            mResultMap.put(bundleKey, bundle);
        }

        int splitNavLayoutFeatureVersion = bundle != null
                ? bundle.getInt(VALUE, 0) : 0;
        Log.v(TAG, String.format("splitNavLayoutFeatureVersion: %s", splitNavLayoutFeatureVersion));
        return splitNavLayoutFeatureVersion;
    }

    private Bundle getFeatureBundle(String feature, String method) {
        try {
            Uri contentUri =
                    new Uri.Builder()
                            .scheme(ContentResolver.SCHEME_CONTENT)
                            .authority(AUTHORITY)
                            .appendPath(method)
                            .build();
            return mContext.getContentResolver().call(
                    contentUri,
                    method,
                    feature,
                    /* extras= */ null);
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, String.format("Fail to resolve %s feature from suw provider", feature));
            return null;
        }
    }

    private FeatureResolver(Context context) {
        this.mContext = context;
    }
}
