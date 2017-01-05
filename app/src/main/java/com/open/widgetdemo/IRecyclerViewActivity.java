package com.open.widgetdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.open.widgets.R;
import com.open.widgets.listview.IListView;
import com.open.widgets.listview.IListViewHeader;
import com.open.widgets.recyclerview.BaseRecyclerAdapter;
import com.open.widgets.recyclerview.BaseRecyclerViewHolder;
import com.open.widgets.recyclerview.DividerGridItemDecoration;
import com.open.widgets.recyclerview.DividerLinearItemDecoration;
import com.open.widgets.recyclerview.IRecyclerView;

import java.util.ArrayList;

public class IRecyclerViewActivity extends Activity implements IListView.IPullEventListener{

    //-------------UI-------------
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.ItemDecoration decor;
    private IRecyclerView mIRecyclerView;
    private TextView linear;
    private TextView grid;
    private TextView staggeredGrid;
    private TextView currentTextView;

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
        setContentView(R.layout.demo_recyclerview);



        mIRecyclerView  = (IRecyclerView)findViewById(R.id.listView);
        linear          = (TextView) findViewById(R.id.linear);
        grid            = (TextView) findViewById(R.id.grid);
        staggeredGrid   = (TextView) findViewById(R.id.staggeredGrid);

        View.OnClickListener listener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                switch (v.getId()){

                    case R.id.linear:
                        currentTextView = linear;
                        linearLayout();
                        break;

                    case R.id.grid:
                        currentTextView = grid;
                        gridLayout();
                        break;

                    case R.id.staggeredGrid:
                        currentTextView = staggeredGrid;
                        staggeredGridLayout();
                        break;
                }

                linear.setText("linear");
                grid.setText("grid");
                staggeredGrid.setText("staggeredGrid");

                pulldown_count = 0;
                min_index = 0;
                max_index = 0;
            }
        };

        linear.setOnClickListener(listener);
        grid.setOnClickListener(listener);
        staggeredGrid.setOnClickListener(listener);

        linear.performClick();
    }

    private void linearLayout(){
        mLayoutManager      = new LinearLayoutManager(getApplicationContext());
        //((LinearLayoutManager)mLayoutManager).setOrientation(LinearLayoutManager.HORIZONTAL);

        decor = new DividerLinearItemDecoration(mLayoutManager.canScrollVertically() ? DividerLinearItemDecoration.ORIENTATION_VERTICAL : DividerLinearItemDecoration.ORIENTATION_HORIZONTAL,
                ContextCompat.getDrawable(getApplicationContext(),R.drawable.linear_itemdecoration),true);

        bindDataList    = new ArrayList<>();
        mIAdapter       = new IAdapter(getApplicationContext(),bindDataList);

        mIRecyclerView.setLayoutManager(mLayoutManager);
        mIRecyclerView.addItemDecoration(decor);
        mIRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mIRecyclerView.setAdapter(mIAdapter);

        mIRecyclerView.setPullEventListener(this);
        mIRecyclerView.startPullDownLoading();
    }

    private void gridLayout(){
        mLayoutManager      = new GridLayoutManager(getApplicationContext(), 2);
        decor = new DividerGridItemDecoration(ContextCompat.getDrawable(getApplicationContext(),R.drawable.linear_itemdecoration));

        bindDataList    = new ArrayList<>();
        mIAdapter       = new IAdapter(getApplicationContext(),bindDataList);

        mIRecyclerView.setLayoutManager(mLayoutManager);
        mIRecyclerView.addItemDecoration(decor);
        mIRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mIRecyclerView.setAdapter(mIAdapter);


        mIRecyclerView.setPullEventListener(this);
        mIRecyclerView.startPullDownLoading();
    }

    private void staggeredGridLayout(){
        mLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        decor = new DividerGridItemDecoration(ContextCompat.getDrawable(getApplicationContext(),R.drawable.linear_itemdecoration));

        bindDataList    = new ArrayList<>();
        mIAdapter       = new IAdapter(getApplicationContext(),bindDataList);

        mIRecyclerView.setLayoutManager(mLayoutManager);
        mIRecyclerView.addItemDecoration(decor);
        mIRecyclerView.setItemAnimator(new DefaultItemAnimator());
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
            mIAdapter.notifyDataSetChanged();
//            mIAdapter.notifyItemRangeInserted(mIRecyclerView,0,10);

//            mIRecyclerView.getAdapter().notifyDataSetChanged();
//            mIRecyclerView.getAdapter().notifyItemRangeInserted(0,10);

            currentTextView.setText("" + bindDataList.size());
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
            mIAdapter.notifyItemRangeInserted(mIRecyclerView,oldSize,10);
//            mIRecyclerView.getAdapter().notifyItemRangeInserted(oldSize,10);

            currentTextView.setText("" + bindDataList.size());
        }
    };

    //------------------------------Adapter------------------------------
    public static class IAdapter extends BaseRecyclerAdapter{

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
            BaseRecyclerViewHolder realHolder = (BaseRecyclerViewHolder)holder;
            ((TextView)(realHolder.itemView)).setText(bindDataList.get(position));
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.v(TAG,"onCreateViewHolder " + viewType);
            return BaseRecyclerViewHolder.createViewHolder(mContext,parent,R.layout.demo_recyclerview_item);
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
