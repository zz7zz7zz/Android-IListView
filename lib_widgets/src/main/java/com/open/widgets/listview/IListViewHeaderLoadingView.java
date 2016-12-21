package com.open.widgets.listview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.open.lib_widgets.R;


/**
 * LoadingView with lib_loading animation in IListvewHeader
 * Created by long on 2016/12/20.
 */
public class IListViewHeaderLoadingView extends View {

    private Drawable load_foreground;
    private Drawable load_background;

    private float load_background_deltadx = 0f;

    private float load_foreground_dx      = 0f;
    private float load_foreground_dy      = 0f;
    private float load_foreground_deltadx = 0f;
    private float load_foreground_deltady = 0f;
    private float load_foreground_maxdeltady = 0f;

    private int oldWidth;
    private int oldHeight;
    private LoadingRunnable mLoadingRunnable = new LoadingRunnable();

    public IListViewHeaderLoadingView(Context context) {
        this(context,null);
    }

    public IListViewHeaderLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IListViewHeaderLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IListViewHeaderLoadingView, defStyleAttr, 0);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            if(attr == R.styleable.IListViewHeaderLoadingView_load_background){
                load_background = a.getDrawable(attr);
            }else if(attr == R.styleable.IListViewHeaderLoadingView_load_foreground){
                load_foreground = a.getDrawable(attr);
            }
//            switch (attr) {
//                case R.styleable.IListViewHeaderLoadingView_load_background:
//                    load_background = a.getDrawable(attr);
//                    break;
//
//                case R.styleable.IListViewHeaderLoadingView_load_foreground:
//                    load_foreground = a.getDrawable(attr);
//                    break;
//            }
        }

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = (int)((float)(measureWidth*load_background.getIntrinsicHeight())/(float)load_background.getIntrinsicWidth());
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        Log.v("PullLoadingView","onMeasure() w:"+ measuredWidth+" h:"+ getMeasuredHeight());

        if(oldWidth != measureWidth || oldHeight != measureHeight){
            oldWidth  = measureWidth;
            oldHeight = measureHeight;

            float virtualDensity = (float)load_background.getIntrinsicWidth()/(float)oldWidth;
            load_background.setBounds(0,0,oldWidth,oldHeight);
            load_foreground.setBounds(0,0,(int)(load_foreground.getIntrinsicWidth()/virtualDensity), (int)(load_foreground.getIntrinsicHeight()/virtualDensity));

            load_foreground_dx = (oldWidth - load_foreground.getBounds().width())/2;
//            load_foreground_dy = (oldHeight- load_foreground.getBounds().height())/2;
//            load_foreground_maxdeltady = 0.3f* load_foreground_dy;

            load_foreground_dy = 0.8f*(oldHeight- load_foreground.getBounds().height())/2;
            load_foreground_maxdeltady = -0.4f*load_foreground_dy;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(-load_background_deltadx,0);
        load_background.draw(canvas);
        canvas.translate(getMeasuredWidth(),0);
        load_background.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.translate(load_foreground_dx + load_foreground_deltadx, load_foreground_dy + load_foreground_deltady);
        load_foreground.draw(canvas);
        canvas.restore();
    }

    //-----------------------------------------------------

    // let's be nice with the cpu
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if(visibility != VISIBLE){
            if(mLoadingRunnable.isRunning()){
                mLoadingRunnable.changeStatus(LoadingRunnable.STATUS_PAUSE);
                removeCallbacks(mLoadingRunnable);
            }
        }else{
            if(mLoadingRunnable.isRunning()){
                mLoadingRunnable.changeStatus(LoadingRunnable.STATUS_START);
            }
        }
    }

    // let's be nice with the cpu
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if(visibility != VISIBLE){
            if(mLoadingRunnable.isRunning()){
                mLoadingRunnable.changeStatus(LoadingRunnable.STATUS_PAUSE);
                removeCallbacks(mLoadingRunnable);
            }
        }else{
            if(mLoadingRunnable.isRunning()){
                mLoadingRunnable.changeStatus(LoadingRunnable.STATUS_START);
            }
        }
    }

    // let's be nice with the cpu
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);

        if(visibility != VISIBLE){
            if(mLoadingRunnable.isRunning()){
                mLoadingRunnable.changeStatus(LoadingRunnable.STATUS_PAUSE);
                removeCallbacks(mLoadingRunnable);
            }
        }else{
            if(mLoadingRunnable.isRunning()){
                mLoadingRunnable.changeStatus(LoadingRunnable.STATUS_START);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (getVisibility() == VISIBLE) {
            if(mLoadingRunnable.isRunning()){
                mLoadingRunnable.changeStatus(LoadingRunnable.STATUS_START);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if(mLoadingRunnable.isRunning()){
            mLoadingRunnable.changeStatus(LoadingRunnable.STATUS_PAUSE);
            removeCallbacks(mLoadingRunnable);
        }
        super.onDetachedFromWindow();
    }

    //-----------------------------------------------------

    public void startAnimation(){
        mLoadingRunnable.changeStatus(LoadingRunnable.STATUS_START);
        removeCallbacks(mLoadingRunnable);
        post(mLoadingRunnable);
    }

    public void stopAnimation(){
        mLoadingRunnable.changeStatus(LoadingRunnable.STATUS_STOP);
        removeCallbacks(mLoadingRunnable);
    }

    public void clearOldStatus(){
        if(load_background_deltadx !=0 || load_foreground_deltady != 0){
            load_background_deltadx = 0;
            load_foreground_deltady = 0;
            invalidate();
        }
    }

    class LoadingRunnable implements Runnable {

        static final int STATUS_START = 1;
        static final int STATUS_PAUSE = 2;
        static final int STATUS_STOP  = 3;

        private final int DURATION_BACKGROUND = 3000;
        private final int DURATION_FOREGROUND = 1250;

        private boolean isRunning = false;
        private int curStatus = STATUS_STOP;
        private long start;//开始时间
        private final int refresh_frequency             = 1;

        @Override
        public void run() {

            if(!isRunning) {
                return;
            }

            if(curStatus == STATUS_START){

                long now = AnimationUtils.currentAnimationTimeMillis();
                long elapsedTime = now - start;

                load_background_deltadx = ((float)(elapsedTime% DURATION_BACKGROUND)/(float) DURATION_BACKGROUND)*getMeasuredWidth();
//                load_foreground_deltady =(int) (load_foreground_maxdeltady *Math.sin((Math.PI/180) *((float)((now-start)% DURATION_FOREGROUND)/(float) DURATION_FOREGROUND)*360));

                double input = Math.sin((Math.PI/180) *((float)(elapsedTime% DURATION_FOREGROUND)/(float) DURATION_FOREGROUND)*360);
                float result = (float)(1.0f - (1.0f - input) * (1.0f - input));
                load_foreground_deltady =(int) (load_foreground_maxdeltady * result);

//                Log.v("PullLoadingView","load_foreground_deltady:"+ load_foreground_deltady);

                invalidate();
                postDelayed(this,refresh_frequency);
            }else if(curStatus == STATUS_PAUSE){
                //just doNothing !
            }else{
                changeStatus(STATUS_STOP);
            }
        }

        public void changeStatus(int newStatus)
        {
//			Log.v("PullLoadingView", " curStatus:"+this.curStatus +" newStatus:"+newStatus );

            if(curStatus != newStatus) {
                curStatus = newStatus;
                if(curStatus == STATUS_START) {
                    isRunning = true;
                    start = AnimationUtils.currentAnimationTimeMillis();
                    removeCallbacks(this);
                    postDelayed(this,refresh_frequency);
                } else if(curStatus == STATUS_PAUSE) {
                    isRunning = true;
                    start = AnimationUtils.currentAnimationTimeMillis();
                    removeCallbacks(this);
                } else {
//                    load_background_deltadx = 0;
//                    load_foreground_deltady = 0;
//                    invalidate();

                    start = 0;
                    isRunning = false;
                }
            }
        }

        public boolean isRunning() {
            return isRunning;
        }

    }
}
