package com.cribcaged.getoffthecouch.misc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.maps.MapView;

/**
 * Customised MapView that is suitable to be placed in a ScrollView.
 */
public class IndependentMapView extends MapView {

	public IndependentMapView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}
	
	public IndependentMapView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
	}
	
	public IndependentMapView(Context arg0, String arg1){
		super(arg0, arg1);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
	    int action = ev.getAction();
	    switch (action) {
	    case MotionEvent.ACTION_DOWN:
	        // Disallow ScrollView to intercept touch events.
	        this.getParent().requestDisallowInterceptTouchEvent(true);
	        break;

	    case MotionEvent.ACTION_UP:
	        // Allow ScrollView to intercept touch events.
	        this.getParent().requestDisallowInterceptTouchEvent(false);
	        break;
	    }

	    // Handle MapView's touch events.
	    super.onTouchEvent(ev);
	    return true;
	}
 
}
