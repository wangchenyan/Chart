package me.wcy.chartsample.pulltorefresh;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import me.wcy.chartsample.R;

/**
 * 自定义的布局，用来管理三个子控件，其中一个是下拉头，
 * 一个是包含内容的PullableView（可以是实现{@link Pullable}接口的的任何View），
 * 还有一个上拉头。
 */
public class PullToRefreshLayout extends RelativeLayout {
    // 刷新成功
    public static final int SUCCEED = 0;
    // 刷新失败
    public static final int FAIL = 1;
    // 没有更多了
    public static final int COMPLETE = 2;

    // 初始状态
    private static final int INIT = 0;
    // 释放刷新
    private static final int RELEASE_TO_REFRESH = 1;
    // 正在刷新
    private static final int REFRESHING = 2;
    // 释放加载
    private static final int RELEASE_TO_LOAD = 3;
    // 正在加载
    private static final int LOADING = 4;
    // 操作完毕
    private static final int DONE = 5;
    // 当前状态
    private int state = INIT;

    // 刷新回调接口
    private OnRefreshListener listener;

    private float lastY;

    // 下拉的距离。注意：pullDownY和pullUpY不可能同时不为0
    private float pullDownY = 0;
    // 上拉的距离
    private float pullUpY = 0;

    // 释放刷新的距离
    private float refreshDist = 200;
    // 释放加载的距离
    private float loadMoreDist = 200;

    private RefreshTimer refreshTimer = new RefreshTimer();
    // 执行自动回滚的handler
    private Handler refreshHandler = new Handler();
    // 回滚速度
    private float MOVE_SPEED = 8;
    // 第一次执行布局
    private boolean isLayout = false;
    // 在刷新过程中滑动操作
    private boolean isTouch = false;
    // 手指滑动距离与下拉头的滑动距离比，中间会随正切函数变化
    private float radio = 2;

    // 箭头旋转动画
    private RotateAnimation rotateAnimationCW;
    private RotateAnimation rotateAnimationCCW;

    // 下拉头
    private View refreshView;
    // 下拉的箭头
    private ImageView pullView;
    // 正在刷新的图标
    private ProgressBar refreshingView;
    // 刷新结果图标
    // private ImageView refreshStateImageView;
    // 刷新结果：成功或失败
    private TextView refreshStateTextView;

    // 上拉头
    private View loadMoreView;
    // 上拉的箭头
    private ImageView pullUpView;
    // 正在加载的图标
    private ProgressBar loadingView;
    // 加载结果图标
    // private ImageView loadStateImageView;
    // 加载结果：成功或失败
    private TextView loadStateTextView;

    // 实现了Pullable接口的View
    private View pullableView;
    // 过滤多点触碰
    private int events;
    // 这两个变量用来控制pull的方向，如果不加控制，当情况满足可上拉又可下拉时没法下拉
    private boolean canPullDown = true;
    private boolean canPullUp = true;

    public PullToRefreshLayout(Context context) {
        super(context);
        init();
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        rotateAnimationCW = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimationCW.setInterpolator(new LinearInterpolator());
        rotateAnimationCW.setFillAfter(true);
        rotateAnimationCW.setDuration(100);
        rotateAnimationCCW = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimationCCW.setInterpolator(new LinearInterpolator());
        rotateAnimationCCW.setFillAfter(true);
        rotateAnimationCCW.setDuration(100);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        refreshView = LayoutInflater.from(getContext()).inflate(R.layout.ptr_header, this, false);
        pullableView = LayoutInflater.from(getContext()).inflate(R.layout.ptr_footer, this, false);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(refreshView, 0, lp);
        addView(pullableView, lp);
    }

    @Override
    protected void onDetachedFromWindow() {
        refreshTimer.release();
        refreshTimer = null;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!isLayout) {
            // 这里是第一次进来的时候做一些初始化
            refreshView = getChildAt(0);
            pullableView = getChildAt(1);
            loadMoreView = getChildAt(2);
            isLayout = true;
            findView();
            refreshDist = ((ViewGroup) refreshView).getChildAt(0).getMeasuredHeight();
            loadMoreDist = ((ViewGroup) loadMoreView).getChildAt(0).getMeasuredHeight();
        }
        // 改变子控件的布局，这里直接用(pullDownY + pullUpY)作为偏移量，这样就可以不对当前状态作区分
        refreshView.layout(0, (int) (pullDownY + pullUpY) - refreshView.getMeasuredHeight(),
                refreshView.getMeasuredWidth(), (int) (pullDownY + pullUpY));
        pullableView.layout(0, (int) (pullDownY + pullUpY), pullableView.getMeasuredWidth(),
                (int) (pullDownY + pullUpY) + pullableView.getMeasuredHeight());
        loadMoreView.layout(0, (int) (pullDownY + pullUpY) + pullableView.getMeasuredHeight(), loadMoreView.getMeasuredWidth(),
                (int) (pullDownY + pullUpY) + pullableView.getMeasuredHeight() + loadMoreView.getMeasuredHeight());
    }

    private void findView() {
        // 初始化下拉布局
        pullView = (ImageView) refreshView.findViewById(R.id.ptr_header_pull_icon);
        refreshingView = (ProgressBar) refreshView.findViewById(R.id.ptr_header_refreshing_icon);
        // refreshStateImageView = (ImageView) refreshView.findViewById(R.id.ptr_header_state_icon);
        refreshStateTextView = (TextView) refreshView.findViewById(R.id.ptr_header_state_hint);
        // 初始化上拉布局
        pullUpView = (ImageView) loadMoreView.findViewById(R.id.ptr_footer_pull_icon);
        loadingView = (ProgressBar) loadMoreView.findViewById(R.id.ptr_footer_loading_icon);
        // loadStateImageView = (ImageView) loadMoreView.findViewById(R.id.ptr_footer_state_icon);
        loadStateTextView = (TextView) loadMoreView.findViewById(R.id.ptr_footer_state_hint);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    public boolean isInProgress() {
        return state == REFRESHING || state == LOADING;
    }

    /**
     * 自动刷新
     */
    public void autoRefresh() {
        AutoRefreshTask task = new AutoRefreshTask();
        task.execute(20);
    }

    /**
     * 自动加载
     */
    public void autoLoadMore() {
        pullUpY = -loadMoreDist;
        requestLayout();
        updateState(LOADING);
        // 加载操作
        if (listener != null) {
            listener.onLoadMore(this);
        }
    }

    /**
     * 完成刷新操作，显示刷新结果。注意：刷新完成后一定要调用这个方法
     *
     * @param refreshResult PullToRefreshLayout.SUCCEED代表成功，PullToRefreshLayout.FAIL代表失败
     */
    public void refreshFinish(int refreshResult) {
        refreshingView.setVisibility(View.GONE);
        switch (refreshResult) {
            case SUCCEED:
                // 刷新成功
                // refreshStateImageView.setVisibility(View.VISIBLE);
                // refreshStateImageView.setImageResource(R.drawable.ptr_refresh_succeed);
                refreshStateTextView.setText(R.string.ptr_refresh_succeed);
                break;
            case FAIL:
            default:
                // 刷新失败
                // refreshStateImageView.setVisibility(View.VISIBLE);
                // refreshStateImageView.setImageResource(R.drawable.ptr_refresh_failed);
                refreshStateTextView.setText(R.string.ptr_refresh_failed);
                break;
        }
        if (pullDownY > 0) {
            // 刷新结果停留1秒
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateState(DONE);
                    hide();
                }
            }, 1000);
        } else {
            updateState(DONE);
            hide();
        }
    }

    /**
     * 加载完毕，显示加载结果。注意：加载完成后一定要调用这个方法
     *
     * @param refreshResult PullToRefreshLayout.SUCCEED代表成功，PullToRefreshLayout.FAIL代表失败
     */
    public void loadMoreFinish(int refreshResult) {
        loadingView.setVisibility(View.GONE);
        switch (refreshResult) {
            case SUCCEED:
                // 加载成功
                // loadStateImageView.setVisibility(View.VISIBLE);
                // loadStateImageView.setImageResource(R.drawable.ptr_refresh_succeed);
                loadStateTextView.setText(R.string.ptr_load_succeed);
                break;
            case COMPLETE:
                // 没有更多了
                // loadStateImageView.setVisibility(View.VISIBLE);
                // loadStateImageView.setImageResource(R.drawable.ptr_refresh_succeed);
                loadStateTextView.setText(R.string.ptr_load_completed);
                break;
            case FAIL:
            default:
                // 加载失败
                // loadStateImageView.setVisibility(View.VISIBLE);
                // loadStateImageView.setImageResource(R.drawable.ptr_refresh_failed);
                loadStateTextView.setText(R.string.ptr_load_failed);
                break;
        }
        if (pullUpY < 0) {
            // 刷新结果停留1秒
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateState(DONE);
                    hide();
                }
            }, 1000);
        } else {
            updateState(DONE);
            hide();
        }
    }

    private void updateState(int newState) {
        state = newState;
        switch (state) {
            case INIT:
                // 下拉布局初始状态
                if (pullView.getAnimation() != null) {
                    // 从 RELEASE_TO_REFRESH 状态返回
                    pullView.startAnimation(rotateAnimationCCW);
                }
                pullView.setVisibility(View.VISIBLE);
                // refreshStateImageView.setVisibility(View.GONE);
                refreshStateTextView.setText(R.string.ptr_pull_to_refresh);
                // 上拉布局初始状态
                if (pullUpView.getAnimation() != null) {
                    // 从 RELEASE_TO_LOAD 状态返回
                    pullUpView.startAnimation(rotateAnimationCCW);
                }
                pullUpView.setVisibility(View.VISIBLE);
                // loadStateImageView.setVisibility(View.GONE);
                loadStateTextView.setText(R.string.ptr_pull_to_load);
                break;
            case RELEASE_TO_REFRESH:
                // 释放刷新状态
                pullView.startAnimation(rotateAnimationCW);
                refreshStateTextView.setText(R.string.ptr_release_to_refresh);
                break;
            case REFRESHING:
                // 正在刷新状态
                pullView.clearAnimation();
                pullView.setVisibility(View.INVISIBLE);
                refreshingView.setVisibility(View.VISIBLE);
                refreshStateTextView.setText(R.string.ptr_refreshing);
                break;
            case RELEASE_TO_LOAD:
                // 释放加载状态
                pullUpView.startAnimation(rotateAnimationCW);
                loadStateTextView.setText(R.string.ptr_release_to_load);
                break;
            case LOADING:
                // 正在加载状态
                pullUpView.clearAnimation();
                pullUpView.setVisibility(View.INVISIBLE);
                loadingView.setVisibility(View.VISIBLE);
                loadStateTextView.setText(R.string.ptr_loading);
                break;
            case DONE:
                // 刷新或加载完毕，啥都不做
                break;
        }
    }

    /**
     * 由父控件决定是否分发事件，防止事件冲突
     *
     * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastY = ev.getY();
                refreshTimer.cancel();
                events = 0;
                releasePull();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                // 过滤多点触碰
                events = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (events == 0) {
                    if (pullDownY > 0 || (((Pullable) pullableView).canPullDown() && canPullDown && state != LOADING)) {
                        // 可以下拉，正在加载时不能下拉
                        // 对实际滑动距离做缩小，造成用力拉的感觉
                        pullDownY = pullDownY + (ev.getY() - lastY) / radio;
                        if (pullDownY < 0) {
                            pullDownY = 0;
                            canPullDown = false;
                            canPullUp = true;
                        }
                        if (pullDownY > getMeasuredHeight()) {
                            pullDownY = getMeasuredHeight();
                        }
                        if (state == REFRESHING) {
                            // 正在刷新的时候触摸移动
                            isTouch = true;
                        }
                    } else if (pullUpY < 0 || (((Pullable) pullableView).canPullUp() && canPullUp && state != REFRESHING)) {
                        // 可以上拉，正在刷新时不能上拉
                        pullUpY = pullUpY + (ev.getY() - lastY) / radio;
                        if (pullUpY > 0) {
                            pullUpY = 0;
                            canPullDown = true;
                            canPullUp = false;
                        }
                        if (pullUpY < -getMeasuredHeight()) {
                            pullUpY = -getMeasuredHeight();
                        }
                        if (state == LOADING) {
                            // 正在加载的时候触摸移动
                            isTouch = true;
                        }
                    } else {
                        releasePull();
                    }
                } else {
                    events = 0;
                }
                lastY = ev.getY();
                // 根据下拉距离改变比例
                radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * (pullDownY + Math.abs(pullUpY))));
                if (pullDownY > 0 || pullUpY < 0) {
                    requestLayout();
                }
                if (pullDownY > 0) {
                    if (pullDownY <= refreshDist && (state == RELEASE_TO_REFRESH || state == DONE)) {
                        // 如果下拉距离没达到刷新的距离且当前状态是释放刷新，改变状态为下拉刷新
                        updateState(INIT);
                    }
                    if (pullDownY >= refreshDist && state == INIT) {
                        // 如果下拉距离达到刷新的距离且当前状态是初始状态刷新，改变状态为释放刷新
                        updateState(RELEASE_TO_REFRESH);
                    }
                } else if (pullUpY < 0) {
                    // 下面是判断上拉加载的，同上，注意pullUpY是负值
                    if (-pullUpY <= loadMoreDist && (state == RELEASE_TO_LOAD || state == DONE)) {
                        updateState(INIT);
                    }
                    // 上拉操作
                    if (-pullUpY >= loadMoreDist && state == INIT) {
                        updateState(RELEASE_TO_LOAD);
                    }
                }
                // 因为刷新和加载操作不能同时进行，所以pullDownY和pullUpY不会同时不为0，
                // 因此这里用(pullDownY + Math.abs(pullUpY))就可以不对当前状态作区分了
                if ((pullDownY + Math.abs(pullUpY)) > 8) {
                    // 防止下拉过程中误触发长按事件和点击事件
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (pullDownY > refreshDist || -pullUpY > loadMoreDist) {
                    // 正在刷新时往下拉（正在加载时往上拉），释放后下拉头（上拉头）不隐藏
                    isTouch = false;
                }
                if (state == RELEASE_TO_REFRESH) {
                    updateState(REFRESHING);
                    // 刷新操作
                    if (listener != null) {
                        listener.onRefresh(this);
                    }
                } else if (state == RELEASE_TO_LOAD) {
                    updateState(LOADING);
                    // 加载操作
                    if (listener != null) {
                        listener.onLoadMore(this);
                    }
                }
                hide();
            default:
                break;
        }
        // 事件分发交给父类
        super.dispatchTouchEvent(ev);
        return true;
    }

    /**
     * 不限制上拉或下拉
     */
    private void releasePull() {
        canPullDown = true;
        canPullUp = true;
    }

    private void hide() {
        if (refreshTimer != null) {
            refreshTimer.schedule(5);
        }
    }

    /**
     * 自动模拟手指滑动的task
     */
    private class AutoRefreshTask extends AsyncTask<Integer, Float, String> {
        @Override
        protected String doInBackground(Integer... params) {
            while (pullDownY < 4 / 3 * refreshDist) {
                pullDownY += MOVE_SPEED;
                publishProgress(pullDownY);
                try {
                    Thread.sleep(params[0]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            updateState(REFRESHING);
            // 刷新操作
            if (listener != null) {
                listener.onRefresh(PullToRefreshLayout.this);
            }
            hide();
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            if (pullDownY > refreshDist) {
                updateState(RELEASE_TO_REFRESH);
            }
            requestLayout();
        }
    }

    private class RefreshTimer {
        private Timer timer;
        private RefreshTask refreshTask;

        public RefreshTimer() {
            timer = new Timer();
        }

        public void schedule(long period) {
            if (refreshTask != null) {
                refreshTask.cancel();
                refreshTask = null;
            }
            refreshTask = new RefreshTask();
            timer.schedule(refreshTask, 0, period);
        }

        public void cancel() {
            if (refreshTask != null) {
                refreshTask.cancel();
                refreshTask = null;
            }
        }

        public void release() {
            cancel();
            timer.cancel();
            timer = null;
        }

        private class RefreshTask extends TimerTask {
            @Override
            public void run() {
                refreshHandler.post(refreshRunnable);
            }
        }
    }

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            // 回弹速度随下拉距离moveDeltaY增大而增大
            MOVE_SPEED = (float) (8 + 5 * Math.tan(Math.PI / 2 / getMeasuredHeight() * (pullDownY + Math.abs(pullUpY))));
            if (!isTouch) {
                // 正在刷新，且没有往上推的话则悬停，显示"正在刷新..."
                if (state == REFRESHING && pullDownY <= refreshDist) {
                    pullDownY = refreshDist;
                    refreshTimer.cancel();
                } else if (state == LOADING && -pullUpY <= loadMoreDist) {
                    pullUpY = -loadMoreDist;
                    refreshTimer.cancel();
                }
            }
            if (pullDownY > 0) {
                pullDownY -= MOVE_SPEED;
            } else if (pullUpY < 0) {
                pullUpY += MOVE_SPEED;
            }
            if (pullDownY < 0) {
                // 已完成回弹
                pullDownY = 0;
                pullView.clearAnimation();
                // 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
                if (state != REFRESHING && state != LOADING) {
                    updateState(INIT);
                }
                refreshTimer.cancel();
                requestLayout();
            }
            if (pullUpY > 0) {
                // 已完成回弹
                pullUpY = 0;
                pullUpView.clearAnimation();
                // 隐藏上拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
                if (state != REFRESHING && state != LOADING) {
                    updateState(INIT);
                }
                refreshTimer.cancel();
                requestLayout();
            }
            // 刷新布局,会自动调用onLayout
            requestLayout();
            // 没有拖拉或者回弹完成
            if (pullDownY + Math.abs(pullUpY) == 0) {
                refreshTimer.cancel();
            }
        }
    };

    public interface OnRefreshListener {
        void onRefresh(PullToRefreshLayout pullToRefreshLayout);

        void onLoadMore(PullToRefreshLayout pullToRefreshLayout);
    }
}
