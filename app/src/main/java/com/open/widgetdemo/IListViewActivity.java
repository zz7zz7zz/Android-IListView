package com.open.widgetdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.open.widgets.R;
import com.open.widgets.listview.IPullCallBacks;
import com.open.widgets.listview.IListView;
import com.open.widgets.listview.IListViewHeader;

import java.util.ArrayList;

public class IListViewActivity extends Activity implements IPullCallBacks.IPullCallBackListener{

    private static final int PER_PAGE_SIZE = 10;

    //-------------UI-------------
    private IListView listView;
    private TextView  listView_size;

    //-------------DATA-------------
    private Handler mHandler = new Handler();

    private ArrayList<String> bindDataList;
    private IAdapter mIAdapter;

    private int pulldown_count = 0;
    private int min_index = 0;//最小索引
    private int max_index = 0;//最大索引

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_listview);

        bindDataList    = new ArrayList<>();
        mIAdapter       = new IAdapter(getApplicationContext(),bindDataList);

        listView        = (IListView)findViewById(R.id.listView);
        listView_size   = (TextView) findViewById(R.id.listView_size);

        listView.setPullCallBackListener(this);
        listView.setAdapter(mIAdapter);
        listView.startPullDownLoading();
    }

    @Override
    public void onPullDown() {
        //模拟网络回调
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.stopPullLoading(IListView.STATUS_PULL_DOWN,mPullDownCallBack);
            }
        },2000);
    }

    @Override
    public void onPullUp() {
        //模拟网络回调
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.stopPullLoading(IListView.STATUS_PULL_UP,mPullUpCallBack);
            }
        },2000);
    }


    private Runnable mPullDownCallBack = new Runnable() {
        @Override
        public void run() {

            ArrayList<String> t_bindDataList = new ArrayList<>(PER_PAGE_SIZE);
            for (int i = 1;i<=PER_PAGE_SIZE;i++){
                t_bindDataList.add(0,"Data (" + (min_index-i)+" ) ");
            }
            min_index -= t_bindDataList.size();

            if(++pulldown_count %2 == 0){
                listView.sendMessage(IListView.DST_HEADER, IListViewHeader.CMD_HEAD_SET_TOAST_TEXT, String.format("更新了%d个数据",t_bindDataList.size()));
            }

            bindDataList.addAll(0,t_bindDataList);
            mIAdapter.notifyDataSetChanged();

            listView_size.setText("" + bindDataList.size());
        }
    };


    private Runnable mPullUpCallBack = new Runnable() {
        @Override
        public void run() {

            ArrayList<String> t_bindDataList = new ArrayList<>(PER_PAGE_SIZE);
            for (int i = 1;i<=PER_PAGE_SIZE;i++){
                t_bindDataList.add("Data (" + (max_index+i)+" ) ");
            }
            max_index += t_bindDataList.size();

            bindDataList.addAll(t_bindDataList);
            mIAdapter.notifyDataSetChanged();

            listView_size.setText("" + bindDataList.size());
        }
    };

    //------------------------------Adapter------------------------------
    public static class IAdapter extends BaseAdapter{

        private Context mContext;
        private ArrayList<String> bindDataList;

        public IAdapter(Context mContext, ArrayList<String> bindDataList) {
            this.mContext = mContext;
            this.bindDataList = bindDataList;
        }

        @Override
        public int getCount() {
            return bindDataList.size();
        }

        @Override
        public Object getItem(int i) {
            return bindDataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(null == view){
                view = new TextView(mContext);
                ((TextView)view).setHeight(150);
                ((TextView)view).setTextColor(Color.GRAY);
                ((TextView)view).setGravity(Gravity.CENTER);
            }
            ((TextView)view).setText(bindDataList.get(i));
            return view;
        }
    }
}
