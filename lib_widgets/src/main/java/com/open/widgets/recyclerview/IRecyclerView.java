package com.open.widgets.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.AbsSavedState;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.open.lib_widgets.R;
import com.open.widgets.listview.IPullCallBacks.IEmptyerCallBack;
import com.open.widgets.listview.IPullCallBacks.IEmptyerViewClickListener;
import com.open.widgets.listview.IPullCallBacks.IFooterCallBack;
import com.open.widgets.listview.IPullCallBacks.IFooterViewClickListener;
import com.open.widgets.listview.IPullCallBacks.IHeaderCallBack;
import com.open.widgets.listview.IPullCallBacks.IMessageHandler;
import com.open.widgets.listview.IPullCallBacks.IMessagerDispatcher;
import com.open.widgets.listview.IPullCallBacks.IPullCallBackListener;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * Created by long on 2016/12/29.
 */

public class IRecyclerView extends RecyclerView implements IMessagerDispatcher, IMessageHandler {

    // -------------------------------------------------------------------------------------------
    public static final String TAG = "IRecyclerView";
    public static final int STATUS_PULL_DOWN = 1;// 往下滑: 下拉刷新
    public static final int STATUS_PULL_UP   = 2;// 往上滑: 上拉刷新

    // -------------------------------------------------------------------------------------------
    // -----------------------静态属性-----------------------------
    private static final int ENABLED_PULL_NONE   = 0x00000000;// 不可上拉/下拉/滑到底部自动更新
    private static final int ENABLED_PULLDOWN    = 0x00000001;// 是否可以下拉刷新
    private static final int ENABLED_PULLUP      = 0x00000002;// 是否可以上拉刷新
    private static final int ENABLED_AUTO_PULLUP = 0x00000004;// 是否下滑到底部自动模拟上拉刷新
    private static final int   DEFAULT_MIN_SPACE_DURATION = 650;  // 每次请求至少停留时间，方便动画的平滑过渡
    private static final float OFFSET_RADIO               = 1.7f; // 缩放因子
    private static final HashMap<String, Constructor<?>> sConstructorMap = new HashMap<>();// 顶部和底部的View可以通过layout.xml文件配置类名来实现，通过反射实现
    private SparseArrayCompat<View> mHeaderViewInfos = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFooterViewInfos = new SparseArrayCompat<>();
    private View mEmptyView;
    private int header_viewType_Index = 0;
    private int footer_viewType_Index = 0;
    // -----------------------布局文件可配置属性-----------------------------
    private String pull_tag;                               // 标识该View用在哪里
    private String pull_headerview_classname;
    private String pull_emptyview_classname;
    private String pull_footerview_classname;
    private int    pull_capacity                     = 0;  // ListView的下拉/上拉能力
    private int    pull_trigger_distance_pulldown    = 0;  // 下拉时额外的触发距离
    private int    pull_trigger_distance_pullup      = 0;  // 上拉时额外的触发距离
    private int    pulldown_smooth_transition_duration;    // 一个网络请求触发后，至少停留的的平滑过渡时间，防止网络请求太快，动画瞬间执行，太唐突的感觉
    private int    pullup_smooth_transition_duration = 384;// 一个网络请求触发后，至少停留的的平滑过渡时间，防止网络请求太快，动画瞬间执行，太唐突的感觉
    private int    pull_more_visible_count           = 6;  // 至少多少条才展示上拉展示更多
    private int    pull_empter_margin                = 0;  // EmpterView 距离四周的距离
    private int    pull_empter_margin_left           = 0;  // EmpterView 距离四周的距离
    private int    pull_empter_margin_right          = 0;  // EmpterView 距离四周的距离
    private int    pull_empter_margin_top            = 0;  // EmpterView 距离四周的距离
    private int    pull_empter_margin_bottom         = 0;  // EmpterView 距离四周的距离
    // ----------------------------------------------------
    private long          latest_pulldown_time   = 0;
    private long          latest_pullup_time     = 0;
    private StartRunnable mPullDownStartRunnable = new StartRunnable();
    private StopRunnable  mPullDownStopRunnable  = new StopRunnable(); // 主要用于延迟回到，用于动画平滑过渡
    private StopRunnable  mPullUpStopRunnable    = new StopRunnable(); // 主要用于延迟回到，用于动画平滑过渡
    private float                 mLastY                = -1;   // 最后一次触摸的Y的位置
    private boolean               isSlideUp;                    // 是否上滑趋势
    private int                   mDataSetSize          = 0;    // 真实数据大小
    private boolean               isPullDownLoading     = false;
    private boolean               isPullDownStopping    = false;//是否正在暂停中，当执行stopPull()后并没有真正的stop，会把该属性设置为true，当stopPull的动画完成后，会把该属性设置为false，同时isPullDownloading也被设置为false，这时候才是真正的stop了
    private boolean               isPullUpLoading       = false;
    private IEmptyerViewClickListener mEmptyerViewListener;
    private IPullCallBackListener mPullCallBackListener = null;
    private IFooterViewClickListener mFootViewClickListener;
    private int                   mTotalItemCount;
    // 头部UI
    private IHeaderCallBack  mHeaderView;
    private IFooterCallBack  mFooterView;
    private IEmptyerCallBack mEmptyerView;
    private boolean          isEmptyViewAdded = false;
    private Runnable mRefreshFooterAndEmptyerRunnable = new Runnable() {

        @Override
        public void run() {
//            if(!isPullUpLoading){
//                mDataSetSize = IRecyclerView.this.getCount() - getHeaderViewsCount() - getFooterViewsCount();
//                boolean isChildFillParent = false;// 是否完全填充，最后一条数据底部坐标>=ListView底部坐标，说明完全填充；其它未完全填充
//                if (mDataSetSize > 0) {// 只有在有数据并且未填充满(<pull_more_visible_count)的时候才去判断一下
//                    LayoutManager layout = getLayoutManager();
//                    if (layout instanceof StaggeredGridLayoutManager) {
//                        int lastVisibleItemPosition = getLastVisiblePosition() - getFooterViewsCount();
//                        if (lastVisibleItemPosition >= 0) {
//                            int spanCount = ((StaggeredGridLayoutManager) layout).getSpanCount();
//                            int row = (lastVisibleItemPosition / spanCount) + (lastVisibleItemPosition % spanCount == 0 ? 0 : 1);
//                            int mListViewHeight = IRecyclerView.this.getMeasuredHeight();
//                            int allChildHeight = row * IRecyclerView.this.getChildAt(getHeaderViewsCount()).getMeasuredHeight();
//                            isChildFillParent = allChildHeight >= mListViewHeight;
//                        }
//                    } else if (layout instanceof GridLayoutManager) {
//                        int lastVisibleItemPosition = getLastVisiblePosition() - getFooterViewsCount();
//                        if (lastVisibleItemPosition >= 0) {
//                            int spanCount = ((GridLayoutManager) layout).getSpanCount();
//                            int row = (lastVisibleItemPosition / spanCount) + (lastVisibleItemPosition % spanCount == 0 ? 0 : 1);
//                            int mListViewHeight = IRecyclerView.this.getMeasuredHeight();
//                            int allChildHeight = row * IRecyclerView.this.getChildAt(getHeaderViewsCount()).getMeasuredHeight();
//                            isChildFillParent = allChildHeight >= mListViewHeight;
//                        }
//                    } else if (layout instanceof LinearLayoutManager) {
//
//                        isChildFillParent = (mDataSetSize > pull_more_visible_count)
//                                || mTotalItemCount > IRecyclerView.this.getChildCount();
//                        if (!isChildFillParent) {
//                            int mListViewHeight = IRecyclerView.this.getMeasuredHeight();
//                            int allChildHeight = 0;
//                            for (int i = 0; i < getChildCount(); i++) {
//                                View childView = getChildAt(i);
//                                if (null != childView && !(childView instanceof IMessageHandler)) {
//                                    allChildHeight += childView.getMeasuredHeight();
//                                }
//                            }
//                            isChildFillParent = allChildHeight >= mListViewHeight;
//                        }
//                    }
//                }
//
//                if (isChildFillParent) {
//                    showFooterView();
//                } else {
//                    hiddenFooterView();
//                }
//            }
            showEmptyView();
        }
    };
    public IRecyclerView(Context context) {
        this(context, null);
    }

    public IRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public IRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IRecyclerView, defStyle, 0);

        pull_tag = a.getString(R.styleable.IRecyclerView_recyclerview_pull_tag);
        pull_capacity = a.getInt(R.styleable.IRecyclerView_recyclerview_pull_capacity, ENABLED_PULL_NONE);
        pulldown_smooth_transition_duration = a.getInt(R.styleable.IRecyclerView_recyclerview_pull_smooth_transition_duration,
                                                       DEFAULT_MIN_SPACE_DURATION);
        pull_more_visible_count = a.getInt(R.styleable.IRecyclerView_recyclerview_pull_more_visible_count,
                                           pull_more_visible_count);

        pull_headerview_classname = a.getString(R.styleable.IRecyclerView_recyclerview_pull_header_classname);
        pull_emptyview_classname = a.getString(R.styleable.IRecyclerView_recyclerview_pull_emptyer_classname);
        pull_footerview_classname = a.getString(R.styleable.IRecyclerView_recyclerview_pull_footer_classname);

        pull_trigger_distance_pulldown = (int) a.getDimension(R.styleable.IRecyclerView_recyclerview_pull_trigger_dis_pulldown, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()));
        pull_trigger_distance_pullup = (int) a.getDimension(R.styleable.IRecyclerView_recyclerview_pull_trigger_dis_pullup, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15,getResources().getDisplayMetrics()));
        pull_empter_margin = (int) a.getDimension(R.styleable.IRecyclerView_recyclerview_pull_empter_margin, 0);
        pull_empter_margin_left = (int) a.getDimension(R.styleable.IRecyclerView_recyclerview_pull_empter_margin_left, pull_empter_margin);
        pull_empter_margin_right = (int) a.getDimension(R.styleable.IRecyclerView_recyclerview_pull_empter_margin_right, pull_empter_margin);
        pull_empter_margin_top = (int) a.getDimension(R.styleable.IRecyclerView_recyclerview_pull_empter_margin_top, pull_empter_margin);
        pull_empter_margin_bottom = (int) a.getDimension(R.styleable.IRecyclerView_recyclerview_pull_empter_margin_bottom, pull_empter_margin);

        a.recycle();

        initView();
    }

    public void addHeaderView(View v) {
        if (null == v) {
            return;
        }
        //有相同的View则不添加进去
        int len = mHeaderViewInfos.size();
        for (int i = 0; i < len; ++i) {
            Object obj = mHeaderViewInfos.valueAt(i);
            if (v == obj) {
                return;
            }
        }

        header_viewType_Index++;
        mHeaderViewInfos.put(HeaderFooterAdapter.BASE_ITEM_VIEW_TYPE_HEADER + header_viewType_Index, v);

        Adapter mAdapter = getAdapter();
        if (null != mAdapter) {
            if (!(mAdapter instanceof HeaderFooterAdapter)) {
                wrapHeaderListAdapterInternal();
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public boolean removeHeaderView(View v) {
        if (null == v) {
            return false;
        }
        if (mHeaderViewInfos.size() > 0) {

            //首先清空缓存池的相关缓存
            int len = mHeaderViewInfos.size();
            for (int i = 0; i < len; ++i) {
                Object obj = mHeaderViewInfos.valueAt(i);
                if (v == obj) {
                    int viewType = mHeaderViewInfos.keyAt(i);
                    getRecycledViewPool().setMaxRecycledViews(viewType,0);
                    break;
                }
            }

            boolean result = false;
            Adapter mAdapter = getAdapter();
            if (mAdapter != null && ((HeaderFooterAdapter) mAdapter).removeHeader(v)) {
                result = true;
                mAdapter.notifyDataSetChanged();
            }
            removeFixedViewInfo(v, mHeaderViewInfos);
            return result;
        }
        return false;
    }

    public void addFooterView(View v) {
        if (null == v) {
            return;
        }

        //有相同的View则不添加进去
        int len = mFooterViewInfos.size();
        for (int i = 0; i < len; ++i) {
            Object obj = mFooterViewInfos.valueAt(i);
            if (v == obj) {
                return;
            }
        }

        footer_viewType_Index++;
        mFooterViewInfos.put(HeaderFooterAdapter.BASE_ITEM_VIEW_TYPE_FOOTER + footer_viewType_Index, v);

        Adapter mAdapter = getAdapter();
        if (null != mAdapter) {
            if (!(mAdapter instanceof HeaderFooterAdapter)) {
                wrapHeaderListAdapterInternal();
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public boolean removeFooterView(View v) {
        if (null == v) {
            return false;
        }
        if (mFooterViewInfos.size() > 0) {

            //首先清空缓存池的相关缓存
            int len = mFooterViewInfos.size();
            for (int i = 0; i < len; ++i) {
                Object obj = mFooterViewInfos.valueAt(i);
                if (v == obj) {
                    int viewType = mFooterViewInfos.keyAt(i);
                    getRecycledViewPool().setMaxRecycledViews(viewType,0);
                    break;
                }
            }

            boolean result = false;
            Adapter mAdapter = getAdapter();
            if (mAdapter != null && ((HeaderFooterAdapter) mAdapter).removeFooter(v)) {
                result = true;
                mAdapter.notifyDataSetChanged();
            }
            removeFixedViewInfo(v, mFooterViewInfos);
            return result;
        }
        return false;
    }

    private void removeFixedViewInfo(View v, SparseArrayCompat<View> where) {
        int len = where.size();
        for (int i = 0; i < len; ++i) {
            Object obj = where.valueAt(i);
            if (v == obj) {
                where.removeAt(i);
                break;
            }
        }
    }

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;

        final Adapter adapter = getAdapter();
        boolean empty;
        if (null != adapter) {
            if (adapter instanceof HeaderFooterAdapter) {
                empty = ((HeaderFooterAdapter) adapter).isEmpty();
            } else {
                empty = (adapter.getItemCount() == 0);
            }
        } else {
            empty = true;
        }

        updateEmptyStatus(empty);
    }

    private void updateEmptyStatus(boolean empty) {
        if (empty) {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.VISIBLE);
                setVisibility(View.GONE);
            } else {
                // If the caller just removed our empty view, make sure the list view is visible
                setVisibility(View.VISIBLE);
            }
        } else {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.GONE);
            }
            setVisibility(View.VISIBLE);
        }
    }

    private void checkFocus() {
        final Adapter adapter = getAdapter();
        boolean empty;
        if (null != adapter) {
            if (adapter instanceof HeaderFooterAdapter) {
                empty = ((HeaderFooterAdapter) adapter).isEmpty();
            } else {
                empty = (adapter.getItemCount() == 0);
            }
        } else {
            empty = true;
        }
        if (mEmptyView != null) {
            updateEmptyStatus(empty);
        }
    }

    protected HeaderFooterAdapter wrapHeaderListAdapterInternal(SparseArrayCompat<View> headerViewInfos,
                                                                SparseArrayCompat<View> footerViewInfos, Adapter adapter) {
        return new HeaderFooterAdapter(headerViewInfos, footerViewInfos, adapter);
    }

    protected void wrapHeaderListAdapterInternal() {
        setAdapter(wrapHeaderListAdapterInternal(mHeaderViewInfos, mFooterViewInfos, getAdapter()));
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (mHeaderViewInfos.size() > 0 || mFooterViewInfos.size() > 0) {
            super.setAdapter(wrapHeaderListAdapterInternal(mHeaderViewInfos, mFooterViewInfos, adapter));
        } else {
            super.setAdapter(adapter);
        }
        checkFocus();
    }

    public int getCount() {
        Adapter adapter = getAdapter();
        return null != adapter ? adapter.getItemCount() : 0;
    }

    public int getHeaderViewsCount() {
        return mHeaderViewInfos.size();
    }

    public int getFooterViewsCount() {
        return mFooterViewInfos.size();
    }

    public int getFirstVisiblePosition() {
        LayoutManager layout = getLayoutManager();
        if (layout instanceof GridLayoutManager) {
            return ((GridLayoutManager) layout).findFirstVisibleItemPosition();
        } else if (layout instanceof StaggeredGridLayoutManager) {
            int[] firstVisibleItems = null;
            firstVisibleItems = ((StaggeredGridLayoutManager) getLayoutManager())
                    .findFirstVisibleItemPositions(firstVisibleItems);

            int size = firstVisibleItems.length;
            int minPosition = Integer.MAX_VALUE;
            for (int i = 0; i < size; i++) {
                minPosition = Math.min(minPosition, firstVisibleItems[i]);
            }
            return minPosition;
        } else if (layout instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layout).findFirstVisibleItemPosition();
        }
        return 0;
    }

    public int getLastVisiblePosition() {
        LayoutManager layout = getLayoutManager();

        if (layout instanceof GridLayoutManager) {
            return ((GridLayoutManager) layout).findLastVisibleItemPosition();
        } else if (layout instanceof StaggeredGridLayoutManager) {
            int[] firstVisibleItems = null;
            firstVisibleItems = ((StaggeredGridLayoutManager) getLayoutManager()).findLastVisibleItemPositions(firstVisibleItems);

            int size = firstVisibleItems.length;
            int maxPosition = Integer.MIN_VALUE;
            for (int i = 0; i < size; i++) {
                maxPosition = Math.max(maxPosition, firstVisibleItems[i]);
            }
            return maxPosition;
        } else if (layout instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layout).findLastVisibleItemPosition();
        }
        return getLayoutManager().getItemCount() - 1;
    }

    public boolean isSlideUp() {
        return isSlideUp;
    }

    protected void initView() {
        internalAddOnScrollListener();

        // 1.空数据UI
        addEmptyerView();

        // 2.头部UI
        addHeaderView();

        // 3.添加尾部
        addFooterView();
        hiddenFooterView();
    }

    private void internalAddOnScrollListener() {
        addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    // 只有在没有加载完成的情况下才去判断是否加载更多
                    if (!isPullUpLoading && isAutoPullUpEnabled() && null != getAdapter()) {
                        int lastPosition = getLastVisiblePosition();
                        if (lastPosition >= getAdapter().getItemCount() - 1) {
                            startPullUpLoading();
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public void setPullCallBackListener(IPullCallBackListener listener) {
        this.mPullCallBackListener = listener;
    }


    public void setEmptyerViewClickListener(IEmptyerViewClickListener mEmptyerViewListener){
        this.mEmptyerViewListener = mEmptyerViewListener;
    }

    public void setFootViewClickListener(IFooterViewClickListener mFootViewClickListener) {
        this.mFootViewClickListener = mFootViewClickListener;
    }

    private IFooterViewClickListener DEFAULT_FOOTER_VIEW_CLICK_LISTENER = new IFooterViewClickListener() {
        @Override
        public void onFooterClick() {
            if (null != mPullCallBackListener) {
                startPullUpLoading();
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (isPullDownLoading && isPullUpLoading) {
            return super.onTouchEvent(ev);
        }

        if (mLastY == -1) {
            mLastY = ev.getRawY();
            isSlideUp = false;
            mDataSetSize = IRecyclerView.this.getCount() - getHeaderViewsCount() - getFooterViewsCount();
            mTotalItemCount = null != getAdapter() ? getAdapter().getItemCount() : 0;

            if(null != mTouchCoordinateChangedListener){
                mTouchCoordinateChangedListener.onTouchStart();
            }
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                mDataSetSize = IRecyclerView.this.getCount() - getHeaderViewsCount() - getFooterViewsCount();
                if(null != mTouchCoordinateChangedListener){
                    mTouchCoordinateChangedListener.onTouchStart();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();

                if(null != mTouchCoordinateChangedListener){
                    mTouchCoordinateChangedListener.onTouchDistanceChanged(mLastY , ev.getRawY() );
                }

                isSlideUp = (deltaY < 0);
                if (mDataSetSize > 0) {// 只有在大于0的情况下才可以下拉/上拉动作, 否则只允许点击空白区域进行加载
                    if (!isPullDownLoading && isPullDownEnabled() && (getFirstVisiblePosition() <= getHeaderViewsCount())) {
                        updateHeaderHeight(deltaY / OFFSET_RADIO);
                    } else if (!isPullUpLoading && isPullUpEnabled() && (deltaY < 0 || getFooterMargin() > 0)
                               && getLastVisiblePosition() == (mTotalItemCount - 1)) {
                        updateFooterHeight(-deltaY / OFFSET_RADIO);
                    }
                }

                break;

            default:
                if (mDataSetSize > 0) {// 只有在大于0的情况下才可以下拉/上拉动作, 否则只允许点击空白区域进行加载

                    if (!isPullDownLoading && isPullDownEnabled() && canPullDown() && getFirstVisiblePosition() == 0) {
                        startPullDownLoading(0);
                    } else if (!isPullUpLoading && ((isPullUpEnabled() && canPullUp()))
                               && getLastVisiblePosition() == (mTotalItemCount - 1)) {

                        startPullUpLoading();
                    }
                }

                resetFooterHeight(isPullUpLoading);
                resetHeaderHeight(isPullDownLoading);
                mLastY = -1;
                if(null != mTouchCoordinateChangedListener){
                    mTouchCoordinateChangedListener.onTouchEnd();
                }
                break;

        }

        return super.onTouchEvent(ev);
    }

    // -----------------------A: Touch 事件处理-------------------------
    private void updateHeaderHeight(float delta) {
        if (null != mHeaderView) {
            mHeaderView.onHeaderUpdateHeight((int) delta);
        }
    }

    private void resetHeaderHeight(boolean isPullDownLoadingNextMoment) {
        if (null != mHeaderView) {
            mHeaderView.onHeaderReset(isPullDownLoadingNextMoment);
        }
    }

    private boolean canPullDown() {
        return null != mHeaderView && mHeaderView.onHeaderCanPullDown();
    }

    private void updateFooterHeight(float delta) {
        if (null != mFooterView) {
            mFooterView.onFooterUpdateHeight((int) delta);
        }
    }

    private void resetFooterHeight(boolean isPullUpLoadingNextMoment) {
        if (null != mFooterView) {
            mFooterView.onFooterReset(isPullUpLoadingNextMoment);
        }
    }

    public void startFooterLoading() {
        if (null != mFooterView) {
            mFooterView.onFooterStart();
        }
    }

    public void stopFooterLoading() {
        if (null != mFooterView) {
            mFooterView.onFooterStop();
        }
    }

    private boolean canPullUp() {
        return null != mFooterView && mFooterView.onFooterCanPullDown();
    }

    private int getFooterMargin() {
        if (null != mFooterView) {
            return mFooterView.onFooterGetMargin();
        }
        return 0;
    }

    // -----------------------B: View 处理-------------------------
    // ------------------------ HeadView------------------------------
    private void addHeaderView() {
        if (null == mHeaderView && !TextUtils.isEmpty(pull_headerview_classname)) {
            try {

                // 1. create view
                Constructor<?> constructor = sConstructorMap.get(pull_headerview_classname);
                Class<?> clazz;

                if (null == constructor) {
                    clazz = getContext().getClassLoader().loadClass(pull_headerview_classname);

                    Class<?>[] mConstructorSignature = new Class[] { Context.class };
                    constructor = clazz.getConstructor(mConstructorSignature);
                    constructor.setAccessible(true);

                    sConstructorMap.put(pull_headerview_classname, constructor);
                }

                Object[] args = { getContext() };
                mHeaderView = (IHeaderCallBack) constructor.newInstance(args);

                if (!(mHeaderView instanceof View)) {
                    throw new Exception("HeaderView must both instanceof View and instanceof IHeaderCallBack! ");
                }

                // 2. init view
                mHeaderView.onHeaderInit(this, pull_trigger_distance_pulldown);



            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 3. addView
        addHeaderView((View) mHeaderView);
    }

    public boolean removeHeaderView() {
        boolean ret = false;
        if (null != mHeaderView) {
            ret = removeHeaderView((View) mHeaderView);
        }
        return ret;
    }

    // ------------------------ FootView------------------------------
    public void addFooterView() {
        if (null == mFooterView && !TextUtils.isEmpty(pull_footerview_classname)) {
            try {

                // 1. create view
                Constructor<?> constructor = sConstructorMap.get(pull_footerview_classname);
                Class<?> clazz;

                if (null == constructor) {
                    clazz = getContext().getClassLoader().loadClass(pull_footerview_classname);

                    Class<?>[] mConstructorSignature = new Class[] { Context.class };
                    constructor = clazz.getConstructor(mConstructorSignature);
                    constructor.setAccessible(true);

                    sConstructorMap.put(pull_footerview_classname, constructor);
                }

                Object[] args = { getContext() };
                mFooterView = (IFooterCallBack) constructor.newInstance(args);

                if (!(mFooterView instanceof View)) {
                    throw new Exception("FooterView must both instanceof View and instanceof IFooterCallBack! ");
                }

                // 2. init view
                mFooterView.onFooterInit(this, pull_trigger_distance_pullup);

                ((View) mFooterView).setOnClickListener(new OnClickListener() {

                    long lastClickTime = 0;// 防止点击太快

                    @Override
                    public void onClick(View v) {
                        long now = System.currentTimeMillis();
                        if (Math.abs(now - lastClickTime) < 650) {
                            return;
                        }
                        lastClickTime = now;

                        if(null != mFootViewClickListener){
                            mFootViewClickListener.onFooterClick();
                            return;
                        }

                        DEFAULT_FOOTER_VIEW_CLICK_LISTENER.onFooterClick();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 3. addView
        addFooterView((View) mFooterView);

        showFooterView();
    }

    public boolean removeFooterView() {
        boolean ret = false;
        if (null != mFooterView) {
            ret = removeFooterView((View) mFooterView);
        }
        return ret;
    }

    public void showFooterView() {
        if (null != mFooterView) {
            mFooterView.onFooterShow();
        }
    }

    public void hiddenFooterView() {
        if (null != mFooterView) {
            mFooterView.onFooterHidden();
        }
    }

    // ------------------------ EmptyView------------------------------
    private void addEmptyerView() {
        if (null == mEmptyerView && !TextUtils.isEmpty(pull_emptyview_classname)) {
            try {

                // 1. create view
                Constructor<?> constructor = sConstructorMap.get(pull_emptyview_classname);
                Class<?> clazz;

                if (null == constructor) {
                    clazz = getContext().getClassLoader().loadClass(pull_emptyview_classname);

                    Class<?>[] mConstructorSignature = new Class[] { Context.class };
                    constructor = clazz.getConstructor(mConstructorSignature);
                    constructor.setAccessible(true);

                    sConstructorMap.put(pull_emptyview_classname, constructor);
                }

                Object[] args = { getContext() };
                try {
                    mEmptyerView = (IEmptyerCallBack) constructor.newInstance(args);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!(mEmptyerView instanceof View)) {
                    throw new Exception("EmptyView must both instanceof View and instanceof IEmptyerCallBack! ");
                }

                // 2. init view
                mEmptyerView.onEmptyerInit(this);

                ((View) mEmptyerView).setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if(null != mEmptyerViewListener && mEmptyerViewListener.onEmpterViewClick()){
                            return;
                        }

                        if (null != mPullCallBackListener) {
                            if(isPullDownEnabled()){
                                startPullDownLoading();
                            }else if(isPullUpEnabled() || isAutoPullUpEnabled()){
                                startPullUpLoading();
                            }
                        }
                    }
                });

                // 3. addView
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showEmptyView() {
        if (null != mEmptyerView) {
            View view = ((View) mEmptyerView);
            if (!isEmptyViewAdded) {
                ViewGroup parent = (ViewGroup) getParent();
                if (null != parent) {
                    MarginLayoutParams lp = new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    lp.leftMargin = pull_empter_margin_left;
                    lp.rightMargin = pull_empter_margin_right;
                    lp.topMargin = pull_empter_margin_top;
                    lp.bottomMargin = pull_empter_margin_bottom;
                    view.setLayoutParams(lp);
                    parent.addView(view);
                    view.setVisibility(View.GONE);

                    isEmptyViewAdded = true;
                }
            }
            setEmptyView(view);
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if(null != mViewVisibilityChangedListener && changedView == this){
            mViewVisibilityChangedListener.onVisibilityChanged(visibility);
        }
    }

    private OnViewVisibilityChangedListener mViewVisibilityChangedListener;
    public void setViewVisibilityChangedListener(OnViewVisibilityChangedListener mViewVisibilityChangedListener){
        this.mViewVisibilityChangedListener = mViewVisibilityChangedListener;
    }

    public interface OnViewVisibilityChangedListener{
        void onVisibilityChanged(int visibility);
    }

    // -----------------------C: 设置各种能力-------------------------
    public void setPulldownEnabled(boolean enable) {
        if (enable) {
            pull_capacity |= ENABLED_PULLDOWN;
        } else {
            pull_capacity = (pull_capacity & ~ENABLED_PULLDOWN);
        }
    }

    private boolean isPullDownEnabled() {
        return (pull_capacity & ENABLED_PULLDOWN) == ENABLED_PULLDOWN;
    }

    private boolean isPullUpEnabled() {
        return (pull_capacity & ENABLED_PULLUP) == ENABLED_PULLUP;
    }

    public void setPullUpEnabled(boolean enable) {
        if (enable) {
            pull_capacity |= ENABLED_PULLUP;
        } else {
            pull_capacity = (pull_capacity & ~ENABLED_PULLUP);
        }
    }

    private boolean isAutoPullUpEnabled() {
        return (pull_capacity & ENABLED_AUTO_PULLUP) == ENABLED_AUTO_PULLUP;
    }

    public void setAutoPullUpEnabled(boolean enable) {
        if (enable) {
            pull_capacity |= ENABLED_AUTO_PULLUP;
        } else {
            pull_capacity = (pull_capacity & ~ENABLED_AUTO_PULLUP);
        }
    }

    // -----------------------D: 下拉/自动刷新-------------------------
    public void startPullDownLoading() {
        startPullDownLoading(250);
    }

    public void startPullDownLoading(long delayMillis) {
        this.removeCallbacks(mPullDownStartRunnable);
        if (delayMillis > 0) {
            this.postDelayed(mPullDownStartRunnable, delayMillis);
        } else {
            mPullDownStartRunnable.run();
        }
    }

    public void startPullUpLoading() {
        // 滚动到底部
        if (!isPullUpLoading && (isPullUpEnabled() || isAutoPullUpEnabled())) {

            isPullUpLoading = true;

            mDataSetSize = IRecyclerView.this.getCount() - getHeaderViewsCount() - getFooterViewsCount();

            if (mDataSetSize <= 0) {// 1.首次没有数据时

                showEmptyView();
                if (null != mEmptyerView) {
                    mEmptyerView.onEmptyerStart();
                }

            } else {// 2.有数据时

                startFooterLoading();
            }

            if (null != mPullCallBackListener) {
                latest_pullup_time = System.currentTimeMillis();
                mPullCallBackListener.onPullUp();
            }

            resetFooterHeight(isPullUpLoading);
        }
    }

    public void stopPullLoading(int pullType, Runnable callBackAction) {
        if (pullType == STATUS_PULL_DOWN || pullType == STATUS_PULL_UP) {
            StopRunnable action;
            long delayMillis;

            if (pullType == STATUS_PULL_DOWN) {

                long elapse_time = System.currentTimeMillis() - latest_pulldown_time;
                delayMillis = elapse_time < pulldown_smooth_transition_duration ? pulldown_smooth_transition_duration
                                                                                  - elapse_time
                                                                                : 1;

                mPullDownStopRunnable.pullType = pullType;
                mPullDownStopRunnable.task = callBackAction;

                action = mPullDownStopRunnable;

            } else {

                long elapse_time = System.currentTimeMillis() - latest_pullup_time;
                delayMillis = elapse_time < pullup_smooth_transition_duration ? pullup_smooth_transition_duration - elapse_time
                                                                              : 1;

                mPullUpStopRunnable.pullType = pullType;
                mPullUpStopRunnable.task = callBackAction;

                action = mPullUpStopRunnable;
            }

            postDelayed(action, delayMillis);
        }
    }

    private long stopPull(int pullType, int oldDataSetSize, int newListSize) {
        long ms = 0;
        if (pullType == STATUS_PULL_DOWN && isPullDownLoading) {

            if (oldDataSetSize <= 0) {
                // 1.重置空EmptyView
                if (null != mEmptyerView) {
                    mEmptyerView.onEmptyerStop(newListSize);
                }
            } else {
                // 2.重置HeadView
                if (null != mHeaderView) {
                    ms = mHeaderView.onHeaderStop();
                }
            }

            // 有动画的话，得动画执行完成后设置下拉为false , 防止处于动画过程中用户会执行下拉动作
            if (ms > 0) {
                isPullDownStopping = true;
                postDelayed(retsetHeaderRunnable, ms + 100);
            } else {
                retsetHeaderRunnable.run();
            }

            return ms;
        } else if (pullType == STATUS_PULL_UP && isPullUpLoading) {

            if (oldDataSetSize == 0) {
                // 1.重置空EmptyView
                if (null != mEmptyerView) {
                    mEmptyerView.onEmptyerStop(newListSize);
                }
            }

            // 重置FootView
            stopFooterLoading();

            if (ms > 0) {
                postDelayed(retsetFooterRunnable, ms + 100);
            } else {
                retsetFooterRunnable.run();
            }

        }

        return ms;
    }

    private Runnable retsetHeaderRunnable = new Runnable() {
        @Override
        public void run() {
//            Log.v("Testing","retsetHeaderRunnable");
            isPullDownStopping = false;
            isPullDownLoading = false;
            resetHeaderHeight(isPullDownLoading);
            refreshFooterAndEmptyer();
        }
    };

    private Runnable retsetFooterRunnable = new Runnable() {
        @Override
        public void run() {
//            Log.v("Testing","retsetFooterRunnable");
            isPullUpLoading = false;
            resetFooterHeight(isPullUpLoading);
            refreshFooterAndEmptyer();
        }
    };
    // ------------------------检查数据来决定是否显示空数据/上拉加载更多------------------------------
    public void refreshFooterAndEmptyer() {
        refreshFooterAndEmptyer(1);
    }

    private void refreshFooterAndEmptyer(long delayMillis) {
        removeCallbacks(mRefreshFooterAndEmptyerRunnable);
        postDelayed(mRefreshFooterAndEmptyerRunnable, delayMillis);
    }


    public boolean isPullDownLoading() {
        return isPullDownLoading;
    }

    public boolean isPullUpLoading() {
        return isPullUpLoading;
    }

    public void resetToDefault(){
        release();
        hiddenFooterView();
        isPullDownLoading = false;
        isPullUpLoading = false;
    }

    public void release() {

        if (null != mHeaderView) {
            mHeaderView.onHeaderRelease();
        }
        if (null != mFooterView) {
            mFooterView.onFooterRelease();
        }
        if (null != mEmptyerView) {
            mEmptyerView.onEmptyerRelease();
        }

        removeCallbacks(mPullDownStartRunnable);
        removeCallbacks(mPullDownStopRunnable);
        removeCallbacks(mPullUpStopRunnable);
        removeCallbacks(mRefreshFooterAndEmptyerRunnable);
        removeCallbacks(retsetHeaderRunnable);
        removeCallbacks(retsetFooterRunnable);
    }

    // -----------------------自定义通信接口--------------------
    @Override
    public void sendMessage(int dst, int cmd, Object... args) {
        switch (dst) {
            case DST_HEADER:
                if (null != mHeaderView) {
                    mHeaderView.onHandMessage(cmd, args);
                }
                break;

            case DST_EMPTY:
                if (null != mEmptyerView) {
                    mEmptyerView.onHandMessage(cmd, args);
                }
                break;

            case DST_FOOTER:
                if (null != mFooterView) {
                    mFooterView.onHandMessage(cmd, args);
                }
                break;

            case DST_ILISTVIEW:
                onHandMessage(cmd, args);
                break;
        }
    }

    @Override
    public Object onHandMessage(int cmd, Object... args) {
        return null;
    }

    // 修复monkey崩溃
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    private class StartRunnable implements Runnable {

        @Override
        public void run() {
//            Log.v("Testing"," StartRunnable run() isPullDownLoading " + isPullDownLoading + " isPullDownStopping "+isPullDownStopping);

            //当正在下拉过程中，并且未处于暂停过程中时，此时下拉无效；而当用户不处于下拉过程中，或者处于下拉过程中但是已经处于暂停中时则可以执行下拉操作
            if (isPullDownLoading && !isPullDownStopping) {
                return;
            }

            removeCallbacks(retsetHeaderRunnable);
            isPullDownStopping = false;

            isPullDownLoading = true;

            mDataSetSize = IRecyclerView.this.getCount() - getHeaderViewsCount() - getFooterViewsCount();

            if (mDataSetSize <= 0) {// 1.首次没有数据时

                showEmptyView();
                if (null != mEmptyerView) {
                    mEmptyerView.onEmptyerStart();
                }

            } else {// 2.有数据时

                if (null != mHeaderView) {
                    mHeaderView.onHeaderStart();
                }
            }

            if (null != mPullCallBackListener) {
                latest_pulldown_time = System.currentTimeMillis();
                mPullCallBackListener.onPullDown();
            }
        }
    }

    private class StopRunnable implements Runnable {

        int      pullType = 0;
        Runnable task;

        StopRunnable() {
        }

        @Override
        public void run() {

            mDataSetSize = IRecyclerView.this.getCount() - getHeaderViewsCount() - getFooterViewsCount();

            if (null != task) {
                task.run();
            }

            int newListSize = IRecyclerView.this.getCount() - IRecyclerView.this.getHeaderViewsCount()
                              - IRecyclerView.this.getFooterViewsCount();

            stopPull(pullType, mDataSetSize, newListSize);
        }
    }

    //--------------------------------------
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        ISavedState ss = new ISavedState(superState);
        ss.pull_capacity = pull_capacity;
        ss.isRemoveHeader= (null == mHeaderView);
        ss.isRemoveFooter= (null == mFooterView);
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof ISavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        ISavedState ss = (ISavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.pull_capacity = ss.pull_capacity;
        if(ss.isRemoveHeader){
            removeHeaderView();
        }
        if(ss.isRemoveFooter){
            removeFooterView();
        }
    }

    public static final class ISavedState extends AbsSavedState {


        private int pull_capacity = -1; // 最近一次的拉取方式
        private boolean isRemoveHeader; // 是否删除了Header
        private boolean isRemoveFooter; // 是否删除了Footer

        ISavedState(Parcelable superState) {
            super(superState);
        }

        protected ISavedState(Parcel in, ClassLoader loader) {
            super(in,loader);
            this.pull_capacity = in.readInt();
            this.isRemoveHeader = in.readByte() != 0;
            this.isRemoveFooter = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.pull_capacity);
            dest.writeByte(this.isRemoveHeader ? (byte) 1 : (byte) 0);
            dest.writeByte(this.isRemoveFooter ? (byte) 1 : (byte) 0);
        }

        public static final Creator<ISavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks() {
            public ISavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new ISavedState(in, loader);
            }

            public ISavedState[] newArray(int size) {
                return new ISavedState[size];
            }
        });

    }

    //--------------------------------------
    public ITouchCoordinateChanged mTouchCoordinateChangedListener;

    public void setTouchMoveDistanceChangedListener(ITouchCoordinateChanged mTouchCoordinateChangedListener){
        this.mTouchCoordinateChangedListener = mTouchCoordinateChangedListener;
    }

    public interface ITouchCoordinateChanged {
        void onTouchStart();
        void onTouchDistanceChanged(float lastY, float curY);
        void onTouchEnd();
    }

}
