package com.open.widgets.recyclerview;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

/**
 * Created by long on 2017/1/4.
 */

public class DividerGridHeaderFooterItemDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG  = "DividerGridHeaderFooter";
    private Drawable mDivider;
    private boolean isDrawHeaderDivider = false;
    private boolean isDrawFooterDivider = false;

    public DividerGridHeaderFooterItemDecoration(Drawable mDivider) {
        this(mDivider,false,false);
    }

    public DividerGridHeaderFooterItemDecoration(Drawable mDivider, boolean isDrawHeaderDivider, boolean isDrawFooterDivider) {
        this.mDivider = mDivider;
        this.isDrawHeaderDivider = isDrawHeaderDivider;
        this.isDrawFooterDivider = isDrawFooterDivider;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawHorizontal(c, parent);
        drawVertical(c, parent);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        int adapterPosition = parent.getChildAdapterPosition(view);
        int rPosition = parent.getChildAdapterPosition(view);

        HeaderFooterAdapter adapter = parent.getAdapter() instanceof HeaderFooterAdapter ? (HeaderFooterAdapter)parent.getAdapter() : null;
        if(null != adapter){
            rPosition = adapter.getRealPosition(adapterPosition);
            Log.v(TAG,"getItemOffsets A adapterPosition "+ adapterPosition + " rPosition "+rPosition);
            if(rPosition == -1 && !isDrawHeaderDivider) {
                outRect.set(0, 0, 0, 0);
                return ;
            }else if(rPosition == -2 && !isDrawFooterDivider) {
                outRect.set(0, 0, 0, 0);
                return ;
            }
            else if(isDrawHeaderDivider){
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
                return;
            }else if(isDrawFooterDivider){
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
                return;
            }
        }

        int spanCount = getSpanCount(parent);
        int childCount = parent.getAdapter().getItemCount();
        int itemPosition = rPosition;

        if (isLastRaw(parent, itemPosition, spanCount, childCount))// 如果是最后一行，则不需要绘制底部
        {
            Log.v(TAG,"getItemOffsets B adapterPosition "+ adapterPosition + " rPosition "+rPosition);
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        } else if (isLastColumn(parent, itemPosition, spanCount, childCount))// 如果是最后一列，则不需要绘制右边
        {
            Log.v(TAG,"getItemOffsets C adapterPosition "+ adapterPosition + " rPosition "+rPosition);
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else
        {
            Log.v(TAG,"getItemOffsets D adapterPosition "+ adapterPosition + " rPosition "+rPosition);
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
        }
    }

    public void drawHorizontal(Canvas c, RecyclerView parent)
    {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            final View child = parent.getChildAt(i);

            if(!isDrawHeaderDivider || !isDrawFooterDivider) {
                HeaderFooterAdapter adapter = parent.getAdapter() instanceof HeaderFooterAdapter ? (HeaderFooterAdapter)parent.getAdapter() : null;
                if(null != adapter){
                    int adapterPosition = parent.getChildAdapterPosition(child);
                    int rPosition = adapter.getRealPosition(adapterPosition);
                    Log.v(TAG,"AAA1 drawHorizontal adapterPosition "+ adapterPosition + " rPosition "+rPosition + " getMeasuredWidth "+ child.getMeasuredWidth()+
                            " getMeasuredHeight "+ child.getMeasuredHeight());
                    if(rPosition == -1 && !isDrawHeaderDivider || rPosition == -2 && !isDrawFooterDivider){
                        continue;
                    }
                }
            }

            if(child.getMeasuredWidth() == 0 || child.getMeasuredHeight() == 0){
                continue;
            }

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getLeft() - params.leftMargin;
            final int right = child.getRight() + params.rightMargin + mDivider.getIntrinsicWidth();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();

            Log.v(TAG,"AAA2 i "+i+" drawHorizontal BBB top "+ top + " bottom "+bottom+ " left "+left+ " right "+right);

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent)
    {
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            final View child = parent.getChildAt(i);

            if(!isDrawHeaderDivider || !isDrawFooterDivider) {
                HeaderFooterAdapter adapter = parent.getAdapter() instanceof HeaderFooterAdapter ? (HeaderFooterAdapter)parent.getAdapter() : null;
                if(null != adapter){
                    int adapterPosition = parent.getChildAdapterPosition(child);
                    int rPosition = adapter.getRealPosition(adapterPosition);
                    Log.v(TAG,"BBB1 drawVertical adapterPosition "+ adapterPosition + " rPosition "+rPosition + " getMeasuredWidth "+ child.getMeasuredWidth()+
                            " getMeasuredHeight "+ child.getMeasuredHeight());
                    if(rPosition == -1 && !isDrawHeaderDivider || rPosition == -2 && !isDrawFooterDivider){
                        continue;
                    }
                }
            }

            if(child.getMeasuredWidth() == 0 || child.getMeasuredHeight() == 0){
                continue;
            }

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getTop() - params.topMargin;
            final int bottom = child.getBottom() + params.bottomMargin;
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicWidth();
            Log.v(TAG,"BBB2 i "+i+" drawVertical BBB top "+ top + " bottom "+bottom+ " left "+left+ " right "+right);
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    private int getSpanCount(RecyclerView parent) {
        // 列数
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return spanCount;
    }

    private boolean isLastColumn(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
            {
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager)
        {
            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL)
            {
                if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
                {
                    return true;
                }
            } else
            {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一列，则不需要绘制右边
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager)
        {
            childCount = childCount - childCount % spanCount;
            if (pos >= childCount)// 如果是最后一行，则不需要绘制底部
            {
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager)
        {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL)
            {
                childCount = childCount - childCount % spanCount;
                // 如果是最后一行，则不需要绘制底部
                if (pos >= childCount)
                {
                    return true;
                }
            } else
            // StaggeredGridLayoutManager 且横向滚动
            {
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0)
                {
                    return true;
                }
            }
        }
        return false;
    }
}
