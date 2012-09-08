package com.cribcaged.getoffthecouch.history;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.R.layout;
import com.cribcaged.getoffthecouch.R.menu;
import com.cribcaged.getoffthecouch.entities.Event;
import com.cribcaged.getoffthecouch.entities.Photo;
import com.cribcaged.getoffthecouch.misc.LargeImageActivity;
import com.cribcaged.getoffthecouch.util.FlickrConnector;
import com.cribcaged.getoffthecouch.util.ImageHelper;
import com.cribcaged.getoffthecouch.util.NetworkManager;

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
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

/**
 * Activity that displays the event photos.
 * @author Yunus Evren
 */
public class ViewPhotosActivity extends Activity {

	private TableLayout tableLayout;
	private LayoutInflater inflater;

	private LinearLayout loadingLayout;
	private LinearLayout noPhotosLayout;

	private ArrayList<Photo> photoList;
	private ArrayList<View> viewList;

	private Photo selectedPhoto;

	private int eventId;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_view_photos);

		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		}
		
		Bundle bundle = getIntent().getExtras();
		eventId = bundle.getInt("event_id");
		
		viewList = new ArrayList<View>();
		
		loadingLayout = (LinearLayout) this.findViewById(R.id.viewphotos_loadinglayout);
		noPhotosLayout = (LinearLayout) this.findViewById(R.id.viewphotos_nophotoslayout);
		
		selectedPhoto = null;
		
		tableLayout = (TableLayout) this.findViewById(R.id.viewphotos_tablelayout);
		inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

		photoList = new ArrayList<Photo>();

		loadingLayout.setVisibility(View.VISIBLE);

		GetPhotosTask getPhotosTask = new GetPhotosTask();
		getPhotosTask.execute();
    }
    
    /**
     * Populates the rows with each photo's details
     */
    private void setPhotoRows() {
    	for(int i = 0; i<photoList.size(); i++){
    		final Photo photo = photoList.get(i);
        	View itemView = inflater.inflate(R.layout.photo_rowview, null);
        	viewList.add(itemView);
        	RelativeLayout layout = (RelativeLayout) itemView.findViewById(R.id.photo_rowview_innerlayout);
        	final int pos = i;

        	final TextView nameText = (TextView) itemView.findViewById(R.id.photo_rowview_nametext);
        	final TextView dateTimeText = (TextView) itemView.findViewById(R.id.photo_rowview_datetext);
        	
        	
        	nameText.setText(photo.getUserName());
        	dateTimeText.setText(photo.getDateAndTime());
        	
        	DownloadPhotoTask downloadPhotoThumbTask = new DownloadPhotoTask();
        	downloadPhotoThumbTask.execute(i);
        	
        	layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					selectedPhoto = photo;
					
					View view = viewList.get(pos);
					ImageView img = (ImageView) view.findViewById(R.id.photo_rowview_image);
					
					byte[] byteArray = null;
					BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
					if(drawable!=null){
						Bitmap bitmap = drawable.getBitmap();
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
						byteArray = stream.toByteArray();
					}
					
					Intent intent = new Intent(ViewPhotosActivity.this, LargeImageActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("large_url", photo.getLargeURL());
					intent.putExtras(bundle);
					intent.putExtra("thumb", byteArray);
					ViewPhotosActivity.this.startActivity(intent);
				}
			});
        	
        	TableLayout.LayoutParams tableRowParams=
        		  new TableLayout.LayoutParams
        		  (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
        	
        	int leftMargin = (int) getResources().getDimension(R.dimen.photo_rowview_left);
        	int rightMargin = (int) getResources().getDimension(R.dimen.photo_rowview_right);
        	int topMargin = (int) getResources().getDimension(R.dimen.photo_rowview_top);
        	int bottomMargin = (int) getResources().getDimension(R.dimen.photo_rowview_bottom);
        	
        	tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
        	tableLayout.addView(itemView, tableRowParams);
        }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_view_photos, menu);
        return true;
    }

    /**
     * Asynchronous task that gets the photos of an event.
	 * @author Yunus Evren
	 */
    class GetPhotosTask extends AsyncTask<Void, Void, Boolean>{

    	String flickrToken = "";
    	
		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "GetPhotosTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(ViewPhotosActivity.this)){
				Log.i(MainActivity.LOG, "GetPhotosTask: Network unavailable");
				return false;
			}
			else{
				try{
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.GetEventPhotos?"
						+ "event_id=" + eventId;
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
						Log.w(MainActivity.LOG, "GetPhotosTask query returned no result.");
						return false;
					}

					for(String line : responseList){
						String[] tokens = line.split("\\|");
						photoList.add(new Photo(tokens[0], Integer.parseInt(tokens[1]), tokens[2]));
						flickrToken = tokens[3];
					}
					
					if(photoList.size()==0){
						Log.w(MainActivity.LOG, "GetPhotosTask photo list empty.");
						return false;
					}
					
					FlickrConnector flickrConnector = new FlickrConnector(flickrToken);
					photoList = flickrConnector.getPhotoInfo(photoList);
					if(photoList==null || photoList.size()==0){
						Log.w(MainActivity.LOG, "GetPhotosTask photo list empty after Flickr operations.");
						return false;
					}
					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetPhotosTask Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetPhotosTask MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetPhotosTask IOException");
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Log.w(MainActivity.LOG, "GetPhotosTask finished successfully.");
				setPhotoRows();
			}
			else{
				noPhotosLayout.setVisibility(View.VISIBLE);
				Log.w(MainActivity.LOG, "GetPhotosTask failed to finish.");
			}
			loadingLayout.setVisibility(View.GONE);
		}
	}

    /**
     * Asynchronous task that downloads a Flickr photo.
	 * @author Yunus Evren
	 */
    class DownloadPhotoTask extends AsyncTask<Integer, Void, Boolean>{

		Bitmap bmp = null;
		int index = -1;

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
			
			String photoURL = photoList.get(index).getSmallURL();
			try {
				URL url_value = new URL(photoURL);
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
					Log.i(MainActivity.LOG, "DownloadPhotoTask: Photo downloaded successfully");
					ImageView imageView = (ImageView) view.findViewById(R.id.photo_rowview_image);
					imageView.setImageBitmap(bmp);
					imageView.setVisibility(View.VISIBLE);
				}
			}
			else{
				Log.i(MainActivity.LOG, "DownloadPhotoTask download failed");
			}
			ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.photo_rowview_progressbar);
			progressBar.setVisibility(View.GONE);
		}
	}
}
