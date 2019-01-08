package me.wcy.chart.config.base;

import android.graphics.Color;

public abstract class GridConfig {
    private boolean isTouchable = true;

    private long enterAnimationDuration = 600;
    private int gridLineColor = Color.LTGRAY;
    private float textSize = 36;
    private int textColor = Color.GRAY;

    public boolean isTouchable() {
        return isTouchable;
    }

    public void setTouchable(boolean touchable) {
        isTouchable = touchable;
    }

    public long getEnterAnimationDuration() {
        return enterAnimationDuration;
    }

    public void setEnterAnimationDuration(long enterAnimationDuration) {
        this.enterAnimationDuration = enterAnimationDuration;
    }

    public int getGridLineColor() {
        return gridLineColor;
    }

    public void setGridLineColor(int gridLineColor) {
        this.gridLineColor = gridLineColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}
