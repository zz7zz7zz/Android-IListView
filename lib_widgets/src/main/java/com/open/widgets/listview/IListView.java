/**
 *
 */
package com.open.widgets.listview;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.open.widgets.listview.IPullCallBacks.*;
import com.open.lib_widgets.R;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * IListvew
 * Created by long on 2016/12/20.
 */
public class IListView extends ListView implements IMessagerDispatcher , IMessageHandler{

	public static final String TAG = "IListView";

	//-----------------------静态属性-----------------------------
	private static final int ENABLED_PULL_NONE		= 0x00000000;//不可上拉/下拉/滑到底部自动更新
	private static final int ENABLED_PULLDOWN		= 0x00000001;//是否可以下拉刷新
	private static final int ENABLED_PULLUP			= 0x00000002;//是否可以上拉刷新
	private static final int ENABLED_AUTO_PULLUP	= 0x00000004;//是否下滑到底部自动模拟上拉刷新

	public static final int STATUS_PULL_DOWN = 1;//往下滑: 下拉刷新
	public static final int STATUS_PULL_UP   = 2;//往上滑: 上拉刷新

	private static final int 	DEFAULT_MIN_SPACE_DURATION = 650;//每次请求至少停留时间，方便动画的平滑过渡
	private static final float 	OFFSET_RADIO = 1.7f; //缩放因子

	private static final HashMap<String, Constructor<?>> sConstructorMap = new HashMap<>();//顶部和底部的View可以通过layout.xml文件配置类名来实现，通过反射实现

	//-----------------------布局文件可配置属性-----------------------------
	private String pull_tag;//标识该View用在哪里
	private String pull_headerview_classname;
	private String pull_emptyview_classname;
	private String pull_footerview_classname;
	private int		pull_capacity = 0; //ListView的下拉/上拉能力
	private int     pull_trigger_distance_pulldown = 0;//下拉时额外的触发距离
	private int 	pull_trigger_distance_pullup = 0;//上拉时额外的触发距离
	private int		pull_smooth_transition_duration;//一个网络请求触发后，至少停留的的平滑过渡时间，防止网络请求太快，动画瞬间执行，太唐突的感觉
	private int 	pull_more_visible_count = 6;//至少多少条才展示上拉展示更多

	//----------------------------------------------------
	private long latest_pulldown_time = 0;
	private long latest_pullup_time = 0;
	private StartRunnable mPullDownStartRunnable 	= new StartRunnable();
	private StopRunnable  mPullDownStopRunnable 	= new StopRunnable();//主要用于延迟回到，用于动画平滑过渡
	private StopRunnable  mPullUpStopRunnable 		= new StopRunnable();//主要用于延迟回到，用于动画平滑过渡

	private float mLastY      = -1; //最后一次触摸的Y的位置
	private boolean isSlideUp;//是否上滑趋势
	private int   mDataSetSize= 0;  //真实数据大小
	private boolean isPullDownLoading = false;
	private boolean isPullUpLoading   = false;
	private IPullCallBackListener mPullCallBackListener = null;
	private OnScrollListener subOnScrollListener=null;
	private int mTotalItemCount;

	//头部UI
	private IHeaderCallBack  mHeaderView;
	private IFooterCallBack  mFooterView;
	private IEmptyerCallBack mEmptyerView;
	private boolean isEmptyViewAdded = false;


	public IListView(Context context) {
		this(context, null);
	}

	public IListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IListView, defStyleAttr, 0);

		pull_tag 						= a.getString(R.styleable.IListView_pull_tag);
		pull_capacity 					= a.getInt(R.styleable.IListView_pull_capacity, ENABLED_PULL_NONE);
		pull_smooth_transition_duration = a.getInt(R.styleable.IListView_pull_smooth_transition_duration, DEFAULT_MIN_SPACE_DURATION);
		pull_more_visible_count 		= a.getInt(R.styleable.IListView_pull_more_visible_count, pull_more_visible_count);

		pull_headerview_classname 		= a.getString(R.styleable.IListView_pull_header_classname);
		pull_emptyview_classname 		= a.getString(R.styleable.IListView_pull_emptyer_classname);
		pull_footerview_classname 		= a.getString(R.styleable.IListView_pull_footer_classname);

		pull_trigger_distance_pulldown = (int)a.getDimension(R.styleable.IListView_pull_trigger_dis_pulldown, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()));
		pull_trigger_distance_pullup 	= (int)a.getDimension(R.styleable.IListView_pull_trigger_dis_pullup, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()));

		a.recycle();

		initView();
	}

	protected void initView()
	{
		setOnScrollListener(mProxyScrollListener);

		//1.空数据UI
		addEmptyerView();

		//2.头部UI
		addHeaderView();

		//3.添加尾部
		addFooterView();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		if(l != mProxyScrollListener) {
			subOnScrollListener = l;
		}
		super.setOnScrollListener(mProxyScrollListener);
	}

	public void setPullCallBackListener(IPullCallBackListener listener) {
		this.mPullCallBackListener = listener;
	}

	private int oldScrollState;
	private OnScrollListener mProxyScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

			oldScrollState = scrollState;

			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

				// 滚动到底部
				if (!isPullUpLoading && isAutoPullUpEnabled() && getLastVisiblePosition() == (mTotalItemCount - 1)){
					startPullUpLoading();
				}

				resetHeaderHeight(isPullDownLoading);
				resetFooterHeight(isPullUpLoading);
			}

			if(null != subOnScrollListener)
			{
				subOnScrollListener.onScrollStateChanged(view, scrollState);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
							 int visibleItemCount, int totalItemCount) {

			//之所以在这里开始上滑加载，是为了稍微平滑一些(暂停之后再进行加载数据，从文字到选择动画有点突然)
			if (isSlideUp && oldScrollState != OnScrollListener.SCROLL_STATE_IDLE && !isPullUpLoading && isAutoPullUpEnabled() && (firstVisibleItem + visibleItemCount == totalItemCount)){
				isSlideUp = false;
				startPullUpLoading();
			}

			mTotalItemCount = totalItemCount;
			if(null != subOnScrollListener) {
				subOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if(isPullDownLoading && isPullUpLoading) {
			return super.onTouchEvent(ev);
		}

		if (mLastY == -1) {
			mLastY = ev.getRawY();
			isSlideUp = false;
			mDataSetSize = IListView.this.getCount() -  IListView.this.getHeaderViewsCount() -  IListView.this.getFooterViewsCount();
		}

		switch(ev.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				mLastY = ev.getRawY();
				mDataSetSize = IListView.this.getCount() -  IListView.this.getHeaderViewsCount() -  IListView.this.getFooterViewsCount();
				break;

			case MotionEvent.ACTION_MOVE:
				final float deltaY = ev.getRawY() - mLastY;
				mLastY = ev.getRawY();

				isSlideUp = (deltaY < 0);
				if(mDataSetSize>0){//只有在大于0的情况下才可以下拉/上拉动作, 否则只允许点击空白区域进行加载
					if(getFirstVisiblePosition() == 0 && !isPullDownLoading && isPullDownEnabled()) {
						updateHeaderHeight(deltaY / OFFSET_RADIO);
					}
					else if(getLastVisiblePosition() ==  (mTotalItemCount-1) && !isPullUpLoading && isPullUpEnabled() && (deltaY < 0 || getFooterMargin() > 0)) {
						updateFooterHeight(-deltaY / OFFSET_RADIO);
					}
				}

				break;

			default:
				if(mDataSetSize>0) {//只有在大于0的情况下才可以下拉/上拉动作, 否则只允许点击空白区域进行加载
					if (getFirstVisiblePosition() == 0 && !isPullDownLoading && isPullDownEnabled() && canPullDown())
					{
						startPullDownLoading(0);
					}
					else if(getLastVisiblePosition() ==  (mTotalItemCount-1) && !isPullUpLoading && ((isPullUpEnabled() && canPullUp()))){

						startPullUpLoading();
					}
				}

				resetFooterHeight(isPullUpLoading);
				resetHeaderHeight(isPullDownLoading);
				mLastY = -1;
				break;

		}

		return super.onTouchEvent(ev);
	}

	//-----------------------A: Touch 事件处理-------------------------
	private void updateHeaderHeight(float delta)
	{
		if(null != mHeaderView) {
			mHeaderView.onHeaderUpdateHeight((int) delta);
		}
	}

	private void resetHeaderHeight(boolean isPullDownLoadingNextMoment)
	{
		if(null != mHeaderView) {
			mHeaderView.onHeaderReset(isPullDownLoadingNextMoment);
		}
	}

	private boolean canPullDown()
	{
		return null != mHeaderView && mHeaderView.onHeaderCanPullDown();
	}

	private void updateFooterHeight(float delta)
	{
		if(null != mFooterView) {
			mFooterView.onFooterUpdateHeight((int) delta);
		}
	}

	private void resetFooterHeight(boolean isPullUpLoadingNextMoment)
	{
		if(null != mFooterView) {
			mFooterView.onFooterReset(isPullUpLoadingNextMoment);
		}
	}

	private boolean canPullUp()
	{
		return null != mFooterView && mFooterView.onFooterCanPullDown();
	}

	private int getFooterMargin()
	{
		if(null != mFooterView) {
			return mFooterView.onFooterGetMargin();
		}
		return 0;
	}

	//-----------------------B: View 处理-------------------------
	//------------------------ HeadView------------------------------
	private void addHeaderView()
	{
		if(null == mHeaderView && !TextUtils.isEmpty(pull_headerview_classname))
		{
			try {

				//1. create view
				Constructor<?> constructor = sConstructorMap.get(pull_headerview_classname);
				Class<?> clazz ;

				if(null == constructor) {
					clazz = getContext().getClassLoader().loadClass(pull_headerview_classname);

					Class<?>[] mConstructorSignature = new Class[] {Context.class};
					constructor = clazz.getConstructor(mConstructorSignature);
					constructor.setAccessible(true);

					sConstructorMap.put(pull_headerview_classname, constructor);
				}

				Object[] args = {getContext()};
				mHeaderView = (IHeaderCallBack) constructor.newInstance(args);

				if(!(mHeaderView instanceof View))
				{
					throw new Exception("HeaderView must both instanceof View and instanceof IHeaderCallBack! ");
				}

				//2. init view
				mHeaderView.onHeaderInit(this,pull_trigger_distance_pulldown);

				//3. addView
				addHeaderView((View) mHeaderView);

			}catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void removeHeaderView()
	{
		if (null != mHeaderView)
		{
			removeHeaderView((View) mHeaderView);
			mHeaderView =null;
		}
	}

	//------------------------ FootView------------------------------
	public void addFooterView()
	{
		if (null == mFooterView && !TextUtils.isEmpty(pull_footerview_classname))
		{
			try {

				//1. create view
				Constructor<?> constructor = sConstructorMap.get(pull_footerview_classname);
				Class<?> clazz ;

				if(null == constructor) {
					clazz = getContext().getClassLoader().loadClass(pull_footerview_classname);

					Class<?>[] mConstructorSignature = new Class[] {Context.class};
					constructor = clazz.getConstructor(mConstructorSignature);
					constructor.setAccessible(true);

					sConstructorMap.put(pull_footerview_classname, constructor);
				}

				Object[] args = {getContext()};
				mFooterView = (IFooterCallBack) constructor.newInstance(args);

				if(!(mFooterView instanceof View))
				{
					throw new Exception("FooterView must both instanceof View and instanceof IFooterCallBack! ");
				}

				//2. init view
				mFooterView.onFooterInit(this,pull_trigger_distance_pullup);

				//3. addView
				addFooterView((View) mFooterView);

			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void removeFooterView()
	{
		if (null != mFooterView)
		{
			removeFooterView((View) mFooterView);
			mFooterView =null;
		}
	}

	private void showFooterView()
	{
		if(null != mFooterView) {
			mFooterView.onFooterShow();
		}
	}

	private void hiddenFooterView()
	{
		if (null != mFooterView) {
			mFooterView.onFooterHidden();
		}
	}

	//------------------------ EmptyView------------------------------
	private void addEmptyerView()
	{
		if(null == mEmptyerView && !TextUtils.isEmpty(pull_emptyview_classname))
		{
			try {

				//1. create view
				Constructor<?> constructor = sConstructorMap.get(pull_emptyview_classname);
				Class<?> clazz ;

				if(null == constructor) {
					clazz = getContext().getClassLoader().loadClass(pull_emptyview_classname);

					Class<?>[] mConstructorSignature = new Class[] {Context.class};
					constructor = clazz.getConstructor(mConstructorSignature);
					constructor.setAccessible(true);

					sConstructorMap.put(pull_emptyview_classname, constructor);
				}

				Object[] args = {getContext()};
				try {
					mEmptyerView = (IEmptyerCallBack)constructor.newInstance(args);
				}catch (Exception e){
					e.printStackTrace();
				}

				if(!(mEmptyerView instanceof View))
				{
					throw new Exception("EmptyView must both instanceof View and instanceof IEmptyerCallBack! ");
				}

				//2. init view
				mEmptyerView.onEmptyerInit(this);
				((View) mEmptyerView).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(null != mPullCallBackListener)
						{
							startPullDownLoading();
						}
					}
				});

				//3. addView

			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void showEmptyView()
	{
		if(null != mEmptyerView){
			View view = ((View) mEmptyerView);
			if(!isEmptyViewAdded){
				ViewGroup parent = (ViewGroup) getParent();
				if(null !=parent){
					view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					parent.addView(view);
					view.setVisibility(View.GONE);

					isEmptyViewAdded =true;
				}
			}
			setEmptyView(view);
		}
	}

	//-----------------------C: 设置各种能力-------------------------
	public void setPulldownEnabled(boolean enable)
	{
		if(enable)
		{
			pull_capacity |=ENABLED_PULLDOWN;
		}
		else
		{
			pull_capacity = (pull_capacity &~ ENABLED_PULLDOWN);
		}
	}

	private boolean isPullDownEnabled()
	{
		return (pull_capacity & ENABLED_PULLDOWN) == ENABLED_PULLDOWN;
	}

	public void setPullUpEnabled(boolean enable)
	{
		if(enable)
		{
			pull_capacity |=ENABLED_PULLUP;
		}
		else
		{
			pull_capacity = (pull_capacity &~ ENABLED_PULLUP);
		}
	}

	private boolean isPullUpEnabled()
	{
		return (pull_capacity & ENABLED_PULLUP) == ENABLED_PULLUP;
	}

	public void setAutoPullUpEnabled(boolean enable)
	{
		if(enable)
		{
			pull_capacity |=ENABLED_AUTO_PULLUP;
		}
		else
		{
			pull_capacity = (pull_capacity &~ ENABLED_AUTO_PULLUP);
		}
	}

	private boolean isAutoPullUpEnabled()
	{
		return (pull_capacity & ENABLED_AUTO_PULLUP) == ENABLED_AUTO_PULLUP;
	}

	//-----------------------D: 下拉/自动刷新-------------------------
	public void startPullDownLoading()
	{
		startPullDownLoading(250);
	}

	public void startPullDownLoading(long delayMillis)
	{
		this.removeCallbacks(mPullDownStartRunnable);
		if(delayMillis>0){
			this.postDelayed(mPullDownStartRunnable,delayMillis);
		}else{
			mPullDownStartRunnable.run();
		}
	}

	public void startPullUpLoading()
	{
		// 滚动到底部
		if (!isPullUpLoading ){

			isPullUpLoading=true;

			if(null != mFooterView) {
				mFooterView.onFooterLoading();
			}

			if(null != mPullCallBackListener) {
				latest_pullup_time = System.currentTimeMillis();
				mPullCallBackListener.onPullUp();
			}

			resetFooterHeight(isPullUpLoading);
		}
	}

	public void stopPullLoading(int pullType , Runnable callBackAction)
	{
		if(pullType == STATUS_PULL_DOWN || pullType == STATUS_PULL_UP){
			long elapse_time;
			StopRunnable action;

			if(pullType == STATUS_PULL_DOWN){

				elapse_time = System.currentTimeMillis()- latest_pulldown_time;

				mPullDownStopRunnable.pullType = pullType;
				mPullDownStopRunnable.task     = callBackAction;

				action = mPullDownStopRunnable;

			} else{

				//			spaceTime = System.currentTimeMillis()- latest_pullup_time;
				elapse_time = pull_smooth_transition_duration;

				mPullUpStopRunnable.pullType = pullType;
				mPullUpStopRunnable.task     = callBackAction;

				action = mPullUpStopRunnable;
			}

			postDelayed(action, elapse_time < pull_smooth_transition_duration ? pull_smooth_transition_duration - elapse_time : 1);
		}
	}

	private long stopPull(int pullType , int oldDataSetSize ,int newListSize)
	{
		long ms =0;
		if(pullType == STATUS_PULL_DOWN && isPullDownLoading){

			if(oldDataSetSize == 0){
				//1.重置空EmptyView
				if(null != mEmptyerView){
					mEmptyerView.onEmptyerStop(newListSize);
				}
			}else{
				//2.重置HeadView
				if(null != mHeaderView){
					ms = mHeaderView.onHeaderStop();
				}
			}

			//有动画的话，得动画执行完成后设置下拉为false , 防止处于动画过程中用户会执行下拉动作
			postDelayed(new Runnable() {
				@Override
				public void run() {
					isPullDownLoading = false;
					resetHeaderHeight(isPullDownLoading);
					refreshFooterAndEmptyer();
				}
			},ms+10);
			return ms;
		}
		else if(pullType == STATUS_PULL_UP && isPullUpLoading){

			//重置FootView
			if(null != mFooterView)
			{
				ms = mFooterView.onFooterStop();
			}

			postDelayed(new Runnable() {
				@Override
				public void run() {
					isPullUpLoading = false;
					resetFooterHeight(isPullUpLoading);
					refreshFooterAndEmptyer();
				}
			},ms+10);
		}

		return ms;
	}

	private class StartRunnable implements Runnable {
		@Override
		public void run() {

			if(isPullDownLoading) {
				return;
			}
			isPullDownLoading = true;

			mDataSetSize = IListView.this.getCount() -  IListView.this.getHeaderViewsCount() -  IListView.this.getFooterViewsCount();

			if(mDataSetSize == 0){//1.首次没有数据时

				showEmptyView();
				if(null != mEmptyerView){
					mEmptyerView.onEmptyerStart();
				}

			}else{//2.有数据时

				if(null != mHeaderView) {
					mHeaderView.onHeaderLoading();
				}
			}

			if(null != mPullCallBackListener) {
				latest_pulldown_time = System.currentTimeMillis();
				mPullCallBackListener.onPullDown();
			}
		}
	}

	private class StopRunnable implements Runnable {
		int pullType = 0;
		Runnable task;

		StopRunnable() {
		}

		@Override
		public void run() {

			mDataSetSize = IListView.this.getCount() -  IListView.this.getHeaderViewsCount() -  IListView.this.getFooterViewsCount();

			if(null !=task){
				task.run();
			}

			int newListSize = IListView.this.getCount() -  IListView.this.getHeaderViewsCount() -  IListView.this.getFooterViewsCount();

			stopPull(pullType , mDataSetSize,newListSize);
		}
	}

	//------------------------检查数据来决定是否显示空数据/上拉加载更多------------------------------
	public void refreshFooterAndEmptyer(){
		refreshFooterAndEmptyer(1);
	}

	private void refreshFooterAndEmptyer(long delayMillis){
		removeCallbacks(mRefreshFooterAndEmptyerRunnable);
		postDelayed(mRefreshFooterAndEmptyerRunnable,delayMillis);
	}

	private Runnable mRefreshFooterAndEmptyerRunnable = new Runnable() {
		@Override
		public void run() {
			mDataSetSize = IListView.this.getCount() -  IListView.this.getHeaderViewsCount() -  IListView.this.getFooterViewsCount();

			boolean isChildFillParent = (mDataSetSize > pull_more_visible_count);//是否完全填充，最后一条数据底部坐标>=ListView底部坐标，说明完全填充；其它未完全填充
			if(!isChildFillParent && mDataSetSize > 0){//只有在有数据并且未填充满(<pull_more_visible_count)的时候才去判断一下
				isChildFillParent = mTotalItemCount>IListView.this.getChildCount();
				if(!isChildFillParent){
					int mListViewHeight = IListView.this.getMeasuredHeight();
					int allChildHeight=0;
					for (int i = 0; i < getChildCount(); i++) {
						View childView = getChildAt(i);
						if(null !=childView && !(childView instanceof IMessageHandler)){
							allChildHeight += childView.getMeasuredHeight();
						}
					}
					isChildFillParent = allChildHeight>=mListViewHeight;
				}
			}

			if (isChildFillParent) {
				showFooterView();
			}else{
				hiddenFooterView();
			}

			showEmptyView();
		}
	};

	//------------------------E: 重写父类相关方法------------------------------
	public void release(){
		setPullCallBackListener(null);
		setOnScrollListener(null);
		removeCallbacks(mPullDownStartRunnable);
		removeCallbacks(mPullDownStopRunnable);
		removeCallbacks(mPullUpStopRunnable);
		removeCallbacks(mRefreshFooterAndEmptyerRunnable);
		if(null != mHeaderView){
			mHeaderView.onHeaderRelease();
		}
		if(null != mFooterView){
			mFooterView.onFooterRelease();
		}
		if(null != mEmptyerView){
			mEmptyerView.onEmptyerRelease();
		}
	}

	//-----------------------自定义通信接口--------------------
	@Override
	public void sendMessage(int dst, int cmd, Object... args){
		switch (dst){
			case DST_HEADER:
				if(null != mHeaderView){
					mHeaderView.onHandMessage(cmd,args);
				}
				break;

			case DST_EMPTY:
				if(null != mEmptyerView){
					mEmptyerView.onHandMessage(cmd,args);
				}
				break;

			case DST_FOOTER:
				if(null != mFooterView){
					mFooterView.onHandMessage(cmd,args);
				}
				break;

			case DST_ILISTVIEW:
				onHandMessage(cmd,args);
				break;
		}
	}

	@Override
	public Object onHandMessage(int cmd, Object... args) {
		switch(cmd){

			case STOP_HEADER:
				break;

			case STOP_FOOTER:
				break;

			case STOP_EMPTYER:
				break;
		}
		return null;
	}

	// 修复monkey崩溃
	public boolean dispatchTouchEvent(MotionEvent ev) {
		try {
			return super.dispatchTouchEvent(ev);
		}catch (Exception e){
			return false;
		}
	}
}
