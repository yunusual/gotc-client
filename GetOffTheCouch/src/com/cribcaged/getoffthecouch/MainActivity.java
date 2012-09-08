package com.cribcaged.getoffthecouch;

import com.cribcaged.getoffthecouch.entities.User;
import com.cribcaged.getoffthecouch.util.FacebookConnector;
import com.cribcaged.getoffthecouch.welcome.WelcomeActivity;
import com.facebook.android.*;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;

import com.google.android.gcm.GCMRegistrar;

/**
 * The main activity for the application. Initiates Facebook login.
 * @author Yunus Evren
 */
public class MainActivity extends Activity{
	
	public static final String LOG = "GetOffTheCouchLOG";
	public static final String SESSION_PREFS = "SessionPrefSettings";
	public static final String LOGIN_PREFS = "LoginPrefSettings";
	public static final String SENDER_ID = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
	
	private RelativeLayout loginButton;
	private FacebookConnector fbConnector;
	
	private LinearLayout loadingLayout;
	
	private SplashScreenDialog splashScreenDialog;
	private int splashScreenTime = 3;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        
        if (customTitleSupported) {
        	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        }
    	
    	showSplashScreen();
        
        fbConnector = new FacebookConnector(MainActivity.this);
        
        loadingLayout = (LinearLayout) this.findViewById(R.id.main_loadinglayout);
        loadingLayout.setVisibility(View.VISIBLE);
        
        loginButton = (RelativeLayout) this.findViewById(R.id.main_loginlayout);
        loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loginButton.setVisibility(View.INVISIBLE);
				loadingLayout.setVisibility(View.VISIBLE);
				if(!fbConnector.sessionIsOn()){
					Log.i("MainActivityLOG","Session is off");
					fbConnector.login();
				}
				else{
					Log.i("MainActivityLOG","Session is on");
					Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
					MainActivity.this.startActivity(intent);
				}
			}
		});
        loginButton.setVisibility(View.INVISIBLE);
    }
    

    /**
     * Shows the splash screen
     */
    protected void showSplashScreen() {
		splashScreenDialog = new SplashScreenDialog(MainActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
		splashScreenDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				splashScreenTime=0;
			}
		});
		splashScreenDialog.show();

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(splashScreenTime>0){
					splashScreenTime--;
					handler.postDelayed(this, 1000);
				}
				else{
					removeSplashScreen();
				}
			}
		}, 1000);
	}

	/**
	 * Removes the splash screen
	 */
	protected void removeSplashScreen() {
		if (splashScreenDialog != null) {
			splashScreenDialog.dismiss();
			splashScreenDialog = null;
		}
		loginButton.setVisibility(View.INVISIBLE);
		if(!fbConnector.sessionIsOn()){
			Log.i("MainActivityLOG","Session is off");
			fbConnector.login();
		}
		else{
			Log.i("MainActivityLOG","Session is on");
			Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
			MainActivity.this.startActivity(intent);
		}
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbConnector.authorizeCallback(requestCode, resultCode, data);
        if(requestCode==32665){
        	if(resultCode == -1){
            	// Login is successful
            	Log.i("MainActivityLOG", "login successful");
            	RegisterTask registerTask = new RegisterTask();
            	registerTask.execute();
            }
            else{
            	// Login failed
            	Log.i("MainActivityLOG", "login failed");
            	Toast.makeText(MainActivity.this, "Facebook login failed!", Toast.LENGTH_SHORT).show();
            	loginButton.setVisibility(View.VISIBLE);
            	loadingLayout.setVisibility(View.GONE);
            }
        }
        
        if(requestCode==88){
        	if(resultCode==999){
            	MainActivity.this.finish();
            }
        	
        	if(resultCode==888){
        		loginButton.setVisibility(View.VISIBLE);
            	loadingLayout.setVisibility(View.GONE);
        	}
        }
    }
    
    public void onResume() {    
        super.onResume();
        fbConnector.extendToken();
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	GCMRegistrar.onDestroy(MainActivity.this);
    }
    
    /**
     * Asynchronous task for registering the user.
     * @author Yunus Evren
     */
    class RegisterTask extends AsyncTask<Void, Void, Boolean>{
    	
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
		protected Boolean doInBackground(Void... unused) {
			try {
				User userInfo = fbConnector.getUserInfo("me");
	        	String facebookId = userInfo.getId();
	        	String name = userInfo.getFullName();
	        	String firstName = userInfo.getFirstName();
	        	
	        	SharedPreferences settings = getSharedPreferences(MainActivity.LOGIN_PREFS, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("facebook_id", facebookId);
				editor.putString("name", name);
				editor.putString("first_name", firstName);
				editor.commit();
				return true;
			} catch (FacebookError e) {
				Log.v(LOG, "RegistrationTask: Facebook error");
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				GCMRegistrar.checkDevice(MainActivity.this);
	            GCMRegistrar.checkManifest(MainActivity.this);
	            final String regId = GCMRegistrar.getRegistrationId(MainActivity.this);
	            if (regId.equals("")) {
	              GCMRegistrar.register(MainActivity.this, SENDER_ID);
	            } else {
	              Log.v(LOG, "RegistrationTask: Already registered");
	            }
	        	Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
	        	MainActivity.this.startActivityForResult(intent, 88);
			}
			else{
				
			}
		}
	}

    /**
     * Custom dialog for splash screen.
     * @author Yunus Evren
     */
    class SplashScreenDialog extends Dialog{

    	private RelativeLayout mainLayout;
    	
    	public SplashScreenDialog(Context context, int theme) {
    		super(context, theme);
    		
    	}
    	
    	@Override
    	public void onCreate(Bundle savedInstanceState) {
    		super.onCreate(savedInstanceState);
    		setContentView(R.layout.splashscreen);
    		
    		mainLayout = (RelativeLayout) this.findViewById(R.id.splashscreen_mainlayout);
    		
    		mainLayout.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				SplashScreenDialog.this.dismiss();
    			}
    		});
    	}
    }

}
