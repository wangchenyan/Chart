package me.wcy.chartsample.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class PullableScrollView extends ScrollView implements Pullable {
    private boolean mEnablePullToRefresh = false;
    private boolean mEnablePullToLoadMore = false;

    public PullableScrollView(Context context) {
        super(context);
    }

    public PullableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
