package com.open.widgets.recyclerview;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by long on 2017/1/3.
 */

public class DividerLinearItemDecoration extends RecyclerView.ItemDecoration {

    public static final String TAG = "DividerLinearItemDecoration";

    public static final int ORIENTATION_HORIZONTAL  = 1;
    public static final int ORIENTATION_VERTICAL    = 2;

    private int mOrientation;
    private Drawable mDivider;
    private boolean isDrawHeaderDivider = false;
    private boolean isDrawFooterDivider = false;

    public DividerLinearItemDecoration(int mOrientation, Drawable mDivider) {
        this(mOrientation,mDivider,false,false);
    }

    public DividerLinearItemDecoration(int mOrientation, Drawable mDivider, boolean isDrawHeaderDivider, boolean isDrawFooterDivider) {
        this.mOrientation = mOrientation;
        this.mDivider = mDivider;
        this.isDrawHeaderDivider = isDrawHeaderDivider;
        this.isDrawFooterDivider = isDrawFooterDivider;
    }

    public void setOrientation(int orientation) {
        if (orientation != ORIENTATION_HORIZONTAL && orientation != ORIENTATION_VERTICAL) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    public void setOrientation(Drawable divider) {
        this.mDivider = divider;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == ORIENTATION_VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == ORIENTATION_VERTICAL) {

            if(!isDrawHeaderDivider || !isDrawFooterDivider) {
                HeaderFooterAdapter adapter = parent.getAdapter() instanceof HeaderFooterAdapter ? (HeaderFooterAdapter)parent.getAdapter() : null;
                if(null != adapter){
                    int adapterPosition = parent.getChildAdapterPosition(view);
                    int rPosition       = adapter.getRealPosition(adapterPosition);
                    Log.v(TAG,"\n getItemOffsets adapterPosition "+ adapterPosition + " rPosition "+rPosition);
                    if(rPosition == -1 && !isDrawHeaderDivider || rPosition == -2 && !isDrawFooterDivider){
                        outRect.set(0, 0, 0, 0);
                        return ;
                    }
                }
            }

            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {

        HeaderFooterAdapter adapter = parent.getAdapter() instanceof HeaderFooterAdapter ? (HeaderFooterAdapter)parent.getAdapter() : null;

        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);

            if(!isDrawHeaderDivider || !isDrawFooterDivider) {
                if(null != adapter){
                    int adapterPosition = parent.getChildAdapterPosition(child);
                    int rPosition       = adapter.getRealPosition(adapterPosition);
                    Log.v(TAG,"\n drawVertical adapterPosition "+ adapterPosition + " rPosition "+rPosition);
                    if(rPosition == -1 && !isDrawHeaderDivider || rPosition == -2 && !isDrawFooterDivider){
                        continue;
                    }
                }
            }

            if(child.getMeasuredHeight() == 0){
                continue;
            }

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {

        HeaderFooterAdapter adapter = parent.getAdapter() instanceof HeaderFooterAdapter ? (HeaderFooterAdapter)parent.getAdapter() : null;

        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);

            if(!isDrawHeaderDivider || !isDrawFooterDivider) {
                if(null != adapter){
                    int adapterPosition = parent.getChildAdapterPosition(child);
                    int rPosition       = adapter.getRealPosition(adapterPosition);
                    Log.v(TAG,"\n drawHorizontal adapterPosition "+ adapterPosition + " rPosition "+rPosition);
                    if(rPosition == -1 && !isDrawHeaderDivider || rPosition == -2 && !isDrawFooterDivider){
                        continue;
                    }
                }
            }

            if(child.getMeasuredWidth() == 0){
                continue;
            }

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicWidth();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
