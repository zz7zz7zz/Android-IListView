package com.open.widgets.recyclerview;

import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by long on 2016/12/29.
 */

public final class HeaderFooterAdapter extends RecyclerView.Adapter {

    private static final String TAG = "HeaderFooterAdapter";

    public static final int BASE_ITEM_VIEW_TYPE_HEADER = 100000;
    public static final int BASE_ITEM_VIEW_TYPE_FOOTER = 200000;
    static final SparseArrayCompat<View> EMPTY_INFO_LIST = new SparseArrayCompat<>(0);

    private SparseArrayCompat<View> mHeaderViewInfos = null;
    private SparseArrayCompat<View> mFooterViewInfos = null;
    private RecyclerView.Adapter    mAdapter;

    HeaderFooterAdapter(SparseArrayCompat<View> headerViewInfos, SparseArrayCompat<View> footerViewInfos,
                        RecyclerView.Adapter mAdapter) {

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
            View view = mHeaderViewInfos.valueAt(i);
            if (view == v) {
                mHeaderViewInfos.removeAt(i);
                return true;
            }
        }
        return false;
    }

    public boolean removeFooter(View v) {
        for (int i = 0; i < mFooterViewInfos.size(); i++) {
            View view = mFooterViewInfos.valueAt(i);
            if (view == v) {
                mFooterViewInfos.removeAt(i);
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------------

    public int getHeadersCount() {
        return mHeaderViewInfos.size();
    }

    public int getFootersCount() {
        return mFooterViewInfos.size();
    }

    public final int getRealPosition(int position) {

        // Header (negative positions will throw an IndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return -1;
        }

        if (null != mAdapter) {
            final int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getItemCount();
            if (adjPosition < adapterCount) {
                return adjPosition;
            }
        }

        // Footer (off-limits positions will throw an IndexOutOfBoundsException)
        return -2;
    }

    // -------------------------------------------------------------------------------

    public boolean isEmpty() {
        return mAdapter == null || mAdapter.getItemCount() == 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Log.v(TAG,"onCreateViewHolder " + viewType);
        if (null != mHeaderViewInfos.get(viewType)) {
            mHeaderViewInfos.get(viewType).setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                                      ViewGroup.LayoutParams.WRAP_CONTENT));
            return BaseRecyclerViewHolder.createViewHolder(mHeaderViewInfos.get(viewType));
        } else if (null != mFooterViewInfos.get(viewType)) {
            mFooterViewInfos.get(viewType).setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                                      ViewGroup.LayoutParams.WRAP_CONTENT));
            return BaseRecyclerViewHolder.createViewHolder(mFooterViewInfos.get(viewType));
        }

        if (null != mAdapter) {
            return mAdapter.onCreateViewHolder(parent, viewType);
        } else {
            throw new IllegalArgumentException("mAdapter is null ,unrecognized viewType " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Log.v(TAG,"onBindViewHolder " + position);

        // Header (negative positions will throw an IndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
//            Log.v(TAG,"onBindViewHolder header " + position);
            return;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        if (mAdapter != null) {
            int adapterCount = mAdapter.getItemCount();
            if (adjPosition < adapterCount) {
//                Log.v(TAG,"onBindViewHolder content " + position);
                mAdapter.onBindViewHolder(holder, adjPosition);
                return;
            }
        }

//        Log.v(TAG,"onBindViewHolder footer " + position);
        // Footer (off-limits positions will throw an IndexOutOfBoundsException)

        //因为有可能footerViewInfos的某个FooterView的子View元素改变了，比如可见性改变了，此时需要使View失效，重新绘制，这样整个ViewGroup才会更新到最新的画面
        int viewType = getItemViewType(position);
        if (null != mFooterViewInfos.get(viewType)) {
            mFooterViewInfos.get(viewType).invalidate();
        }

        return;
    }

    @Override
    public int getItemCount() {
        int itemCount;
        if (mAdapter != null) {
            itemCount = getFootersCount() + getHeadersCount() + mAdapter.getItemCount();
        } else {
            itemCount = getFootersCount() + getHeadersCount();
        }
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        // Log.v(TAG,"getItemViewType ( " + position + " ) getItemCount " + getItemCount() + " mAdapter.getItemCount()
        // "+mAdapter.getItemCount());
        // Header (negative positions will throw an IndexOutOfBoundsException)

        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return mHeaderViewInfos.keyAt(position);
        }

        // Adapter
        int mAdapterSize = 0;
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapterSize = mAdapter.getItemCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemViewType(adjPosition);
            }
        }

        // Footer (off-limits positions will throw an IndexOutOfBoundsException)
         return mFooterViewInfos.keyAt(position - numHeaders - mAdapterSize);
    }

    public long getItemId(int position) {
        // Log.v(TAG,"getItemId " + position);
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

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        if (mAdapter != null) {
            mAdapter.registerAdapterDataObserver(observer);
        }
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
        if (mAdapter != null) {
            mAdapter.unregisterAdapterDataObserver(observer);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup oldSpanSizeLookup = gridLayoutManager.getSpanSizeLookup();

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

                @Override
                public int getSpanSize(int position) {

                    int retSpanSize = 1;
                    int viewType = getItemViewType(position);
                    if (mHeaderViewInfos.get(viewType) != null || mFooterViewInfos.get(viewType) != null) {
                        retSpanSize = gridLayoutManager.getSpanCount();
                    } else if (oldSpanSizeLookup != null) {
                        retSpanSize = oldSpanSizeLookup.getSpanSize(position);
                    }

                    // Log.v(TAG,"onAttachedToRecyclerView " + " getItemViewType("+position+")
                    // "+getItemViewType(position) + " retSpanSize "+retSpanSize);
                    return retSpanSize;
                }
            });
            gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
        }

    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        int position = holder.getLayoutPosition();
        int viewType = getItemViewType(position);
        if (mHeaderViewInfos.get(viewType) != null || mFooterViewInfos.get(viewType) != null) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();

            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }
}
