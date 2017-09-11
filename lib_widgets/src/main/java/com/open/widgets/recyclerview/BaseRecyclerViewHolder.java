package com.open.widgets.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by long on 2016/12/29.
 */

public class BaseRecyclerViewHolder extends RecyclerView.ViewHolder{

    public BaseRecyclerViewHolder(View itemView)
    {
        super(itemView);
    }

    public static BaseRecyclerViewHolder createViewHolder(View itemView)
    {
        BaseRecyclerViewHolder holder = new BaseRecyclerViewHolder(itemView);
        return holder;
    }

    public static BaseRecyclerViewHolder createViewHolder(Context context, ViewGroup parent, int layoutId) {
        return createViewHolder(LayoutInflater.from(context),parent,layoutId);
    }

    public static BaseRecyclerViewHolder createViewHolder(LayoutInflater mLayoutInflater, ViewGroup parent, int layoutId) {
        View itemView = mLayoutInflater.inflate(layoutId, parent, false);
        BaseRecyclerViewHolder holder = new BaseRecyclerViewHolder(itemView);
        return holder;
    }
}
