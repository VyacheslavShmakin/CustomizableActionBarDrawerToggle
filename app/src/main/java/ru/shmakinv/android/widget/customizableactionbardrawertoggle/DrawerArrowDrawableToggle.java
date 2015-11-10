package ru.shmakinv.android.widget.customizableactionbardrawertoggle;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;

/**
 * DrawerArrowDrawableToggle
 *
 * @author: VyacheslavShmakin
 * @version: 09.04.2015
 */
class DrawerArrowDrawableToggle extends DrawerArrowDrawable implements
        ActionBarDrawerToggle.DrawerToggle {
    private final Activity mActivity;

    public DrawerArrowDrawableToggle(Activity activity, Context themedContext) {
        super(themedContext);
        this.mActivity = activity;
    }

    protected boolean isLayoutRtl() {
        return ViewCompat.getLayoutDirection(this.mActivity.getWindow().getDecorView()) == 1;
    }

    public float getPosition() {
        return super.getProgress();
    }

    public void setPosition(float position, int imageState) {
        if (imageState == DrawerImageState.TOGGLE_DRAWER_MIRRORED) {
            this.setVerticalMirror(true);
        } else if (imageState == DrawerImageState.TOGGLE_DRAWER_NORMAL) {
            this.setVerticalMirror(false);
        }

        super.setProgress(position);
    }
}
