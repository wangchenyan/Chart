package me.wcy.chart.view.base;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ScrollView;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

import me.wcy.chart.R;
import me.wcy.chart.config.base.GridConfig;
import me.wcy.chart.data.GridData;
import me.wcy.chart.gesture.ChartGestureDetector;
import me.wcy.chart.gesture.ChartGestureListener;
import me.wcy.chart.utils.ChartUtils;

/**
 * Created by hzwangchenyan on 2016/10/9.
 */
public abstract class GridChart extends View {
    private static final int ROW_COUNT = 5;
    private static final int MAX_SCALE = 3;

    private Paint solidLinePaint = new Paint();
    private Paint dashLinePaint = new Paint();
    protected TextPaint textPaint = new TextPaint();
    protected Paint.FontMetrics fontMetrics;
    private Path linePath = new Path();

    private ValueAnimator enterAnimator;
    protected float enterFraction = 1;
    private ValueAnimator doubleTapAnimator;
    private ValueAnimator flingAnimator;
    private Scroller flingScroller;

    private ScrollView scrollView;
    protected GridConfig config;
    private ChartGestureDetector gestureDetector;
    protected List<GridData> dataList = new ArrayList<>();

    protected float defaultItemWidth;
    private float itemHeight;
    private int gridHeight;
    protected float horizontalOffset;

    protected int firstRenderItem;
    protected int lastRenderItem;
    protected List<Integer> renderTitleList = new ArrayList<>();
    private float titleMaxWidth;

    protected float translateX;
    private float scaleFocusX;
    private float scaleValue;

    private boolean hasLayout = false;
    private boolean isNeedAnimation = false;

    public GridChart(Context context) {
        this(context, null);
    }

    public GridChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        config = getConfig();

        TypedArray ta = getResources().obtainAttributes(attrs, R.styleable.GridChart);
        config.setTouchable(ta.getBoolean(R.styleable.GridChart_gcTouchable, true));
        ta.recycle();

        gestureDetector = new ChartGestureDetector(getContext());
        gestureDetector.setGestureListener(gestureListener);

        enterAnimator = ValueAnimator.ofFloat(0, 1);
        enterAnimator.setDuration(config.getEnterAnimationDuration());
        enterAnimator.setInterpolator(new LinearInterpolator());
        enterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                enterFraction = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    public void setDataList(List<GridData> dataList, boolean showAnimation) {
        if (enterAnimator.isRunning()) {
            enterAnimator.end();
        }

        if (dataList == null) {
            throw new IllegalArgumentException("data list can not be null");
        }
        this.dataList.clear();
        this.dataList.addAll(dataList);

        if (hasLayout) {
            onDataChanged();
            if (showAnimation) {
                enterAnimator.start();
            } else {
                invalidate();
            }
        } else {
            isNeedAnimation = showAnimation;
        }
    }

    public void setScrollView(ScrollView scrollView) {
        this.scrollView = scrollView;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!hasLayout) {
            hasLayout = true;
            onDataChanged();

            if (isNeedAnimation) {
                enterAnimator.start();
            }
        }
    }

    protected void onDataChanged() {
        if (dataList.isEmpty()) {
            return;
        }

        translateX = 0;
        scaleValue = 1;
        titleMaxWidth = calculateTitleMaxWidth();
        gridHeight = calculateGridHeight();
        itemHeight = (getChartBottom() - getTextHeight()) / ROW_COUNT;
        horizontalOffset = textPaint.measureText(String.valueOf(gridHeight * 5)) + getTextMargin();
        defaultItemWidth = getChartWidth() / dataList.size();
    }

    protected void setupPaints() {
        // 文字画笔
        textPaint.setAntiAlias(true);
        textPaint.setColor(config.getTextColor());
        textPaint.setTextSize(config.getTextSize());
        fontMetrics = textPaint.getFontMetrics();

        // 虚线画笔
        dashLinePaint.reset();
        dashLinePaint.setColor(config.getGridLineColor());
        dashLinePaint.setStyle(Paint.Style.STROKE);
        dashLinePaint.setStrokeWidth(1);
        // 设置虚线的间隔和点的长度
        float dot = ChartUtils.dp2px(getContext(), 1);
        PathEffect effects = new DashPathEffect(new float[]{dot, dot}, 1);
        dashLinePaint.setPathEffect(effects);

        // 实线画笔
        solidLinePaint.setColor(config.getGridLineColor());
        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(ChartUtils.dp2px(getContext(), 1));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (dataList.isEmpty()) {
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
            return;
        }

        calculateRenderRange();
        calculateRenderTitle();

        drawGridLine(canvas);
        drawGridText(canvas);
        drawDesc(canvas);

        canvas.save();
        canvas.translate(horizontalOffset, 0);
        canvas.clipRect(0, 0, getChartWidth() * enterFraction, getHeight());
        canvas.translate(translateX, 0);
        drawVerticalLine(canvas);
        drawTitle(canvas);
        drawContent(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouch(event);
    }

    private void drawGridLine(Canvas canvas) {
        linePath.reset();
        linePath.moveTo(horizontalOffset, getChartBottom() - itemHeight * 5);
        linePath.lineTo(horizontalOffset, getChartBottom());
        linePath.lineTo(getWidth(), getChartBottom());
        canvas.drawPath(linePath, solidLinePaint);

        linePath.reset();
        for (int i = 1; i <= ROW_COUNT; i++) {
            linePath.moveTo(horizontalOffset, getChartBottom() - itemHeight * i);
            linePath.lineTo(getWidth(), getChartBottom() - itemHeight * i);
        }
        canvas.drawPath(linePath, dashLinePaint);
    }

    private void drawGridText(Canvas canvas) {
        // 绘制水平文字
        textPaint.setColor(config.getTextColor());
        textPaint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= ROW_COUNT; i++) {
            String text = String.valueOf(gridHeight * i);
            canvas.drawText(text, horizontalOffset - getTextMargin(), getChartBottom() - itemHeight * i +
                    getTextHeight() / 2 - getTextOffsetY(), textPaint);
        }
    }

    private void drawVerticalLine(Canvas canvas) {
        linePath.reset();
        for (int i = firstRenderItem; i <= lastRenderItem; i++) {
            if (i > 0) {
                linePath.moveTo(getItemScaledWidth() * i, getChartBottom());
                linePath.lineTo(getItemScaledWidth() * i, getTextHeight());
            }
        }
        canvas.drawPath(linePath, dashLinePaint);
    }

    private void drawTitle(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(getConfig().getTextColor());
        for (int i = 0; i < renderTitleList.size(); i++) {
            int index = renderTitleList.get(i);
            String title = dataList.get(index).getTitle();
            float drawX = getItemScaledWidth() * (index + 0.5f);
            canvas.drawText(title, drawX, getChartBottom() + getBottomTextHeight() - getTextOffsetY(), textPaint);
        }
    }

    private void calculateRenderTitle() {
        float spacing = ChartUtils.dp2px(getContext(), 4);
        float expectChartWidth = titleMaxWidth * dataList.size() + spacing * (dataList.size() - 1);
        int skip = (int) Math.ceil(expectChartWidth / getChartMeasuredWidth());

        renderTitleList.clear();
        for (int i = firstRenderItem; i <= lastRenderItem; i += skip) {
            renderTitleList.add(i);
        }
    }

    private float calculateTitleMaxWidth() {
        float titleMaxWidth = -1;
        for (GridData data : dataList) {
            float width = textPaint.measureText(data.getTitle());
            titleMaxWidth = Math.max(titleMaxWidth, width);
        }
        return titleMaxWidth;
    }

    private int calculateGridHeight() {
        float max = 0;
        for (GridData data : dataList) {
            max = Math.max(max, data.getMaxValue());
        }

        int gridHeight = (int) Math.ceil(max / 5);
        gridHeight = (gridHeight == 0) ? 1 : gridHeight;

        if (gridHeight > 5) {
            gridHeight = (int) Math.ceil((float) gridHeight / 5) * 5;
        }
        return gridHeight;
    }

    protected abstract GridConfig getConfig();

    protected float getTextHeight() {
        return fontMetrics.descent - fontMetrics.ascent;
    }

    protected float getChartBottom() {
        return getHeight() - getDescHeight() - getBottomTextHeight();
    }

    protected float getBottomTextHeight() {
        return getTextHeight() + getTextMargin();
    }

    protected float getDescHeight() {
        return getTextHeight() + ChartUtils.dp2px(getContext(), 10);
    }

    protected float getChartWidth() {
        return getWidth() - horizontalOffset;
    }

    protected float getChartMeasuredWidth() {
        return getItemScaledWidth() * dataList.size();
    }

    protected float getItemScaledWidth() {
        return defaultItemWidth * scaleValue;
    }

    protected float getItemHeightRatio() {
        return itemHeight / gridHeight;
    }

    protected float getTextMargin() {
        return getTextHeight() / 2;
    }

    protected float getTextOffsetY() {
        return fontMetrics.descent;
    }

    protected abstract void drawDesc(Canvas canvas);

    protected abstract void drawContent(Canvas canvas);

    protected abstract void calculateRenderRange();

    private ChartGestureListener gestureListener = new ChartGestureListener() {
        @Override
        public boolean onDown(float x, float y) {
            onTouchStart();
            return config.isTouchable();
        }

        @Override
        public boolean onSingleTap(float x, float y) {
            return GridChart.this.onSingleTap(x, y);
        }

        @Override
        public boolean onDoubleTap(float x, float y) {
            float newScaleFocusX = x - horizontalOffset;
            doubleTap(newScaleFocusX);
            return true;
        }

        @Override
        public boolean onScroll(float distanceX, float distanceY) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                requestDisallowInterceptTouchEvent();
            }

            // 平滑处理
            float offset = -distanceX * 0.5f;
            return updateTranslateX(translateX + offset);
        }

        @Override
        public boolean onFling(float velocityX, float velocityY) {
            return fling(velocityX);
        }

        @Override
        public boolean onScaleBegin(float focusX, float focusY) {
            requestDisallowInterceptTouchEvent();

            scaleFocusX = focusX - horizontalOffset;
            return true;
        }

        @Override
        public boolean onScale(float factorX, float factorY) {
            return scale(factorX);
        }
    };

    protected void onTouchStart() {
        stopFling();
    }

    protected boolean onSingleTap(float x, float y) {
        return false;
    }

    private boolean scale(float factorX) {
        // 平滑处理
        factorX = 1 + (factorX - 1) * 0.5f;
        float newScaleValue = scaleValue * factorX;
        if (newScaleValue < 1) {
            newScaleValue = 1;
        } else if (newScaleValue > MAX_SCALE) {
            newScaleValue = MAX_SCALE;
        }

        return updateScale(newScaleValue);
    }

    private boolean fling(float velocityX) {
        flingScroller = new Scroller(getContext());
        flingScroller.fling((int) translateX, 0, (int) velocityX, 0, (int) (getChartWidth() - getChartMeasuredWidth()), 0, 0, 0);
        flingAnimator = ValueAnimator.ofInt(0, 1);
        // 由Scroller去控制有没有滚动完
        flingAnimator.setDuration(flingScroller.getDuration())
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (flingScroller != null) {
                            if (flingScroller.computeScrollOffset()) {
                                updateTranslateX(flingScroller.getCurrX());
                            } else {
                                flingScroller = null;
                                flingAnimator = null;
                            }
                        }
                    }
                });
        flingAnimator.start();
        return true;
    }

    private void stopFling() {
        if (flingAnimator != null && flingAnimator.isRunning()) {
            flingAnimator.cancel();
        }
    }

    private void doubleTap(float newScaleFocusX) {
        if (doubleTapAnimator != null && doubleTapAnimator.isRunning()) {
            doubleTapAnimator.end();
        }

        scaleFocusX = newScaleFocusX;

        float target = (scaleValue == 1) ? 2 : 1;
        doubleTapAnimator = ValueAnimator.ofFloat(scaleValue, target);
        doubleTapAnimator.setDuration(300);
        doubleTapAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateScale((Float) animation.getAnimatedValue());
            }
        });
        doubleTapAnimator.start();
    }

    private boolean updateScale(float newScaleValue) {
        if (newScaleValue == scaleValue) {
            return false;
        }

        float offset = (translateX - scaleFocusX) * (newScaleValue - scaleValue) / scaleValue;
        translateX += offset;

        if (translateX > 0) {
            translateX = 0;
        } else if (getChartMeasuredWidth() + translateX < getChartWidth()) {
            translateX = getChartWidth() - getChartMeasuredWidth();
        }

        scaleValue = newScaleValue;
        invalidate();
        return true;
    }

    private boolean updateTranslateX(float newTranslateX) {
        if (newTranslateX > 0) {
            newTranslateX = 0;
        } else if (getChartMeasuredWidth() + newTranslateX < getChartWidth()) {
            newTranslateX = getChartWidth() - getChartMeasuredWidth();
        }

        if (newTranslateX != translateX) {
            translateX = newTranslateX;
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    private void requestDisallowInterceptTouchEvent() {
        if (scrollView != null) {
            scrollView.requestDisallowInterceptTouchEvent(true);
        }
    }
}
