package net.mikaelzero.mojito.impl;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class LoadingView extends View {
    private Paint mPaint;
    private Paint circleBgPaint;
    private float mProgress = 0f;
    private float mAnimatedProgress = 0f;
    private ValueAnimator mAnimator;
    private RectF mRectF = new RectF();
    private float mStrokeWidth;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        if (!(getLayoutParams() instanceof FrameLayout.LayoutParams)) {
            FrameLayout.LayoutParams layoutParams =
                    new FrameLayout.LayoutParams(
                            dip2Px(getContext(), 48),
                            dip2Px(getContext(), 48),
                            Gravity.CENTER);
            setLayoutParams(layoutParams);
        }

        mStrokeWidth = dip2Px(getContext(), 4);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(Color.WHITE);

        circleBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleBgPaint.setStyle(Paint.Style.FILL);
        circleBgPaint.setColor(Color.parseColor("#40000000"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2f - mStrokeWidth;

        // Draw background circle
        canvas.drawCircle(width / 2, height / 2, radius + mStrokeWidth / 2, circleBgPaint);

        // Draw progress arc
        mRectF.set(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius);
        
        // Start from top (-90 degrees)
        float sweepAngle = mAnimatedProgress * 360f;
        if (sweepAngle < 5f) sweepAngle = 5f; // Show a small dot at start
        
        canvas.drawArc(mRectF, -90, sweepAngle, false, mPaint);
    }

    public void setProgress(double progress) {
        float targetProgress = (float) (progress / 100f);
        if (targetProgress < 0) targetProgress = 0f;
        if (targetProgress > 1f) targetProgress = 1f;

        if (mProgress == targetProgress) return;
        mProgress = targetProgress;

        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = ValueAnimator.ofFloat(mAnimatedProgress, mProgress);
        mAnimator.setDuration(300);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addUpdateListener(animation -> {
            mAnimatedProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
        
        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
            setAlpha(0f);
            animate().alpha(1f).setDuration(200).start();
        }
    }

    public void loadCompleted() {
        animate().alpha(0f).setDuration(200).withEndAction(() -> setVisibility(GONE)).start();
    }

    public void loadFaild() {
        loadCompleted();
    }

    public void setOutsideCircleColor(int color) {
        // Not used anymore for background circle, but can be adapted
    }

    public void setInsideCircleColor(int color) {
        mPaint.setColor(color);
    }

    public void setTargetView(View target) {
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }

        if (target == null) {
            return;
        }

        if (target.getParent() instanceof FrameLayout) {
            ((FrameLayout) target.getParent()).addView(this);
        } else if (target.getParent() instanceof ViewGroup) {
            ViewGroup parentContainer = (ViewGroup) target.getParent();
            int groupIndex = parentContainer.indexOfChild(target);
            parentContainer.removeView(target);

            FrameLayout badgeContainer = new FrameLayout(getContext());
            ViewGroup.LayoutParams parentLayoutParams = target.getLayoutParams();

            badgeContainer.setLayoutParams(parentLayoutParams);
            target.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            parentContainer.addView(badgeContainer, groupIndex, parentLayoutParams);
            badgeContainer.addView(target);
            badgeContainer.addView(this);
        }
    }

    public static int dip2Px(Context context, float dip) {
        return (int) (dip * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
