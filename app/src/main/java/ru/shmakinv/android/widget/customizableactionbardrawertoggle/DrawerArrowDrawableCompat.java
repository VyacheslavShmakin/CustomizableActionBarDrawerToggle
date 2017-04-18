package ru.shmakinv.android.widget.customizableactionbardrawertoggle;

import android.content.Context;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;

/**
 * DrawerArrowDrawableCompat
 *
 * @author Vyacheslav Shmakin
 * @version 18.04.2017
 */
public class DrawerArrowDrawableCompat extends DrawerArrowDrawable {

    public DrawerArrowDrawableCompat(Context themedContext) {
        super(themedContext);
    }

    public float getPosition() {
        return super.getProgress();
    }

    public void setPosition(float position, @DrawerImageState int imageState) {
        if (imageState == DrawerImageState.TOGGLE_DRAWER_MIRRORED) {
            this.setVerticalMirror(true);
        } else if (imageState == DrawerImageState.TOGGLE_DRAWER_NORMAL) {
            this.setVerticalMirror(false);
        }

        super.setProgress(position);
    }
}


