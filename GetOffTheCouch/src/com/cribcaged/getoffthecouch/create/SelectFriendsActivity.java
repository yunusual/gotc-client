package com.cribcaged.getoffthecouch.create;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.R.layout;
import com.cribcaged.getoffthecouch.R.menu;
import com.cribcaged.getoffthecouch.entities.Friend;
import com.cribcaged.getoffthecouch.entities.Location;
import com.cribcaged.getoffthecouch.util.FacebookConnector;
import com.cribcaged.getoffthecouch.util.NetworkManager;
import com.cribcaged.getoffthecouch.welcome.WelcomeActivity;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

/**
 * Activity that asks the user to select the Facebook friends who will be invited to the event.
 * @author Yunus Evren
 */
public class SelectFriendsActivity extends Activity {

	private FacebookConnector fbConnector;

	private TableLayout tableLayout;
	private LayoutInflater inflater;

	private ArrayList<View> viewList;

	private LinearLayout loadingLayout;
	private ArrayList<Friend> friendsList;

	private ImageView rightArrow;
	private ImageView leftArrow;

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

	private Set<Integer> checkBoxSet;

	private LinearLayout noFriendsLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_select_friends);

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

		fbConnector = new FacebookConnector(SelectFriendsActivity.this);
		fbConnector.restoreSession();
		if(fbConnector.sessionIsOn()){
			Log.i("WelcomeActivityLOG","Session is on");
		}
		else{
			Log.i("WelcomeActivityLOG","Session off m8");
		}

		viewList = new ArrayList<View>();

		friendsList = new ArrayList<Friend>();

		checkBoxSet = new HashSet<Integer>();

		loadingLayout = (LinearLayout) this.findViewById(R.id.selectfriends_loadinglayout);
		noFriendsLayout = (LinearLayout) this.findViewById(R.id.selectfriends_nofriendslayout);

		rightArrow = (ImageView) this.findViewById(R.id.selectfriends_rightarrowimage);
		rightArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<String> friendIds = new ArrayList<String>();
				ArrayList<String> friendNames = new ArrayList<String>();

				for(Integer i : checkBoxSet){
					friendIds.add(friendsList.get(i).getId());
					friendNames.add(friendsList.get(i).getFullName());
				}
				Intent intent = new Intent(SelectFriendsActivity.this, SelectDateActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("user_id", userId);
				bundle.putString("full_name", fullName);
				bundle.putInt("cat_id", catId);
				bundle.putString("cat_name", catName);
				bundle.putInt("loc_id", locId);
				bundle.putFloat("latitude", latitude);
				bundle.putFloat("longitude", longitude);
				bundle.putString("loc_name", locName);
				bundle.putString("loc_address", locAddress);
				bundle.putInt("loc_score", locScore);
				bundle.putStringArrayList("friend_ids", friendIds);
				bundle.putStringArrayList("friend_names", friendNames);
				intent.putExtras(bundle);
				SelectFriendsActivity.this.startActivityForResult(intent, 40);
			}
		});

		rightArrow.setVisibility(View.INVISIBLE);

		leftArrow = (ImageView) this.findViewById(R.id.selectfriends_leftarrowimage);
		leftArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SelectFriendsActivity.this.finish();
			}
		});

		tableLayout = (TableLayout) this.findViewById(R.id.selectfriends_tablelayout);
		inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

		GetFriendsTask getFriendsTask = new GetFriendsTask();
		getFriendsTask.execute();

	}

	/**
	 * Populates the rows with each friend's data
	 */
	public void setFriendRows() {
		for(int i = 0; i<friendsList.size(); i++){
			Friend friend = friendsList.get(i);
			View itemView = inflater.inflate(R.layout.friends_rowview, null);
			viewList.add(itemView);
			final int pos = i;

			RelativeLayout layout = (RelativeLayout) itemView.findViewById(R.id.friends_rowview_innerlayout);
			final TextView nameText = (TextView) itemView.findViewById(R.id.friends_rowview_text);
			ImageView image = (ImageView) itemView.findViewById(R.id.friends_rowview_image);

			image.setVisibility(View.INVISIBLE);
			nameText.setText(friend.getFullName());

			GetProfilePictureTask getProfilePictureTask = new GetProfilePictureTask();
			getProfilePictureTask.execute(i);

			layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					v.setBackgroundResource(R.drawable.menu_border_focused);
					nameText.setTextColor(getResources().getColor(R.color.text_white));
					checkBoxSelected(pos);
				}
			});

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

	/**
	 * Handles the selection of friends
	 * @param pos - index of the selected friend
	 */
	protected void checkBoxSelected(int pos) {
		View view = viewList.get(pos);
		RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.friends_rowview_innerlayout);
		TextView nameText = (TextView) view.findViewById(R.id.friends_rowview_text);
		CheckBox checkBox = (CheckBox) view.findViewById(R.id.friends_rowview_checkbox);

		if(checkBoxSet.size()==3&&!checkBoxSet.contains(pos)){
			Toast.makeText(SelectFriendsActivity.this, "You can only select up to three friends", Toast.LENGTH_SHORT).show();
			layout.setBackgroundResource(R.drawable.menu_border_selector);
			nameText.setTextColor(getResources().getColor(R.color.text_gray));
		}
		else{
			if(checkBoxSet.contains(pos)){
				checkBoxSet.remove(pos);
				checkBox.setChecked(false);
				layout.setBackgroundResource(R.drawable.menu_border_selector);
				nameText.setTextColor(getResources().getColor(R.color.text_gray));
			}
			else{
				checkBoxSet.add(pos);
				checkBox.setChecked(true);
			}

			if(checkBoxSet.size()>0){
				rightArrow.setVisibility(View.VISIBLE);
			}
			else{
				rightArrow.setVisibility(View.INVISIBLE);
			}
		}


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_select_friends, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 999){
			SelectFriendsActivity.this.setResult(999);
			SelectFriendsActivity.this.finish();
		}
	}

	/**
	 * @author Yunus Evren
	 * Asynchronous task that gets the friends from the Facebook API
	 */
	class GetFriendsTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "SelectFriendsActivity: GetFriendsTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(SelectFriendsActivity.this)){
				Log.i(MainActivity.LOG, "GetFriendsTask: Network unavailable");
				return false;
			}
			else{
				friendsList = fbConnector.getInstalledFriends("me");
				if(friendsList.size()!=0){
					return true;
				}
				else{
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			loadingLayout.setVisibility(View.GONE);
			if(result){
				setFriendRows();
			}
			else{
				noFriendsLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Asynchronous task that gets the profile picture of a Facebook friend.
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
				bmp = fbConnector.getProfilePicture(friendsList.get(index).getId());
				if(bmp==null){
					return false;
				}
				return true;
			} catch (FacebookError e) {
				// TODO Auto-generated catch block
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
					ImageView image = (ImageView) view.findViewById(R.id.friends_rowview_image);
					image.setImageBitmap(bmp);
					image.setVisibility(View.VISIBLE);
				}
			}
			else{
				Log.i(MainActivity.LOG, "GetProfilePictureTask download failed");
			}
			ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.friends_rowview_progressbar);
			progressBar.setVisibility(View.GONE);
		}
	}
}
