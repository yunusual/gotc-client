package com.cribcaged.getoffthecouch.history;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.groups.Group;
import com.aetrion.flickr.groups.GroupsInterface;
import com.aetrion.flickr.groups.pools.PoolsInterface;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.uploader.UploadMetaData;
import com.aetrion.flickr.uploader.Uploader;
import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.current.CurrentEventDetailsActivity;
import com.cribcaged.getoffthecouch.misc.IndependentMapView;
import com.cribcaged.getoffthecouch.util.FlickrConnector;
import com.cribcaged.getoffthecouch.util.NetworkManager;
import com.cribcaged.getoffthecouch.welcome.WelcomeActivity;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that displays the details of a completed event.
 * @author Yunus Evren
 */
public class EventHistoryDetailsActivity extends MapActivity {

	private final int PICK_IMAGE_CODE = 120;

	private String facebookId;
	private String userName;
	
	private int eventId;
	private String categoryName;
	private int categoryId;
	private String locationName;
	private String founderName;
	private String description;
	private String dateAndTime;
	private int totalScore;

	private float latitude;
	private float longitude;

	private LinearLayout middleLayout;

	private ProgressBar mapProgressBar;
	private ProgressBar detailsProgressBar;

	private TextView categoryText;
	private TextView locationText;
	private TextView descriptionText;
	private TextView dateTimeText;
	private TextView friendsText;
	private TextView scoreText;

	private IndependentMapView mapView;
	private MapController mc;
	private GeoPoint point;

	private Button uploadButton;
	private Button viewButton;
	private ProgressDialog uploadDialog;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_event_history_details);

		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		}

		Bundle bundle = getIntent().getExtras();
		facebookId = bundle.getString("facebook_id");
		userName = bundle.getString("user_name");
		
		eventId = bundle.getInt("event_id");
		categoryName = bundle.getString("category_name");
		categoryId = bundle.getInt("category_id");
		locationName = bundle.getString("location_name");
		founderName = bundle.getString("founder_name");
		description = bundle.getString("description");
		dateAndTime = bundle.getString("date_time");
		totalScore = bundle.getInt("total_score");

		middleLayout = (LinearLayout) this.findViewById(R.id.eventhistorydetails_middledetailsinnerlayout);

		mapProgressBar = (ProgressBar) this.findViewById(R.id.eventhistorydetails_mapviewprogressbar);
		detailsProgressBar = (ProgressBar) this.findViewById(R.id.eventhistorydetails_detailsprogressbar);

		categoryText = (TextView) this.findViewById(R.id.eventhistorydetails_categorytext);
		locationText = (TextView) this.findViewById(R.id.eventhistorydetails_locationtext);
		descriptionText = (TextView) this.findViewById(R.id.eventhistorydetails_detailstext);
		dateTimeText = (TextView) this.findViewById(R.id.eventhistorydetails_datetimetext);
		friendsText = (TextView) this.findViewById(R.id.eventhistorydetails_friendstext);
		scoreText = (TextView) this.findViewById(R.id.eventhistorydetails_scoretext);

		categoryText.setText(categoryName);
		locationText.setText(locationName);
		descriptionText.setText(description);
		dateTimeText.setText(dateAndTime);
		scoreText.setText(totalScore + " points");

		if(description.length()==0){
			final LinearLayout descriptionLayout = (LinearLayout) this.findViewById(R.id.eventhistorydetails_detailslayout);
			descriptionLayout.setVisibility(View.GONE);
		}

		middleLayout.setVisibility(View.INVISIBLE);

		uploadButton = (Button) this.findViewById(R.id.eventhistorydetails_uploadbutton);
		uploadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, PICK_IMAGE_CODE); 
			}
		});
		
		viewButton = (Button) this.findViewById(R.id.eventhistorydetails_viewbutton);
		viewButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EventHistoryDetailsActivity.this, ViewPhotosActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("event_id", eventId);
				intent.putExtras(bundle);
				EventHistoryDetailsActivity.this.startActivity(intent);
			}
		});

		GetEventParticipantsTask getEventParticipantsTask = new GetEventParticipantsTask();
		getEventParticipantsTask.execute();

		GetLocationCoordinatesTask getLocationCoordinatesTask = new GetLocationCoordinatesTask();
		getLocationCoordinatesTask.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_event_history_details, menu);
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

		switch(requestCode) { 
		case PICK_IMAGE_CODE:
			if(resultCode == RESULT_OK){  
				Uri selectedImage = imageReturnedIntent.getData();
				String[] filePathColumn = {MediaStore.Images.Media.DATA};

				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();

				Log.i(MainActivity.LOG, "file path: " + filePath);

				String[] params = new String[1];
				params[0] = filePath;

				UploadPhotoTask uploadPhotoTask = new UploadPhotoTask();
				uploadPhotoTask.execute(params);

				uploadDialog = ProgressDialog.show(EventHistoryDetailsActivity.this, "", "Uploading photo.\nPlease wait...");
			}
		}
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
	 * Asynchronous task that gets the participants of an event.
	 * @author Yunus Evren
	 */
	class GetEventParticipantsTask extends AsyncTask<Void, Void, Boolean>{
		String friendsList = "";

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "GetEventParticipantsTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(EventHistoryDetailsActivity.this)){
				Log.i(MainActivity.LOG, "GetEventParticipantsTask: Network unavailable");
				return false;
			}
			else{
				try{
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.GetEventParticipants?"
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
						Log.w(MainActivity.LOG, "GetEventParticipantsTask query returned no result.");
						return false;
					}

					for(int i = 0; i<responseList.size(); i++){
						String line = responseList.get(i);
						if(i==responseList.size()-1){
							friendsList+=line;
						}
						else{
							friendsList+=line+"\n";
						}
					}
					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetEventParticipantsTask Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetEventParticipantsTask MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetEventParticipantsTask IOException");
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Log.w(MainActivity.LOG, "GetEventParticipantsTask successfully finished.");
				friendsText.setText(friendsList);
			}
			else{
				Log.w(MainActivity.LOG, "GetEventParticipantsTask failed to finish.");
				friendsText.setText(founderName);
			}
			detailsProgressBar.setVisibility(View.GONE);
			middleLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Asynchronous task that gets the location of an event.
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
			if(!NetworkManager.isNetworkAvailable(EventHistoryDetailsActivity.this)){
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
				mapView = (IndependentMapView) EventHistoryDetailsActivity.this.findViewById(R.id.eventhistorydetails_mapview);
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
	 * Asynchronous task that uploads a photo into Flickr using the Flickr API.
	 * @author Yunus Evren
	 */
	class UploadPhotoTask extends AsyncTask<String, Void, Boolean>{
		String flickrToken = "";
		String filePath = "";
		String photoId = "";
		String groupId = "";
		
		
		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "UploadPhotoTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if(!NetworkManager.isNetworkAvailable(EventHistoryDetailsActivity.this)){
				Log.i(MainActivity.LOG, "UploadPhotoTask: Network unavailable");
				return false;
			}
			else{
				try{
					// Get groupId and token
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.GetFlickrToken"
						+ "?category_id="+categoryId;
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
						Log.w(MainActivity.LOG, "UploadPhotoTask GetFlickrToken query returned no result.");
						return false;
					}

					for(String line : responseList){
						String[] tokens = line.split("\\|");
						flickrToken = tokens[0];
						groupId = tokens[1];
					}

					// Authenticate into Flickr

					FlickrConnector flickrConnector = new FlickrConnector(flickrToken);
					filePath = params[0];
					
					// Upload the file
					Log.i(MainActivity.LOG, "file path: " + filePath);
					photoId = flickrConnector.uploadPhoto(filePath, locationName, dateAndTime, userName, friendsText.getText().toString(), groupId);
					
					if(photoId.length()==0){
						Log.w(MainActivity.LOG, "UploadPhotoTask Upload to Flickr has failed.");
						return false;
					}
					
					// Insert the photo into DB
					String link2 = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.AddNewPhoto"
						+ "?photo_id=" + photoId
						+ "&event_id=" + eventId
						+ "&facebook_id=" + facebookId;
					URL url2 = new URL(link2);
					URLConnection urlConn2 = url2.openConnection();

					BufferedReader in2 = 
						new BufferedReader( new InputStreamReader( urlConn2.getInputStream() ) );

					int count2 = 0;

					ArrayList<String> responseList2 = new ArrayList<String>();
					String response2;
					while((response2=in2.readLine())!=null){
						responseList2.add(response2);
					}

					count2 = responseList2.size();
					if(count2==0){
						Log.w(MainActivity.LOG, "UploadPhotoTask GetFlickrToken query returned no result.");
						return false;
					}

					for(String line : responseList2){
						if(line.contains("ERROR")){
							Log.w(MainActivity.LOG, "UploadPhotoTask AddNewPhoto received error response from server.");
							return false;
						}
					}
					return true;
				}
				catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "UploadPhotoTask GetFlickrToken Null Pointer Exception"+e);
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "UploadPhotoTask GetFlickrToken MalformedURLException"+e);
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "UploadPhotoTask GetFlickrToken IOException"+e);
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			uploadDialog.dismiss();
			if(result){
				Log.i(MainActivity.LOG, "UploadPhotoTask completed successfully");
				Toast.makeText(EventHistoryDetailsActivity.this, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show();
			}
			else{
				Log.i(MainActivity.LOG, "UploadPhotoTask failed");
				Toast.makeText(EventHistoryDetailsActivity.this, "Photo upload failed.\nPlease try again.", Toast.LENGTH_SHORT).show();
			}
		}

	}
}
