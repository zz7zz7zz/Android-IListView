package com.open.widgets.listview;

/**
 * Created by long on 2017/1/9.
 */

public class IPullCallBacks {

    //----------------------------Header/Footer/Emptyer实现类，用于不同View之间进行事件交互----------------------------------
    public interface IMessageHandler
    {
        Object onHandMessage(int cmd, Object... args);
    }


    public interface IHeaderCallBack extends IMessageHandler {
        void 	onHeaderInit(Object... args);
        void 	onHeaderUpdateHeight(int delta);
        boolean onHeaderCanPullDown();
        void    onHeaderLoading();
        void 	onHeaderReset(boolean isPullDownLoadingNextMoment);
        long 	onHeaderStop();
        void 	onHeaderRelease();
    }


    public interface IFooterCallBack extends IMessageHandler {
        void 	onFooterInit(Object... args);
        void 	onFooterUpdateHeight(int delta);
        boolean onFooterCanPullDown();
        void    onFooterLoading();
        void 	onFooterReset(boolean isPullDownLoadingNextMoment);
        long 	onFooterStop();
        void 	onFooterShow();
        void 	onFooterHidden();
        int 	onFooterGetMargin();
        void 	onFooterRelease();
    }

    public interface IEmptyerCallBack extends IMessageHandler {
        void 	onEmptyerInit(Object... args);
        void 	onEmptyerStart();
        void 	onEmptyerStop(int listSize);
        void 	onEmptyerRelease();
    }


    //----------------------------下拉/上拉事件的监听器----------------------------------
    public interface IPullCallBackListener {

        /**
         * 触发下拉事件
         */
        void onPullDown();

        /**
         * 触发上拉事件
         */
        void onPullUp();

    }
}
