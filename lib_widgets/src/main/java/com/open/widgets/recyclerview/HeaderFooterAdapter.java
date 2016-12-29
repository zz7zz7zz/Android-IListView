package com.open.widgets.recyclerview;

import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by long on 2016/12/29.
 */

public final class HeaderFooterAdapter extends RecyclerView.Adapter {

    public static final int BASE_ITEM_VIEW_TYPE_HEADER = 100000;
    public static final int BASE_ITEM_VIEW_TYPE_FOOTER = 200000;

    private SparseArrayCompat<View> mHeaderViewInfos = null;
    private SparseArrayCompat<View> mFooterViewInfos = null;
    private RecyclerView.Adapter mAdapter;

    static final SparseArrayCompat<View> EMPTY_INFO_LIST = new SparseArrayCompat<>(0);

    public HeaderFooterAdapter(SparseArrayCompat<View> headerViewInfos, SparseArrayCompat<View> footerViewInfos, RecyclerView.Adapter mAdapter) {

        if (headerViewInfos == null) {
            mHeaderViewInfos = EMPTY_INFO_LIST;
        } else {
            mHeaderViewInfos = headerViewInfos;
        }

        if (footerViewInfos == null) {
            mFooterViewInfos = EMPTY_INFO_LIST;
        } else {
            mFooterViewInfos = footerViewInfos;
        }
        this.mAdapter = mAdapter;
    }

    public boolean removeHeader(View v) {
        for (int i = 0; i < mHeaderViewInfos.size(); i++) {
            View view = mHeaderViewInfos.get(i);
            if (view == v) {
                mHeaderViewInfos.removeAt(i);
                return true;
            }
        }
        return false;
    }

    public boolean removeFooter(View v) {
        for (int i = 0; i < mFooterViewInfos.size(); i++) {
            View view = mFooterViewInfos.get(i);
            if (view == v) {
                mHeaderViewInfos.removeAt(i);
                return true;
            }
        }
        return false;
    }

    //-------------------------------------------------------------------------------

    public int getHeadersCount() {
        return mHeaderViewInfos.size();
    }

    public int getFootersCount() {
        return mFooterViewInfos.size();
    }

    //-------------------------------------------------------------------------------

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(null != mHeaderViewInfos.get(viewType)){
            return BaseViewHolder.createViewHolder(mHeaderViewInfos.get(viewType));
        }
        else if(null != mFooterViewInfos.get(viewType)){
            return BaseViewHolder.createViewHolder(mFooterViewInfos.get(viewType));
        }
        return mAdapter.onCreateViewHolder(parent,viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Header (negative positions will throw an IndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return ;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getItemCount();
            if (adjPosition < adapterCount) {
                mAdapter.onBindViewHolder(holder,position);
                return ;
            }
        }

        // Footer (off-limits positions will throw an IndexOutOfBoundsException)
        return;
    }

    @Override
    public int getItemCount() {
        if (mAdapter != null) {
            return getFootersCount() + getHeadersCount() + mAdapter.getItemCount();
        } else {
            return getFootersCount() + getHeadersCount();
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Header (negative positions will throw an IndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if(position < numHeaders){
            return BASE_ITEM_VIEW_TYPE_HEADER+position;
        }

        // Adapter
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getItemCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemViewType(adjPosition);
            }
        }

        // Footer (off-limits positions will throw an IndexOutOfBoundsException)
        return BASE_ITEM_VIEW_TYPE_FOOTER+position;
    }

    public long getItemId(int position) {
        int numHeaders = getHeadersCount();
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getItemCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemId(adjPosition);
            }
        }
        return -1;
    }

}
