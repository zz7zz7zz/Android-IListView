package com.open.widgets.recyclerview;

import android.support.v7.widget.RecyclerView;

/**
 * Created by long on 2017/3/6.
 */

public class IRecycledViewPoll extends RecyclerView.RecycledViewPool{

    public IRecycledViewPoll() {
        super();
        setMaxRecycledViews(HeaderFooterAdapter.BASE_ITEM_VIEW_TYPE_HEADER,0);
        setMaxRecycledViews(HeaderFooterAdapter.BASE_ITEM_VIEW_TYPE_FOOTER,0);
    }

    @Override
    public RecyclerView.ViewHolder getRecycledView(int viewType) {
        return super.getRecycledView(viewType);
    }

    @Override
    public void putRecycledView(RecyclerView.ViewHolder scrap) {
        super.putRecycledView(scrap);
    }

    //---------------------缓存池设置为单列--------------------------
    private static IRecycledViewPoll INS = null;
    public static IRecycledViewPoll getInstance(){
        if(INS == null) {
            INS = new IRecycledViewPoll();
        }
        return INS;
    }

    public static void destroy(){
        if(null != INS){
            INS.clear();
            INS = null;
        }
    }
}
