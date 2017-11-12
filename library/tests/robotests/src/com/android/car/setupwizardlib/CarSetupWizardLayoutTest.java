/*
 * Copyright (C) 2017 Android Open Source Project
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

package com.android.car.setupwizardlib;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.android.car.setupwizardlib.robolectric.BaseRobolectricTest;
import com.android.car.setupwizardlib.robolectric.CarSetupWizardLibRobolectricTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

/**
 * Tests for the CarSetupWizardLayout
 */
@RunWith(CarSetupWizardLibRobolectricTestRunner.class)
public class CarSetupWizardLayoutTest extends BaseRobolectricTest {
    private static final String TAG = "CarSetupWizardLayoutTest";

    private boolean isClicked;
    private CarSetupWizardLayout mCarSetupWizardLayout;
    private CarSetupWizardLayoutTestActivity mCarSetupWizardLayoutTestActivity;
    private View mBackButton;
    private Button mPrimaryContinueButton;
    private Button mSecondaryContinueButton;

    @Before
    public void setUp() {
        mCarSetupWizardLayoutTestActivity = Robolectric
                .buildActivity(CarSetupWizardLayoutTestActivity.class)
                .create()
                .get();

        mCarSetupWizardLayout = mCarSetupWizardLayoutTestActivity.
                findViewById(R.id.car_setup_wizard_layout);

        mBackButton = mCarSetupWizardLayout.
                findViewById(R.id.back_button);

        mPrimaryContinueButton = mCarSetupWizardLayout.
                findViewById(R.id.primary_continue_button);

        mSecondaryContinueButton = mCarSetupWizardLayout.
                findViewById(R.id.secondary_continue_button);

        isClicked = false;
    }

    private String getTestString() {
        Context context = RuntimeEnvironment.application;
        return context.getString(R.string.test_text);
    }

    /**
     * Test that setBackButtonListener does set the back button listener
     */
    @Test
    public void testSetBackButtonListener() {
        mCarSetupWizardLayout.setBackButtonListener(v -> {
            isClicked = true;
        });

        assertThat(isClicked).isFalse();

        mBackButton.performClick();

        assertThat(isClicked).isTrue();
    }

    /**
     * Test that setBackButtonVisible does set the back button visibility
     */
    @Test
    public void testSetBackButtonVisible() {
        mCarSetupWizardLayout.setBackButtonVisible(true);
        assertThat(mBackButton.getVisibility()).isEqualTo(View.VISIBLE);

        mCarSetupWizardLayout.setBackButtonVisible(false);
        assertThat(mBackButton.getVisibility()).isEqualTo(View.GONE);
    }

    /**
     * Test that setPrimaryContinueButtonEnabled does set whether the primary continue button is
     * enabled
     */
    @Test
    public void testSetPrimaryContinueButtonEnabled() {
        mCarSetupWizardLayout.setPrimaryContinueButtonEnabled(true);
        assertThat(mPrimaryContinueButton.isEnabled()).isEqualTo(true);

        mCarSetupWizardLayout.setPrimaryContinueButtonEnabled(false);
        assertThat(mPrimaryContinueButton.isEnabled()).isEqualTo(false);
    }

    /**
     * Test that setPrimaryContinueButtonListener does set the primary continue button listener
     */
    @Test
    public void testSetPrimaryContinueButtonListener() {
        mCarSetupWizardLayout.setPrimaryContinueButtonListener(v -> {
            isClicked = true;
        });

        assertThat(isClicked).isFalse();

        mPrimaryContinueButton.performClick();

        assertThat(isClicked).isTrue();
    }

    /**
     * Test that setPrimaryContinueButtonText does set the primary continue button text
     */
    @Test
    public void testSetPrimaryContinueButtonText() {
        mCarSetupWizardLayout.setPrimaryContinueButtonText(getTestString());
        assertThat(mPrimaryContinueButton.getText()).isEqualTo(getTestString());
    }

    /**
     * Test that set does set the primary continue button visibility
     */
    @Test
    public void testSetPrimaryContinueButtonVisible() {
        mCarSetupWizardLayout.setPrimaryContinueButtonVisible(true);
        assertThat(mPrimaryContinueButton.getVisibility()).isEqualTo(View.VISIBLE);

        mCarSetupWizardLayout.setPrimaryContinueButtonVisible(false);
        assertThat(mPrimaryContinueButton.getVisibility()).isEqualTo(View.GONE);
    }

    /**
     * Test that setSecondaryContinueButtonEnabled does set whether the secondary continue button is
     * enabled
     */
    @Test
    public void testSetSecondaryContinueButtonEnabled() {
        mCarSetupWizardLayout.setSecondaryContinueButtonEnabled(true);
        assertThat(mSecondaryContinueButton.isEnabled()).isEqualTo(true);

        mCarSetupWizardLayout.setSecondaryContinueButtonEnabled(false);
        assertThat(mSecondaryContinueButton.isEnabled()).isEqualTo(false);
    }

    /**
     * Test that setSecondaryContinueButtonListener does set the secondary continue button listener
     */
    @Test
    public void testSetSecondaryContinueButtonListener() {
        mCarSetupWizardLayout.setSecondaryContinueButtonListener(v -> {
            isClicked = true;
        });

        assertThat(isClicked).isFalse();

        mSecondaryContinueButton.performClick();

        assertThat(isClicked).isTrue();
    }

    /**
     * Test that setSecondaryContinueButtonText does set the secondary continue button text
     */
    @Test
    public void testSetSecondaryContinueButtonText() {
        mCarSetupWizardLayout.setSecondaryContinueButtonText(getTestString());
        assertThat(mSecondaryContinueButton.getText()).isEqualTo(getTestString());
    }

    /**
     * Test that set does set the secondary continue button visibility
     */
    @Test
    public void testSetSecondaryContinueButtonVisible() {
        mCarSetupWizardLayout.setSecondaryContinueButtonVisible(true);
        assertThat(mSecondaryContinueButton.getVisibility()).isEqualTo(View.VISIBLE);

        mCarSetupWizardLayout.setSecondaryContinueButtonVisible(false);
        assertThat(mSecondaryContinueButton.getVisibility()).isEqualTo(View.GONE);
    }
}
