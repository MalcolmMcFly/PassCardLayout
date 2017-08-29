package com.flytxt.mobile.passcardlayout.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.flytxt.mobile.passcardlayout.R;


/**
 * Created by amalg on 26-08-2017.
 */

@RemoteViews.RemoteView
public class PassCardLayout extends ViewGroup {
    Paint shadowPaint;
    Path shadowPath;
    Paint paint;
    int shadowOffset = 20;

    int headerColor, footerColor, dividerColor, dividerWidth;
    private int circleRadius, customPadding;

    interface Defaults {
        int headerColor = Color.GREEN;
        int footerColor = Color.RED;
    }

    private PassCardInternalContainer internalHeader, internalFooter;

    private MedianView medianView;

    /**
     * These are used for computing child frames based on their gravity.
     */
    private final Rect mTmpContainerRect = new Rect();
    private final Rect mTmpChildRect = new Rect();

    public PassCardLayout(Context context) {
        super(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        shadowPath.reset();
        // h - shadowOffset
        //w,h,heightHeader,circleRadius,heightFooter
        Utils.addRoundedRect3(shadowPath,
                shadowOffset, shadowOffset, w - shadowOffset, h - shadowOffset,
                60.0f, 60.0f, internalHeader.getMeasuredHeight() - 2 * shadowOffset-10, circleRadius, internalFooter.getMeasuredHeight() - 2 * shadowOffset-10);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        int save = canvas.save();
        //paint.setShader(null);
        //float adjust = (.0095f * getHeight() / 2);
        //paint.setShadowLayer(8, adjust, -adjust, 0xaa000000);.
        if (!isInEditMode()) {
            shadowPaint = new Paint();
            shadowPaint.setColor(0xf0000000);
            shadowPaint.setStyle(Paint.Style.STROKE);
            shadowPaint.setAntiAlias(true);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            shadowPaint.setStrokeWidth(4.0f);
            shadowPaint.setMaskFilter(new BlurMaskFilter(4, BlurMaskFilter.Blur.NORMAL));
            canvas.drawPath(shadowPath, shadowPaint);
        }
        //canvas.clipPath(shadowPath);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }

    public PassCardLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(attrs);
    }

    public PassCardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PassCardLayout);
            headerColor = typedArray.getColor(R.styleable.PassCardLayout_headerColor, PassCardLayout.Defaults.headerColor);
            footerColor = typedArray.getColor(R.styleable.PassCardLayout_footerColor, PassCardLayout.Defaults.footerColor);
            circleRadius = typedArray.getDimensionPixelSize(R.styleable.PassCardLayout_circleRadius, 10);
            customPadding = typedArray.getDimensionPixelSize(R.styleable.PassCardLayout_customPadding, 10);
            dividerColor = typedArray.getColor(R.styleable.PassCardLayout_dividerColor, MedianView.Defaults.dividerColor);
            dividerWidth = typedArray.getDimensionPixelSize(R.styleable.PassCardLayout_dividerWidth, 8);
            typedArray.recycle();
        }

        internalHeader = new PassCardInternalContainer(getContext());
        internalHeader.setMode(PassCardInternalContainer.Mode.Header);
        internalHeader.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        medianView = new MedianView(getContext());
        medianView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        medianView.setCircleRadius(circleRadius);
        medianView.setCustomPadding(customPadding);
        medianView.setDividerWidth(dividerWidth);
        medianView.setDividerColor(Color.BLACK);
        medianView.setHeaderColor(headerColor);
        medianView.setFooterColor(footerColor);

        internalFooter = new PassCardInternalContainer(getContext());
        internalFooter.setMode(PassCardInternalContainer.Mode.Footer);
        internalFooter.setLayoutParams(new LayoutParams(MarginLayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));


//        paint = new Paint();
//        paint.setDither(true);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setStrokeJoin(Paint.Join.ROUND);
//        paint.setStrokeCap(Paint.Cap.ROUND);
//        paint.setAntiAlias(true);
//        paint.setStrokeWidth(1.0f);


        shadowPath = new Path();

    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(addToContainerView(child), index, params);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(addToContainerView(child), params);
    }

    /**
     * Any layout manager that doesn't scroll will want this.
     */
    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }


    /**
     * Ask all children to measure themselves and compute the measurement of this
     * layout based on the children.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        /* Measurement will ultimately be computing these values. */
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        /*
        Iterate through all children, measuring them and computing our dimensions
        from their size.
        */
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                //Measure the child.
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

                //Update our size information based on the layout params.  Children
                //that asked to be positioned on the left or right go in those gutters.

                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

                //maxWidth += childWidth;
                maxWidth = Math.max(maxWidth, childWidth);
                maxHeight += childHeight;
                //maxHeight = Math.max(maxHeight, childHeight);

                childState = combineMeasuredStates(childState, child.getMeasuredState());
            }
        }
        /* Check against our minimum height and width */
        maxHeight = Math.max(maxHeight + 2 * shadowOffset, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth + 2 * shadowOffset, getSuggestedMinimumWidth());

        /* Report our final dimensions. */
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(medianView, 1);
    }

    private View addToContainerView(View view) {
        if (view instanceof PassCardInternalContainer || view instanceof MedianView)
            return view;
        switch (getChildCount()) {
            case 0:
                internalHeader.setId(R.id.headerLayout);
                view.setBackgroundColor(headerColor);
                internalHeader.addView(view);
                return internalHeader;
            case 1:
                internalFooter.setId(R.id.footerLayout);
                view.setBackgroundColor(footerColor);
                internalFooter.addView(view);
                return internalFooter;
        }
        return view;

    }

    /**
     * Position all children within this layout.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        if (getChildCount() != 3)
            throw new IllegalStateException("PassCardLayout can only hold two direct subchilds.");

        // These are the far left and right edges in which we are performing layout.
        int leftPos = getPaddingLeft() + shadowOffset;
        int rightPos = right - left - getPaddingRight() -shadowOffset;

        // These are the top and bottom edges in which we are performing layout.
        final int parentTop = getPaddingTop() + shadowOffset;
        final int parentBottom = bottom - top - getPaddingBottom();

        int currentHeight = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                mTmpContainerRect.left = leftPos + lp.leftMargin;
                mTmpContainerRect.right = rightPos - lp.rightMargin;

                mTmpContainerRect.top = parentTop + currentHeight + lp.topMargin;
                currentHeight = mTmpContainerRect.bottom = mTmpContainerRect.top + height + lp.bottomMargin;
                currentHeight -= shadowOffset;

                // mTmpContainerRect.top = parentTop + lp.topMargin;
                // mTmpContainerRect.bottom = parentBottom - lp.bottomMargin;
                //Use the child's gravity and size to determine its final
                //frame within its container.

                Gravity.apply(lp.gravity, width, height, mTmpContainerRect, mTmpChildRect);

                //Place the child.
                //TODO elimintae if posstivle shadow
                child.layout(mTmpChildRect.left + shadowOffset, mTmpChildRect.top,
                        mTmpChildRect.right - shadowOffset, mTmpChildRect.bottom);
            }
        }

    }

    /*
    ----------------------------------------------------------------------
    The rest of the implementation is for custom per-child layout parameters.
    If you do not need these (for example you are writing a layout manager
    that does fixed positioning of its children), you can drop all of this.
    */


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new PassCardLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /**
     * Custom per-child layout information.
     */
    public static class LayoutParams extends MarginLayoutParams {
        /**
         * The gravity to apply with the View to which these layout parameters
         * are associated.
         */
        public int gravity = Gravity.CENTER | Gravity.CENTER;


        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            /*
            Pull the layout param values from the layout XML during
            inflation.  This is not needed if you don't care about
            changing the layout behavior in XML.
            */
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.PassCardLayout);
            gravity = a.getInt(R.styleable.PassCardLayout_android_layout_gravity, gravity);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}