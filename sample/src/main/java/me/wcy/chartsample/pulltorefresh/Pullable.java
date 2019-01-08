package me.wcy.chartsample.pulltorefresh;

public interface Pullable {
    boolean canPullDown();

    boolean canPullUp();

    void setEnable(boolean pullToRefresh, boolean pullToLoadMore);
}
