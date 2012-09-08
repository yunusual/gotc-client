package com.cribcaged.getoffthecouch.create;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.R.layout;
import com.cribcaged.getoffthecouch.R.menu;
import com.cribcaged.getoffthecouch.entities.Category;
import com.cribcaged.getoffthecouch.entities.Location;
import com.cribcaged.getoffthecouch.misc.LargeImageActivity;
import com.cribcaged.getoffthecouch.misc.MapViewActivity;
import com.cribcaged.getoffthecouch.util.ImageHelper;
import com.cribcaged.getoffthecouch.util.NetworkManager;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

/**
 * Activity which asks the user to select an event location.
 * @author Yunus Evren
 */
public class SelectLocationActivity extends Activity {

	private TableLayout tableLayout;
	private LayoutInflater inflater;

	private LinearLayout loadingLayout;
	private ArrayList<Location> locationList;

	private ImageView rightArrow;
	private ImageView leftArrow;

	private ArrayList<View> viewList;

	private String userId;
	private String fullName;
	
	private int catId;
	private String catName;
	
	private Location selectedLocation;
	
	private Bitmap bmp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_select_location);

		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		}

		Bundle bundle = getIntent().getExtras();
		userId = bundle.getString("user_id");
		fullName = bundle.getString("full_name");
		
		catId = bundle.getInt("cat_id");
		catName = bundle.getString("cat_name");
		
		selectedLocation = null;
		
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pub);

		locationList = new ArrayList<Location>();

		viewList = new ArrayList<View>();

		loadingLayout = (LinearLayout) this.findViewById(R.id.selectlocation_loadinglayout);

		rightArrow = (ImageView) this.findViewById(R.id.selectlocation_rightarrowimage);
		rightArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(selectedLocation!=null){
					String address = "";
					Geocoder geoLocation = new Geocoder(SelectLocationActivity.this, Locale.getDefault());
					List<Address> addressList = null;
					try {
						float latitude = selectedLocation.getLatitude();
						float longitude = selectedLocation.getLongitude();
						addressList = geoLocation.getFromLocation(latitude, longitude, 1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
					
					Intent intent = new Intent(SelectLocationActivity.this, SelectFriendsActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("user_id", userId);
					bundle.putString("full_name", fullName);
					bundle.putInt("cat_id", catId);
					bundle.putString("cat_name", catName);
					bundle.putInt("loc_id", selectedLocation.getId());
					bundle.putString("loc_name", selectedLocation.getName());
					bundle.putFloat("latitude", selectedLocation.getLatitude());
					bundle.putFloat("longitude", selectedLocation.getLongitude());
					bundle.putString("loc_address", address);
					bundle.putInt("loc_score", selectedLocation.getScore());
					intent.putExtras(bundle);
					SelectLocationActivity.this.startActivityForResult(intent, 30);
				}
			}
		});

		rightArrow.setVisibility(View.INVISIBLE);
		
		
		leftArrow = (ImageView) this.findViewById(R.id.selectlocation_leftarrowimage);
		leftArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SelectLocationActivity.this.finish();
			}
		});

		tableLayout = (TableLayout) this.findViewById(R.id.selectlocation_tablelayout);
		inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

		GetLocationsTask getLocationsTask = new GetLocationsTask();
		getLocationsTask.execute();

	}

	/**
	 * Populates the rows with the location data
	 */
	public void setLocationRows() {
		for(int i = 0; i<locationList.size(); i++){
			final Location location = locationList.get(i);
			View itemView = inflater.inflate(R.layout.location_rowview, null);
			viewList.add(itemView);
			final int pos = i;

			RelativeLayout layout = (RelativeLayout) itemView.findViewById(R.id.location_rowview_innerlayout);
			FrameLayout imageFrame = (FrameLayout) itemView.findViewById(R.id.location_rowview_imageframe);
			final TextView locationText = (TextView) itemView.findViewById(R.id.location_rowview_locationtext);
			final TextView descText = (TextView) itemView.findViewById(R.id.location_rowview_desctext);
			TextView scoreText = (TextView) itemView.findViewById(R.id.location_rowview_scoretext);
			ImageView image = (ImageView) itemView.findViewById(R.id.location_rowview_image);

			imageFrame.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					View view = viewList.get(pos);
					ImageView img = (ImageView) view.findViewById(R.id.location_rowview_image);
					
					byte[] byteArray = null;
					BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
					if(drawable!=null){
						Bitmap bitmap = drawable.getBitmap();
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
						byteArray = stream.toByteArray();
					}
					Intent intent = new Intent(SelectLocationActivity.this, LargeImageActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("large_url", location.getPhotoLarge());
					intent.putExtras(bundle);
					intent.putExtra("thumb", byteArray);
					SelectLocationActivity.this.startActivity(intent);
				}
			});

			image.setVisibility(View.INVISIBLE);

			locationText.setText(location.getName());
			descText.setText(location.getDesc());
			scoreText.setText("Score: " + location.getScore() + " pts/friend");

			DownloadPhotoThumbTask downloadPhotoTask = new DownloadPhotoThumbTask();
			downloadPhotoTask.execute(i);

			layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					v.setBackgroundResource(R.drawable.menu_border_focused);
					locationText.setTextColor(getResources().getColor(R.color.text_white));
					descText.setTextColor(getResources().getColor(R.color.desc_text_selected));
					radioButtonSelected(pos);
					selectedLocation = location;
				}
			});

			layout.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Intent intent = new Intent(SelectLocationActivity.this, MapViewActivity.class);
					Bundle bundle = new Bundle();
					bundle.putFloat("latitude", location.getLatitude());
					bundle.putFloat("longitude", location.getLongitude());
					bundle.putString("name", location.getName());
					intent.putExtras(bundle);
					SelectLocationActivity.this.startActivity(intent);
					return false;
				}
			});

			TableLayout.LayoutParams tableRowParams=
				new TableLayout.LayoutParams
				(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
			
			int leftMargin = (int) getResources().getDimension(R.dimen.location_rowview_left);
			int rightMargin = (int) getResources().getDimension(R.dimen.location_rowview_right);
			int topMargin = (int) getResources().getDimension(R.dimen.location_rowview_top);
			int bottomMargin = (int) getResources().getDimension(R.dimen.location_rowview_bottom);

			tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
			tableLayout.addView(itemView, tableRowParams);
		}
	}

	/**
	 * Handles the selection of a radio button
	 * @param position - index of the selected location's radio button
	 */
	protected void radioButtonSelected(int position) {
		rightArrow.setVisibility(View.VISIBLE);
		for(int i = 0; i<tableLayout.getChildCount(); i++){
			View view = tableLayout.getChildAt(i);
			RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.location_rowview_innerlayout);
			TextView locationText = (TextView) view.findViewById(R.id.location_rowview_locationtext);
			TextView descText = (TextView) view.findViewById(R.id.location_rowview_desctext);
			RadioButton radioButton = (RadioButton) view.findViewById(R.id.location_rowview_radio);
			if(i!=position){
				radioButton.setChecked(false);
				layout.setBackgroundResource(R.drawable.menu_border_selector);
				locationText.setTextColor(getResources().getColor(R.color.text_gray));
				descText.setTextColor(getResources().getColor(R.color.desc_text_normal));
			}
			else{
				radioButton.setChecked(true);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_select_location, menu);
		return true;
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 999){
        	SelectLocationActivity.this.setResult(999);
        	SelectLocationActivity.this.finish();
        }
    }

	/**
	 * Asynchronous task that gets the locations from the server.
	 * @author Yunus Evren
	 */
	class GetLocationsTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "GetCategoriesTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(SelectLocationActivity.this)){
				Log.i(MainActivity.LOG, "GetCategoriesTask: Network unavailable");
				return false;
			}
			else{
				try{
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.GetLocations?cat_id=" + catId;
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
						Log.w(MainActivity.LOG, "GetCategoriesTask query returned no result.");
						return false;
					}

					for(String line : responseList){
						String[] tokens = line.split("\\|");
						int id = Integer.parseInt(tokens[0]);
						String name = tokens[1];
						String desc = tokens[2];
						String photoThumb = tokens[3];
						String photoLarge = tokens[4];
						float latitude = Float.parseFloat(tokens[5]);
						float longitude = Float.parseFloat(tokens[6]);
						int score = Integer.parseInt(tokens[7]);
						locationList.add(new Location(id, name, desc, photoThumb, photoLarge, latitude, longitude, score));
					}
					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetCategoriesTask Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetCategoriesTask MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetCategoriesTask IOException");
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				if(locationList.size()>0){
					setLocationRows();
					loadingLayout.setVisibility(View.GONE);
				}
			}
			else{

			}
		}
	}

	/**
	 * Asynchronous task that downloads the thumbnail image of a location.
	 * @author Yunus Evren
	 */
	class DownloadPhotoThumbTask extends AsyncTask<Integer, Void, Boolean>{

		Bitmap bmp = null;
		int index = -1;
		String largeURL = "";

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "DownloadPhotoTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			index = params[0];
			Location location = locationList.get(index);
			largeURL = location.getPhotoLarge();
			try {
				URL url_value = new URL(location.getPhotoThumb());
				bmp = BitmapFactory.decodeStream(url_value.openConnection().getInputStream());
				return true;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			View view = viewList.get(index);
			if(result){
				if(bmp!=null&&index!=-1){
					Log.i(MainActivity.LOG, "DownloadPhotoThumbTask: Photo downloaded successfully");
					bmp = ImageHelper.getRoundedCornerBitmap(bmp, 20);
					ImageView imageView = (ImageView) view.findViewById(R.id.location_rowview_image);
					imageView.setImageBitmap(bmp);
					imageView.setVisibility(View.VISIBLE);
				}
			}
			else{
				Log.i(MainActivity.LOG, "DownloadPhotoThumbTask download failed");
			}
			ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.location_rowview_progressbar);
			progressBar.setVisibility(View.GONE);
		}
	}

}
