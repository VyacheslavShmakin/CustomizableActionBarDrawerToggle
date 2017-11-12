/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (C) 2017 Vyacheslav Shmakin
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

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

/**
 * This class provides a handy way to tie together the functionality of
 * {@link android.support.v4.widget.DrawerLayout} and the framework <code>ActionBar</code> to
 * implement the recommended design for navigation drawers.
 * <p>
 * <p>To use <code>ActionBarDrawerToggle</code>, create one in your Activity and call through
 * to the following methods corresponding to your Activity callbacks:</p>
 * <p>
 * <ul>
 * <li>{@link android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
 * onConfigurationChanged}
 * <li>{@link android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 * onOptionsItemSelected}</li>
 * </ul>
 * <p>
 * <p>Call {@link #syncState(Bundle savedInstanceState)} from your <code>Activity</code>'s
 * {@link android.app.Activity#onPostCreate(android.os.Bundle) onPostCreate} to synchronize the
 * indicator with the state of the linked DrawerLayout after <code>onRestoreInstanceState</code>
 * has occurred.</p>
 * <p>
 * <p><code>ActionBarDrawerToggle</code> can be used directly as a
 * {@link android.support.v4.widget.DrawerLayout.DrawerListener}, or if you are already providing
 * your own listener, call through to each of the listener methods from your own.</p>
 * <p>
 * <p>
 * You can customize the the animated toggle by defining the
 * {android.support.v7.appcompat.R.styleable#DrawerArrowToggle drawerArrowStyle} in your
 * ActionBar theme.
 */
public class ActionBarDrawerToggle implements DrawerLayout.DrawerListener {

    /**
     * Allows an implementing Activity to return an {@link ActionBarDrawerToggle.Delegate} to use
     * with ActionBarDrawerToggle.
     */
    public interface DelegateProvider {

        /**
         * @return Delegate to use for ActionBarDrawableToggles, or null if the Activity
         * does not wish to override the default behavior.
         */
        @Nullable
        Delegate getDrawerToggleDelegate();
    }

    public interface Delegate {

        /**
         * Set the Action Bar's up indicator drawable and content description.
         *
         * @param upDrawable     - Drawable to set as up indicator
         * @param contentDescRes - Content description to set
         */
        void setActionBarUpIndicator(Drawable upDrawable, @StringRes int contentDescRes);

        /**
         * Set the Action Bar's up indicator content description.
         *
         * @param contentDescRes - Content description to set
         */
        void setActionBarDescription(@StringRes int contentDescRes);

        /**
         * Returns the drawable to be set as up button when DrawerToggle is disabled
         */
        Drawable getThemeUpIndicator();

        /**
         * Returns the context of ActionBar
         */
        Context getActionBarThemedContext();

        /**
         * Returns whether navigation icon is visible or not.
         * Used to print warning messages in case developer forgets to set displayHomeAsUp to true
         */
        boolean isNavigationVisible();
    }

    private static final String KEY_DRAWER_POSITION = "LastDrawerPositionInstance";
    private static final String KEY_INTERPOLATOR_DURATION = "InterpolatorDurationInstance";
    private static final String KEY_ROTATE_DIRECTION = "RotateDirectionBackInstance";
    private static final String KEY_AUTO_RESET_ROTATE_DIRECTION = "AutoResetRotateDirectionInstance";
    private static final String TAG = "ActionBarDrawerToggle";
    private static final String MESSAGE_NAV_ICON_NOT_VISIBLE = "DrawerToggle may not show up " +
            "because NavigationIcon is not visible. You may need to call actionbar.setDisplayHomeAsUpEnabled(true);";

    private static final float SLIDER_START_POSITION = 0.0F;
    private static final float SLIDER_END_POSITION = 1.0F;

    private final Delegate mActivityImpl;
    private final DrawerLayout mDrawerLayout;

    private DrawerArrowDrawableCompat mSlider;
    private Drawable mHomeAsUpIndicator;
    private boolean mDrawerIndicatorEnabled = true;
    private boolean mDrawerSlideAnimationEnabled = true;
    private boolean mHasCustomUpIndicator;

    @StringRes
    private final int mOpenDrawerContentDescRes;
    @StringRes
    private final int mCloseDrawerContentDescRes;

    // used in toolbar mode when DrawerToggle is disabled
    private ToolbarNavigationClickListener mToolbarNavigationClickListener;

    // If developer does not set displayHomeAsUp, DrawerToggle won't show up.
    // DrawerToggle logs a warning if this case is detected
    private boolean mWarnedForDisplayHomeAsUp = false;

    private TimeInterpolator mInterpolator = new MaterialInterpolator();
    private int mInterpolatorDuration = 400;
    private boolean mRotateDirectionBack = false;
    private boolean mAutoResetRotateDirection = true;

    /**
     * Construct a new ActionBarDrawerToggle.
     * <p>
     * <p>The given {@link Activity} will be linked to the specified {@link DrawerLayout} and
     * its Actionbar's Up button will be set to a custom drawable.
     * <p>This drawable shows a Hamburger icon when drawer is closed and an arrow when drawer
     * is open. It animates between these two states as the drawer opens.</p>
     * <p>
     * <p>String resources must be provided to describe the open/close drawer actions for
     * accessibility services.</p>
     *
     * @param activity                  The Activity hosting the drawer. Should have an ActionBar.
     * @param drawerLayout              The DrawerLayout to link to the given Activity's ActionBar
     * @param openDrawerContentDescRes  A String resource to describe the "open drawer" action
     *                                  for accessibility
     * @param closeDrawerContentDescRes A String resource to describe the "close drawer" action
     *                                  for accessibility
     */
    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout,
                                 @StringRes int openDrawerContentDescRes,
                                 @StringRes int closeDrawerContentDescRes) {
        this(activity, null, drawerLayout, null, openDrawerContentDescRes,
                closeDrawerContentDescRes);
    }

    /**
     * Construct a new ActionBarDrawerToggle with a Toolbar.
     * <p>
     * The given {@link Activity} will be linked to the specified {@link DrawerLayout} and
     * the Toolbar's navigation icon will be set to a custom drawable. Using this constructor
     * will set Toolbar's navigation click listener to toggle the drawer when it is clicked.
     * <p>
     * This drawable shows a Hamburger icon when drawer is closed and an arrow when drawer
     * is open. It animates between these two states as the drawer opens.
     * <p>
     * String resources must be provided to describe the open/close drawer actions for
     * accessibility services.
     * <p>
     * Please use {@link #ActionBarDrawerToggle(Activity, DrawerLayout, int, int)} if you are
     * setting the Toolbar as the ActionBar of your activity.
     *
     * @param activity                  The Activity hosting the drawer.
     * @param toolbar                   The toolbar to use if you have an independent Toolbar.
     * @param drawerLayout              The DrawerLayout to link to the given Activity's ActionBar
     * @param openDrawerContentDescRes  A String resource to describe the "open drawer" action
     *                                  for accessibility
     * @param closeDrawerContentDescRes A String resource to describe the "close drawer" action
     *                                  for accessibility
     */
    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout,
                                 Toolbar toolbar, @StringRes int openDrawerContentDescRes,
                                 @StringRes int closeDrawerContentDescRes) {
        this(activity, toolbar, drawerLayout, null, openDrawerContentDescRes,
                closeDrawerContentDescRes);
    }

    /**
     * In the future, we can make this constructor public if we want to let developers customize
     * the
     * animation.
     */
    public ActionBarDrawerToggle(Activity activity, Toolbar toolbar, DrawerLayout drawerLayout,
                                 DrawerArrowDrawableCompat slider, @StringRes int openDrawerContentDescRes,
                                 @StringRes int closeDrawerContentDescRes) {
        if (toolbar != null) {
            mActivityImpl = new ToolbarCompatDelegate(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawerIndicatorEnabled) {
                        toggle();
                    } else if (mToolbarNavigationClickListener != null) {
                        mToolbarNavigationClickListener.onClick(v);
                    }
                }
            });
        } else if (activity instanceof DelegateProvider) { // Allow the Activity to provide an impl
            mActivityImpl = ((DelegateProvider) activity).getDrawerToggleDelegate();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mActivityImpl = new JellybeanMr2Delegate(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mActivityImpl = new IcsDelegate(activity);
        } else {
            mActivityImpl = new DummyDelegate(activity);
        }

        mDrawerLayout = drawerLayout;
        mOpenDrawerContentDescRes = openDrawerContentDescRes;
        mCloseDrawerContentDescRes = closeDrawerContentDescRes;
        if (slider == null && mActivityImpl != null) {
            mSlider = new DrawerArrowDrawableCompat(mActivityImpl.getActionBarThemedContext());
        } else {
            mSlider = slider;
        }

        mHomeAsUpIndicator = getThemeUpIndicator();
    }

    /**
     * Synchronize the state of the drawer indicator/affordance with the linked DrawerLayout.
     * <p>
     * <p>This should be called from your <code>Activity</code>'s
     * {@link Activity#onPostCreate(android.os.Bundle) onPostCreate} method to synchronize after
     * the DrawerLayout's instance state has been restored, and any other time when the state
     * may have diverged in such a way that the ActionBarDrawerToggle was not notified.
     * (For example, if you stop forwarding appropriate drawer events for a period of time.)</p>
     */
    public void syncState(@Nullable Bundle savedInstanceState) {
        float position = SLIDER_START_POSITION;
        if (savedInstanceState != null) {
            position = (float) savedInstanceState.getInt(KEY_DRAWER_POSITION);
            mInterpolatorDuration = savedInstanceState.getInt(KEY_INTERPOLATOR_DURATION);
            mRotateDirectionBack = savedInstanceState.getBoolean(KEY_ROTATE_DIRECTION);
            mAutoResetRotateDirection = savedInstanceState.getBoolean(KEY_AUTO_RESET_ROTATE_DIRECTION);
        }

        this.mSlider.setPosition(position, DrawerImageState.TOGGLE_DRAWER_DEFAULT);

        if (this.mDrawerIndicatorEnabled) {
            this.setActionBarUpIndicator(mSlider,
                    mDrawerLayout.isDrawerOpen(GravityCompat.START) ?
                            mCloseDrawerContentDescRes : mOpenDrawerContentDescRes);
        }
    }

    public void onSavedInstanceState(Bundle bundle) {
        bundle.putInt(KEY_DRAWER_POSITION, Math.round(mSlider.getPosition()));
        bundle.putInt(KEY_INTERPOLATOR_DURATION, mInterpolatorDuration);
        bundle.putBoolean(KEY_ROTATE_DIRECTION, mRotateDirectionBack);
        bundle.putBoolean(KEY_AUTO_RESET_ROTATE_DIRECTION, mAutoResetRotateDirection);
    }

    /**
     * This method should always be called by your <code>Activity</code>'s
     * {@link Activity#onConfigurationChanged(android.content.res.Configuration)
     * onConfigurationChanged}
     * method.
     *
     * @param newConfig The new configuration
     */
    public void onConfigurationChanged(Configuration newConfig) {
        // Reload drawables that can change with configuration
        if (!mHasCustomUpIndicator) {
            mHomeAsUpIndicator = getThemeUpIndicator();
        }
        syncState(null);
    }

    /**
     * This method should be called by your <code>Activity</code>'s
     * {@link Activity#onOptionsItemSelected(android.view.MenuItem) onOptionsItemSelected} method.
     * If it returns true, your <code>onOptionsItemSelected</code> method should return true and
     * skip further processing.
     *
     * @param item the MenuItem instance representing the selected menu item
     * @return true if the event was handled and further processing should not occur
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home && mDrawerIndicatorEnabled) {
            toggle();
            return true;
        }
        return false;
    }

    private void toggle() {
        int drawerLockMode = mDrawerLayout.getDrawerLockMode(GravityCompat.START);
        if (mDrawerLayout.isDrawerVisible(GravityCompat.START)
                && (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_OPEN)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public boolean isRotateDirectionBack() {
        return mRotateDirectionBack;
    }

    public void setRotateDirectionBack(boolean rotateDirectionBack) {
        this.mRotateDirectionBack = rotateDirectionBack;
    }

    public boolean isAutoResetRotateDirection() {
        return mAutoResetRotateDirection;
    }

    public void setAutoResetRotateDirection(boolean autoResetRotateDirection) {
        this.mAutoResetRotateDirection = autoResetRotateDirection;
    }

    public TimeInterpolator getInterpolator() {
        return this.mInterpolator;
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public int getInterpolatorDuration() {
        return mInterpolatorDuration;
    }

    public void setInterpolatorDuration(int interpolatorDuration) {
        this.mInterpolatorDuration = interpolatorDuration;
    }

    /**
     * Set the up indicator to display when the drawer indicator is not
     * enabled.
     * <p>
     * If you pass <code>null</code> to this method, the default drawable from
     * the theme will be used.
     *
     * @param indicator A drawable to use for the up indicator, or null to use
     *                  the theme's default
     * @see #setDrawerIndicatorEnabled(boolean)
     */
    public void setHomeAsUpIndicator(Drawable indicator) {
        if (indicator == null) {
            mHomeAsUpIndicator = getThemeUpIndicator();
            mHasCustomUpIndicator = false;
        } else {
            mHomeAsUpIndicator = indicator;
            mHasCustomUpIndicator = true;
        }

        if (!mDrawerIndicatorEnabled) {
            setActionBarUpIndicator(mHomeAsUpIndicator, 0);
        }
    }

    /**
     * Set the up indicator to display when the drawer indicator is not
     * enabled.
     * <p>
     * If you pass 0 to this method, the default drawable from the theme will
     * be used.
     *
     * @param resId Resource ID of a drawable to use for the up indicator, or 0
     *              to use the theme's default
     * @see #setDrawerIndicatorEnabled(boolean)
     */
    public void setHomeAsUpIndicator(int resId) {
        Drawable indicator = null;
        if (resId != 0) {
            indicator = mDrawerLayout.getResources().getDrawable(resId);
        }
        setHomeAsUpIndicator(indicator);
    }

    /**
     * @return true if the enhanced drawer indicator is enabled, false otherwise
     * @see #setDrawerIndicatorEnabled(boolean)
     */
    public boolean isDrawerIndicatorEnabled() {
        return mDrawerIndicatorEnabled;
    }

    /**
     * Enable or disable the drawer indicator. The indicator defaults to enabled.
     * <p>
     * <p>When the indicator is disabled, the <code>ActionBar</code> will revert to displaying
     * the home-as-up indicator provided by the <code>Activity</code>'s theme in the
     * <code>android.R.attr.homeAsUpIndicator</code> attribute instead of the animated
     * drawer glyph.</p>
     *
     * @param enable true to enable, false to disable
     */
    public void setDrawerIndicatorEnabled(boolean enable) {
        if (enable != mDrawerIndicatorEnabled) {
            if (enable) {
                setActionBarUpIndicator(
                        mSlider,
                        mDrawerLayout.isDrawerOpen(GravityCompat.START)
                                ? mCloseDrawerContentDescRes
                                : mOpenDrawerContentDescRes);
            } else {
                setActionBarUpIndicator(mHomeAsUpIndicator, 0);
            }
            mDrawerIndicatorEnabled = enable;
        }
    }

    /**
     * @return DrawerArrowDrawable that is currently shown by the ActionBarDrawerToggle.
     */
    @NonNull
    public DrawerArrowDrawableCompat getDrawerArrowDrawable() {
        return mSlider;
    }

    /**
     * Sets the DrawerArrowDrawableCompat that should be shown by this ActionBarDrawerToggle.
     *
     * @param drawable DrawerArrowDrawableCompat that should be shown by this ActionBarDrawerToggle
     */
    public void setDrawerArrowDrawable(@NonNull DrawerArrowDrawableCompat drawable) {
        mSlider = drawable;
        syncState(null);
    }

    public void toggleIndicator(final boolean backDirection) {
        this.mRotateDirectionBack = backDirection;
        this.mAutoResetRotateDirection = true;

        float position = Math.round(this.mSlider.getPosition());
        float start, end;
        if (position == SLIDER_START_POSITION) {
            start = SLIDER_START_POSITION;
            end = SLIDER_END_POSITION;
        } else {
            start = SLIDER_END_POSITION;
            end = SLIDER_START_POSITION;
        }

        animateDrawer(start, end);
    }

    public void animateDrawer(final float start, final float end) {

        final ValueAnimator anim = ValueAnimator.ofFloat(start, end);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                setPosition(slideOffset);
                if (slideOffset == end) {
                    anim.removeAllListeners();
                }
            }
        });
        anim.setInterpolator(mInterpolator);
        // You can change this duration to more closely match that of the default animation.
        anim.setDuration(mInterpolatorDuration);
        anim.start();
    }

    /**
     * Specifies whether the drawer arrow should animate when the drawer position changes.
     *
     * @param enabled if this is {@code true} then the animation will run, else it will be skipped
     */
    public void setDrawerSlideAnimationEnabled(boolean enabled) {
        mDrawerSlideAnimationEnabled = enabled;
        if (!enabled) {
            setPosition(SLIDER_START_POSITION);
        }
    }

    /**
     * @return whether the drawer slide animation is enabled
     */
    public boolean isDrawerSlideAnimationEnabled() {
        return mDrawerSlideAnimationEnabled;
    }

    /**
     * {@link DrawerLayout.DrawerListener} callback method. If you do not use your
     * ActionBarDrawerToggle instance directly as your DrawerLayout's listener, you should call
     * through to this method from your own listener object.
     *
     * @param drawerView  The child view that was moved
     * @param slideOffset The new offset of this drawer within its range, from 0-1
     */
    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        setPosition(mDrawerSlideAnimationEnabled ? slideOffset : SLIDER_START_POSITION);
    }

    /**
     * SLIDER_END_POSITION, DrawerImageState.TOGGLE_DRAWER_DEFAULT
     * {@link DrawerLayout.DrawerListener} callback method. If you do not use your
     * ActionBarDrawerToggle instance directly as your DrawerLayout's listener, you should call
     * through to this method from your own listener object.
     *
     * @param drawerView Drawer view that is now open
     */
    @Override
    public void onDrawerOpened(View drawerView) {
        setPosition(SLIDER_END_POSITION);
        if (mDrawerIndicatorEnabled) {
            setActionBarDescription(mCloseDrawerContentDescRes);
        }
    }

    /**
     * {@link DrawerLayout.DrawerListener} callback method. If you do not use your
     * ActionBarDrawerToggle instance directly as your DrawerLayout's listener, you should call
     * through to this method from your own listener object.
     *
     * @param drawerView Drawer view that is now closed
     */
    @Override
    public void onDrawerClosed(View drawerView) {
        setPosition(SLIDER_START_POSITION);
        if (mDrawerIndicatorEnabled) {
            setActionBarDescription(mOpenDrawerContentDescRes);
        }
    }

    /**
     * {@link DrawerLayout.DrawerListener} callback method. If you do not use your
     * ActionBarDrawerToggle instance directly as your DrawerLayout's listener, you should call
     * through to this method from your own listener object.
     *
     * @param newState The new drawer motion state
     */
    @Override
    public void onDrawerStateChanged(int newState) {
    }

    /**
     * Returns the fallback listener for Navigation icon click events.
     *
     * @return The click listener which receives Navigation click events from Toolbar when
     * drawer indicator is disabled.
     * @see #setToolbarNavigationClickListener(ToolbarNavigationClickListener)
     * @see #setDrawerIndicatorEnabled(boolean)
     * @see #isDrawerIndicatorEnabled()
     */
    public ToolbarNavigationClickListener getToolbarNavigationClickListener() {
        return mToolbarNavigationClickListener;
    }

    /**
     * When DrawerToggle is constructed with a Toolbar, it sets the click listener on
     * the Navigation icon. If you want to listen for clicks on the Navigation icon when
     * DrawerToggle is disabled ({@link #setDrawerIndicatorEnabled(boolean)}, you should call this
     * method with your listener and DrawerToggle will forward click events to that listener
     * when drawer indicator is disabled.
     *
     * @see #setDrawerIndicatorEnabled(boolean)
     */
    public void setToolbarNavigationClickListener(ToolbarNavigationClickListener onClickListener) {
        mToolbarNavigationClickListener = onClickListener;
    }

    void setActionBarUpIndicator(Drawable upDrawable, int contentDescRes) {
        if (!mWarnedForDisplayHomeAsUp && !mActivityImpl.isNavigationVisible()) {
            Log.w(TAG, MESSAGE_NAV_ICON_NOT_VISIBLE);
            mWarnedForDisplayHomeAsUp = true;
        }
        mActivityImpl.setActionBarUpIndicator(upDrawable, contentDescRes);
    }

    void setActionBarDescription(int contentDescRes) {
        mActivityImpl.setActionBarDescription(contentDescRes);
    }

    Drawable getThemeUpIndicator() {
        return mActivityImpl.getThemeUpIndicator();
    }

    private void setPosition(float slideOffset) {
        float position = Math.min(SLIDER_END_POSITION, Math.max(SLIDER_START_POSITION, slideOffset));
        int mirrored = DrawerImageState.TOGGLE_DRAWER_DEFAULT;

        if (mRotateDirectionBack) {
            mirrored = DrawerImageState.TOGGLE_DRAWER_MIRRORED;
        } else {
            if (position == SLIDER_START_POSITION) {
                mirrored = DrawerImageState.TOGGLE_DRAWER_NORMAL;
            } else if (position == SLIDER_END_POSITION) {
                mirrored = DrawerImageState.TOGGLE_DRAWER_MIRRORED;
            }
        }

        this.mSlider.setPosition(position, mirrored);
        if (mAutoResetRotateDirection && mRotateDirectionBack && position == SLIDER_END_POSITION) {
            mRotateDirectionBack = !mRotateDirectionBack;
        }
    }

    /**
     * Delegate if SDK version is between ICS and JBMR2
     */
    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static class IcsDelegate implements Delegate {

        final Activity mActivity;
        ActionBarDrawerToggleHoneycomb.SetIndicatorInfo mSetIndicatorInfo;

        IcsDelegate(Activity activity) {
            mActivity = activity;
        }

        @Override
        public Drawable getThemeUpIndicator() {
            return ActionBarDrawerToggleHoneycomb.getThemeUpIndicator(mActivity);
        }

        @Override
        public Context getActionBarThemedContext() {
            final ActionBar actionBar = mActivity.getActionBar();
            final Context context;
            if (actionBar != null) {
                context = actionBar.getThemedContext();
            } else {
                context = mActivity;
            }
            return context;
        }

        @Override
        public boolean isNavigationVisible() {
            final ActionBar actionBar = mActivity.getActionBar();
            return actionBar != null
                    && (actionBar.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) != 0;
        }

        @Override
        public void setActionBarUpIndicator(Drawable themeImage, int contentDescRes) {
            final ActionBar actionBar = mActivity.getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowHomeEnabled(true);
                mSetIndicatorInfo = ActionBarDrawerToggleHoneycomb.setActionBarUpIndicator(
                        mSetIndicatorInfo,
                        mActivity,
                        themeImage,
                        contentDescRes);

                actionBar.setDisplayShowHomeEnabled(false);
            }
        }

        @Override
        public void setActionBarDescription(int contentDescRes) {
            mSetIndicatorInfo = ActionBarDrawerToggleHoneycomb.setActionBarDescription(
                    mSetIndicatorInfo, mActivity, contentDescRes);
        }
    }

    /**
     * Delegate if SDK version is JB MR2 or newer
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static class JellybeanMr2Delegate implements Delegate {

        final Activity mActivity;

        JellybeanMr2Delegate(Activity activity) {
            mActivity = activity;
        }

        @Override
        public Drawable getThemeUpIndicator() {
            final TypedArray a = getActionBarThemedContext().obtainStyledAttributes(null,
                    new int[]{android.R.attr.homeAsUpIndicator}, android.R.attr.actionBarStyle, 0);
            final Drawable result = a.getDrawable(0);
            a.recycle();
            return result;
        }

        @Override
        public Context getActionBarThemedContext() {
            final ActionBar actionBar = mActivity.getActionBar();
            final Context context;
            if (actionBar != null) {
                context = actionBar.getThemedContext();
            } else {
                context = mActivity;
            }
            return context;
        }

        @Override
        public boolean isNavigationVisible() {
            final ActionBar actionBar = mActivity.getActionBar();
            return actionBar != null &&
                    (actionBar.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) != 0;
        }

        @Override
        public void setActionBarUpIndicator(Drawable drawable, int contentDescRes) {
            final ActionBar actionBar = mActivity.getActionBar();
            if (actionBar != null) {
                actionBar.setHomeAsUpIndicator(drawable);
                actionBar.setHomeActionContentDescription(contentDescRes);
            }
        }

        @Override
        public void setActionBarDescription(int contentDescRes) {
            final ActionBar actionBar = mActivity.getActionBar();
            if (actionBar != null) {
                actionBar.setHomeActionContentDescription(contentDescRes);
            }
        }
    }

    /**
     * Used when DrawerToggle is initialized with a Toolbar
     */
    static class ToolbarCompatDelegate implements Delegate {

        final Toolbar mToolbar;
        final Drawable mDefaultUpIndicator;
        final CharSequence mDefaultContentDescription;

        ToolbarCompatDelegate(Toolbar toolbar) {
            mToolbar = toolbar;
            mDefaultUpIndicator = toolbar.getNavigationIcon();
            mDefaultContentDescription = toolbar.getNavigationContentDescription();
        }

        @Override
        public void setActionBarUpIndicator(Drawable upDrawable, @StringRes int contentDescRes) {
            mToolbar.setNavigationIcon(upDrawable);
            setActionBarDescription(contentDescRes);
        }

        @Override
        public void setActionBarDescription(@StringRes int contentDescRes) {
            if (contentDescRes == 0) {
                mToolbar.setNavigationContentDescription(mDefaultContentDescription);
            } else {
                mToolbar.setNavigationContentDescription(contentDescRes);
            }
        }

        @Override
        public Drawable getThemeUpIndicator() {
            return mDefaultUpIndicator;
        }

        @Override
        public Context getActionBarThemedContext() {
            return mToolbar.getContext();
        }

        @Override
        public boolean isNavigationVisible() {
            return true;
        }
    }

    /**
     * Fallback delegate
     */
    static class DummyDelegate implements Delegate {
        final Activity mActivity;

        DummyDelegate(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void setActionBarUpIndicator(Drawable upDrawable, @StringRes int contentDescRes) {

        }

        @Override
        public void setActionBarDescription(@StringRes int contentDescRes) {

        }

        @Override
        public Drawable getThemeUpIndicator() {
            return null;
        }

        @Override
        public Context getActionBarThemedContext() {
            return mActivity;
        }

        @Override
        public boolean isNavigationVisible() {
            return true;
        }
    }
}
