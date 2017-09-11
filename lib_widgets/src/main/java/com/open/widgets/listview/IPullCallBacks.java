package com.open.widgets.listview;

/**
 * Created by long on 2017/1/9.
 */

public class IPullCallBacks {

    // ----------------------------Header/Footer/Emptyer实现类，用于不同View之间进行事件交互----------------------------------
    // 分发命令

    public interface IMessagerDispatcher {

        int DST_HEADER    = 1;
        int DST_EMPTY     = 2;
        int DST_FOOTER    = 3;
        int DST_ILISTVIEW = 4;

        void sendMessage(int dst, int cmd, Object... args);
    }

    // 处理命令
    public interface IMessageHandler {

        int STOP_HEADER  = 1;
        int STOP_FOOTER  = 2;
        int STOP_EMPTYER = 3;

        Object onHandMessage(int cmd, Object... args);
    }

    public interface IHeaderCallBack extends IMessageHandler {

        void onHeaderInit(IMessagerDispatcher messagDispatcher, Object... args);

        void onHeaderStart();

        long onHeaderStop();

        void onHeaderRelease();

        //---------------------

        void onHeaderUpdateHeight(int delta);

        boolean onHeaderCanPullDown();

        void onHeaderReset(boolean isPullDownLoadingNextMoment);
    }

    public interface IFooterCallBack extends IMessageHandler {

        void onFooterInit(IMessagerDispatcher messagDispatcher, Object... args);

        void onFooterStart();

        long onFooterStop();

        void onFooterRelease();

        //---------------------

        void onFooterUpdateHeight(int delta);

        boolean onFooterCanPullDown();

        void onFooterReset(boolean isPullDownLoadingNextMoment);

        void onFooterShow();

        void onFooterHidden();

        int onFooterGetMargin();
    }

    public interface IEmptyerCallBack extends IMessageHandler {

        void onEmptyerInit(IMessagerDispatcher messagDispatcher, Object... args);

        void onEmptyerStart();

        void onEmptyerStop(int listSize);

        void onEmptyerRelease();
    }

    // ----------------------------下拉/上拉事件的监听器----------------------------------
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
