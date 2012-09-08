package com.cribcaged.getoffthecouch.scoreboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.entities.Friend;
import com.cribcaged.getoffthecouch.util.FacebookConnector;
import com.cribcaged.getoffthecouch.util.NetworkManager;
import com.facebook.android.FacebookError;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * Activity that displays the scoreboard.
 * @author Yunus Evren
 */
public class ScoreboardActivity extends Activity {
	
	private FacebookConnector fbConnector;
	
	private TableLayout tableLayout;
	private LayoutInflater inflater;

	private LinearLayout loadingLayout;
	
	private String facebookId;
	
	private ArrayList<View> viewList;
	
	private ArrayList<Friend> userList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_scoreboard);

		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		}

		Bundle bundle = getIntent().getExtras();
		facebookId = bundle.getString("facebook_id");
		
		fbConnector = new FacebookConnector(ScoreboardActivity.this);
		
		loadingLayout = (LinearLayout) this.findViewById(R.id.scoreboard_loadinglayout);
		
		tableLayout = (TableLayout) this.findViewById(R.id.scoreboard_tablelayout);
		inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

		userList = new ArrayList<Friend>();
		viewList = new ArrayList<View>();
		
		loadingLayout.setVisibility(View.VISIBLE);
		
		GetScoreboardTask getScoreboardTask = new GetScoreboardTask();
		getScoreboardTask.execute();
    }
    
    /**
     * Populates the rows with each user's name, profile photo and score
     */
    private void setScoreboard(){
    	for (int i = 0; i<userList.size(); i++){
    		final Friend user = userList.get(i);
    		View itemView = inflater.inflate(R.layout.scoreboard_rowview, null);
    		viewList.add(itemView);
    		
    		ImageView image = (ImageView) itemView.findViewById(R.id.scoreboard_rowview_image);
    		TextView rankText = (TextView) itemView.findViewById(R.id.scoreboard_rowview_ranktext);
    		TextView nameText = (TextView) itemView.findViewById(R.id.scoreboard_rowview_nametext);
    		TextView scoreText = (TextView) itemView.findViewById(R.id.scoreboard_rowview_scoretext);
    		
    		rankText.setText((i+1)+".");
    		nameText.setText(user.getFullName());
    		scoreText.setText(user.getScore()+"");
    		image.setVisibility(View.INVISIBLE);
    		
    		GetProfilePictureTask getProfilePictureTask = new GetProfilePictureTask();
			getProfilePictureTask.execute(i);
			
			TableLayout.LayoutParams tableRowParams=
				new TableLayout.LayoutParams
				(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);

			int leftMargin = (int) getResources().getDimension(R.dimen.friends_rowview_left);
			int rightMargin = (int) getResources().getDimension(R.dimen.friends_rowview_right);
			int topMargin = (int) getResources().getDimension(R.dimen.friends_rowview_top);
			int bottomMargin = (int) getResources().getDimension(R.dimen.friends_rowview_bottom);

			tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
			tableLayout.addView(itemView, tableRowParams);
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_scoreboard, menu);
        return true;
    }

    /**
     * Asynchronous task that gets the scoreboard data from the server.
     * @author Yunus Evren
     */
    class GetScoreboardTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "GetScoreboardTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(ScoreboardActivity.this)){
				Log.i(MainActivity.LOG, "GetScoreboardTask: Network unavailable");
				return false;
			}
			else{
				try{
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.GetScoreboard";
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
						Log.w(MainActivity.LOG, "GetScoreboardTask query returned no result.");
						return false;
					}

					for(String line : responseList){
						String[] tokens = line.split("\\|");
						userList.add(new Friend(tokens[0], tokens[1], Integer.parseInt(tokens[2])));
					}
					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetScoreboardTask Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetScoreboardTask MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetScoreboardTask IOException");
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Log.w(MainActivity.LOG, "GetScoreboardTask finished successfully.");
				if(userList.size()>0){
					setScoreboard();
				}
			}
			else{
				
			}
			loadingLayout.setVisibility(View.GONE);
		}
	}
    
    /**
     * Asynchronous task that downloads the profile photo of a Facebook user.
     * @author Yunus Evren
     */
    class GetProfilePictureTask extends AsyncTask<Integer, Void, Boolean>{
		Bitmap bmp = null;
		int index = -1;

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "GetProfilePictureTask executed");
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
			try {
				bmp = fbConnector.getProfilePicture(userList.get(index).getId());
				if(bmp==null){
					return false;
				}
				return true;
			} catch (FacebookError e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			View view = viewList.get(index);
			if(result){
				if(bmp!=null&&index!=-1){
					Log.i(MainActivity.LOG, "GetProfilePictureTask: Photo downloaded successfully");
					ImageView image = (ImageView) view.findViewById(R.id.scoreboard_rowview_image);
					image.setImageBitmap(bmp);
					image.setVisibility(View.VISIBLE);
				}
			}
			else{
				Log.i(MainActivity.LOG, "GetProfilePictureTask download failed");
			}
			ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.scoreboard_rowview_progressbar);
			progressBar.setVisibility(View.GONE);
		}
	}
}
