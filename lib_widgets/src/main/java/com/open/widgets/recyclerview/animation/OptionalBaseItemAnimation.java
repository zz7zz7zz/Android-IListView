package com.open.widgets.recyclerview.animation;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by long on 2017/2/13.
 */

public class OptionalBaseItemAnimation extends BaseItemAnimator {

    private static final String TAG = "OptBaseItemAnimation";
    private boolean isAnimAdd = true;
    private boolean isAnimRemove = true;
    private boolean isAnimMove = true;
    private boolean isAnimChange = true;

    public OptionalBaseItemAnimation(boolean isAnimAdd, boolean isAnimRemove, boolean isAnimMove, boolean isAnimChange) {
        this.isAnimAdd = isAnimAdd;
        this.isAnimRemove = isAnimRemove;
        this.isAnimMove = isAnimMove;
        this.isAnimChange = isAnimChange;
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        Log.v(TAG,"animateRemove");
        if(!isAnimRemove){
            dispatchRemoveFinished(holder);
            return false;
        }
        return super.animateRemove(holder);
    }

    @Override
    protected void animateRemoveImpl(final RecyclerView.ViewHolder holder) {
//        super.animateRemoveImpl(holder);
        Log.v(TAG,"animateRemoveImpl");
        final View view = holder.itemView;
        final ViewPropertyAnimatorCompat animation = ViewCompat.animate(view);
        mRemoveAnimations.add(holder);
        animation.setDuration(getRemoveDuration())
                .translationX(view.getWidth()).alpha(0).setListener(new VpaListenerAdapter() {
            @Override
            public void onAnimationStart(View view) {
                dispatchRemoveStarting(holder);
            }

            @Override
            public void onAnimationEnd(View view) {
                animation.setListener(null);
                ViewCompat.setAlpha(view, 1);
                ViewCompat.setTranslationX(view,0);
                dispatchRemoveFinished(holder);
                mRemoveAnimations.remove(holder);
                dispatchFinishedWhenDone();
            }
        }).start();
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        Log.v(TAG,"animateAdd");
        if(!isAnimAdd){
            dispatchAddFinished(holder);
            return false;
        }
        return super.animateAdd(holder);
    }

    @Override
    protected void animateAddImpl(RecyclerView.ViewHolder holder) {
        Log.v(TAG,"animateAddImpl");
        super.animateAddImpl(holder);
    }

    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        Log.v(TAG,"animateMove");
        if(!isAnimMove){
            dispatchMoveFinished(holder);
            return false;
        }
        return super.animateMove(holder, fromX, fromY, toX, toY);
    }

    @Override
    protected void animateMoveImpl(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        Log.v(TAG,"animateMoveImpl");
        super.animateMoveImpl(holder, fromX, fromY, toX, toY);
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        Log.v(TAG,"animateChange");
        if(!isAnimChange){
            dispatchChangeFinished(oldHolder, true);
            dispatchChangeFinished(newHolder, true);
            return false;
        }
        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
    }

    @Override
    protected void animateChangeImpl(BaseItemAnimator.ChangeInfo changeInfo) {
        Log.v(TAG,"animateChangeImpl");
        super.animateChangeImpl(changeInfo);
    }
}
