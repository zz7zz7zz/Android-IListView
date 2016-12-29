package com.open.widgets.recyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by long on 2016/12/29.
 */

public class IRecyclerView extends RecyclerView {

    private SparseArrayCompat<View> mHeaderViewInfos = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFooterViewInfos = new SparseArrayCompat<>();
    private View mEmptyView;

    public IRecyclerView(Context context) {
        super(context);
    }

    public IRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //-------------------------------------------------------------------------------------------
    public void addHeaderView(View v) {
        if(null == v){
            return;
        }
        mHeaderViewInfos.put(mHeaderViewInfos.size() + HeaderFooterAdapter.BASE_ITEM_VIEW_TYPE_HEADER,v);
        Adapter mAdapter = getAdapter();
        if(null != mAdapter){
            if (!(mAdapter instanceof HeaderFooterAdapter)) {
                wrapHeaderListAdapterInternal();
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public boolean removeHeaderView(View v) {
        if(null == v){
            return false;
        }
        if (mHeaderViewInfos.size() > 0) {
            boolean result = false;
            Adapter mAdapter = getAdapter();
            if (mAdapter != null && ((HeaderFooterAdapter) mAdapter).removeHeader(v)) {
                result = true;
                mAdapter.notifyDataSetChanged();
            }
            removeFixedViewInfo(v,mHeaderViewInfos);
            return result;
        }
        return false;
    }

    public void addFooterView(View v) {
        if(null == v){
            return;
        }
        mFooterViewInfos.put(mFooterViewInfos.size() + HeaderFooterAdapter.BASE_ITEM_VIEW_TYPE_FOOTER,v);
        Adapter mAdapter = getAdapter();
        if(null != mAdapter){
            if (!(mAdapter instanceof HeaderFooterAdapter)) {
                wrapHeaderListAdapterInternal();
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public boolean removeFooterView(View v){
        if(null == v){
            return false;
        }
        if (mFooterViewInfos.size() > 0) {
            boolean result = false;
            Adapter mAdapter = getAdapter();
            if (mAdapter != null && ((HeaderFooterAdapter) mAdapter).removeFooter(v)) {
                result = true;
                mAdapter.notifyDataSetChanged();
            }
            removeFixedViewInfo(v,mFooterViewInfos);
            return result;
        }
        return false;
    }

    private void removeFixedViewInfo(View v, SparseArrayCompat<View> where) {
        int len = where.size();
        for (int i = 0; i < len; ++i) {
            Object obj = mHeaderViewInfos.valueAt(i);
            if(v == obj){
                where.removeAt(i);
                break;
            }
        }
    }

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;

        final Adapter adapter = getAdapter();
        final boolean empty = ((adapter == null) || adapter.getItemCount() == 0);
        updateEmptyStatus(empty);
    }

    private void updateEmptyStatus(boolean empty) {
        if (empty) {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.VISIBLE);
                setVisibility(View.GONE);
            } else {
                // If the caller just removed our empty view, make sure the list view is visible
                setVisibility(View.VISIBLE);
            }
        } else {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.GONE);
            }
            setVisibility(View.VISIBLE);
        }
    }

    private void checkFocus() {
        final Adapter adapter = getAdapter();
        final boolean empty = adapter == null || adapter.getItemCount()== 0;
        if (mEmptyView != null) {
            updateEmptyStatus(empty);
        }
    }

    protected HeaderFooterAdapter wrapHeaderListAdapterInternal(
            SparseArrayCompat<View> headerViewInfos,
            SparseArrayCompat<View> footerViewInfos,
            Adapter adapter) {
        return new HeaderFooterAdapter(headerViewInfos, footerViewInfos, adapter);
    }

    protected void wrapHeaderListAdapterInternal() {
        setAdapter(wrapHeaderListAdapterInternal(mHeaderViewInfos, mFooterViewInfos, getAdapter()));
    }

    //-------------------------------------------------------------------------------------------




}
