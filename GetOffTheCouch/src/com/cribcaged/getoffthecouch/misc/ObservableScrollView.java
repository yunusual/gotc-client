package com.cribcaged.getoffthecouch.misc;

import com.cribcaged.getoffthecouch.MainActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

/**
 * Customised ScrollView that is able to monitor the current scroll position.
 */
public class ObservableScrollView extends ScrollView {

    private ScrollViewListener scrollViewListener = null;

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if(scrollViewListener != null) {
        	View view = (View) getChildAt(getChildCount()-1);
            int diff = (view.getBottom()-(getHeight()+getScrollY()));
            if( diff <= 0 ){
                scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
            }
        }
    }

}