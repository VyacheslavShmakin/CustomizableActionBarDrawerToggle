package ru.shmakinv.android.widget.customizableactionbardrawertoggle;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static ru.shmakinv.android.widget.customizableactionbardrawertoggle.DrawerImageState.TOGGLE_DRAWER_DEFAULT;
import static ru.shmakinv.android.widget.customizableactionbardrawertoggle.DrawerImageState.TOGGLE_DRAWER_MIRRORED;
import static ru.shmakinv.android.widget.customizableactionbardrawertoggle.DrawerImageState.TOGGLE_DRAWER_NORMAL;

/**
 * DrawerImageState
 *
 * @author Vyacheslav Shmakin
 * @version 18.04.2017
 */
@IntDef({TOGGLE_DRAWER_DEFAULT, TOGGLE_DRAWER_NORMAL, TOGGLE_DRAWER_MIRRORED})
@Retention(RetentionPolicy.SOURCE)
public @interface DrawerImageState {
    int TOGGLE_DRAWER_DEFAULT = 0;
    int TOGGLE_DRAWER_NORMAL = 1;
    int TOGGLE_DRAWER_MIRRORED = 2;
}
