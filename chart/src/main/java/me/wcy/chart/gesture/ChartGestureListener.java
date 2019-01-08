package me.wcy.chart.gesture;

public class ChartGestureListener {

    public boolean onDown(float x, float y) {
        return false;
    }

    public void onShowPress(float x, float y) {
    }

    public void onLongPress(float x, float y) {
    }

    public boolean onSingleTap(float x, float y) {
        return false;
    }

    public boolean onDoubleTap(float x, float y) {
        return false;
    }

    public boolean onScroll(float distanceX, float distanceY) {
        return false;
    }

    public boolean onFling(float velocityX, float velocityY) {
        return false;
    }

    public boolean onScaleBegin(float focusX, float focusY) {
        return false;
    }

    public boolean onScale(float factorX, float factorY) {
        return false;
    }
}
