package com.google.android.glass.touchpad;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class TouchpadHandlingTextView extends TextView
        implements View.OnAttachStateChangeListener {

    private final GestureDetector mTouchDetector;

    public TouchpadHandlingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchDetector = createGestureDetector(context);
        // must set the view to be focusable
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public TouchpadHandlingTextView(Context context) {
        this(context, null);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        requestFocus();
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
    }

    /** Pass a MotionEvent into the gesture detector */
    @Override
    public boolean dispatchGenericFocusedEvent(MotionEvent event) {
        if (isFocused()) {
            return mTouchDetector.onMotionEvent(event);
        }
        return super.dispatchGenericFocusedEvent(event);
    }

    /** Create gesture detector that triggers onClickListener. */
    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gd = new GestureDetector(context);
        gd.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    return performClick();
                }
                return false;
            }
        });
        return gd;
    }
}