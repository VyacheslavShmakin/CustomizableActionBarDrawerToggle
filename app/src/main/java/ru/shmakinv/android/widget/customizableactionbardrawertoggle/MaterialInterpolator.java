package ru.shmakinv.android.widget.customizableactionbardrawertoggle;

import android.view.animation.Interpolator;

/**
 * MaterialInterpolator
 *
 * @author: Vyacheslav Shmakin
 * @version: 09.04.2015
 */
class MaterialInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float x) {
        return (float) (6 * Math.pow(x, 2) - 8 * Math.pow(x, 3) + 3 * Math.pow(x, 4));
    }
}
