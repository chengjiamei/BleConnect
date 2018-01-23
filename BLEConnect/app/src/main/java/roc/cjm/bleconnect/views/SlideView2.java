package roc.cjm.bleconnect.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;


/**
 * Created by Administrator on 2017/9/1.
 */

public class SlideView2 extends LinearLayout {

    private String TAG = "SlideView2";

    private long DURATION = 150;
    private LinearLayout mContentParent;
    private View mViewContent;
    private View mergeView;
    private Context mContext;
    private boolean isEdit;///是否处于编辑状态
    private float editWidth;///mergeview width\
    private SlideView2.OnSlideViewListener listener;

    public SlideView2(Context context) {
        super(context);
        init(context);
    }

    public SlideView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlideView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        setOrientation(LinearLayout.HORIZONTAL);
        mContentParent = new LinearLayout(context);
        int width = getResources().getDisplayMetrics().widthPixels;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        mContentParent.setLayoutParams(params);

        this.addView(mContentParent);

        /*mergeView = LayoutInflater.from(context).inflate(R.layout.slide_view_merge, null);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(tw*2, tw);
        mergeView.findViewById(R.id.modify).setOnClickListener(this);
        mergeView.findViewById(R.id.delete).setOnClickListener(this);
        mergeView.setLayoutParams(params1);*//**//*

        this.addView(mergeView);*/
    }

    public void setOnSlideViewListener(OnSlideViewListener listener) {
        this.listener = listener;
    }

    public void setContentView(View view) {
        if(view != null) {
            if( mContentParent.getChildCount() >0) {
                mContentParent.removeAllViews();
            }
            mContentParent.addView(view);
            mViewContent = view;
        }
    }

    public void setMergeView(View mergeView) {
        if(mergeView != null) {
            if (this.mergeView != null) {
                this.removeView(mergeView);
            }
            this.addView(mergeView);
            this.mergeView = mergeView;
        }
    }

    public View getMergeView() {
        return this.mergeView;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onInterceptTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "onInterceptTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onInterceptTouchEvent ACTION_UP");
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public View getContentView() {
        return mViewContent;
    }

    public void updateParams(float dx) {
        if(editWidth == 0) {
            editWidth = (mergeView == null ? 0 :mergeView.getWidth());
        }
        if(mContentParent != null ) {
            LinearLayout.LayoutParams params = (LayoutParams) mContentParent.getLayoutParams();
            params.rightMargin = 0;
            params.leftMargin = (int) ((params.leftMargin + dx)<-1*editWidth?-1*editWidth:(params.leftMargin + dx>0?0:params.leftMargin + dx));
            mContentParent.setLayoutParams(params);
        }
        LinearLayout.LayoutParams params2 = (LayoutParams) mergeView.getLayoutParams();
        params2.leftMargin = 0;
        params2.rightMargin = 0;
        mergeView.setLayoutParams(params2);
    }

    private ValueAnimator animator;

    public void startAnimator(float value, int orientation) {
        animator = new ValueAnimator();
        animator.setDuration(DURATION);
        LinearInterpolator interpolator = new LinearInterpolator();
        animator.setInterpolator(interpolator);

        animator.setFloatValues(value, (orientation == 1 || orientation ==0 )? 0 : -mergeView.getWidth());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setLeftMargin((int) (((Float)animation.getAnimatedValue()).floatValue()));
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(listener != null) {
                    listener.onSlideStateChanged((mergeView == null || getLeftMargin() == 0)?1:-1);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    ///获取偏移量 负数
    public int getLeftMargin() {
        if (mContentParent == null) {
            return 0;
        }
        return ((LayoutParams) mContentParent.getLayoutParams()).leftMargin;
    }

    public void setLeftMargin(int margin) {
        LinearLayout.LayoutParams params = (LayoutParams) mContentParent.getLayoutParams();
        params.leftMargin = margin;
        params.rightMargin = 0;

        mContentParent.setLayoutParams(params);
        LinearLayout.LayoutParams params1 = (LayoutParams) mergeView.getLayoutParams();
        params1.leftMargin = 0;
        params1.rightMargin = 0;

        mergeView.setLayoutParams(params1);
    }

    public void stopAnimator() {
        if (isAnimator()) {
            animator.cancel();
            animator = null;
        }
    }

    public boolean isAnimator() {
        if (animator == null)
            return false;
        return animator.isStarted();
    }

    public interface OnSlideViewListener {
        void onSlideStateChanged(int state);
    }
}
