package me.wcy.chart.config;

import me.wcy.chart.config.base.GridConfig;

/**
 * Created by hzwangchenyan on 2016/10/10.
 */
public class LineConfig extends GridConfig {
    // 曲线/折线
    private boolean isCurvedLine = false;
    // 是否显示阴影
    private boolean isShowShadow = false;
    // 是否是渐变阴影
    private boolean isGradientShadow = false;

    private float lineWidth = 6;

    public boolean isCurvedLine() {
        return isCurvedLine;
    }

    public void setCurvedLine(boolean curvedLine) {
        isCurvedLine = curvedLine;
    }

    public boolean isShowShadow() {
        return isShowShadow;
    }

    public void setShowShadow(boolean showShadow) {
        isShowShadow = showShadow;
    }

    public boolean isGradientShadow() {
        return isGradientShadow;
    }

    public void setGradientShadow(boolean gradientShadow) {
        isGradientShadow = gradientShadow;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }
}
