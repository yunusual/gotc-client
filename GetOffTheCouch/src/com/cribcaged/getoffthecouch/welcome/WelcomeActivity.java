package com.cribcaged.getoffthecouch.welcome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.create.ConfirmCreateActivity;
import com.cribcaged.getoffthecouch.create.SelectCategoryActivity;
import com.cribcaged.getoffthecouch.current.CurrentEventsActivity;
import com.cribcaged.getoffthecouch.entities.Friend;
import com.cribcaged.getoffthecouch.entities.User;
import com.cribcaged.getoffthecouch.fbinvite.FacebookInviteActivity;
import com.cribcaged.getoffthecouch.history.EventHistoryActivity;
import com.cribcaged.getoffthecouch.invitations.InvitationsActivity;
import com.cribcaged.getoffthecouch.pending.PendingEventsActivity;
import com.cribcaged.getoffthecouch.scoreboard.ScoreboardActivity;
import com.cribcaged.getoffthecouch.util.FacebookConnector;
import com.cribcaged.getoffthecouch.util.NetworkManager;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that displays the main menu when the user is authenticated.
 * @author Yunus Evren
 */
public class WelcomeActivity extends Activity {
	
	private FacebookConnector fbConnector;
	private RelativeLayout inviteMenu;
	private RelativeLayout createEventMenu;
	private RelativeLayout currentMenu;
	private RelativeLayout invitationsMenu;
	private RelativeLayout pendingMenu;
	private RelativeLayout historyMenu;
	private RelativeLayout scoreboardMenu;
	private RelativeLayout logoutMenu;
	
	private TextView welcomeText;
	private TextView invitationNumberText;
	
	private ImageView welcomeImage;
	
	private LinearLayout welcomeLayout;
	
	private ScrollView scrollView;
	
	private ProgressBar progressBar;
	
	private String facebookId;
	private String fullName;
	private String firstName;
	
	private int invitationCount;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_welcome);
        
        Toast.makeText(WelcomeActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
        
        if (customTitleSupported) {
        	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        }
        
        setResult(999);
        
        welcomeLayout = (LinearLayout) this.findViewById(R.id.welcome_welcomelayout);
        scrollView = (ScrollView) this.findViewById(R.id.welcome_scrolllayout);
        progressBar = (ProgressBar) this.findViewById(R.id.welcome_progressbar);
        
        welcomeLayout.setVisibility(View.INVISIBLE);
        scrollView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        
        SharedPreferences settings = getSharedPreferences(MainActivity.LOGIN_PREFS, 0);
		facebookId = settings.getString("facebook_id", "NOT_SPECIFIED");
		fullName = settings.getString("name", "NOT_SPECIFIED");
		firstName = settings.getString("first_name", "NOT_SPECIFIED");
        
		invitationCount = -1;
        
        fbConnector = new FacebookConnector(WelcomeActivity.this);
        fbConnector.restoreSession();
        if(fbConnector.sessionIsOn()){
        	Log.i("WelcomeActivityLOG","Session is on");
        	GetUserInfoTask userInfoTask = new GetUserInfoTask();
        	userInfoTask.execute();
        }
        else{
        	Log.i("WelcomeActivityLOG","Session off m8");
        }
        
        inviteMenu = (RelativeLayout) this.findViewById(R.id.welcome_invitelayout);
        createEventMenu = (RelativeLayout) this.findViewById(R.id.welcome_createlayout);
        currentMenu = (RelativeLayout) this.findViewById(R.id.welcome_currentlayout);
        invitationsMenu = (RelativeLayout) this.findViewById(R.id.welcome_invitationslayout);
        pendingMenu = (RelativeLayout) this.findViewById(R.id.welcome_pendinglayout);
        historyMenu = (RelativeLayout) this.findViewById(R.id.welcome_historylayout);
        scoreboardMenu = (RelativeLayout) this.findViewById(R.id.welcome_scorelayout);
        logoutMenu = (RelativeLayout) this.findViewById(R.id.welcome_logoutlayout);
        
        welcomeText = (TextView) this.findViewById(R.id.welcome_welcometext);
        welcomeImage = (ImageView) this.findViewById(R.id.welcome_welcomeimage);
        
        invitationNumberText = (TextView) this.findViewById(R.id.welcome_invitationcounttext);
        
        createEventMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, SelectCategoryActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("user_id", facebookId);
				bundle.putString("full_name", fullName);
				intent.putExtras(bundle);
				WelcomeActivity.this.startActivityForResult(intent, 10);
			}
		});
        
        currentMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, CurrentEventsActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("facebook_id", facebookId);
				intent.putExtras(bundle);
				WelcomeActivity.this.startActivity(intent);
			}
		});
        
        invitationsMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, InvitationsActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("invitation_count", invitationCount);
				bundle.putString("facebook_id", facebookId);
				intent.putExtras(bundle);
				WelcomeActivity.this.startActivityForResult(intent, 5);
			}
		});
        
        pendingMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, PendingEventsActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("facebook_id", facebookId);
				intent.putExtras(bundle);
				WelcomeActivity.this.startActivity(intent);
			}
		});
        
        historyMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, EventHistoryActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("facebook_id", facebookId);
				bundle.putString("user_name", fullName);
				intent.putExtras(bundle);
				WelcomeActivity.this.startActivity(intent);
			}
		});
        
        scoreboardMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, ScoreboardActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("facebook_id", facebookId);
				intent.putExtras(bundle);
				WelcomeActivity.this.startActivity(intent);
			}
		});
        
        inviteMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, FacebookInviteActivity.class);
				WelcomeActivity.this.startActivity(intent);
			}
		});
        
        logoutMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean result = fbConnector.logout();
				if(result){
					Toast.makeText(WelcomeActivity.this, "Successfully logged out.\nYou will not receive any more notifications", Toast.LENGTH_SHORT).show();
					WelcomeActivity.this.setResult(888);
					WelcomeActivity.this.finish();
				}
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_welcome, menu);
        return true;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10 && resultCode == 999){
        	Toast.makeText(WelcomeActivity.this, "Event successfully created!\nInvitations have been sent.", Toast.LENGTH_LONG).show();
        }
        
        if(requestCode == 5 && resultCode == 999){
        	Toast.makeText(WelcomeActivity.this, "Invitation response successfully sent!", Toast.LENGTH_SHORT).show();
        	GetInvitationNumberTask getInvitationNumberTask = new GetInvitationNumberTask();
        	getInvitationNumberTask.execute(facebookId);
        }
    }
    
    /**
     * Asynchronous task that gets the user information from Facebook API.
     * @author Yunus Evren
     */
    class GetUserInfoTask extends AsyncTask<Void, Void, Boolean>{
    	Bitmap bmp = null;
    	
		@Override
		protected void onPreExecute() 
		{
			Log.i("GetUserInfoTask", "GetUserInfoTask executed");
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
				bmp = fbConnector.getProfilePicture(facebookId);
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
			if(result){
				welcomeText.setText("Welcome " + firstName + "!");
				welcomeImage.setImageBitmap(bmp);
				GetInvitationNumberTask getInvitationNumberTask = new GetInvitationNumberTask();
				getInvitationNumberTask.execute(facebookId);
			}
			else{
				progressBar.setVisibility(View.INVISIBLE);
		        scrollView.setVisibility(View.VISIBLE);
			}
		}
	}
    
    /**
     * Asynchronous task that gets the number of invitations that the user has.
     * @author Yunus Evren
     */
    class GetInvitationNumberTask extends AsyncTask<String, Void, Boolean>{
    	String id = "";
    	int count = -1;
    	
		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "GetInvitationNumberTask for " + id + " executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			id = params[0];
			if(!NetworkManager.isNetworkAvailable(WelcomeActivity.this)){
				Log.i(MainActivity.LOG, "GetInvitationNumberTask: Network unavailable");
				return false;
			}
			else{
				try{
					Thread.sleep(1000);
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.GetInvitationNumber?" +
						"facebook_id=" + id;
					
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
						if(s.contains("GetInvitationNumber:")){
							if(s.contains("ERROR")){
								Log.i(MainActivity.LOG, "GetInvitationNumberTask Failed - Error response from server");
								return false;
							}
						}
					}
					
					count = Integer.parseInt(responseList.get(responseList.size()-1));
					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetInvitationNumberTask for " + id + " Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetInvitationNumberTask for " + id + " MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetInvitationNumberTask for " + id + " IOException");
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
				if(count!=-1){
					Log.i(MainActivity.LOG, "GetInvitationNumberTask for " + id + " Successfully completed");
					invitationCount = count;
					if (count==0){
						invitationNumberText.setVisibility(View.INVISIBLE);
					}
					else{
						invitationNumberText.setText("  " + count + "  ");
						invitationNumberText.setVisibility(View.VISIBLE);
					}
				}
			}
			else{
				Log.i(MainActivity.LOG, "GetInvitationNumberTask for " + id + " Failed to complete");
			}
			progressBar.setVisibility(View.INVISIBLE);
			welcomeLayout.setVisibility(View.VISIBLE);
	        scrollView.setVisibility(View.VISIBLE);
		}
	}
    
}
