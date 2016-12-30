package com.open.widgetdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.open.widgets.R;
import com.open.widgets.listview.IListView;
import com.open.widgets.listview.IListViewHeader;
import com.open.widgets.recyclerview.BaseViewHolder;
import com.open.widgets.recyclerview.IRecyclerView;

import java.util.ArrayList;

public class IRecyclerViewActivity extends Activity implements IListView.IPullEventListener{

    //-------------UI-------------
    private LinearLayoutManager mLayoutManager;
    private IRecyclerView mIRecyclerView;
    private TextView  listView_size;

    //-------------DATA-------------
    private Handler mHandler = new Handler();

    private ArrayList<String> bindDataList;
    private IAdapter mIAdapter;

    private int min_index = 0;//最小索引
    private int max_index = 0;//最大索引

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_recyclerview);

        bindDataList    = new ArrayList<>();
        mIAdapter       = new IAdapter(getApplicationContext(),bindDataList);

        mIRecyclerView = (IRecyclerView)findViewById(R.id.listView);
        listView_size   = (TextView) findViewById(R.id.listView_size);

        mLayoutManager      = new LinearLayoutManager(getApplicationContext());
        mIRecyclerView.setLayoutManager(mLayoutManager);
        mIRecyclerView.setAdapter(mIAdapter);


        mIRecyclerView.setPullEventListener(this);
        mIRecyclerView.startPullDownLoading();
    }

    @Override
    public void onPullDown() {
        //模拟网络回调
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIRecyclerView.stopPullLoading(IListView.STATUS_PULL_DOWN,mPullDownCallBack);
            }
        },2000);
    }

    @Override
    public void onPullUp() {
        //模拟网络回调
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIRecyclerView.stopPullLoading(IListView.STATUS_PULL_UP,mPullUpCallBack);
            }
        },2000);
    }

    private int pulldown_count = 0;
    private Runnable mPullDownCallBack = new Runnable() {
        @Override
        public void run() {

            ArrayList<String> t_bindDataList = new ArrayList<>(10);
            for (int i = 1;i<=10;i++){
                t_bindDataList.add(0,"Data (" + (min_index-i)+" ) ");
            }
            min_index -= t_bindDataList.size();

            if(++pulldown_count %2 == 0){
                mIRecyclerView.sendMessage(IListView.DST_HEADER, IListViewHeader.CMD_HEAD_SET_TOAST_TEXT, String.format("更新了%d个数据",t_bindDataList.size()));
            }

            bindDataList.addAll(0,t_bindDataList);
//            mIAdapter.notifyDataSetChanged();
//            mIAdapter.notifyItemRangeChanged(0,10);
//            mIAdapter.notifyItemRangeChanged(0,bindDataList.size());
//            mIAdapter.notifyItemRangeInserted(0,10);

            mIRecyclerView.getAdapter().notifyDataSetChanged();
//            mIRecyclerView.getAdapter().notifyItemRangeInserted(0,10);

            listView_size.setText("" + bindDataList.size());
        }
    };


    private Runnable mPullUpCallBack = new Runnable() {
        @Override
        public void run() {

            ArrayList<String> t_bindDataList = new ArrayList<>(10);
            for (int i = 1;i<=10;i++){
                t_bindDataList.add("Data (" + (max_index+i)+" ) ");
            }
            max_index += t_bindDataList.size();
            int oldSize = bindDataList.size();
            bindDataList.addAll(t_bindDataList);
//            mIAdapter.notifyItemRangeInserted(oldSize,10);
            mIRecyclerView.getAdapter().notifyItemRangeInserted(oldSize,10);
            listView_size.setText("" + bindDataList.size());
        }
    };

    //------------------------------Adapter------------------------------
    public static class IAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private static final String  TAG = "IAdapter";

        private Context mContext;
        private ArrayList<String> bindDataList;

        public IAdapter(Context mContext, ArrayList<String> bindDataList) {
            this.mContext = mContext;
            this.bindDataList = bindDataList;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Log.v(TAG,"onBindViewHolder " + position + " text"+ bindDataList.get(position));
            BaseViewHolder realHolder = (BaseViewHolder)holder;
            ((TextView)(realHolder.itemView)).setText(bindDataList.get(position));
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            TextView view = new TextView(mContext);
//            view.setHeight(150);
//            view.setBackgroundColor(Color.GREEN);
//            view.setTextColor(Color.GRAY);
//            view.setGravity(Gravity.CENTER);
//            return BaseViewHolder.createViewHolder(view);

            Log.v(TAG,"onCreateViewHolder " + viewType);
            return BaseViewHolder.createViewHolder(mContext,parent,R.layout.demo_recyclerview_item);
        }

        @Override
        public int getItemCount() {
            return bindDataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }
}
