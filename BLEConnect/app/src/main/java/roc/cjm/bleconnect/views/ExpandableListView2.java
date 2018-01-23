package roc.cjm.bleconnect.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ExpandableListView;

import roc.cjm.bleconnect.utils.Util;

/**
 * Created by Marcos on 2017/10/24.
 */

public class ExpandableListView2 extends ExpandableListView {

    private String TAG = "SlideListView2";
    private OnChildClickListener onTpItemClickListener;
    private OnChildClickListener mOnChildClickListener;

    private SlideView2 mFocusView;

    public ExpandableListView2(Context context) {
        super(context);
    }

    public ExpandableListView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableListView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnChildClickListener(OnChildClickListener onChildClickListener) {
        super.setOnChildClickListener(onChildClickListener);
        mOnChildClickListener = onChildClickListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        int position = pointToPosition((int)ev.getX(), (int)ev.getY());

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                hasConsume = false;
                Log.e(TAG, "dispatchTouchEvent ACTION_DOWN");
                if(position != INVALID_POSITION) {
                    if (getChildAt(position - getFirstVisiblePosition()) != mFocusView && mFocusView != null) {
                        mFocusView.startAnimator(mFocusView.getLeftMargin(), 0);
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "dispatchTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                if (mFocusView != null) {
                    if (hasConsume) {
                        hasConsume = false;
                        /************滑动过程不触发OnItemClickListener******************/
                        onTpItemClickListener = mOnChildClickListener;
                        setOnChildClickListener(null);
                        /******************************/

                        mFocusView.startAnimator(mFocusView.getLeftMargin(), orientation);
                        orientation = 0;
                    } else if (mFocusView.getLeftMargin() != 0) {
                        mFocusView.startAnimator(mFocusView.getLeftMargin(), 0);
                        orientation = 0;
                    } else {

                    }
                }
                Log.e(TAG, "dispatchTouchEvent ACTION_UP");
                break;
        }

        boolean isreturn = super.dispatchTouchEvent(ev);
        Log.e(TAG, "dispatchTouchEvent isreturn = " + isreturn);
        return isreturn;
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
        boolean isreturn = super.onInterceptTouchEvent(ev);
        Log.e(TAG, "onInterceptTouchEvent isreturn = " + isreturn);
        return isreturn;
    }

    private float lastX;
    private float lastY;
    private boolean hasConsume = false;
    private int orientation = 0;// -1 向左 1向右

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        int position = pointToPosition(x, y);
        if (position != INVALID_POSITION) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    lastX = ev.getX();
                    lastY = ev.getY();
                    Log.e(TAG, "onTouchEvent ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.e(TAG, "onTouchEvent ACTION_MOVE");
                    float dx = ev.getX() - lastX;
                    float dy = ev.getY() - lastY;
                    ////判断是否达到滑动阈值
                    if ((Math.abs(dx) / 2 > Math.abs(dy) && Math.abs(dx) > Util.dp2px(getContext(),10) && hasConsume == false ) || hasConsume) {
                        if(mFocusView == null) {
                            //获取当前ItemView
                            try {
                                if(getChildAt(position - getFirstVisiblePosition()) instanceof SlideView2) {
                                    mFocusView = (SlideView2) getChildAt(position - getFirstVisiblePosition());
                                    mFocusView.setOnSlideViewListener(new SlideView2.OnSlideViewListener() {
                                        @Override
                                        public void onSlideStateChanged(int state) {
                                            if (state == 1) {
                                                mFocusView = null;
                                            }
                                            if (onTpItemClickListener != null) {
                                                setOnChildClickListener(onTpItemClickListener);
                                            }
                                        }
                                    });
                                }
                            }catch (ClassCastException e) {
                                e.printStackTrace();
                            }

                        }
                        if (orientation == 0) {
                            orientation = (dx < 0 ? -1 : (dx == 0 ? 0 : 1));
                        }
                        hasConsume = true;
                        hasConsume = true;
                        if (mFocusView != null) {
                            mFocusView.updateParams(ev.getX() - lastX);
                        }
                        lastX = ev.getX();
                        lastY = ev.getY();
                        return false;
                    } else {

                    }
                    break;
                case MotionEvent.ACTION_UP:
                    hasConsume = false;
                    Log.e(TAG, "onTouchEvent ACTION_UP");
                    break;
            }

        }
        boolean isreturn = super.onTouchEvent(ev);
        Log.e(TAG, "onTouchEvent isreturn = " + isreturn);
        return isreturn;
    }
}
