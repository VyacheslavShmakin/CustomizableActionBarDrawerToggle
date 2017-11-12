/*
 * Copyright (C) 2014 The Android Open Source Project
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


package ru.shmakinv.android.widget.customizableactionbardrawertoggle;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Method;

/**
 * This class encapsulates some awful hacks.
 *
 * Before JB-MR2 (API 18) it was not possible to change the home-as-up indicator glyph
 * in an action bar without some really gross hacks. Since the MR2 SDK is not published as of
 * this writing, the new API is accessed via reflection here if available.
 *
 * Moved from Support-v4
 */
class ActionBarDrawerToggleHoneycomb {
    private static final String TAG = "ActionBarDrawerToggleHoneycomb";
    private static final String MESSAGE_CONTENT_DESCR = "Couldn't set content description via JB-MR2 API";
    private static final String MESSAGE_HOME_AS_UP = "Couldn't set home-as-up indicator";
    private static final String MESSAGE_HOME_AS_UP_JB_MR2 = "Couldn't set home-as-up indicator via JB-MR2 API";

    private static final String METHOD_NAME_HOME_AS_UP = "setHomeAsUpIndicator";

    private static final int[] THEME_ATTRS = new int[] {
            R.attr.homeAsUpIndicator
    };

    @SuppressLint("LongLogTag")
    public static SetIndicatorInfo setActionBarUpIndicator(SetIndicatorInfo info, Activity activity,
            Drawable drawable, int contentDescRes) {
        if (info == null) {
            info = new SetIndicatorInfo(activity);
        }
        if (info.setHomeAsUpIndicator != null) {
            try {
                final ActionBar actionBar = activity.getActionBar();
                info.setHomeAsUpIndicator.invoke(actionBar, drawable);
                info.setHomeActionContentDescription.invoke(actionBar, contentDescRes);
            } catch (Exception e) {
                Log.w(TAG, MESSAGE_HOME_AS_UP_JB_MR2, e);
            }
        } else if (info.upIndicatorView != null) {
            info.upIndicatorView.setImageDrawable(drawable);
        } else {
            Log.w(TAG, MESSAGE_HOME_AS_UP);
        }
        return info;
    }

    @SuppressLint("LongLogTag")
    public static SetIndicatorInfo setActionBarDescription(SetIndicatorInfo info, Activity activity,
            int contentDescRes) {
        if (info == null) {
            info = new SetIndicatorInfo(activity);
        }
        if (info.setHomeAsUpIndicator != null) {
            try {
                final ActionBar actionBar = activity.getActionBar();
                info.setHomeActionContentDescription.invoke(actionBar, contentDescRes);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    // For API 19 and earlier, we need to manually force the
                    // action bar to generate a new content description.
                    actionBar.setSubtitle(actionBar.getSubtitle());
                }
            } catch (Exception e) {
                Log.w(TAG, MESSAGE_CONTENT_DESCR, e);
            }
        }
        return info;
    }

    public static Drawable getThemeUpIndicator(Activity activity) {
        final TypedArray a = activity.obtainStyledAttributes(THEME_ATTRS);
        final Drawable result = a.getDrawable(0);
        a.recycle();
        return result;
    }

    static class SetIndicatorInfo {
        public Method setHomeAsUpIndicator;
        public Method setHomeActionContentDescription;
        public ImageView upIndicatorView;

        SetIndicatorInfo(Activity activity) {
            try {
                this.setHomeAsUpIndicator = ActionBar.class.getDeclaredMethod(
                        METHOD_NAME_HOME_AS_UP, Drawable.class);

                // If we got the method we won't need the stuff below.
                return;
            } catch (NoSuchMethodException e) {
                // Oh well. We'll use the other mechanism below instead.
            }

            final View home = activity.findViewById(R.id.home);
            if (home == null) {
                // Action bar doesn't have a known configuration, an OEM messed with things.
                return;
            }

            final ViewGroup parent = (ViewGroup) home.getParent();
            final int childCount = parent.getChildCount();
            if (childCount != 2) {
                // No idea which one will be the right one, an OEM messed with things.
                return;
            }

            final View first = parent.getChildAt(0);
            final View second = parent.getChildAt(1);
            final View up = first.getId() == R.id.home ? second : first;

            if (up instanceof ImageView) {
                // Jackpot! (Probably...)
                upIndicatorView = (ImageView) up;
            }
        }
    }
}
