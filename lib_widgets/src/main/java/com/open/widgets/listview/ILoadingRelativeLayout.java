package com.open.widgets.listview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.open.lib_widgets.R;


/**
 * RelativeLayout with lib_loading animation
 * Created by long on 2016/12/20.
 */
public class ILoadingRelativeLayout extends RelativeLayout
{
    private ImageView           loading_image;
    private AnimationDrawable   loading_image_animation_drawable;

    public ILoadingRelativeLayout(Context context) {
        super(context);
    }

    public ILoadingRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ILoadingRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init( Context mContext)
    {
        loading_image = new ImageView(mContext);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(loading_image, lp );

        loading_image.setBackgroundResource(R.drawable.lib_loading);
        loading_image_animation_drawable = (AnimationDrawable) loading_image.getBackground();

        loading_image.setVisibility(View.GONE);
    }


    public void startLoadingAnimation()
    {
        loading_image_animation_drawable.start();
        loading_image.setVisibility(View.VISIBLE);
    }

    public void stopLoadingAnimation()
    {
        loading_image_animation_drawable.stop();
        loading_image.setVisibility(View.GONE);
    }
}
