package com.cribcaged.getoffthecouch.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.entities.Friend;
import com.cribcaged.getoffthecouch.entities.User;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.android.Facebook.DialogListener;
import com.google.android.gcm.GCMRegistrar;

/**
 * Class that handles the communication with Facebook API.
 * @author Yunus Evren
 */
public class FacebookConnector {
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	private SharedPreferences mPrefs;
	private Activity activity;

	/**
	 * Constructor
	 * @param activity - activity that creates the object
	 */
	public FacebookConnector(Activity activity){
		this.activity = activity;
		mPrefs = activity.getSharedPreferences(MainActivity.SESSION_PREFS, Activity.MODE_PRIVATE);
		mFacebook = new Facebook("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
	}

	/**
	 * Restores a session that is already running
	 */
	public void restoreSession() {
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);
		Log.i("FacebookConnector","token: " + access_token);
		Log.i("FacebookConnector","expires: " + expires);
		if(access_token != null) {
			mFacebook.setAccessToken(access_token);
		}
		if(expires != 0) {
			mFacebook.setAccessExpires(expires);
		}
	}

	/**
	 * Authenticates the user and saves their login information
	 */
	public void login(){  
		if(!mFacebook.isSessionValid()){
			mFacebook.authorize(activity, new String[] {"email", "publish_checkins"}, new DialogListener() {
				@Override
				public void onComplete(Bundle values) {
					SharedPreferences.Editor editor = mPrefs.edit();
					Log.i("FacebookConnector","bunlarý koyduk: " + mFacebook.getAccessToken());
					Log.i("FacebookConnector","bunlarý koyduk: " + mFacebook.getAccessExpires());
					editor.putString("access_token", mFacebook.getAccessToken());
					editor.putLong("access_expires", mFacebook.getAccessExpires());
					editor.commit();
				}

				@Override
				public void onFacebookError(FacebookError error) {
				}

				@Override
				public void onError(DialogError e) {
				}

				@Override
				public void onCancel() {
				}
			});
		}
	}

	/**
	 * Authorises API callback
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void authorizeCallback(int requestCode, int resultCode, Intent data){
		mFacebook.authorizeCallback(requestCode, resultCode, data);
	}

	/**
	 * Extends the token used to access the API
	 */
	public void extendToken(){
		mFacebook.extendAccessTokenIfNeeded(activity, null);
	}

	/**
	 * @return true if the session is currently on, false if the session is off
	 */
	public boolean sessionIsOn(){
		if(mFacebook.isSessionValid()){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Returns the details of a Facebook user - first name, last name and full name
	 * @param id - Facebook id of the user
	 * @return a User object
	 */
	public User getUserInfo(String id){
		try {
			String s = mFacebook.request(id);
			Log.i("FacebookConnectorLOG", s);
			JSONObject json = Util.parseJson(s);
			String realId = json.getString("id");
			String firstName = json.getString("first_name");
			String lastName = json.getString("last_name");
			String fullName = json.getString("name");
			return new User(realId, firstName, lastName, fullName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FacebookError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns the small profile photo of the user
	 * @param id - Facebook id of the user
	 * @return small profile photo of the user
	 */
	public Bitmap getProfilePictureSmall(String id){
		Bitmap bmp = null;
		String url = "http://graph.facebook.com/"+id+"/picture";
		try {
			bmp = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FacebookError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return bmp;
	}

	/**
	 * Returns the large profile photo of the user
	 * @param id - Facebook id of the user
	 * @return large profile photo of the user
	 */
	public Bitmap getProfilePicture(String id){
		Bitmap bmp = null;
		String url = "http://graph.facebook.com/"+id+"/picture?type=large";
		try {
			bmp = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FacebookError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return bmp;
	}
	
	/**
	 * Returns the list of Facebook friends of the user
	 * @param id - Facebook id of the user
	 * @return list of Facebook friends
	 */
	public ArrayList<Friend> getFriendsList(String id){
		String s = "";
		try {
			ArrayList<Friend> friendList = new ArrayList<Friend>();
			s = mFacebook.request(id + "/friends");
			JSONObject json1 = Util.parseJson(s);
			final JSONArray friends = json1.getJSONArray("data");
			for(int i =0 ; i<friends.length(); i++){
				String s2 = friends.getString(i);
				JSONObject json2 = Util.parseJson(s2);
				String realId = json2.getString("id");
				String fullName = json2.getString("name");
				friendList.add(new Friend(realId, fullName, 0));
			}
			return friendList;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FacebookError e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the list of Facebook friends of the user who have Get off the Couch installed
	 * @param id - Facebook id of the user
	 * @return list of Facebook friends that also have Get off the Couch installed
	 */
	public ArrayList<Friend> getInstalledFriends(String id){
		try {
			ArrayList<Friend> friendList = new ArrayList<Friend>();
			Bundle bundle = new Bundle();
			bundle.putString("fields", "installed");
			String s1 = mFacebook.request(id + "/friends", bundle);
			JSONObject json1 = Util.parseJson(s1);
			final JSONArray friends = json1.getJSONArray("data");
			for(int i =0 ; i<friends.length(); i++){
				String s2 = friends.getString(i);
				Log.i(MainActivity.LOG, s2);
				JSONObject json2 = Util.parseJson(s2);
				if(json2.has("installed")){
					String realId = json2.getString("id");
					Log.i(MainActivity.LOG, realId);
					User user = getUserInfo(realId);
					String fullName = user.getFullName();
					friendList.add(new Friend(realId, fullName, 0));
					Log.i(MainActivity.LOG, realId + ", " + fullName);
				}
			}
			return friendList;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FacebookError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Sends an application request to a list of Facebook friends
	 * @param friendIds - list of Facebook id's of the users to send application request
	 */
	public void sendRequest(ArrayList<String> friendIds){
		Bundle params = new Bundle();
		String idsString = "";
		for(int i = 0; i<friendIds.size(); i++){
			if(i==friendIds.size()-1){
				idsString = idsString + friendIds.get(i);
			}
			else{
				idsString = idsString + friendIds.get(i) + ",";
			}
		}
	    params.putString("to", idsString);
	    params.putString("message", "Hey mate! This is an awesome app. Download it!");
	    mFacebook.dialog(activity, "apprequests", params, new DialogListener(){
			@Override
			public void onComplete(Bundle values) {
				final String returnId = values.getString("request");
				Toast.makeText(activity, "Invitations successfully sent.", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFacebookError(FacebookError e) {

			}

			@Override
			public void onError(DialogError e) {
				
			}

			@Override
			public void onCancel() {
				Log.i(MainActivity.LOG, "cancel");
			}
	    });
	}

	/**
	 * Logs the user out and removes him/her from the notification service
	 * @return true if the logout is successful
	 */
	public boolean logout(){
		mAsyncRunner.logout(activity, new RequestListener() {
			@Override
			public void onComplete(String response, Object state) {}

			@Override
			public void onIOException(IOException e, Object state) {}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {}

			@Override
			public void onFacebookError(FacebookError e, Object state) {}
		});
		
		GCMRegistrar.unregister(activity);
		return true;
	}
}
