package com.cribcaged.getoffthecouch.fbinvite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.R.layout;
import com.cribcaged.getoffthecouch.R.menu;
import com.cribcaged.getoffthecouch.create.SelectFriendsActivity;
import com.cribcaged.getoffthecouch.entities.Friend;
import com.cribcaged.getoffthecouch.misc.ObservableScrollView;
import com.cribcaged.getoffthecouch.misc.ScrollViewListener;
import com.cribcaged.getoffthecouch.util.FacebookConnector;
import com.cribcaged.getoffthecouch.util.NetworkManager;
import com.facebook.android.FacebookError;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

/**
 * Activity that asks the user to select Facebook friends who will be sent an application request.
 * @author Yunus Evren
 */
public class FacebookInviteActivity extends Activity implements ScrollViewListener{

	private FacebookConnector fbConnector;

	private TableLayout tableLayout;
	private LayoutInflater inflater;

	private ArrayList<View> viewList;

	private LinearLayout loadingLayout;
	private ArrayList<Friend> friendsList;

	private ImageView rightArrow;
	private ImageView leftArrow;

	private Set<Integer> checkBoxSet;

	private LinearLayout noFriendsLayout;
	
	private int currentStartIndex = 0;
	private int currentEndIndex = 20;
	
	private ObservableScrollView scrollView;
	
	
	private boolean loadMore = false;
	private Handler loadMoreHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_facebook_invite);

		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		}

		fbConnector = new FacebookConnector(FacebookInviteActivity.this);
		fbConnector.restoreSession();
		if(fbConnector.sessionIsOn()){
			Log.i(MainActivity.LOG,"FacebookInviteActivity: Session is on");
		}
		else{
			Log.i(MainActivity.LOG,"FacebookInviteActivity: Session off");
		}
		
		loadMoreHandler = new Handler();

		viewList = new ArrayList<View>();

		friendsList = new ArrayList<Friend>();

		checkBoxSet = new HashSet<Integer>();
		
		scrollView = (ObservableScrollView) this.findViewById(R.id.facebookinvite_scrollview);
		scrollView.setScrollViewListener(this);
		
		
		loadingLayout = (LinearLayout) this.findViewById(R.id.facebookinvite_loadinglayout);
		noFriendsLayout = (LinearLayout) this.findViewById(R.id.facebookinvite_nofriendslayout);

		rightArrow = (ImageView) this.findViewById(R.id.facebookinvite_rightarrowimage);
		rightArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<String> friendIds = new ArrayList<String>();
				for(Integer i : checkBoxSet){
					friendIds.add(friendsList.get(i).getId());
				}
				fbConnector.sendRequest(friendIds);
			}
		});

		rightArrow.setVisibility(View.INVISIBLE);

		leftArrow = (ImageView) this.findViewById(R.id.facebookinvite_leftarrowimage);
		leftArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FacebookInviteActivity.this.finish();
			}
		});

		tableLayout = (TableLayout) this.findViewById(R.id.facebookinvite_tablelayout);
		inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

		GetFriendsTask getFriendsTask = new GetFriendsTask();
		getFriendsTask.execute();
	}

	/**
	 * Populates the rows with the Facebook friend names and photos
	 * @param start - starting index of the Facebook friend to be fetched
	 * @param end - end index of the Facebook friend to be fetched
	 */
	public void setFriendRows(int start, int end) {
		if(end>friendsList.size()){
			end=friendsList.size();
		}
		
		for(int i = start; i<end; i++){
			Friend friend = friendsList.get(i);
			View itemView = inflater.inflate(R.layout.invitefriends_rowview, null);
			viewList.add(itemView);
			final int pos = i;

			RelativeLayout layout = (RelativeLayout) itemView.findViewById(R.id.invitefriends_rowview_innerlayout);
			final TextView nameText = (TextView) itemView.findViewById(R.id.invitefriends_rowview_text);
			ImageView image = (ImageView) itemView.findViewById(R.id.invitefriends_rowview_image);

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
		
		currentStartIndex = end;
		currentEndIndex = end + 20;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_facebook_invite, menu);
		return true;
	}

	/**
	 * Handles the selection of Facebook friends
	 * @param pos - index of the selected Facebook friend
	 */
	protected void checkBoxSelected(int pos) {
		View view = viewList.get(pos);
		RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.invitefriends_rowview_innerlayout);
		TextView nameText = (TextView) view.findViewById(R.id.invitefriends_rowview_text);
		CheckBox checkBox = (CheckBox) view.findViewById(R.id.invitefriends_rowview_checkbox);
		
		if(checkBoxSet.size()==5&&!checkBoxSet.contains(pos)){
			Toast.makeText(FacebookInviteActivity.this, "You can only select up to five friends", Toast.LENGTH_SHORT).show();
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

	/**
	 * Asynchronous task that gets user's Facebook friends from the Facebook API.
	 * @author Yunus Evren
	 */
	class GetFriendsTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "FacebookInviteActivity: GetFriendsTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(FacebookInviteActivity.this)){
				Log.i(MainActivity.LOG, "GetFriendsTask: Network unavailable");
				return false;
			}
			else{
				friendsList = fbConnector.getFriendsList("me");
				Collections.sort(friendsList);
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
				setFriendRows(currentStartIndex, currentEndIndex);
			}
			else{
				noFriendsLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Asynchronous task that gets a friend's profile photo.
	 * @author Yunus Evren
	 */
	class GetProfilePictureTask extends AsyncTask<Integer, Void, Boolean>{
		Bitmap bmp = null;
		int index = -1;

		@Override
		protected void onPreExecute() 
		{
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
				bmp = fbConnector.getProfilePictureSmall(friendsList.get(index).getId());
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
					ImageView image = (ImageView) view.findViewById(R.id.invitefriends_rowview_image);
					image.setImageBitmap(bmp);
					image.setVisibility(View.VISIBLE);
				}
			}
			else{
				Log.i(MainActivity.LOG, "GetProfilePictureTask download failed");
			}
			ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.invitefriends_rowview_progressbar);
			progressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onScrollChanged(ObservableScrollView scrollView, int x, int y,
			int oldx, int oldy) {
		if(loadMore==false){
			loadMore = true;
			setFriendRows(currentStartIndex, currentEndIndex);
			loadMoreHandler.postDelayed(new Runnable(){
				@Override
				public void run() {
					loadMore = false;
				}
			}, 1000);
		}
	}

}
