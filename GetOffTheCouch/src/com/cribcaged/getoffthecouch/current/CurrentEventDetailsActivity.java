package com.cribcaged.getoffthecouch.current;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.invitations.InvitationResponseActivity;
import com.cribcaged.getoffthecouch.misc.IndependentMapView;
import com.cribcaged.getoffthecouch.util.DistanceCalculator;
import com.cribcaged.getoffthecouch.util.MyLocationManager;
import com.cribcaged.getoffthecouch.util.MyLocationManager.LocationResult;
import com.cribcaged.getoffthecouch.util.NetworkManager;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DiscretePathEffect;
import android.graphics.Point;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that displays the details of a selected current event.
 * @author Yunus Evren
 */
public class CurrentEventDetailsActivity extends MapActivity {

	private String facebookId;
	private int eventId;
	private String categoryName;
	private String locationName;
	private String founderName;
	private String description;
	private String dateAndTime;
	private int totalScore;

	private float latitude;
	private float longitude;

	private LinearLayout participantsLayout;

	private RelativeLayout participantLayout0;
	private RelativeLayout participantLayout1;
	private RelativeLayout participantLayout2;
	private RelativeLayout participantLayout3;

	private TextView participantText0;
	private TextView participantText1;
	private TextView participantText2;
	private TextView participantText3;

	private ImageView participantImage0;
	private ImageView participantImage1;
	private ImageView participantImage2;
	private ImageView participantImage3;

	private ProgressBar mapProgressBar;
	private ProgressBar participantsProgressBar;

	private TextView categoryText;
	private TextView locationText;
	private TextView descriptionText;
	private TextView dateTimeText;
	private TextView scoreText;

	private IndependentMapView mapView;
	private MapController mc;
	private GeoPoint point;

	private ArrayList<String> participantNames;
	private ArrayList<Boolean> participationStatus;

	private DistanceCalculator distanceCalculator;
	private LocationResult locationResult;

	private Button checkinButton;

	public ProgressDialog checkinDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_current_event_details);

		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		}

		Bundle bundle = getIntent().getExtras();
		facebookId = bundle.getString("facebook_id");
		eventId = bundle.getInt("event_id");
		categoryName = bundle.getString("category_name");
		locationName = bundle.getString("location_name");
		founderName = bundle.getString("founder_name");
		description = bundle.getString("description");
		dateAndTime = bundle.getString("date_time");
		totalScore = bundle.getInt("total_score");

		participantsLayout = (LinearLayout) this.findViewById(R.id.currenteventdetails_participantsinnerlayout);

		participantLayout0 = (RelativeLayout) this.findViewById(R.id.currenteventdetails_participantlayout0);
		participantLayout1 = (RelativeLayout) this.findViewById(R.id.currenteventdetails_participantlayout1);
		participantLayout2 = (RelativeLayout) this.findViewById(R.id.currenteventdetails_participantlayout2);
		participantLayout3 = (RelativeLayout) this.findViewById(R.id.currenteventdetails_participantlayout3);

		participantText0 = (TextView) this.findViewById(R.id.currenteventdetails_participanttext0);
		participantText1 = (TextView) this.findViewById(R.id.currenteventdetails_participanttext1);
		participantText2 = (TextView) this.findViewById(R.id.currenteventdetails_participanttext2);
		participantText3 = (TextView) this.findViewById(R.id.currenteventdetails_participanttext3);

		participantImage0 = (ImageView) this.findViewById(R.id.currenteventdetails_participantimage0);
		participantImage1 = (ImageView) this.findViewById(R.id.currenteventdetails_participantimage1);
		participantImage2 = (ImageView) this.findViewById(R.id.currenteventdetails_participantimage2);
		participantImage3 = (ImageView) this.findViewById(R.id.currenteventdetails_participantimage3);

		mapProgressBar = (ProgressBar) this.findViewById(R.id.currenteventdetails_mapviewprogressbar);
		participantsProgressBar = (ProgressBar) this.findViewById(R.id.currenteventdetails_participantsprogressbar);

		categoryText = (TextView) this.findViewById(R.id.currenteventdetails_categorytext);
		locationText = (TextView) this.findViewById(R.id.currenteventdetails_locationtext);
		descriptionText = (TextView) this.findViewById(R.id.currenteventdetails_detailstext);
		dateTimeText = (TextView) this.findViewById(R.id.currenteventdetails_datetimetext);
		scoreText = (TextView) this.findViewById(R.id.currenteventdetails_scoretext);

		categoryText.setText(categoryName);
		locationText.setText(locationName);
		descriptionText.setText(description);
		dateTimeText.setText(dateAndTime);
		scoreText.setText(totalScore + " points");

		if(description.length()==0){
			final LinearLayout descriptionLayout = (LinearLayout) this.findViewById(R.id.currenteventdetails_detailslayout);
			descriptionLayout.setVisibility(View.GONE);
		}

		participantNames = new ArrayList<String>();
		participationStatus = new ArrayList<Boolean>();

		distanceCalculator = new DistanceCalculator();

		checkinButton = (Button) this.findViewById(R.id.currenteventdetails_checkinbutton);
		checkinButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				checkinDialog = ProgressDialog.show(CurrentEventDetailsActivity.this, "", "Checking in.\nPlease wait...");
				locationResult = new LocationResult(){
					@Override
					public void gotLocation(Location location){
						// Got the location
						float userLatitude = (float) location.getLatitude();
						float userLongitude = (float) location.getLongitude();
						Log.w(MainActivity.LOG, "User location: " + userLatitude + ", " + userLongitude);
						Log.w(MainActivity.LOG, "Event location: " + latitude + ", " + longitude);
						GeoPoint p1 = new GeoPoint((int) (userLatitude * 1E6), (int) (userLongitude * 1E6));
						GeoPoint p2 = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
						double distance = distanceCalculator.calculateDistance(p1, p2);
						Log.i(MainActivity.LOG, "distance: " + distance);
						if(distance<=0.5){
							CheckinUserTask checkinUserTask = new CheckinUserTask();
							checkinUserTask.execute();
						}
						else{
							checkinDialog.dismiss();
							Toast.makeText(CurrentEventDetailsActivity.this, "You don't seem to be close\nto the event location.\nPlease try again.", Toast.LENGTH_LONG).show();
						}
					}
				};
				MyLocationManager myLocation = new MyLocationManager();
				myLocation.getLocation(CurrentEventDetailsActivity.this, locationResult);
			}
		});



		GetCheckinStatusTask getCheckinStatusTask = new GetCheckinStatusTask();
		getCheckinStatusTask.execute();

		GetLocationCoordinatesTask getLocationCoordinatesTask = new GetLocationCoordinatesTask();
		getLocationCoordinatesTask.execute();
	}

	/**
	 * Sets the name and participation status of each invited event
	 */
	public void setParticipants(){
		for(int i = 0; i<participantNames.size(); i++){
			if(i==0){
				participantText0.setText(participantNames.get(0));
				if(participationStatus.get(0)){
					participantImage0.setImageResource(R.drawable.check);
				}
				else{
					participantImage0.setImageResource(R.drawable.question_mark);
				}

			}
			if(i==1){
				participantText1.setText(participantNames.get(1));
				if(participationStatus.get(1)){
					participantImage1.setImageResource(R.drawable.check);
				}
				else{
					participantImage1.setImageResource(R.drawable.question_mark);
				}
			}
			if(i==2){
				participantText2.setText(participantNames.get(2));
				if(participationStatus.get(2)){
					participantImage2.setImageResource(R.drawable.check);
				}
				else{
					participantImage2.setImageResource(R.drawable.question_mark);
				}
			}
			if(i==3){
				participantText3.setText(participantNames.get(3));
				if(participationStatus.get(3)){
					participantImage3.setImageResource(R.drawable.check);
				}
				else{
					participantImage3.setImageResource(R.drawable.question_mark);
				}
			}

			if(participantNames.size()==1){
				participantLayout1.setVisibility(View.GONE);
				participantLayout2.setVisibility(View.GONE);
				participantLayout3.setVisibility(View.GONE);
			}
			if(participantNames.size()==2){
				participantLayout2.setVisibility(View.GONE);
				participantLayout3.setVisibility(View.GONE);
			}
			if(participantNames.size()==3){
				participantLayout3.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_current_event_details, menu);
		return true;
	}

	@Override
	protected boolean isRouteDisplayed() {
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
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pin_small);

			int height = bmp.getHeight();
			int width = bmp.getWidth()/2;

			canvas.drawBitmap(bmp, screenPts.x-width, screenPts.y-height, null);
			return true;
		}
	}

	/**
	 * Asynchronous task that gets the check-in status of the event participants.
	 * @author Yunus Evren
	 */
	class GetCheckinStatusTask extends AsyncTask<Void, Void, Boolean>{


		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "GetCheckinStatusTask executed");
			participantsProgressBar.setVisibility(View.VISIBLE);
			participantsLayout.setVisibility(View.INVISIBLE);
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(CurrentEventDetailsActivity.this)){
				Log.i(MainActivity.LOG, "GetCheckinStatusTask: Network unavailable");
				return false;
			}
			else{
				try{
					participantNames = new ArrayList<String>();
					participationStatus = new ArrayList<Boolean>();
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.GetCheckinStatus?"
						+"event_id=" + eventId;
					URL url = new URL(link);
					URLConnection urlConn = url.openConnection();

					BufferedReader in = 
						new BufferedReader( new InputStreamReader( urlConn.getInputStream() ) );

					int count = 0;

					ArrayList<String> responseList = new ArrayList<String>();
					String response;
					while((response=in.readLine())!=null){
						responseList.add(response);
					}

					count = responseList.size();
					if(count==0){
						Log.w(MainActivity.LOG, "GetCheckinStatusTask query returned no result.");
						return false;
					}

					for(int i = 0; i<responseList.size(); i++){
						String line = responseList.get(i);
						String[] tokens = line.split("\\|");
						participantNames.add(tokens[0]);
						int confirmed = Integer.parseInt(tokens[1]);
						if(confirmed==0){
							participationStatus.add(false);
						}
						else{
							participationStatus.add(true);
						}
					}
					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetCheckinStatusTask Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetCheckinStatusTask MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetCheckinStatusTask IOException");
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Log.w(MainActivity.LOG, "GetCheckinStatusTask successfully finished.");
				setParticipants();
			}
			else{
				Log.w(MainActivity.LOG, "GetCheckinStatusTask failed to finish.");
			}
			participantsProgressBar.setVisibility(View.GONE);
			participantsLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Asynchronous task that gets the coordinates of the event location.
	 * @author Yunus Evren
	 */
	class GetLocationCoordinatesTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "GetLocationCoordinatesTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(CurrentEventDetailsActivity.this)){
				Log.i(MainActivity.LOG, "GetLocationCoordinatesTask: Network unavailable");
				return false;
			}
			else{
				try{
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.GetLocationCoordinates?"
						+"event_id=" + eventId;
					URL url = new URL(link);
					URLConnection urlConn = url.openConnection();

					BufferedReader in = 
						new BufferedReader( new InputStreamReader( urlConn.getInputStream() ) );

					int count = 0;

					ArrayList<String> responseList = new ArrayList<String>();
					String response;
					while((response=in.readLine())!=null){
						responseList.add(response);
					}

					count = responseList.size();
					if(count==0){
						Log.w(MainActivity.LOG, "GetLocationCoordinatesTask query returned no result.");
						return false;
					}

					for(String line : responseList){
						String[] tokens = line.split("\\|");
						latitude = Float.parseFloat(tokens[0]);
						longitude = Float.parseFloat(tokens[1]);
					}
					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetLocationCoordinatesTask Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetLocationCoordinatesTask MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetLocationCoordinatesTask IOException");
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Log.w(MainActivity.LOG, "GetLocationCoordinatesTask successfully finished.");
				mapView = (IndependentMapView) CurrentEventDetailsActivity.this.findViewById(R.id.currenteventdetails_mapview);
				mapView.setBuiltInZoomControls(false);
				mapView.setSatellite(false);

				mc = mapView.getController();
				point = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
				mc.animateTo(point);
				mc.setZoom(15);

				MapOverlay mapOverlay = new MapOverlay();
				List<Overlay> listOfOverlays = mapView.getOverlays();
				listOfOverlays.clear();
				listOfOverlays.add(mapOverlay);
			}
			else{
				Log.w(MainActivity.LOG, "GetLocationCoordinatesTask failed to finish.");
			}

			mapProgressBar.setVisibility(View.GONE);
		}
	}

	/**
	 * Asynchronous task that performs the check-in.
	 * @author Yunus Evren
	 */
	class CheckinUserTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "CheckinUserTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(CurrentEventDetailsActivity.this)){
				Log.i(MainActivity.LOG, "CheckinUserTask: Network unavailable");
				return false;
			}
			else{
				try{
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.CheckinUser?"
						+"facebook_id=" + facebookId
						+"&event_id=" + eventId;
					URL url = new URL(link);
					URLConnection urlConn = url.openConnection();

					BufferedReader in = 
						new BufferedReader( new InputStreamReader( urlConn.getInputStream() ) );

					int count = 0;

					ArrayList<String> responseList = new ArrayList<String>();
					String response;
					while((response=in.readLine())!=null){
						responseList.add(response);
					}

					count = responseList.size();
					if(count==0){
						Log.w(MainActivity.LOG, "CheckinUserTask query returned no result.");
						return false;
					}

					for(String line : responseList){
						if(line.contains("ERROR")){
							Log.w(MainActivity.LOG, "CheckinUserTask received error response from server.");
							return false;
						}
					}
					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "CheckinUserTask Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "CheckinUserTask MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "CheckinUserTask IOException");
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			checkinDialog.dismiss();
			if(result){
				Log.w(MainActivity.LOG, "CheckinUserTask successfully finished.");
				GetCheckinStatusTask getCheckinStatusTask = new GetCheckinStatusTask();
				getCheckinStatusTask.execute();
			}
			else{
				Log.w(MainActivity.LOG, "CheckinUserTask failed to finish.");
				Toast.makeText(CurrentEventDetailsActivity.this, "Failed to send check-in.\nPlease try again.", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
