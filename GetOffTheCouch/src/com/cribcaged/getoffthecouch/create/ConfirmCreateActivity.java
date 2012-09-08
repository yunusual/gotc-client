package com.cribcaged.getoffthecouch.create;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.misc.IndependentMapView;
import com.cribcaged.getoffthecouch.util.NetworkManager;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that displays event details and requests the user to confirm.
 * Then, the event gets created.
 * @author Yunus Evren
 */

public class ConfirmCreateActivity extends MapActivity {

	private final String EDIT_TEXT = "(e.g.: Dredg concert, Chelsea - Liverpool match)";

	private String userId;
	private String fullName;

	private int catId;
	private String catName;

	private int locId;
	private float latitude;
	private float longitude;
	private String locName;
	private String locAddress;
	private int locScore;

	private int totalScore;

	private ArrayList<String> friendIds;
	private ArrayList<String> friendNames;

	private String date;
	private String time;

	private String displayDate;
	private String displayTime;

	private String description;

	private ImageView leftArrow;
	private ImageView rightArrow;

	private TextView categoryText;
	private TextView locationText;
	private TextView addressText;
	private TextView dateText;
	private TextView timeText;
	private TextView friendsText;
	private TextView scoreText;

	private EditText descriptionText;

	private CheckBox confirmCheckBox;

	private IndependentMapView mapView;
	private MapController mc;
	private GeoPoint point;

	private int progressCount;
	private ProgressDialog invitationProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_confirm_create);

		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		}

		Bundle bundle = getIntent().getExtras();
		userId = bundle.getString("user_id");
		fullName = bundle.getString("full_name");

		catId = bundle.getInt("cat_id");
		catName = bundle.getString("cat_name");

		locId = bundle.getInt("loc_id");
		latitude = bundle.getFloat("latitude");
		longitude = bundle.getFloat("longitude");
		locName = bundle.getString("loc_name");
		locAddress = bundle.getString("loc_address");
		locScore = bundle.getInt("loc_score");

		friendIds = bundle.getStringArrayList("friend_ids");
		friendNames = bundle.getStringArrayList("friend_names");

		date = bundle.getString("date");
		time = bundle.getString("time");

		adjustTimeAndDate();

		description = "";

		categoryText = (TextView) this.findViewById(R.id.confirmcreate_categorytext);
		locationText = (TextView) this.findViewById(R.id.confirmcreate_locationtext);
		addressText = (TextView) this.findViewById(R.id.confirmcreate_addresstext);
		dateText = (TextView) this.findViewById(R.id.confirmcreate_datetext);
		timeText = (TextView) this.findViewById(R.id.confirmcreate_timetext);
		friendsText = (TextView) this.findViewById(R.id.confirmcreate_friendstext);
		scoreText = (TextView) this.findViewById(R.id.confirmcreate_scoretext);

		categoryText.setText(catName);
		locationText.setText(locName);
		addressText.setText(locAddress);
		dateText.setText(displayDate);
		timeText.setText(displayTime);

		String friends = "";
		for(int i = 0; i<friendNames.size(); i++){
			if(i==friendNames.size()-1){
				friends += friendNames.get(i);
			}
			else{
				friends += friendNames.get(i) + "\n";
			}
		}

		friendsText.setText(friends);

		totalScore = locScore + (locScore * friendIds.size());
		scoreText.setText(totalScore+"");

		descriptionText = (EditText) this.findViewById(R.id.confirmcreate_descriptiontext);
		descriptionText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					descriptionText.setText("");
				}
				else{
					if(descriptionText.getText().toString().length()==0){
						descriptionText.setText(EDIT_TEXT);
					}
				}
			}
		});

		descriptionText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void afterTextChanged(Editable s) {
				for(int i = s.length(); i > 0; i--){
					if(s.subSequence(i-1, i).toString().equals("\n"))
						s.replace(i-1, i, " ");
				}
			}
		});

		leftArrow = (ImageView) this.findViewById(R.id.confirmcreate_leftarrowimage);
		leftArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ConfirmCreateActivity.this.finish();
			}
		});

		rightArrow = (ImageView) this.findViewById(R.id.confirmcreate_rightarrowimage);
		rightArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(descriptionText.getText().toString().compareTo(EDIT_TEXT)!=0){
					description = descriptionText.getText().toString();
				}
				CreateEventTask createEventTask = new CreateEventTask();
				createEventTask.execute();
			}
		});

		rightArrow.setVisibility(View.INVISIBLE);

		confirmCheckBox = (CheckBox) this.findViewById(R.id.confirmcreate_confirmcheckbox);
		final float scale = this.getResources().getDisplayMetrics().density;
		confirmCheckBox.setPadding(confirmCheckBox.getPaddingLeft() + (int)(5.0f * scale + 0.5f),
				confirmCheckBox.getPaddingTop(),
				confirmCheckBox.getPaddingRight(),
				confirmCheckBox.getPaddingBottom());

		confirmCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					rightArrow.setVisibility(View.VISIBLE);
				}
				else{
					rightArrow.setVisibility(View.INVISIBLE);
				}
			}
		});

		mapView = (IndependentMapView) ConfirmCreateActivity.this.findViewById(R.id.confirmcreate_mapview);
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

	/**
	 * Method that sets the date and time in a format that can be displayed to the user
	 */
	private void adjustTimeAndDate() {
		String[] dates = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

		String[] dateTokens = date.split("-");
		displayDate = dates[Integer.parseInt(dateTokens[1])-1] + " " + dateTokens[2] + ", " + dateTokens[0];

		String[] timeTokens = time.split(":");
		int hour = Integer.parseInt(timeTokens[0]);
		int minute = Integer.parseInt(timeTokens[1]);

		if(hour<10){
			displayTime = "0" + hour + ":";
		}
		else{
			displayTime = "" + hour + ":";
		}

		if(minute<10){
			displayTime += "0" + minute;
		}
		else{
			displayTime += minute;
		}
	}

	/**
	 * Sends notification to each invited user
	 * @param eventId - id of the created event
	 */
	private void sendNotifications(int eventId) {
		String fromId = userId;
		String fromName = fullName;
		progressCount = friendIds.size();
		invitationProgressDialog = ProgressDialog.show(ConfirmCreateActivity.this, "", "Sending invitations...");
		for(int i = 0; i<friendIds.size(); i++){
			String toId = friendIds.get(i);
			String[] params = new String[4];
			params[0] = toId;
			params[1] = fromId;
			params[2] = fromName;
			params[3] = eventId + "";

			SendInvitationTask sendInvitationTask = new SendInvitationTask();
			sendInvitationTask.execute(params);
		}
	}
	
	/**
	 * Finishes the activity with a successful result
	 */
	private void createEventCompleted(){
		invitationProgressDialog.dismiss();
		ConfirmCreateActivity.this.setResult(999);
		ConfirmCreateActivity.this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_confirm_create, menu);
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
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pin_small);

			int height = bmp.getHeight();
			int width = bmp.getWidth()/2;

			canvas.drawBitmap(bmp, screenPts.x-width, screenPts.y-height, null);
			return true;
		}
	}

	/**
	 * Asynchronous task that creates the event.
	 * @author Yunus Evren
	 */
	class CreateEventTask extends AsyncTask<Void, Void, Boolean>{

		int eventId = -1;
		private ProgressDialog createEventProgressDialog;

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "CreateEventTask executed");
			super.onPreExecute();
			createEventProgressDialog = ProgressDialog.show(ConfirmCreateActivity.this, "", "Creating event...");
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(ConfirmCreateActivity.this)){
				Log.i(MainActivity.LOG, "CreateEventTask: Network unavailable");
				return false;
			}
			else{
				try{
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.CreateEvent?" +
					"loc_id=" + locId +
					"&user_id=" + userId + 
					"&date=" + URLEncoder.encode(date, "UTF-8") + 
					"&time=" + URLEncoder.encode(time, "UTF-8") +
					"&total_score=" + totalScore +
					"&details=" + URLEncoder.encode(description, "UTF-8");
					URL url = new URL(link);
					URLConnection urlConn = url.openConnection();

					BufferedReader in = 
						new BufferedReader( new InputStreamReader( urlConn.getInputStream() ) );


					ArrayList<String> responseList = new ArrayList<String>();
					String response;
					while((response=in.readLine())!=null){
						responseList.add(response);
					}

					for(String s : responseList){
						if(s.contains("CreateEvent:")){
							if(s.contains("ERROR")){
								Log.i(MainActivity.LOG, "CreateEventTask Failed to create new task - Error");
								return false;
							}
						}
					}

					eventId = Integer.parseInt(responseList.get(responseList.size()-1));

					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "CreateEventTask Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "CreateEventTask MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "CreateEventTask IOException");
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				if(eventId!=-1){
					Log.i(MainActivity.LOG, "CreateEventTask Event successfully created, eventId: " + eventId);
					sendNotifications(eventId);
				}
				else{
					Log.i(MainActivity.LOG, "CreateEventTask Failed to create event, no eventId set");
				}
			}
			else{

			}
			createEventProgressDialog.dismiss();
		}
	}

	/**
	 * Asynchronous task that sends invitation notifications.
	 * @author Yunus Evren
	 */
	class SendInvitationTask extends AsyncTask<String, Void, Boolean>{
		String toId = "";
		String fromId = "";
		String fromName = "";
		String eventId = "";

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "SendInvitationTask for " + toId + " executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			toId = params[0];
			fromId = params[1];
			fromName = params[2];
			eventId = params[3];
			if(!NetworkManager.isNetworkAvailable(ConfirmCreateActivity.this)){
				Log.i(MainActivity.LOG, "SendInvitationTask: Network unavailable");
				return false;
			}
			else{
				try{
					Thread.sleep(1000);
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.SendInvitation?" +
					"to_id=" + toId +
					"&from_id=" + fromId + 
					"&from_name=" + URLEncoder.encode(fromName, "UTF-8") + 
					"&event_id=" + eventId;

					URL url = new URL(link);
					URLConnection urlConn = url.openConnection();

					BufferedReader in = 
						new BufferedReader( new InputStreamReader( urlConn.getInputStream() ) );


					ArrayList<String> responseList = new ArrayList<String>();
					String response;
					while((response=in.readLine())!=null){
						responseList.add(response);
					}

					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "SendInvitationTask for " + toId + " Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "SendInvitationTask for " + toId + " MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "SendInvitationTask for " + toId + " IOException");
					return false;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Log.i(MainActivity.LOG, "SendInvitationTask for " + toId + " Successfully completed");
			}
			else{
				Log.i(MainActivity.LOG, "SendInvitationTask for " + toId + " Failed to complete");
			}
			progressCount--;
			if(progressCount == 0){
				createEventCompleted();
			}
		}
	}
}
