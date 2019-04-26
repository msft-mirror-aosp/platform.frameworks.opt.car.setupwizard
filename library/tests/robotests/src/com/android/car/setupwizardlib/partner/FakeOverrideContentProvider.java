/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.car.setupwizardlib.partner;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import org.robolectric.Robolectric;

/**
 * An implementation of
 * {@link com.google.android.car.setupwizard.partner.PartnerCustomizationProvider} for
 * Robolectric tests.
 */
public class FakeOverrideContentProvider extends ContentProvider {

    private final Bundle mFakeProviderResultBundle = new Bundle();

    public FakeOverrideContentProvider injectResourceEntry(ResourceEntry resourceEntry) {
        mFakeProviderResultBundle.putBundle(resourceEntry.getResourceName(),
                resourceEntry.toBundle());
        return this;
    }

    public static FakeOverrideContentProvider installEmptyProvider() {
        PartnerConfigHelper.resetForTesting();
        return Robolectric.setupContentProvider(
                FakeOverrideContentProvider.class, PartnerConfigHelper.SUW_AUTHORITY);
    }

    public static FakeOverrideContentProvider installDefaultProvider() {
        return installEmptyProvider()
                .injectResourceEntry(new ResourceEntry("test", "testResource", 123));
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(
            Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        throw new UnsupportedOperationException("query operation not supported currently.");
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("getType operation not supported currently.");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("insert operation not supported currently.");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("delete operation not supported currently.");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("update operation not supported currently.");
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (TextUtils.equals(method, PartnerConfigHelper.SUW_GET_PARTNER_CONFIG_METHOD)) {
            return mFakeProviderResultBundle;
        }
        return null;
    }
}
