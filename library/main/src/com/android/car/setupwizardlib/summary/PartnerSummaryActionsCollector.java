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


package com.android.car.setupwizardlib.summary;

import android.annotation.Nullable;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collects the actions provided by partners and compiles them into a list of {@link SummaryAction}
 * items.
 */
public class PartnerSummaryActionsCollector {

    private static final String TAG = "PartnerSummaryActionsCollector";

    private static final String CONTENT_PROVIDER_INTENT_ACTION =
            "com.google.android.car.setupwizard.SETUP_ACTIONS_STATE_PROVIDER";

    // Methods for fetching info from the content provider.
    private static final String METHOD_GET_ACTION_COMPLETION_STATE = "get_action_completion_state";
    private static final String METHOD_GET_ACTION_SUMMARY_STATE = "get_action_summary_state";
    private static final String METHOD_GET_SUMMARY_ACTIONS = "get_summary_actions";

    // Constants for fetching information from the bundles passed back by the content provider.
    private static final String EXTRA_SUMMARY_ACTIONS_LIST = "summary_actions_list";
    private static final String EXTRA_IS_ACTION_COMPLETED = "is_action_completed";

    // Constants for information contained within the summary action bundle.
    private static final String EXTRA_SUMMARY_ACTION_HAS_DEPENDENCY =
            "summary_action_has_dependency";
    private static final String EXTRA_SUMMARY_ACTION_DEPENDENCY_DESCRIPTION =
            "summary_action_dependency_description";
    private static final String EXTRA_SUMMARY_ACTION_TITLE = "summary_action_title";
    private static final String EXTRA_SUMMARY_ACTION_DESCRIPTION = "summary_action_description";
    private static final String EXTRA_SUMMARY_ACTION_REQUIRES_NETWORK =
            "summary_action_requires_network";
    private static final String EXTRA_SUMMARY_ACTION_WIZARD_SCRIPT = "summary_action_wizard_script";
    private static final String EXTRA_SUMMARY_ACTION_PRIORITY = "summary_action_priority";

    // Extra used as a key for the action id passed in to query summary action state.
    private static final String EXTRA_ACTION_ID = "action_id";
    private static PartnerSummaryActionsCollector partnerSummaryActionsCollector;
    private final Context context;

    /** private constructor, should use getter. */
    private PartnerSummaryActionsCollector(Context context) {
        this.context = context;
    }

    /** Gets the current instance of the {@link PartnerSummaryActionsCollector}. */
    public static PartnerSummaryActionsCollector get(Context context) {
        if (partnerSummaryActionsCollector == null) {
            partnerSummaryActionsCollector = new PartnerSummaryActionsCollector(context);
        }
        return partnerSummaryActionsCollector;
    }

    /**
     * Creates a summary action using the passed in completion state and summary state {@link
     * Bundle}.
     * This will pull out all relevant state such as title, description, dependencies, and anything
     * else that defines a summary item. Returns null if the bundle does not have all the required
     * state or is null.
     */
    @Nullable
    private static SummaryAction buildSummaryAction(
            boolean completed, Bundle summaryStateBundle) {
        if (summaryStateBundle == null) {
            return null;
        }

        String title = summaryStateBundle.getString(EXTRA_SUMMARY_ACTION_TITLE);
        if (title == null) {
            Log.e(TAG, "No title provided in summaryStateBundle: " + summaryStateBundle);
            return null;
        }

        String scriptUri = summaryStateBundle.getString(EXTRA_SUMMARY_ACTION_WIZARD_SCRIPT);
        if (scriptUri == null) {
            Log.e(TAG, "No wizard script provided in summaryStateBundle: " + summaryStateBundle);
            return null;
        }

        String description = summaryStateBundle.getString(EXTRA_SUMMARY_ACTION_DESCRIPTION, "");
        boolean requiresNetwork =
                summaryStateBundle.getBoolean(EXTRA_SUMMARY_ACTION_REQUIRES_NETWORK, false);
        boolean hasUnfinishedDependency =
                summaryStateBundle.getBoolean(EXTRA_SUMMARY_ACTION_HAS_DEPENDENCY, false);
        String unfinishedDependencyDescription = null;
        if (hasUnfinishedDependency) {
            unfinishedDependencyDescription =
                    summaryStateBundle.getString(EXTRA_SUMMARY_ACTION_DEPENDENCY_DESCRIPTION);
        }
        // Fetch priority, default 0 so that if no priority is provided they will be placed above
        // the Google items which are located in 100-200.
        int priority = summaryStateBundle.getInt(EXTRA_SUMMARY_ACTION_PRIORITY, 0);

        return new SummaryAction(
                title,
                description,
                requiresNetwork,
                completed,
                priority,
                scriptUri,
                hasUnfinishedDependency,
                unfinishedDependencyDescription);
    }

    private static ResolveInfo getSummaryContentProviderResolveInfo(PackageManager packageManager) {
        Intent contentProviderQueryIntent = new Intent(CONTENT_PROVIDER_INTENT_ACTION);
        List<ResolveInfo> queryResults =
                packageManager.queryIntentContentProviders(contentProviderQueryIntent, 0);
        Log.v(TAG, "Query results size before pruning for system packages: " + queryResults.size());
        queryResults =
                queryResults.stream()
                        .filter(
                                resolveInfo ->
                                        resolveInfo.providerInfo != null
                                                && resolveInfo.providerInfo.applicationInfo != null
                                                && (resolveInfo.providerInfo.applicationInfo.flags
                                                & ApplicationInfo.FLAG_SYSTEM)
                                                != 0)
                        .collect(Collectors.toList());
        if (queryResults.size() > 1 || queryResults.isEmpty()) {
            Log.v(
                    TAG,
                    "Found "
                            + queryResults.size()
                            + " content providers, there should be exactly 1 to show partner "
                            + "actions. Ignoring"
                            + " all partner actions.");
            return null;
        }
        return queryResults.get(0);
    }

    private static Uri getSummaryContentProviderUri(ResolveInfo resolveInfo) {
        if (resolveInfo.providerInfo == null || TextUtils.isEmpty(
                resolveInfo.providerInfo.authority)) {
            Log.e(TAG, "Incorrectly configured partner content provider");
            return null;
        }
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(resolveInfo.providerInfo.authority)
                .build();
    }

    /**
     * Gets the list of provided partner summary actions. Will return an empty list if none are
     * found
     * or there is an error loading them.
     */
    public List<SummaryAction> getPartnerSummaryActions() {
        ResolveInfo resolveInfo = getSummaryContentProviderResolveInfo(context.getPackageManager());

        if (resolveInfo == null) {
            Log.v(TAG, "Could not find partner content provider.");
            return new ArrayList<>();
        }

        Uri contentProviderUri = getSummaryContentProviderUri(resolveInfo);

        if (contentProviderUri == null) {
            Log.e(TAG, "Could not fetch content provider URI, ignoring partner summary items");
            return new ArrayList<>();
        }
        ArrayList<String> partnerSummaryActions;
        try {
            partnerSummaryActions = getPartnerSummaryActionsFromContentProvider(contentProviderUri);
        } catch (NullPointerException | IllegalArgumentException e) {
            Log.e(TAG, "Unable to find or successfully query content provider, ignoring action", e);
            return new ArrayList<>();
        }

        if (partnerSummaryActions == null || partnerSummaryActions.isEmpty()) {
            Log.v(TAG, "No actions were fetched for partners");
            return new ArrayList<>();
        }

        List<SummaryAction> summaryActionList = new ArrayList<>();
        for (String actionId : partnerSummaryActions) {
            Log.v(TAG, "Attempting to generate summary action for id: " + actionId);
            try {
                boolean completed =
                        getActionCompletionStateFromContentProvider(actionId, contentProviderUri);
                Bundle summaryActionBundle =
                        getActionSummaryStateFromContentProvider(actionId, contentProviderUri);
                SummaryAction summaryAction = buildSummaryAction(completed, summaryActionBundle);
                if (summaryAction != null) {
                    summaryActionList.add(summaryAction);
                }
            } catch (NullPointerException | IllegalArgumentException e) {
                Log.e(
                        TAG,
                        "Unable to load the completion or config state for summary action: "
                                + actionId,
                        e);
                continue;
            }
        }
        return summaryActionList;
    }

    /**
     * Gets the list of actionId's for the partner summary actions form the passed in content
     * provider
     * {@link Uri}.
     *
     * @throws NullPointerException     if the method is null on the content provider.
     * @throws IllegalArgumentException if uri is not known or the request method is not supported
     *                                  properly.
     */
    private ArrayList<String> getPartnerSummaryActionsFromContentProvider(Uri contentProviderUri) {
        Bundle result = context.getContentResolver().call(
                        contentProviderUri,
                        METHOD_GET_SUMMARY_ACTIONS,
                        /* arg= */ null,
                        /* extras= */ null);
        if (result == null || result.getStringArrayList(EXTRA_SUMMARY_ACTIONS_LIST) == null) {
            Log.e(
                    TAG,
                    "No summary actions returned from content resolve call, can't fetch partner "
                            + "actions.");
            throw new IllegalArgumentException(
                    "Uri: " + contentProviderUri + " did not return a list of actionId's.");
        }
        return result.getStringArrayList(EXTRA_SUMMARY_ACTIONS_LIST);
    }

    /**
     * Gets the completion state for the specific actionId passed in using the passed in content
     * provider {@link Uri}.
     *
     * @throws NullPointerException     if the method is null on the content provider.
     * @throws IllegalArgumentException if uri is not known or the request method is not supported
     *                                  properly.
     */
    private boolean getActionCompletionStateFromContentProvider(
            String actionId, Uri contentProviderUri) {
        Bundle completionStateArgs = new Bundle();
        completionStateArgs.putString(EXTRA_ACTION_ID, actionId);
        Bundle result = context.getContentResolver().call(
                        contentProviderUri,
                        METHOD_GET_ACTION_COMPLETION_STATE,
                        /* arg= */ null,
                        completionStateArgs);
        if (result == null || !result.containsKey(EXTRA_IS_ACTION_COMPLETED)) {
            throw new IllegalArgumentException(
                    "No action with id " + actionId + " found in content provider");
        }
        return result.getBoolean(EXTRA_IS_ACTION_COMPLETED, true);
    }

    private Bundle getActionSummaryStateFromContentProvider(String actionId,
            Uri contentProviderUri) {
        Bundle summaryStateArgs = new Bundle();
        summaryStateArgs.putString(EXTRA_ACTION_ID, actionId);
        Bundle result = context.getContentResolver().call(
                        contentProviderUri,
                        METHOD_GET_ACTION_SUMMARY_STATE,
                        /* arg= */ null,
                        summaryStateArgs);
        if (result == null) {
            throw new IllegalArgumentException(
                    "No action summary found in content provider for " + actionId);
        }
        return result;
    }
}