/**
 * @file XFooterView.java
 * @create Mar 31, 2012 9:33:43 PM
 * @author Maxwin
 * @description XListView's footer
 */
package com.open.widgets.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.open.lib_widgets.R;
import com.open.widgets.listview.IPullCallBacks.IFooterCallBack;
import com.open.widgets.listview.IPullCallBacks.IMessagerDispatcher;


/**
 * Footer in IListvew
 * Created by long on 2016/12/20.
 */
public class IListViewFooter extends LinearLayout implements IFooterCallBack {

	public static final int CMD_BOTTOM_SET_TEXT 			= 3001;
	public static final int STATE_NORMAL 					= 11;
	public static final int STATE_READY 					= 12;

	IMessagerDispatcher messagDispatcher;

	private Context mContext;
	private LinearLayout 	footer;
	private View 			footer_content;
	private View 			footer_progressbar;
	private TextView 		footer_hint_textview;

	private int triggerDis;

	public IListViewFooter(Context context) {
		this(context,null);
	}

	public IListViewFooter(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public IListViewFooter(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	private void initView(Context context) {
		mContext = context;
		footer = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.lib_listview_footer, null);
		addView(footer);
		footer.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

		footer_content = footer.findViewById(R.id.footer_content);
		footer_progressbar = footer.findViewById(R.id.footer_progressbar);
		footer_hint_textview = (TextView) footer.findViewById(R.id.footer_hint_textview);
	}

	private void setBottomMargin(int height) {
		if (height < 0)
			return;
		LayoutParams lp = (LayoutParams) footer_content.getLayoutParams();
		lp.bottomMargin = height;
		footer_content.setLayoutParams(lp);
	}

	private int getBottomMargin() {
		LayoutParams lp = (LayoutParams) footer_content.getLayoutParams();
		return lp.bottomMargin;
	}

	public void normal() {
		footer_hint_textview.setVisibility(View.VISIBLE);
		footer_progressbar.setVisibility(View.GONE);
	}

	public void loading() {
		footer_hint_textview.setVisibility(View.GONE);
		footer_progressbar.setVisibility(View.VISIBLE);
	}

	public void show() {
		LayoutParams lp = (LayoutParams) footer_content.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		footer.setLayoutParams(lp);
	}

	public void hide() {
		LayoutParams lp = (LayoutParams) footer_content.getLayoutParams();
		lp.height = 0;
		footer.setLayoutParams(lp);
	}

	public void setText(String alter) {
		footer_hint_textview.setText(alter);
	}

	private void onStateChanged(int state) {

		if (state == STATE_READY) {

			normal();
			setText(getContext().getResources().getString(R.string.lib_ilistview_footer_normal));

		}else {

			normal();
			setText(getContext().getResources().getString(R.string.lib_ilistview_footer_ready));
		}
	}

	@Override
	public Object onHandMessage(int cmd, Object... args) {
		switch(cmd){

			case CMD_BOTTOM_SET_TEXT:
				setText((String)args[0]);
				break;
		}

		return null;
	}

	private void on_bottom_reset(boolean isPullDownLoadingNextMoment){
		if(isPullDownLoadingNextMoment){
			mNormalStopRunnable.start(NormalStopRunnable.SCROLLBACK_CURRENT_TO_LOADING);
		}else{
			mNormalStopRunnable.stop();
			normal();
		}
	}

	//---------------------------------重写一些基本方法----------------------------------------

	@Override
	public void onFooterInit(IMessagerDispatcher messagDispatcher, Object... args) {
		this.messagDispatcher = messagDispatcher;
		hide();
		this.triggerDis = (int)args[0];
	}

	@Override
	public void onFooterUpdateHeight(int delta) {
		int margin = getBottomMargin()+ delta;
		setBottomMargin(margin);
		onStateChanged((margin >= triggerDis) ? STATE_READY : STATE_NORMAL);
	}

	@Override
	public boolean onFooterCanPullDown() {
		return getBottomMargin() >= triggerDis;
	}

	@Override
	public void onFooterLoading() {
		loading();
	}

	@Override
	public void onFooterReset(boolean isPullDownLoadingNextMoment) {
		on_bottom_reset(isPullDownLoadingNextMoment);
	}

	@Override
	public long onFooterStop() {
		on_bottom_reset(false);
		return NormalStopRunnable.DURATION;
	}

	@Override
	public void onFooterShow() {
		show();
	}

	@Override
	public void onFooterHidden() {
		hide();
	}

	@Override
	public int onFooterGetMargin() {
		return getBottomMargin();
	}

	@Override
	public void onFooterRelease() {
		mNormalStopRunnable.stop();
	}


	//---------------------------------Normal To Stop----------------------------------------
	public NormalStopRunnable mNormalStopRunnable = new NormalStopRunnable();
	private class NormalStopRunnable implements Runnable {

		static final int STATUS_START = 1;
		static final int STATUS_STOP  = 3;

		static final long DURATION = 500;

		private boolean isRunning = false;
		private int curStatus = STATUS_STOP;
		private long start;//开始时间

		/**
		 ----------------max     line------------------
		 |
		 ----------------lib_loading line------------------
		 |
		 |
		 ----------------init    line------------------
		 see the two lines :
		 if pulldown the headView over the lib_loading line and release , than the pulldown event will trigger and the headView will scrollback to the lib_loading line;
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
			this.srcY       = getBottomMargin();
			this.dstY       = (scrollType == SCROLLBACK_CURRENT_TO_DEFAULT) ? 0 : 0;
			if(srcY == dstY){
				return;
			}

			changeStatus(STATUS_STOP);
			changeStatus(STATUS_START);
		}

		public void stop(){
			setBottomMargin(dstY);
			changeStatus(STATUS_STOP);

			messagDispatcher.sendMessage(IMessagerDispatcher.DST_ILISTVIEW, IPullCallBacks.IMessageHandler.STOP_FOOTER);
		}

		@Override
		public void run() {

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

					setBottomMargin(newHeight);
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
}
