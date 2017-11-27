package com.sun.splitverticalscrollnumberview.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.sun.splitverticalscrollnumberview.R;


/**
 * Created by Sun on 22/11/2017.
 */

public class SplitScrollNumberView extends View {

    private static final String ORIGINAL_COUNT = "original_count";
    private static final String INSTANCE = "instance";
    private int mOriginalCount;
    private Paint mTextPaint;
    private float mTextSize;
    private int mTextColor;
    private String[] mNumContainer;
    private float mOldOffsetY;
    private float mNewOffsetY;
    private int mStartX;
    private int mStartY;
    private boolean mToBigger;
    private float mOffSetMin;
    private float mOffSetMax;
    private double mLastClickTime;
    private long mAnimationDuration = 500;
    private Rect mRect;

    public SplitScrollNumberView(Context context) {
        this(context, null);
    }

    public SplitScrollNumberView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SplitScrollNumberView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SplitScrollNumberView);
        mOriginalCount = typedArray.getInt(R.styleable.SplitScrollNumberView_origin_count, 0);
        mTextSize = typedArray.getDimension(R.styleable.SplitScrollNumberView_text_Size, 10);
        mTextColor = typedArray.getColor(R.styleable.SplitScrollNumberView_text_Color, Color.parseColor("#cccccc"));
        typedArray.recycle();

        init();
    }

    private void init() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

        mNumContainer = new String[]{formatNumber(mOriginalCount), "", ""};
        mOffSetMin = 0;
        mOffSetMax = 1.5f * sp2px(mTextSize);

        mRect = new Rect();
        String s = String.valueOf(mOriginalCount);
        mTextPaint.getTextBounds(s, 0, s.length(), mRect);
    }

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        bundle.putInt(ORIGINAL_COUNT, mOriginalCount);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mOriginalCount = bundle.getInt(ORIGINAL_COUNT);
            mNumContainer[0] = String.valueOf(mOriginalCount);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getWidth(widthMeasureSpec), getHeight(heightMeasureSpec));
    }

    private int getWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = getContentWidth();
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                result = Math.max(getContentWidth(), result);
                break;
        }
        return result;
    }

    private int getHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = getContentHeight();
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                result = Math.max(getContentHeight(), result);
                break;
        }
        return result;
    }

    private int getContentWidth() {
        int result;
        result = (int) mTextPaint.measureText(formatNumber(mOriginalCount));
        result += getPaddingLeft() + getPaddingRight();
        return result;
    }

    private int getContentHeight() {
        int result;
        result = Math.max(sp2px(mTextSize), 0);
        result += getPaddingTop() + getPaddingBottom();
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStartX = getPaddingLeft();
        mStartY = (int) ((h + mRect.height()) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        float y = 0;

        mTextPaint.setColor(mTextColor);
        canvas.drawText(String.valueOf(mNumContainer[0]), mStartX, mStartY + y, mTextPaint);

        String text = formatNumber(mOriginalCount);
        float textWidth = mTextPaint.measureText(text) / text.length();

        canvas.drawText(String.valueOf(mNumContainer[1]), mStartX + textWidth * mNumContainer[0].length(), mStartY + y - mOldOffsetY, mTextPaint);

        canvas.drawText(String.valueOf(mNumContainer[2]), mStartX + textWidth * mNumContainer[0].length(), mStartY + y - mNewOffsetY, mTextPaint);
    }

    public void setTextOffsetY(float offsetY) {
        this.mOldOffsetY = offsetY;//变大是从[0,1]，变小是[0,-1]
        if (mToBigger) {//从下到上[-1,0]
            this.mNewOffsetY = offsetY - mOffSetMax;
        } else {//从上到下[1,0]
            this.mNewOffsetY = mOffSetMax + offsetY;
        }
        postInvalidate();
    }

    public float getTextOffsetY() {
        return mOffSetMin;
    }

    private void calculateChangeNum(int number) {
        if (number == 0) {
            mNumContainer[0] = formatNumber(mOriginalCount);
            mNumContainer[1] = "";
            mNumContainer[2] = "";
            return;
        }
        mToBigger = number > mOriginalCount;
        String oldNum = formatNumber(mOriginalCount);
        String newNum = formatNumber(number);

        int oldNumLen = oldNum.length();
        int newNumLen = newNum.length();

        if (oldNumLen != newNumLen) {
            mNumContainer[0] = "";
            mNumContainer[1] = oldNum;
            mNumContainer[2] = newNum;
        } else {
            for (int i = 0; i < oldNumLen; i++) {
                char oldC1 = oldNum.charAt(i);
                char newC1 = newNum.charAt(i);
                if (oldC1 != newC1) {
                    if (i == 0) {
                        mNumContainer[0] = "";
                    } else {
                        mNumContainer[0] = newNum.substring(0, i);
                    }
                    mNumContainer[1] = oldNum.substring(i);
                    mNumContainer[2] = newNum.substring(i);
                    break;
                }
            }
        }
    }

    private String formatNumber(int number) {
        return String.valueOf(number);
    }

    public void setNumber(int number) {

        if (number == mOriginalCount) {
            return;
        }
        if (System.currentTimeMillis() - mLastClickTime < mAnimationDuration) {
            return;
        }

        mLastClickTime = System.currentTimeMillis();

        if (number > mOriginalCount) {
            calculateChangeNum(number);
            requestLayout();
            upAnima();
        } else {
            calculateChangeNum(number);
            requestLayout();
            downAnim();
        }

        mOriginalCount = number;
    }

    private void upAnima() {
        ObjectAnimator textOffsetY = ObjectAnimator.ofFloat(this, "textOffsetY", mOffSetMin, mOffSetMax);
        textOffsetY.setDuration(mAnimationDuration);

        textOffsetY.start();
    }

    private void downAnim() {
        ObjectAnimator textOffsetY = ObjectAnimator.ofFloat(this, "textOffsetY", mOffSetMin, -mOffSetMax);
        textOffsetY.setDuration(mAnimationDuration);

        textOffsetY.start();
    }

}
