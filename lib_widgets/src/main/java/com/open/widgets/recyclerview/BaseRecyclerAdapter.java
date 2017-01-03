package com.open.widgets.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by long on 2017/1/3.
 */

public abstract class BaseRecyclerAdapter extends RecyclerView.Adapter {

    private static final String TAG = "BaseRecyclerAdapter";

    public final void notifyDataSetChanged(IRecyclerView recyclerView) {
        super.notifyDataSetChanged();
    }

    public final void notifyItemChanged(IRecyclerView recyclerView, int position) {
        int rPosition = getRealPosition(recyclerView,position);
        Log.v(TAG,"position "+ position + " rPosition "+rPosition);
        if(rPosition == -1){
            return ;
        }
        super.notifyItemRangeChanged(rPosition, 1);
    }

    public final void notifyItemChanged(IRecyclerView recyclerView, int position, Object payload) {
        int rPosition = getRealPosition(recyclerView,position);
        Log.v(TAG,"position "+ position + " rPosition "+rPosition);
        if(rPosition == -1){
            return ;
        }
        super.notifyItemRangeChanged(rPosition, 1, payload);
    }

    public final void notifyItemRangeChanged(IRecyclerView recyclerView, int positionStart, int itemCount) {
        int rPosition = getRealPosition(recyclerView,positionStart);
        Log.v(TAG,"positionStart "+ positionStart + " rPosition "+rPosition);
        if(rPosition == -1){
            return ;
        }
        super.notifyItemRangeChanged(rPosition, itemCount);
    }

    public final void notifyItemRangeChanged(IRecyclerView recyclerView, int positionStart, int itemCount, Object payload) {
        int rPosition = getRealPosition(recyclerView,positionStart);
        Log.v(TAG,"positionStart "+ positionStart + " rPosition "+rPosition);
        if(rPosition == -1){
            return ;
        }
        super.notifyItemRangeChanged(rPosition, itemCount, payload);
    }

    public final void notifyItemInserted(IRecyclerView recyclerView, int position) {
        int rPosition = getRealPosition(recyclerView,position);
        Log.v(TAG,"positionStart "+ position + " rPosition "+rPosition);
        if(rPosition == -1){
            return ;
        }
        super.notifyItemRangeInserted(rPosition, 1);
    }

    public final void notifyItemMoved(IRecyclerView recyclerView, int fromPosition, int toPosition) {
        int rPosition = getRealPosition(recyclerView,fromPosition);
        Log.v(TAG,"positionStart "+ fromPosition + " rPosition "+rPosition);
        if(rPosition == -1){
            return ;
        }

        int rPosition2 = getRealPosition(recyclerView,toPosition);
        Log.v(TAG,"toPosition "+ toPosition + " rPosition "+rPosition);
        if(rPosition2 == -1){
            return ;
        }
        super.notifyItemMoved(rPosition, rPosition2);
    }

    public final void notifyItemRangeInserted(IRecyclerView recyclerView, int positionStart, int itemCount) {
        int rPosition = getRealPosition(recyclerView,positionStart);
        Log.v(TAG,"positionStart "+ positionStart + " rPosition "+rPosition);
        if(rPosition == -1){
            return ;
        }
        super.notifyItemRangeInserted(rPosition, itemCount);
    }

    public final void notifyItemRemoved(IRecyclerView recyclerView, int position) {
        int rPosition = getRealPosition(recyclerView,position);
        Log.v(TAG,"position "+ position + " rPosition "+rPosition);
        if(rPosition == -1){
            return ;
        }
        super.notifyItemRangeRemoved(rPosition, 1);
    }

    public final void notifyItemRangeRemoved(IRecyclerView recyclerView, int positionStart, int itemCount) {
        int rPosition = getRealPosition(recyclerView,positionStart);
        Log.v(TAG,"positionStart "+ positionStart + " rPosition "+rPosition);
        if(rPosition == -1){
            return ;
        }
        super.notifyItemRangeRemoved(rPosition, itemCount);
    }

    public final int getRealPosition(IRecyclerView recyclerView , int position){

        if (recyclerView.getAdapter() instanceof HeaderFooterAdapter) {

            // Header (negative positions will throw an IndexOutOfBoundsException)
            int numHeaders = recyclerView.getHeaderViewsCount();

            final int adjPosition = position + numHeaders;
            int adapterCount = (recyclerView.getAdapter()).getItemCount();
            if (adjPosition < adapterCount) {
                return adjPosition;
            }

            // Footer (off-limits positions will throw an IndexOutOfBoundsException)
            return -1;
        }else{
            return position;
        }
    }
}
