package com.cribcaged.getoffthecouch.misc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.R.id;
import com.cribcaged.getoffthecouch.R.layout;
import com.cribcaged.getoffthecouch.R.menu;
import com.cribcaged.getoffthecouch.entities.Location;
import com.cribcaged.getoffthecouch.util.ImageHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * Activity that displays the large version of a photo.
 * Allows zooming in and out.
 * @author Yunus Evren
 */
public class LargeImageActivity extends Activity{
	
	private Bitmap thumbBmp;
	private String largeURL;
	private TouchImageView imageView;
	private ProgressBar progressBar;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_large_image);
        
        Bundle extras = getIntent().getExtras();
        largeURL = extras.getString("large_url");
        
        progressBar = (ProgressBar) this.findViewById(R.id.largeimage_progressbar);
        
        imageView = (TouchImageView) this.findViewById(R.id.largeimage_imageview);
        imageView.setContext(LargeImageActivity.this.getApplicationContext());
        
        byte[] byteArray = extras.getByteArray("thumb");
        if(byteArray!=null){
        	thumbBmp= BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            imageView.setImageBitmap(thumbBmp);
        }
        
        DownloadPhotoLargeTask downloadPhotoLargeTask = new DownloadPhotoLargeTask();
        downloadPhotoLargeTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_large_image, menu);
        return true;
    }
    
    /**
     * Asynchronous task that downloads a photo from a URL.
	 * @author Yunus Evren
	 */
    class DownloadPhotoLargeTask extends AsyncTask<Void, Void, Boolean>{

		Bitmap bmp = null;

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "DownloadPhotoLargeTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			try {
				URL url_value = new URL(largeURL);
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
			if(result){
				imageView.setImageBitmap(bmp);
				imageView.setMaxZoom(4f);
			}
			else{

			}
			progressBar.setVisibility(View.GONE);
		}
	}
}
