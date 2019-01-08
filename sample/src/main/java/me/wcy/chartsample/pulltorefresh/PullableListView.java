package me.wcy.chartsample.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by hzwangchenyan on 2016/10/28.
 */
public class PullableListView extends ListView implements Pullable {
    private boolean mEnablePullToRefresh = false;
    private boolean mEnablePullToLoadMore = false;

    public PullableListView(Context context) {
        super(context);
    }

    public PullableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean canPullDown() {
        return mEnablePullToRefresh && !canScrollVertically(-1);
    }

    @Override
    public boolean canPullUp() {
        return mEnablePullToLoadMore && !canScrollVertically(1);
    }

    @Override
    public void setEnable(boolean pullToRefresh, boolean pullToLoadMore) {
        mEnablePullToRefresh = pullToRefresh;
        mEnablePullToLoadMore = pullToLoadMore;
    }
}
