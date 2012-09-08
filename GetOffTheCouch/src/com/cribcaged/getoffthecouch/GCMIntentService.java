package com.cribcaged.getoffthecouch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.util.NetworkManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Main controller class for Google Cloud Messaging notifications.
 * @author Yunus Evren
 */
public class GCMIntentService extends com.google.android.gcm.GCMBaseIntentService {

	/** 
	 * Called when GCM encounters an error
	 */
	@Override
	protected void onError(Context ctx, String errorId) {
		
	}

	/** 
	 * Called when the application receives a new notification
	 */
	@Override
	protected void onMessage(Context ctx, Intent intent) {
		String action = intent.getAction();
		Log.w(MainActivity.LOG, "NotificationReceiver Message Receiver called, action:" + action);
		if ("com.google.android.c2dm.intent.RECEIVE".equals(action)) {
			Log.w(MainActivity.LOG, "NotificationReceiver Received message");
			final String payload = intent.getStringExtra("user");
			Log.w(MainActivity.LOG, "NotificationReceiver dmControl: payload = " + payload);
			
			if(payload.compareTo("EventConfirmed")==0){
				int icon = R.drawable.ic_launcher;
				String tickerText = "An event has been confirmed! Mark your calendar";
				
				long[] vibrate = {0,100,200,300};
				NotificationManager mNotificationManager = (NotificationManager)ctx.getSystemService(NOTIFICATION_SERVICE);
				Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				notification.defaults |= Notification.DEFAULT_VIBRATE;
				notification.vibrate = vibrate;
				
				PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
		                new Intent(ctx, MainActivity.class), 0);
				Context context = getApplicationContext();
				notification.setLatestEventInfo(context, "An event has been confirmed!", "Tap to view details", contentIntent);
				mNotificationManager.notify(44, notification);
			}
			else{
				int icon = R.drawable.ic_launcher;
				String tickerText = "You have been invited to a new event!";
				
				long[] vibrate = {0,100,200,300};
				NotificationManager mNotificationManager = (NotificationManager)ctx.getSystemService(NOTIFICATION_SERVICE);
				Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				notification.defaults |= Notification.DEFAULT_VIBRATE;
				notification.vibrate = vibrate;
				
				PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
		                new Intent(ctx, MainActivity.class), 0);
				Context context = getApplicationContext();
				notification.setLatestEventInfo(context, "Invitation from " + payload, "Tap to respond", contentIntent);
				mNotificationManager.notify(44, notification);
			}
		}
	}

	/** 
	 * Called when the application registers the user
	 */
	@Override
	protected void onRegistered(Context ctx, String regId) {
		SharedPreferences settings = getSharedPreferences(MainActivity.LOGIN_PREFS, 0);
		String facebookId = settings.getString("facebook_id", "NOT_SPECIFIED");
		String name = settings.getString("name", "NOT_SPECIFIED");
		try {
			name = URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(facebookId.compareTo("NOT_SPECIFIED")==0 && name.compareTo("NOT_SPECIFIED")==0){
			System.err.println("facebookId name boş");
		}
		else{
			System.err.println("facebookId: " + facebookId + " name: " + name);
			String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.Registration?facebook_id=" 
				+ facebookId + "&name=" + name + "&reg_id=" + regId;

			System.out.println(link);
			
			if(!NetworkManager.isNetworkAvailable(ctx)){
				Log.w(MainActivity.LOG, "GCMIntentService onRegistered(): Network is not available.");
			}
			else{
				try {
					URL url = new URL(link);
					URLConnection urlConn = url.openConnection();
					BufferedReader in = 
						new BufferedReader( new InputStreamReader( urlConn.getInputStream() ) );

					String response;
					while((response=in.readLine())!=null){
						System.err.println(response);
					}

				} catch (MalformedURLException e) {
					Log.w(MainActivity.LOG, "GCMIntentService onRegistered(): MalformedURLException");
					e.printStackTrace();
				} catch (IOException e) {
					Log.w(MainActivity.LOG, "GCMIntentService onRegistered(): IOException");
					e.printStackTrace();
				}
			}
		}
	}

	/** 
	 * Called when the application unregisters the user
	 */
	@Override
	protected void onUnregistered(Context ctx, String regId) {
		
		SharedPreferences settings = getSharedPreferences(MainActivity.LOGIN_PREFS, 0);
		String facebookId = settings.getString("facebook_id", "NOT_SPECIFIED");
		
		if(facebookId.compareTo("NOT_SPECIFIED")==0){
			System.err.println("facebookId name boş");
		}
		else{
			System.err.println("facebookId: " + facebookId);
			
			String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.Unregistration?facebook_id=" 
				+ facebookId;

			System.out.println(link);
			
			if(!NetworkManager.isNetworkAvailable(ctx)){
				Log.w(MainActivity.LOG, "GCMIntentService onRegistered(): Network is not available.");
			}
			else{
				try {
					URL url = new URL(link);
					URLConnection urlConn = url.openConnection();
					BufferedReader in = 
						new BufferedReader( new InputStreamReader( urlConn.getInputStream() ) );

					String response;
					while((response=in.readLine())!=null){
						System.err.println(response);
					}

				} catch (MalformedURLException e) {
					Log.w(MainActivity.LOG, "GCMIntentService onUnregistered(): MalformedURLException");
					e.printStackTrace();
				} catch (IOException e) {
					Log.w(MainActivity.LOG, "GCMIntentService onUnregistered(): IOException");
					e.printStackTrace();
				}
			}
		}
	}

}
