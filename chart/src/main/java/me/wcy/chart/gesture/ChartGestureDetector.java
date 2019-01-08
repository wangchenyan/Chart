package me.wcy.chart.gesture;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by hzwangchenyan on 2016/10/2.
 */
public class ChartGestureDetector {
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private ChartGestureListener mListener;

    public ChartGestureDetector(Context context) {
        mGestureDetector = new GestureDetector(context, mGestureListener);
        mScaleGestureDetector = new ScaleGestureDetector(context, mScaleListener);
    }

    public void setGestureListener(ChartGestureListener listener) {
        mListener = listener;
    }

    public boolean onTouch(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        if (mScaleGestureDetector.isInProgress()) {
            return true;
        }

        return mGestureDetector.onTouchEvent(event);
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            if (mListener != null) {
                return mListener.onDown(e.getX(), e.getY());
            }
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            if (mListener != null) {
                mListener.onShowPress(e.getX(), e.getY());
            }
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mListener != null) {
                mListener.onLongPress(e.getX(), e.getY());
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mListener != null) {
                return mListener.onSingleTap(e.getX(), e.getY());
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mListener != null) {
                return mListener.onDoubleTap(e.getX(), e.getY());
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mListener != null) {
                return mListener.onScroll(distanceX, distanceY);
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mListener != null) {
                return mListener.onFling(velocityX, velocityY);
            }
            return false;
        }
    };

    private ScaleGestureDetector.SimpleOnScaleGestureListener mScaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if (mListener != null) {
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                return mListener.onScaleBegin(focusX, focusY);
            }
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (mListener != null) {
                float factorX = 1;
                float factorY = 1;
                if (detector.getPreviousSpanX() > 0) {
                    factorX = detector.getCurrentSpanX() / detector.getPreviousSpanX();
                }
                if (detector.getPreviousSpanY() > 0) {
                    factorY = detector.getCurrentSpanY() / detector.getPreviousSpanY();
                }
                return mListener.onScale(factorX, factorY);
            }
            return false;
        }
    };
}
