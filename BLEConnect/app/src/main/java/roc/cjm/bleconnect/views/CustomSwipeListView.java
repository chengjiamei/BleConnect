package roc.cjm.bleconnect.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class CustomSwipeListView extends ListView {

	private static final String TAG = "ListViewCompat";

	public static SwipeItemView mFocusedItemView;
	private int mPosition;

	public CustomSwipeListView(Context context) {
		super(context);
	}

	public CustomSwipeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomSwipeListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void shrinkListItem(int position) {
		View item = getChildAt(position);

		if (item != null) {
			try {
				((SwipeItemView) item).shrink();
			} catch (ClassCastException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			int x = (int) event.getX();
			int y = (int) event.getY();
			int position = pointToPosition(x, y);
			Log.e(TAG, "postion=" + position);
			if (position != INVALID_POSITION) {
				int firstPos = getFirstVisiblePosition();
				mFocusedItemView = (SwipeItemView) getChildAt(position
						- firstPos);
			}
		}
		default:
			break;
		}

		if (mFocusedItemView != null) {
			mFocusedItemView.onRequireTouchEvent(event);

		}

		return super.onTouchEvent(event);
	}

/*	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		final int actionMasked = ev.getActionMasked() & MotionEvent.ACTION_MASK;

		if (actionMasked == MotionEvent.ACTION_DOWN) {
			mPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
			return super.dispatchTouchEvent(ev);
		}
		if (mFocusedItemView != null) {
			if (mFocusedItemView.getisHorizontalMove()) {

				if (actionMasked == MotionEvent.ACTION_MOVE) {

					return true;
				}
			}
		}
		if (actionMasked == MotionEvent.ACTION_UP
				|| actionMasked == MotionEvent.ACTION_CANCEL) {
			if (pointToPosition((int) ev.getX(), (int) ev.getY()) == mPosition) {
				super.dispatchTouchEvent(ev);
			} else {
				setPressed(false);
				invalidate();
				return true;
			}
		}

		return super.dispatchTouchEvent(ev);
	}*/

}
