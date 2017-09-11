package com.open.widgets.listview;
/**
 * 无数据时的暂时页面
 * @author long
 * 2016年3月31日
 */

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.open.lib_widgets.R;
import com.open.widgets.listview.IPullCallBacks.IHeaderCallBack;
import com.open.widgets.listview.IPullCallBacks.IMessagerDispatcher;

/**
 * Header in IListvew
 * Created by long on 2016/12/20.
 */
public class IListViewHeader extends LinearLayout implements IHeaderCallBack {

	public static final String TAG = "IListViewHeader";

	public static final int CMD_HEAD_SET_TOAST_TEXT = 2001;
	public static final int STATE_NORMAL 			= 11;
	public static final int STATE_READY 			= 12;

	IMessagerDispatcher messagDispatcher;

	private LinearLayout 				header;
	private IListViewHeaderLoadingView 	header_loading_animview;
	private TextView 					header_loading_toast;

	private int triggerDis;
	private int maxHeadHeight;
	private int loadingHeight;

	public IListViewHeader(Context context) {
		super(context);
		initView(context);
	}

	private void initView(final Context context) {
		LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
		header = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.lib_listview_header, null);
		addView(header, lp);
		setGravity(Gravity.BOTTOM);

		header_loading_animview = (IListViewHeaderLoadingView) header.findViewById(R.id.header_loading_animview);
		header_loading_toast = (TextView) header.findViewById(R.id.header_loading_toast);
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				} else {
					getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}

				loadingHeight = Math.max(dp2px(getContext(),66.66f),getDefaultHeadHeight());
//				maxHeadHeight = loadingHeight + triggerDis + dp2px(context,10);
				maxHeadHeight = Integer.MAX_VALUE;
			}
		});
	}

	public static int dp2px(Context context, float dpVal) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources()
				.getDisplayMetrics());
	}

	@Override
	public Object onHandMessage(int cmd, Object... args) {

		switch(cmd){

			case CMD_HEAD_SET_TOAST_TEXT:
				setToastText((String)args[0]);
				break;
		}

		return null;
	}

	//------------------------------自定义方法-----------------------------------
	public void setVisiableHeight(int height)
	{
		if (height < 0)
		{
			height = 0;
		}
		LayoutParams lp = (LayoutParams) header.getLayoutParams();
		lp.height = Math.min(height, maxHeadHeight);
		header.setLayoutParams(lp);
	}

	public int getVisiableHeight() {
		return header.getLayoutParams().height;
	}

	public int getDefaultHeadHeight()
	{
		return header_loading_animview.getHeight();
	}

	public void onStateChanged(int state) {

		switch (state)
		{
			case STATE_NORMAL:
				header_loading_animview.stopAnimation();
				header_loading_animview.clearOldStatus();
				break;

			default:
				break;
		}
	}

	//---------------------------------重写一些基本方法----------------------------------------

	@Override
	public void onHeaderInit(IMessagerDispatcher messagDispatcher, Object... args) {
		this.messagDispatcher = messagDispatcher;
		triggerDis = (int)args[0];
	}

	@Override
	public void onHeaderUpdateHeight(int delta) {
		int newHeight = getVisiableHeight()+ delta;
		setVisiableHeight(newHeight);
		onStateChanged((newHeight >= loadingHeight + triggerDis) ? STATE_READY : STATE_NORMAL);
	}

	@Override
	public void onHeaderReset(boolean isPullDownLoadingNextMoment) {
		if (mToastToStopRunnable.isRunning || mNormalStopRunnable.isRunning) {// 如果正在执行动画回调则不进行回调
			return;
		}
		mNormalStopRunnable.start(isPullDownLoadingNextMoment ? NormalStopRunnable.SCROLLBACK_CURRENT_TO_LOADING : NormalStopRunnable.SCROLLBACK_CURRENT_TO_DEFAULT);
	}

	@Override
	public boolean onHeaderCanPullDown() {
		return getVisiableHeight() >= (loadingHeight + triggerDis);
	}

	@Override
	public void onHeaderStart() {
        //暂停以前的动画
        mToastToStopRunnable.stopAnim();
        mNormalStopRunnable.stopAnim();

        //接着开始做下拉动画
        header_loading_animview.startAnimation();
        mNormalStopRunnable.start( NormalStopRunnable.SCROLLBACK_CURRENT_TO_LOADING );
	}

	@Override
	public long onHeaderStop() {
		if (!TextUtils.isEmpty(toastText)) {
			mToastToStopRunnable.start();
			return ToastToStopRunnable.DURATION;
		}else {
			mNormalStopRunnable.start(NormalStopRunnable.SCROLLBACK_CURRENT_TO_DEFAULT);
			return NormalStopRunnable.DURATION;
		}
	}

	@Override
	public void onHeaderRelease() {
		mToastToStopRunnable.stop();
		mNormalStopRunnable.stop();
	}

	//---------------------------------Normal To Stop----------------------------------------
	public NormalStopRunnable mNormalStopRunnable = new NormalStopRunnable();
	private class NormalStopRunnable implements Runnable {

		static final int STATUS_START = 1;
		static final int STATUS_STOP  = 2;

		static final long DURATION = 500;

		private boolean isRunning = false;
		private int curStatus = STATUS_STOP;
		private long start;//开始时间

		/**
		 ----------------max     line------------------
		 |
		 ----------------header_loading_animview line------------------
		 |
		 |
		 ----------------init    line------------------
		 see the two lines :
		 if pulldown the headView over the header_loading_animview line and release , than the pulldown event will trigger and the headView will scrollback to the header_loading_animview line;
		 otherwise , scrollback to the init line;
		 */

		private static final int SCROLLBACK_CURRENT_TO_DEFAULT = 1;//当前位置 -> 最初位置
		private static final int SCROLLBACK_CURRENT_TO_LOADING = 2;//当前位置 -> 加载位置

		private int scrollType;
		private int srcY;
		private int dstY;
		private final int refresh_frequency             = 0;

		public void start(int scrollType){
			if(this.scrollType == scrollType && isRunning){
				return;
			}

			this.scrollType = scrollType;
			this.srcY       = getVisiableHeight();
			this.dstY       = (scrollType == SCROLLBACK_CURRENT_TO_DEFAULT) ? 0 : loadingHeight;
			if(srcY == dstY){
				if(scrollType == SCROLLBACK_CURRENT_TO_DEFAULT){
					header_loading_animview.stopAnimation();
					header_loading_animview.clearOldStatus();
				}
				return;
			}

			changeStatus(STATUS_STOP);
			changeStatus(STATUS_START);
		}

		public void stop(){
			setVisiableHeight(dstY);
			changeStatus(STATUS_STOP);

			if(scrollType == SCROLLBACK_CURRENT_TO_DEFAULT){
				header_loading_animview.stopAnimation();
				header_loading_animview.clearOldStatus();
			}else{
				header_loading_animview.setVisibility(VISIBLE);
				header_loading_animview.startAnimation();
			}

			messagDispatcher.sendMessage(IMessagerDispatcher.DST_ILISTVIEW, IPullCallBacks.IMessageHandler.STOP_HEADER);
		}

		public void stopAnim(){
			changeStatus(STATUS_STOP);
		}

		@Override
		public void run() {
			if(!isRunning){
				return;
			}

			if(curStatus == STATUS_START){

				long now = AnimationUtils.currentAnimationTimeMillis();
				long elapsedTime = now - start;
				if(elapsedTime <= DURATION){

					//UniformInterpolator 匀速
//					int newHeight = srcY + (int)(((float)(elapsedTime)/(float)DURATION)*(dstY - srcY));

					//DecelerateInterpolator 减速
					float input = (float)(elapsedTime)/(float)DURATION;
					float value = (1.0f - (1.0f - input) * (1.0f - input));
					int newHeight = srcY + (int)(value*(dstY - srcY));

					setVisiableHeight(newHeight);
					postDelayed(this,refresh_frequency);
				}else{
					stop();
				}
			}else{
				stop();
			}
		}

		void changeStatus(int newStatus)
		{
			if(curStatus != newStatus) {
				curStatus = newStatus;

				if(newStatus == STATUS_START) {

					isRunning = true;
					start = AnimationUtils.currentAnimationTimeMillis();
					removeCallbacks(this);
					postDelayed(this,refresh_frequency);

				} else if(newStatus == STATUS_STOP) {

					isRunning = false;
					start = 0;
					removeCallbacks(this);
				}
			}
		}

	}


	//---------------------------------Toast  To Stop----------------------------------------
	private ToastToStopRunnable mToastToStopRunnable = new ToastToStopRunnable();

	private String toastText;
	private void setToastText(String toastText){
		this.toastText = toastText;
	}

	private class ToastToStopRunnable implements Runnable {

		static final int STATUS_START = 1;
		static final int STATUS_STOP  = 2;

		private static final int DURATION_SCROLLBACK_TO_TOAST	= 300;
		private static final int DURATION_PAUSE					= 1200;
		private static final int DURATION_SCROLLBACK 			= 300;
		static final long DURATION = DURATION_SCROLLBACK_TO_TOAST + DURATION_PAUSE +DURATION_SCROLLBACK;
		private final int refresh_frequency             = 1;

		private long start;//开始时间
		private int curStatus = STATUS_STOP;
		private int distanceFromTopToToast;
		private int toastHeight ;
		private boolean isRunning = false;

		public void start(){
			if(toastHeight == 0 ){
				toastHeight = header_loading_toast.getMeasuredHeight();
			}

			header_loading_toast.setText(toastText);
			changeStatus(STATUS_START);
		}

		public void stop() {
			onStateChanged(STATE_NORMAL);
			changeStatus(STATUS_STOP);
			setVisiableHeight(0);
		}

		public void stopAnim(){
			changeStatus(STATUS_STOP);
		}

		@Override
		public void run() {
			if(!isRunning){
				return;
			}
			long now = AnimationUtils.currentAnimationTimeMillis();
			long elapsedTime = now - start;
			if(elapsedTime < DURATION_SCROLLBACK_TO_TOAST){

				float rate = ((float)(elapsedTime)/(float)DURATION_SCROLLBACK_TO_TOAST);
				int realHeight = distanceFromTopToToast - (int)(distanceFromTopToToast*rate);

				header_loading_animview.setAlpha(1-rate);
				header_loading_toast.setVisibility(View.VISIBLE);
				header_loading_toast.setAlpha(rate);

				setVisiableHeight(toastHeight+realHeight);
				postDelayed(this,refresh_frequency);
			}
			else if(elapsedTime -DURATION_SCROLLBACK_TO_TOAST < DURATION_PAUSE){

//				header_loading_animview.setVisibility(INVISIBLE);
				header_loading_animview.setAlpha(0);
				header_loading_toast.setVisibility(View.VISIBLE);
				header_loading_toast.setAlpha(1);

				setVisiableHeight(toastHeight);
				postDelayed(this,refresh_frequency);
			}else if(elapsedTime - DURATION_SCROLLBACK_TO_TOAST- DURATION_PAUSE < DURATION_SCROLLBACK){

				//UniformInterpolator 匀速
//				int realHeight = toastHeight - (int)(toastHeight*((float)(elapsedTime - DURATION_SCROLLBACK_TO_TOAST - DURATION_PAUSE)/(float)DURATION_SCROLLBACK));

				//DecelerateInterpolator 减速
				float input = (float)(elapsedTime - DURATION_SCROLLBACK_TO_TOAST - DURATION_PAUSE)/(float)DURATION_SCROLLBACK;
				float value = (1.0f - (1.0f - input) * (1.0f - input));
				int realHeight = toastHeight - (int)(toastHeight*value);

				setVisiableHeight(realHeight);
				postDelayed(this,refresh_frequency);
			}else{
				stop();
			}
		}

		void changeStatus(int newStatus)
		{
			if(curStatus != newStatus) {
				curStatus = newStatus;

				if(newStatus == STATUS_START) {

					isRunning = true;
					distanceFromTopToToast = header_loading_animview.getMeasuredHeight() - toastHeight;
//					header_loading_animview.stopAnimation();
					start = AnimationUtils.currentAnimationTimeMillis();
					removeCallbacks(this);
					postDelayed(this,refresh_frequency);

				} else if(newStatus == STATUS_STOP) {

					isRunning = false;
					start = 0;
					removeCallbacks(this);

					toastText = "";
					header_loading_animview.setVisibility(View.VISIBLE);
					header_loading_animview.setAlpha(1);
					header_loading_toast.setVisibility(View.GONE);
					header_loading_toast.setAlpha(0);
				}
			}
		}
	}

}
