package me.wcy.chartsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.wcy.chart.data.GridData;
import me.wcy.chart.view.BarChart;
import me.wcy.chart.view.LineChart;
import me.wcy.chart.view.base.GridChart;
import me.wcy.chartsample.pulltorefresh.PullToRefreshLayout;
import me.wcy.chartsample.pulltorefresh.PullableScrollView;

public class MainActivity extends AppCompatActivity implements PullToRefreshLayout.OnRefreshListener {
    private LineChart lineChart;
    private LineChart lineChart2;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PullToRefreshLayout ptrLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        PullableScrollView scrollView = (PullableScrollView) findViewById(R.id.scroll_view);
        lineChart = (LineChart) findViewById(R.id.line_chart);
        lineChart2 = (LineChart) findViewById(R.id.line_chart2);
        barChart = (BarChart) findViewById(R.id.bar_chart);

        ptrLayout.setOnRefreshListener(this);
        scrollView.setEnable(true, false);

        lineChart.setScrollView(scrollView);
        lineChart2.setScrollView(scrollView);
        barChart.setScrollView(scrollView);

        setChartData(lineChart, lineChart2, barChart);
    }

    @Override
    public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
        pullToRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                setChartData(lineChart, lineChart2, barChart);

                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
            }
        }, 1000);
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
    }

    private void setChartData(GridChart... charts) {
        List<GridData> dataList = new ArrayList<>();
        dataList.add(randomData("1月"));
        dataList.add(randomData("2月"));
        dataList.add(randomData("3月"));
        dataList.add(randomData("4月"));
        dataList.add(randomData("5月"));
        dataList.add(randomData("6月"));
        dataList.add(randomData("7月"));
        dataList.add(randomData("8月"));
        dataList.add(randomData("9月"));
        dataList.add(randomData("10月"));
        dataList.add(randomData("11月"));
        dataList.add(randomData("12月"));

        for (GridChart chart : charts) {
            chart.setDataList(dataList, true);
        }
    }

    private GridData randomData(String title) {
        GridData.Entry[] entries = new GridData.Entry[3];
        String[] descs = new String[]{"2015年", "2016年", "2017年"};
        int[] colors = new int[]{0xFF7394E7, 0xFFF87FA9, 0xFF60D1AC};
        for (int i = 0; i < 3; i++) {
            entries[i] = new GridData.Entry(colors[i], descs[i], new Random().nextInt(101));
        }
        return new GridData(title, entries);
    }
}
