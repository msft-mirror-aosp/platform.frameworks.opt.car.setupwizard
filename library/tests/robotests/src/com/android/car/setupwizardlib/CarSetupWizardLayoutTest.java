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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.car.setupwizardlib.robolectric.BaseRobolectricTest;
import com.android.car.setupwizardlib.robolectric.CarSetupWizardLibRobolectricTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.Locale;

/**
 * Tests for the CarSetupWizardLayout
 */
@RunWith(CarSetupWizardLibRobolectricTestRunner.class)
public class CarSetupWizardLayoutTest extends BaseRobolectricTest {
    private static final String TAG = "CarSetupWizardLayoutTest";

    private static final Locale LOCALE_EN_US = new Locale("en", "US");
    // Hebrew locale can be used to test RTL.
    private static final Locale LOCALE_IW_IL = new Locale("iw", "IL");

    private boolean isClicked;
    private CarSetupWizardLayout mCarSetupWizardLayout;
    private CarSetupWizardLayoutTestActivity mCarSetupWizardLayoutTestActivity;
    private View mBackButton;
    private TextView mToolbarTitleView;
    private Button mPrimaryToolbarButton;
    private Button mSecondaryToolbarButton;
    private ProgressBar mProgressBar;

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

        mPrimaryToolbarButton = mCarSetupWizardLayout.
                findViewById(R.id.primary_toolbar_button);

        // Have to make this call first to ensure secondaryToolbar button is created from stub.
        mCarSetupWizardLayout.setSecondaryToolbarButtonVisible(true);
        mCarSetupWizardLayout.setSecondaryToolbarButtonVisible(false);
        mSecondaryToolbarButton = mCarSetupWizardLayout.
                findViewById(R.id.secondary_toolbar_button);

        mToolbarTitleView = mCarSetupWizardLayout.
                findViewById(R.id.toolbar_title);

        mProgressBar = mCarSetupWizardLayout.findViewById(R.id.progress_bar);

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
     * Test that setBackButtonVisible does set the back button visibility and updates the touch
     * delegate accordingly
     */
    @Test
    public void testSetBackButtonVisible() {
        mCarSetupWizardLayout.setBackButtonVisible(true);
        assertThat(mBackButton.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(((View) mBackButton.getParent()).getTouchDelegate()).isNotNull();

        mCarSetupWizardLayout.setBackButtonVisible(false);
        assertThat(mBackButton.getVisibility()).isEqualTo(View.GONE);
        assertThat(((View) mBackButton.getParent()).getTouchDelegate()).isNull();
    }

    /**
     * Tests that setToolbarTitleVisible sets the toolbar title visibility
     */
    @Test
    public void testSetToolbarTitleVisible() {
        mCarSetupWizardLayout.setToolbarTitleVisible(true);
        assertThat(mToolbarTitleView.getVisibility()).isEqualTo(View.VISIBLE);

        mCarSetupWizardLayout.setToolbarTitleVisible(false);
        assertThat(mToolbarTitleView.getVisibility()).isEqualTo(View.GONE);
    }

    /**
     * Tests that setToolbarTitleText does set the toolbar title text
     */
    @Test
    public void testSetToolbarTitleText() {
        mCarSetupWizardLayout.setToolbarTitleText(getTestString());
        assertThat(mToolbarTitleView.getText()).isEqualTo(getTestString());
    }

    /**
     * Test that setPrimaryToolbarButtonEnabled does set whether the primary continue button is
     * enabled
     */
    @Test
    public void testSetPrimaryToolbarButtonEnabled() {
        mCarSetupWizardLayout.setPrimaryToolbarButtonEnabled(true);
        assertThat(mPrimaryToolbarButton.isEnabled()).isEqualTo(true);

        mCarSetupWizardLayout.setPrimaryToolbarButtonEnabled(false);
        assertThat(mPrimaryToolbarButton.isEnabled()).isEqualTo(false);
    }

    /**
     * Test that setPrimaryToolbarButtonListener does set the primary continue button listener
     */
    @Test
    public void testSetPrimaryToolbarButtonListener() {
        mCarSetupWizardLayout.setPrimaryToolbarButtonListener(v -> {
            isClicked = true;
        });

        assertThat(isClicked).isFalse();

        mPrimaryToolbarButton.performClick();

        assertThat(isClicked).isTrue();
    }

    /**
     * Test that setPrimaryToolbarButtonText does set the primary continue button text
     */
    @Test
    public void testSetPrimaryToolbarButtonText() {
        mCarSetupWizardLayout.setPrimaryToolbarButtonText(getTestString());
        assertThat(mPrimaryToolbarButton.getText()).isEqualTo(getTestString());
    }

    /**
     * Test that set does set the primary continue button visibility
     */
    @Test
    public void testSetPrimaryToolbarButtonVisible() {
        mCarSetupWizardLayout.setPrimaryToolbarButtonVisible(true);
        assertThat(mPrimaryToolbarButton.getVisibility()).isEqualTo(View.VISIBLE);

        mCarSetupWizardLayout.setPrimaryToolbarButtonVisible(false);
        assertThat(mPrimaryToolbarButton.getVisibility()).isEqualTo(View.GONE);
    }

    /**
     * Test that setSecondaryToolbarButtonEnabled does set whether the secondary continue button is
     * enabled
     */
    @Test
    public void testSetSecondaryToolbarButtonEnabled() {
        mCarSetupWizardLayout.setSecondaryToolbarButtonEnabled(true);
        assertThat(mSecondaryToolbarButton.isEnabled()).isEqualTo(true);

        mCarSetupWizardLayout.setSecondaryToolbarButtonEnabled(false);
        assertThat(mSecondaryToolbarButton.isEnabled()).isEqualTo(false);
    }

    /**
     * Test that setSecondaryToolbarButtonListener does set the secondary continue button listener
     */
    @Test
    public void testSetSecondaryToolbarButtonListener() {
        mCarSetupWizardLayout.setSecondaryToolbarButtonListener(v -> {
            isClicked = true;
        });

        assertThat(isClicked).isFalse();

        mSecondaryToolbarButton.performClick();

        assertThat(isClicked).isTrue();
    }

    /**
     * Test that setSecondaryToolbarButtonText does set the secondary continue button text
     */
    @Test
    public void testSetSecondaryToolbarButtonText() {
        mCarSetupWizardLayout.setSecondaryToolbarButtonText(getTestString());
        assertThat(mSecondaryToolbarButton.getText()).isEqualTo(getTestString());
    }

    /**
     * Test that set does set the secondary continue button visibility
     */
    @Test
    public void testSetSecondaryToolbarButtonVisible() {
        mCarSetupWizardLayout.setSecondaryToolbarButtonVisible(true);
        assertThat(mSecondaryToolbarButton.getVisibility()).isEqualTo(View.VISIBLE);

        mCarSetupWizardLayout.setSecondaryToolbarButtonVisible(false);
        assertThat(mSecondaryToolbarButton.getVisibility()).isEqualTo(View.GONE);
    }

    /**
     * Test that setProgressBarVisible does set the progress bar visibility
     */
    @Test
    public void testSetProgressBarVisible() {
        mCarSetupWizardLayout.setProgressBarVisible(true);
        assertThat(mProgressBar.getVisibility()).isEqualTo(View.VISIBLE);

        mCarSetupWizardLayout.setProgressBarVisible(false);
        assertThat(mProgressBar.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void testApplyUpdatedLocale() {
        mCarSetupWizardLayout.applyLocale(LOCALE_IW_IL);
        assertThat(mToolbarTitleView.getLayoutDirection()).isEqualTo(View.LAYOUT_DIRECTION_RTL);
        assertThat(mToolbarTitleView.getTextLocale()).isEqualTo(LOCALE_IW_IL);
        assertThat(mPrimaryToolbarButton.getLayoutDirection()).isEqualTo(View.LAYOUT_DIRECTION_RTL);
        assertThat(mPrimaryToolbarButton.getTextLocale()).isEqualTo(LOCALE_IW_IL);
        assertThat(mSecondaryToolbarButton.getLayoutDirection())
                .isEqualTo(View.LAYOUT_DIRECTION_RTL);
        assertThat(mSecondaryToolbarButton.getTextLocale()).isEqualTo(LOCALE_IW_IL);

        mCarSetupWizardLayout.applyLocale(LOCALE_EN_US);
        assertThat(mToolbarTitleView.getLayoutDirection()).isEqualTo(View.LAYOUT_DIRECTION_LTR);
        assertThat(mToolbarTitleView.getTextLocale()).isEqualTo(LOCALE_EN_US);
        assertThat(mPrimaryToolbarButton.getLayoutDirection()).isEqualTo(View.LAYOUT_DIRECTION_LTR);
        assertThat(mPrimaryToolbarButton.getTextLocale()).isEqualTo(LOCALE_EN_US);
        assertThat(mSecondaryToolbarButton.getLayoutDirection())
                .isEqualTo(View.LAYOUT_DIRECTION_LTR);
        assertThat(mSecondaryToolbarButton.getTextLocale()).isEqualTo(LOCALE_EN_US);
    }

    /**
     * Test that setProgressBarProgress does set the progress bar progress
     */
    @Test
    public void testSetProgressBarProgress() {
        int progress = 80;
        mCarSetupWizardLayout.setProgressBarProgress(progress);
        assertThat(mProgressBar.isIndeterminate()).isFalse();
        assertThat(mProgressBar.getProgress()).isEqualTo(progress);
    }

    /**
     * Test that setting primary button to flat replaces it with a flat button.
     */
    @Test
    public void testSetPrimaryToolbarButtonFlat() {
        mCarSetupWizardLayout.setPrimaryToolbarButtonFlat(true);
        ViewGroup buttonGroup = mCarSetupWizardLayoutTestActivity
                .findViewById(R.id.button_container);
        assertThat(buttonGroup.getChildCount() == 2);
        mPrimaryToolbarButton = mCarSetupWizardLayoutTestActivity
                .findViewById(R.id.primary_toolbar_button);
        assertThat(mPrimaryToolbarButton.getBackground()
                .equals(mCarSetupWizardLayoutTestActivity
                        .getDrawable(R.drawable.abc_btn_borderless_material)));
        assertThat(buttonGroup.indexOfChild(mPrimaryToolbarButton) == 1);
    }
}
