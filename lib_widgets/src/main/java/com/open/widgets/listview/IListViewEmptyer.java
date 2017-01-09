package com.open.widgets.listview;


import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.open.widgets.listview.IPullCallBacks.IEmptyerCallBack;
import com.open.widgets.listview.IPullCallBacks.IMessagerDispatcher;
import com.open.lib_widgets.R;


/**
 * Emptyer in IListvew
 * Created by long on 2016/12/20.
 */
public class IListViewEmptyer extends LinearLayout implements IEmptyerCallBack {

	public static final int CMD_EMPTY_SET_TEXT		 	= 4001;// 设置空页面显示的字

	IMessagerDispatcher messagDispatcher;

	private ILoadingRelativeLayout  emptyer;
	private TextView				emptyer_result;

	public IListViewEmptyer(Context context)
	{
		super(context);
		initView(context);
	}

	private void initView(Context context)
	{
		emptyer = (ILoadingRelativeLayout) LayoutInflater.from(context).inflate(R.layout.lib_listview_emptyer, this,false);
		emptyer_result = (TextView) emptyer.findViewById(R.id.emptyer_result);

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		addView(emptyer, lp);
		setGravity(Gravity.CENTER);
	}

	@Override
	public Object onHandMessage(int cmd, Object... args) {
		switch(cmd){

			case CMD_EMPTY_SET_TEXT:
				emptyer_result.setText((int)args[0]);
				break;
		}

		return null;
	}

	//---------------------------------重写一些基本方法----------------------------------------
	@Override
	public void onEmptyerInit(IMessagerDispatcher messagDispatcher, Object... args) {
		this.messagDispatcher = messagDispatcher;
	}

	@Override
	public void onEmptyerStart() {
		emptyer_result.setVisibility(INVISIBLE);
		emptyer.startLoadingAnimation();
	}

	@Override
	public void onEmptyerStop(int listSize) {
		emptyer.stopLoadingAnimation();
		emptyer_result.setVisibility(listSize ==0 ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	public void onEmptyerRelease() {
		emptyer.stopLoadingAnimation();
	}

}
