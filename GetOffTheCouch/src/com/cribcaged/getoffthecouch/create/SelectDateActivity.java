package com.cribcaged.getoffthecouch.create;

import java.util.ArrayList;

import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.R.layout;
import com.cribcaged.getoffthecouch.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

/**
 * Activity which asks the user to specify the event date and time.
 * @author Yunus Evren
 */
public class SelectDateActivity extends Activity {
	
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
	
	private ArrayList<String> friendIds;
	private ArrayList<String> friendNames;
	
	private ImageView leftArrow;
	private ImageView rightArrow;
	
	private DatePicker datePicker;
	private TimePicker timePicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_select_date);

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
		
		friendIds = bundle.getStringArrayList("friend_ids");
		friendNames = bundle.getStringArrayList("friend_names");
		
		datePicker = (DatePicker) this.findViewById(R.id.selectdate_datepicker);
		timePicker = (TimePicker) this.findViewById(R.id.selectdate_timepicker);
		
		leftArrow = (ImageView) this.findViewById(R.id.selectdate_leftarrowimage);
		leftArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SelectDateActivity.this.finish();
			}
		});
		
		rightArrow = (ImageView) this.findViewById(R.id.selectdate_rightarrowimage);
		rightArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SelectDateActivity.this, ConfirmCreateActivity.class);
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
				String date = datePicker.getYear() + "-" + (datePicker.getMonth()+1) + "-" + datePicker.getDayOfMonth();
				String time = timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute();
				bundle.putString("date", date);
				bundle.putString("time", time);
				intent.putExtras(bundle);
				SelectDateActivity.this.startActivityForResult(intent, 50);
			}
		});
		
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_date, menu);
        return true;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 999){
        	SelectDateActivity.this.setResult(999);
        	SelectDateActivity.this.finish();
        }
    }

    
}
