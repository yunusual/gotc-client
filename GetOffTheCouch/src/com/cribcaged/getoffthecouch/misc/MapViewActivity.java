package com.cribcaged.getoffthecouch.misc;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.R.drawable;
import com.cribcaged.getoffthecouch.R.id;
import com.cribcaged.getoffthecouch.R.layout;
import com.cribcaged.getoffthecouch.R.menu;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.widget.TextView;

/**
 * Activity that displays an event location and address on the map.
 * @author Yunus Evren
 */
public class MapViewActivity extends MapActivity {
	
	private TextView locationText;
	private TextView addressText;
	
	private String name;
	private String address;
	private float latitude;
	private float longitude;
	
	private MapView mapView;
	private MapController mc;
	private GeoPoint point;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_map_view);

		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		}
		
		Bundle bundle = MapViewActivity.this.getIntent().getExtras();
		name = bundle.getString("name");
		latitude = bundle.getFloat("latitude");
		longitude = bundle.getFloat("longitude");
		
		locationText = (TextView) this.findViewById(R.id.mapview_locationtext);
		addressText = (TextView) this.findViewById(R.id.mapview_addresstext);
		
		mapView = (MapView) this.findViewById(R.id.mapview_mapview);
		mapView.setBuiltInZoomControls(false);
        mapView.setSatellite(false);
        
        mc = mapView.getController();
        
        point = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
        mc.animateTo(point);
        mc.setZoom(15);
        
        Geocoder geoLocation = new Geocoder(MapViewActivity.this, Locale.getDefault());
		List<Address> addressList = null;
		try {
			addressList = geoLocation.getFromLocation(latitude, longitude, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		address = "";
		
		if(addressList!=null && addressList.size()!=0){
			Address add = addressList.get(0);
			for (int i = 0; i<add.getMaxAddressLineIndex(); i++){
				if(i!=add.getMaxAddressLineIndex()-1){
					address = address + add.getAddressLine(i) +", ";
				}
				else{
					address = address + add.getAddressLine(i);
				}
			}
		}
		
		locationText.setText(name);
		addressText.setText(address);
		
		MapOverlay mapOverlay = new MapOverlay();
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);        
 
        mapView.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_map_view, menu);
        return true;
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Class that displays an icon on the map.
	 * @author Yunus Evren
	 */
	class MapOverlay extends com.google.android.maps.Overlay
    {
        @Override
        public boolean draw(Canvas canvas, MapView mapView, 
        boolean shadow, long when) 
        {
            super.draw(canvas, mapView, shadow);                   
 
            //---translate the GeoPoint to screen pixels---
            Point screenPts = new Point();
            mapView.getProjection().toPixels(point, screenPts);
 
            //---add the marker---
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pin);
            
            int height = bmp.getHeight();
            int width = bmp.getWidth()/2;
            
            canvas.drawBitmap(bmp, screenPts.x-width, screenPts.y-height, null);
            return true;
        }
    }
}
