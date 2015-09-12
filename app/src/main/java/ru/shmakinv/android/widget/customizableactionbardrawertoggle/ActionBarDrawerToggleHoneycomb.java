//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package ru.shmakinv.android.widget.customizableactionbardrawertoggle;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Method;

class ActionBarDrawerToggleHoneycomb {
    private static final String TAG = "ActionBarDrawerToggleHoneycomb";
    private static final String MESSAGE_CONTENT_DESCR = "Couldn\'t set content description via " +
            "JB-MR2 API";
    private static final String MESSAGE_HOME_AS_UP = "Couldn\'t set home-as-up indicator";
    private static final String MESSAGE_HOME_AS_UP_JB_MR2 = "Couldn\'t set home-as-up indicator " +
            "via JB-MR2 API";

    private static final String METHOD_NAME_HOME_AS_UP = "setHomeAsUpIndicator";
    private static final String METHOD_NAME_CONTENT_DESCR = "setHomeActionContentDescription";

    private static final int[] THEME_ATTRS = new int[]{16843531};
    private static final int HOME_VIEW_ID = 16908332;

    ActionBarDrawerToggleHoneycomb() {
    }

    public static SetIndicatorInfo setActionBarUpIndicator(
            SetIndicatorInfo info, Activity activity, Drawable drawable, int contentDescRes) {

        info = new SetIndicatorInfo(activity);

        if (info.setHomeAsUpIndicator != null) {
            try {
                ActionBar e = activity.getActionBar();
                info.setHomeAsUpIndicator.invoke(e, drawable);
                info.setHomeActionContentDescription.invoke(e, contentDescRes);
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

    public static SetIndicatorInfo setActionBarDescription(
            SetIndicatorInfo info, Activity activity, int contentDescRes) {

        if (info == null) {
            info = new SetIndicatorInfo(activity);
        }

        if (info.setHomeAsUpIndicator != null) {

            try {
                ActionBar e = activity.getActionBar();
                info.setHomeActionContentDescription.invoke(e, contentDescRes);

                if (VERSION.SDK_INT <= 19 && e != null) {
                    e.setSubtitle(e.getSubtitle());
                }
            } catch (Exception e) {
                Log.w(TAG, MESSAGE_CONTENT_DESCR, e);
            }
        }

        return info;
    }

    public static Drawable getThemeUpIndicator(Activity activity) {
        TypedArray a = activity.obtainStyledAttributes(THEME_ATTRS);
        Drawable result = a.getDrawable(0);
        a.recycle();
        return result;
    }

    public static class SetIndicatorInfo {
        public Method setHomeAsUpIndicator;
        public Method setHomeActionContentDescription;
        public ImageView upIndicatorView;

        SetIndicatorInfo(Activity activity) {
            try {
                this.setHomeAsUpIndicator = ActionBar.class.getDeclaredMethod(
                        METHOD_NAME_HOME_AS_UP, Drawable.class);

                this.setHomeActionContentDescription = ActionBar.class.getDeclaredMethod(
                        METHOD_NAME_CONTENT_DESCR, Integer.TYPE);

            } catch (NoSuchMethodException e) {
                View home = activity.findViewById(HOME_VIEW_ID);

                if (home != null) {
                    ViewGroup parent = (ViewGroup) home.getParent();
                    int childCount = parent.getChildCount();

                    if (childCount == 2) {
                        View first = parent.getChildAt(0);
                        View second = parent.getChildAt(1);
                        View up = first.getId() == HOME_VIEW_ID ? second : first;

                        if (up instanceof ImageView) {
                            this.upIndicatorView = (ImageView) up;
                        }
                    }
                }
            }
        }
    }
}
