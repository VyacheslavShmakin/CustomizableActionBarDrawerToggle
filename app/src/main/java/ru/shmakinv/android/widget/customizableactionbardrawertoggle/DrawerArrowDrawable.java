//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package ru.shmakinv.android.widget.customizableactionbardrawertoggle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;

abstract class DrawerArrowDrawable extends Drawable {
    private final Paint mPaint = new Paint();
    private static final float ARROW_HEAD_ANGLE = (float)Math.toRadians(45.0D);
    private final float mBarThickness;
    private final float mTopBottomArrowSize;
    private final float mBarSize;
    private final float mMiddleArrowSize;
    private final float mBarGap;
    private final boolean mSpin;
    private final Path mPath = new Path();
    private final int mSize;
    private boolean mVerticalMirror = false;
    private float mProgress;
    private float mMaxCutForBarSize;
    private float mCenterOffset;

    public DrawerArrowDrawable(Context context) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                null,
                R.styleable.DrawerArrowToggle,
                R.attr.drawerArrowStyle,
                R.style.Base_Widget_AppCompat_DrawerArrowToggle);

        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(typedArray.getColor(R.styleable.DrawerArrowToggle_color, 0));
        this.mSize = typedArray.getDimensionPixelSize(R.styleable.DrawerArrowToggle_drawableSize, 0);
        this.mBarSize = (float)Math.round(typedArray.getDimension(R.styleable.DrawerArrowToggle_barSize, 0.0F));
        this.mTopBottomArrowSize = (float)Math.round(typedArray.getDimension(R.styleable.DrawerArrowToggle_topBottomBarArrowSize, 0.0F));
        this.mBarThickness = typedArray.getDimension(R.styleable.DrawerArrowToggle_thickness, 0.0F);
        this.mBarGap = (float)Math.round(typedArray.getDimension(R.styleable.DrawerArrowToggle_gapBetweenBars, 0.0F));
        this.mSpin = typedArray.getBoolean(R.styleable.DrawerArrowToggle_spinBars, true);
        this.mMiddleArrowSize = typedArray.getDimension(R.styleable.DrawerArrowToggle_middleBarArrowSize, 0.0F);
        int remainingSpace = (int)((float)this.mSize - this.mBarThickness * 3.0F - this.mBarGap * 2.0F);
        this.mCenterOffset = (float)(remainingSpace / 4 * 2);
        this.mCenterOffset = (float)((double)this.mCenterOffset + (double)this.mBarThickness * 1.5D + (double)this.mBarGap);
        typedArray.recycle();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeJoin(Join.MITER);
        this.mPaint.setStrokeCap(Cap.BUTT);
        this.mPaint.setStrokeWidth(this.mBarThickness);
        this.mMaxCutForBarSize = (float)((double)(this.mBarThickness / 2.0F) * Math.cos((double)ARROW_HEAD_ANGLE));
    }

    abstract boolean isLayoutRtl();

    protected void setVerticalMirror(boolean verticalMirror) {
        this.mVerticalMirror = verticalMirror;
    }

    public void draw(Canvas canvas) {
        Rect bounds = this.getBounds();
        boolean isRtl = this.isLayoutRtl();
        float arrowSize = lerp(this.mBarSize, this.mTopBottomArrowSize, this.mProgress);
        float middleBarSize = lerp(this.mBarSize, this.mMiddleArrowSize, this.mProgress);
        float middleBarCut = (float)Math.round(lerp(0.0F, this.mMaxCutForBarSize, this.mProgress));
        float rotation = lerp(0.0F, ARROW_HEAD_ANGLE, this.mProgress);
        float canvasRotate = lerp(isRtl?0.0F:-180.0F, isRtl?180.0F:0.0F, this.mProgress);
        float arrowWidth = (float)Math.round((double)arrowSize * Math.cos((double)rotation));
        float arrowHeight = (float)Math.round((double)arrowSize * Math.sin((double)rotation));
        this.mPath.rewind();
        float topBottomBarOffset = lerp(this.mBarGap + this.mBarThickness, -this.mMaxCutForBarSize, this.mProgress);
        float arrowEdge = -middleBarSize / 2.0F;
        this.mPath.moveTo(arrowEdge + middleBarCut, 0.0F);
        this.mPath.rLineTo(middleBarSize - middleBarCut * 2.0F, 0.0F);
        this.mPath.moveTo(arrowEdge, topBottomBarOffset);
        this.mPath.rLineTo(arrowWidth, arrowHeight);
        this.mPath.moveTo(arrowEdge, -topBottomBarOffset);
        this.mPath.rLineTo(arrowWidth, -arrowHeight);
        this.mPath.close();
        canvas.save();
        canvas.translate((float)bounds.centerX(), this.mCenterOffset);
        if(this.mSpin) {
            canvas.rotate(canvasRotate * (float)(this.mVerticalMirror ^ isRtl?-1:1));
        } else if(isRtl) {
            canvas.rotate(180.0F);
        }

        canvas.drawPath(this.mPath, this.mPaint);
        canvas.restore();
    }

    public void setAlpha(int i) {
        this.mPaint.setAlpha(i);
    }

    public boolean isAutoMirrored() {
        return true;
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }

    public int getIntrinsicHeight() {
        return this.mSize;
    }

    public int getIntrinsicWidth() {
        return this.mSize;
    }

    public int getOpacity() {
        return -3;
    }

    public float getProgress() {
        return this.mProgress;
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        this.invalidateSelf();
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
