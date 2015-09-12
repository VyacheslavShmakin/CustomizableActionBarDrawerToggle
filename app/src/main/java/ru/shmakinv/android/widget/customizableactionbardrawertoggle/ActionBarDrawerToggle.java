//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

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
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * ActionBarDrawerToggle
 *
 * @author: VyacheslavShmakin
 * @version: 09.04.2015
 */
public class ActionBarDrawerToggle implements DrawerListener {

    private static final String DRAWER_POSITION_INSTANCE = "LastDrawerPositionInstance";
    private static final String INTERPOLATOR_DURATION_INSTANCE = "InterpolatorDurationInstance";
    private static final String ROTATE_DIRECTION_BACK_INSTANCE = "RotateDirectionBackInstance";
    private static final String AUTO_RESET_ROTATE_DIRECTION_INSTANCE
            = "AutoResetRotateDirectionInstance";
    private static final String TAG = "ActionBarDrawerToggle";
    private static final String MESSAGE_NAV_ICON_NOT_VISIBLE = "DrawerToggle may not show up " +
            "because NavigationIcon is not visible. You may need to call actionbar " +
            ".setDisplayHomeAsUpEnabled(true);";

    private static final int[] THEME_UP_INDICATOR_ATTR = {16843531};
    private static final int THEME_UP_INDICATOR_DEF_STYLE_ATTR = 16843470;
    private static final int DRAWER_GRAVITY = 8388611;
    private static final int MENU_ITEM_ID = 16908332;

    private final Delegate mActivityImpl;
    private final DrawerLayout mDrawerLayout;
    private final float mSliderStartPosition = 0.0F;
    private final float mSliderEndPosition = 1.0F;
    private final int mOpenDrawerContentDescRes;
    private final int mCloseDrawerContentDescRes;

    private DrawerToggle mSlider;
    private OnClickListener mToolbarNavigationClickListener;
    private Drawable mHomeAsUpIndicator;
    private boolean mDrawerIndicatorEnabled;
    private boolean mHasCustomUpIndicator;
    private boolean mWarnedForDisplayHomeAsUp;

    private TimeInterpolator interpolator = new MaterialInterpolator();
    private int interpolatorDuration = 400;
    private boolean rotateDirectionBack = false;
    private boolean autoResetRotateDirection = true;

    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout,
                                 int openDrawerContentDescRes, int closeDrawerContentDescRes) {

        this(activity, null, drawerLayout, null,
                openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar,
                                 int openDrawerContentDescRes, int closeDrawerContentDescRes) {

        this(activity, toolbar, drawerLayout, null,
                openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    private <T extends Drawable & DrawerToggle> ActionBarDrawerToggle(
            Activity activity, Toolbar toolbar, DrawerLayout drawerLayout, T slider,
            int openDrawerContentDescRes, int closeDrawerContentDescRes) {

        this.mDrawerIndicatorEnabled = true;
        this.mWarnedForDisplayHomeAsUp = false;

        if (toolbar != null) {
            this.mActivityImpl = new ToolbarCompatDelegate(toolbar);
            toolbar.setNavigationOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (ActionBarDrawerToggle.this.mDrawerIndicatorEnabled) {
                        ActionBarDrawerToggle.this.toggle();
                    } else if (ActionBarDrawerToggle.this.mToolbarNavigationClickListener != null) {
                        ActionBarDrawerToggle.this.mToolbarNavigationClickListener.onClick(v);
                    }
                }
            });
        } else if (activity instanceof DelegateProvider) {
            this.mActivityImpl
                    = ((DelegateProvider) activity).getDrawerToggleDelegate();

        } else if (activity instanceof TmpDelegateProvider) {
            this.mActivityImpl
                    = ((TmpDelegateProvider) activity).getV7DrawerToggleDelegate();

        } else if (VERSION.SDK_INT >= 18) {
            this.mActivityImpl = new JellybeanMr2Delegate(activity);
        } else if (VERSION.SDK_INT >= 11) {
            this.mActivityImpl = new HoneycombDelegate(activity);
        } else {
            this.mActivityImpl = new DummyDelegate(activity);
        }

        if (drawerLayout == null) {
            drawerLayout = new DrawerLayout(activity);
        }

        this.mDrawerLayout = drawerLayout;
        this.mOpenDrawerContentDescRes = openDrawerContentDescRes;
        this.mCloseDrawerContentDescRes = closeDrawerContentDescRes;
        if (slider == null) {
            if (this.mActivityImpl != null) {
                this.mSlider = new DrawerArrowDrawableToggle(
                        activity, this.mActivityImpl.getActionBarThemedContext());
            }
        } else {
            this.mSlider = slider;
        }

        this.mHomeAsUpIndicator = this.getThemeUpIndicator();
    }

    public void syncState(Bundle savedInstanceState) {
        float position = mSliderStartPosition;
        if (savedInstanceState != null) {
            position = (float) savedInstanceState.getInt(DRAWER_POSITION_INSTANCE);
            interpolatorDuration = savedInstanceState.getInt(INTERPOLATOR_DURATION_INSTANCE);
            rotateDirectionBack = savedInstanceState.getBoolean(ROTATE_DIRECTION_BACK_INSTANCE);
            autoResetRotateDirection
                    = savedInstanceState.getBoolean(AUTO_RESET_ROTATE_DIRECTION_INSTANCE);
        }

        this.mSlider.setPosition(position, DrawerImageState.TOGGLE_DRAWER_DEFAULT);

        if (this.mDrawerIndicatorEnabled) {
            this.setActionBarUpIndicator((Drawable) this.mSlider, getContentDescRes());
        }
    }

    public void onSavedInstanceState(Bundle bundle) {
        bundle.putInt(DRAWER_POSITION_INSTANCE, Math.round(this.mSlider.getPosition()));
        bundle.putInt(INTERPOLATOR_DURATION_INSTANCE, interpolatorDuration);
        bundle.putBoolean(ROTATE_DIRECTION_BACK_INSTANCE, rotateDirectionBack);
        bundle.putBoolean(AUTO_RESET_ROTATE_DIRECTION_INSTANCE, autoResetRotateDirection);
    }

    private int getContentDescRes() {
        if (this.mDrawerLayout.isDrawerOpen(DRAWER_GRAVITY)) {
            return this.mCloseDrawerContentDescRes;
        } else {
            return this.mOpenDrawerContentDescRes;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (!this.mHasCustomUpIndicator) {
            this.mHomeAsUpIndicator = this.getThemeUpIndicator();
        }

        this.syncState(null);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == MENU_ITEM_ID && this.mDrawerIndicatorEnabled) {
            this.toggle();
            return true;
        } else {
            return false;
        }
    }

    private void toggle() {
        if (this.mDrawerLayout.isDrawerVisible(DRAWER_GRAVITY)) {
            this.mDrawerLayout.closeDrawer(DRAWER_GRAVITY);
        } else {
            this.mDrawerLayout.openDrawer(DRAWER_GRAVITY);
        }
    }

    public boolean isRotateDirectionBack() {
        return rotateDirectionBack;
    }

    public void setRotateDirectionBack(boolean rotateDirectionBack) {
        this.rotateDirectionBack = rotateDirectionBack;
    }

    public boolean isAutoResetRotateDirection() {
        return autoResetRotateDirection;
    }

    public void setAutoResetRotateDirection(boolean autoResetRotateDirection) {
        this.autoResetRotateDirection = autoResetRotateDirection;
    }

    public TimeInterpolator getInterpolator() {
        return interpolator;
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        this.interpolator = interpolator;
    }

    public int getInterpolatorDuration() {
        return interpolatorDuration;
    }

    public void setInterpolatorDuration(int interpolatorDuration) {
        this.interpolatorDuration = interpolatorDuration;
    }

    public void setHomeAsUpIndicator(Drawable indicator) {
        if (indicator == null) {
            this.mHomeAsUpIndicator = this.getThemeUpIndicator();
            this.mHasCustomUpIndicator = false;
        } else {
            this.mHomeAsUpIndicator = indicator;
            this.mHasCustomUpIndicator = true;
        }

        if (!this.mDrawerIndicatorEnabled) {
            this.setActionBarUpIndicator(this.mHomeAsUpIndicator, 0);
        }

    }

    public void setHomeAsUpIndicator(int resId) {
        Drawable indicator = null;
        if (resId != 0) {
            if (VERSION.SDK_INT > 20) {
                indicator = this.mDrawerLayout.getResources().getDrawable(resId, null);
            } else {
                indicator = this.mDrawerLayout.getResources().getDrawable(resId);
            }
        }

        this.setHomeAsUpIndicator(indicator);
    }

    public boolean isDrawerIndicatorEnabled() {
        return this.mDrawerIndicatorEnabled;
    }

    public void setDrawerIndicatorEnabled(boolean enable) {
        if (enable != this.mDrawerIndicatorEnabled) {
            if (enable) {
                this.setActionBarUpIndicator((Drawable) this.mSlider, getContentDescRes());
            } else {
                this.setActionBarUpIndicator(this.mHomeAsUpIndicator, 0);
            }

            this.mDrawerIndicatorEnabled = enable;
        }
    }

    public void toggleIndicator(final boolean backDirection) {
        this.rotateDirectionBack = backDirection;
        this.autoResetRotateDirection = true;

        float position = Math.round(this.mSlider.getPosition());
        float start, end;
        if (position == mSliderStartPosition) {
            start = mSliderStartPosition;
            end = mSliderEndPosition;
        } else {
            start = mSliderEndPosition;
            end = mSliderStartPosition;
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
        anim.setInterpolator(interpolator);
        // You can change this duration to more closely match that of the default animation.
        anim.setDuration(interpolatorDuration);
        anim.start();
    }

    public void onDrawerSlide(View drawerView, float slideOffset) {
        setPosition(slideOffset);
    }

    private void setPosition(float slideOffset) {
        float position = Math.min(mSliderEndPosition, Math.max(mSliderStartPosition, slideOffset));
        int mirrored = DrawerImageState.TOGGLE_DRAWER_DEFAULT;

        if (rotateDirectionBack) {
            mirrored = DrawerImageState.TOGGLE_DRAWER_MIRRORED;
        } else {
            if (position == mSliderStartPosition) {
                mirrored = DrawerImageState.TOGGLE_DRAWER_NORMAL;
            } else if (position == mSliderEndPosition) {
                mirrored = DrawerImageState.TOGGLE_DRAWER_MIRRORED;
            }
        }

        this.mSlider.setPosition(position, mirrored);
        if (autoResetRotateDirection && rotateDirectionBack && position == mSliderEndPosition) {
            rotateDirectionBack = !rotateDirectionBack;
        }
    }

    public void onDrawerOpened(View drawerView) {
        this.mSlider.setPosition(mSliderEndPosition, DrawerImageState.TOGGLE_DRAWER_DEFAULT);
        if (this.mDrawerIndicatorEnabled) {
            this.setActionBarDescription(this.mCloseDrawerContentDescRes);
        }
    }

    public void onDrawerClosed(View drawerView) {
        this.mSlider.setPosition(mSliderStartPosition, DrawerImageState.TOGGLE_DRAWER_DEFAULT);
        if (this.mDrawerIndicatorEnabled) {
            this.setActionBarDescription(this.mOpenDrawerContentDescRes);
        }
    }

    public void onDrawerStateChanged(int newState) {
    }

    public OnClickListener getToolbarNavigationClickListener() {
        return this.mToolbarNavigationClickListener;
    }

    public void setToolbarNavigationClickListener(OnClickListener onToolbarNavigationClickListener) {
        this.mToolbarNavigationClickListener = onToolbarNavigationClickListener;
    }

    void setActionBarUpIndicator(Drawable upDrawable, int contentDescRes) {
        if (!this.mWarnedForDisplayHomeAsUp && !this.mActivityImpl.isNavigationVisible()) {
            Log.w(TAG, MESSAGE_NAV_ICON_NOT_VISIBLE);
            this.mWarnedForDisplayHomeAsUp = true;
        }

        this.mActivityImpl.setActionBarUpIndicator(upDrawable, contentDescRes);
    }

    void setActionBarDescription(int contentDescRes) {
        this.mActivityImpl.setActionBarDescription(contentDescRes);
    }

    Drawable getThemeUpIndicator() {
        return this.mActivityImpl.getThemeUpIndicator();
    }

    interface DrawerToggle {
        void setPosition(float position, int toggleImageState);

        float getPosition();
    }

    public interface Delegate {
        void setActionBarUpIndicator(Drawable drawable, int resId);

        void setActionBarDescription(int resId);

        Drawable getThemeUpIndicator();

        Context getActionBarThemedContext();

        boolean isNavigationVisible();
    }

    /**
     * @deprecated
     */
    interface TmpDelegateProvider {
        @Nullable
        Delegate getV7DrawerToggleDelegate();
    }

    public interface DelegateProvider {
        @Nullable
        Delegate getDrawerToggleDelegate();
    }

    static class DummyDelegate implements Delegate {
        final Activity mActivity;

        DummyDelegate(Activity activity) {
            this.mActivity = activity;
        }

        public void setActionBarUpIndicator(Drawable upDrawable, int contentDescRes) {
        }

        public void setActionBarDescription(int contentDescRes) {
        }

        public Drawable getThemeUpIndicator() {
            return null;
        }

        public Context getActionBarThemedContext() {
            return this.mActivity;
        }

        public boolean isNavigationVisible() {
            return true;
        }
    }

    private static class ToolbarCompatDelegate implements Delegate {
        private final Toolbar mToolbar;
        private final Drawable mDefaultUpIndicator;
        private final CharSequence mDefaultContentDescription;

        private ToolbarCompatDelegate(Toolbar toolbar) {
            this.mToolbar = toolbar;
            this.mDefaultUpIndicator = toolbar.getNavigationIcon();
            this.mDefaultContentDescription = toolbar.getNavigationContentDescription();
        }

        public void setActionBarUpIndicator(Drawable upDrawable, int contentDescRes) {
            this.mToolbar.setNavigationIcon(upDrawable);
            this.setActionBarDescription(contentDescRes);
        }

        public void setActionBarDescription(int contentDescRes) {
            if (contentDescRes == 0) {
                this.mToolbar.setNavigationContentDescription(this.mDefaultContentDescription);
            } else {
                this.mToolbar.setNavigationContentDescription(contentDescRes);
            }
        }

        public Drawable getThemeUpIndicator() {
            return this.mDefaultUpIndicator;
        }

        public Context getActionBarThemedContext() {
            return this.mToolbar.getContext();
        }

        public boolean isNavigationVisible() {
            return true;
        }
    }

    private static class JellybeanMr2Delegate implements Delegate {
        private final Activity mActivity;

        private JellybeanMr2Delegate(Activity activity) {
            this.mActivity = activity;
        }

        public Drawable getThemeUpIndicator() {
            TypedArray a = this.getActionBarThemedContext().obtainStyledAttributes(
                    null, THEME_UP_INDICATOR_ATTR, THEME_UP_INDICATOR_DEF_STYLE_ATTR, 0);

            Drawable result = a.getDrawable(0);
            a.recycle();
            return result;
        }

        public Context getActionBarThemedContext() {
            ActionBar actionBar = this.mActivity.getActionBar();
            Object context;
            if (actionBar != null) {
                context = actionBar.getThemedContext();
            } else {
                context = this.mActivity;
            }

            return (Context) context;
        }

        public boolean isNavigationVisible() {
            ActionBar actionBar = this.mActivity.getActionBar();
            return actionBar != null && (actionBar.getDisplayOptions() & 4) != 0;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        public void setActionBarUpIndicator(Drawable drawable, int contentDescRes) {
            ActionBar actionBar = this.mActivity.getActionBar();
            if (actionBar != null) {
                actionBar.setHomeAsUpIndicator(drawable);
                actionBar.setHomeActionContentDescription(contentDescRes);
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        public void setActionBarDescription(int contentDescRes) {
            ActionBar actionBar = this.mActivity.getActionBar();
            if (actionBar != null) {
                actionBar.setHomeActionContentDescription(contentDescRes);
            }
        }
    }

    private static class HoneycombDelegate implements Delegate {
        private final Activity mActivity;
        private ActionBarDrawerToggleHoneycomb.SetIndicatorInfo mSetIndicatorInfo;

        private HoneycombDelegate(Activity activity) {
            this.mActivity = activity;
        }

        public Drawable getThemeUpIndicator() {
            return ActionBarDrawerToggleHoneycomb.getThemeUpIndicator(this.mActivity);
        }

        public Context getActionBarThemedContext() {
            ActionBar actionBar = this.mActivity.getActionBar();
            Object context;
            if (actionBar != null) {
                context = actionBar.getThemedContext();
            } else {
                context = this.mActivity;
            }

            return (Context) context;
        }

        public boolean isNavigationVisible() {
            ActionBar actionBar = this.mActivity.getActionBar();
            return actionBar != null && (actionBar.getDisplayOptions() & 4) != 0;
        }

        public void setActionBarUpIndicator(Drawable themeImage, int contentDescRes) {
            this.mActivity.getActionBar().setDisplayShowHomeEnabled(true);
            this.mSetIndicatorInfo = ActionBarDrawerToggleHoneycomb.setActionBarUpIndicator(
                    this.mSetIndicatorInfo, this.mActivity, themeImage, contentDescRes);

            this.mActivity.getActionBar().setDisplayShowHomeEnabled(false);
        }

        public void setActionBarDescription(int contentDescRes) {
            this.mSetIndicatorInfo = ActionBarDrawerToggleHoneycomb.setActionBarDescription(
                    this.mSetIndicatorInfo, this.mActivity, contentDescRes);
        }
    }
}
