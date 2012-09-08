package com.cribcaged.getoffthecouch.invitations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.entities.Invitation;
import com.cribcaged.getoffthecouch.entities.Location;
import com.cribcaged.getoffthecouch.util.ImageHelper;
import com.cribcaged.getoffthecouch.util.NetworkManager;
import com.cribcaged.getoffthecouch.welcome.WelcomeActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that displays the list of non-responded invitations.
 * @author Yunus Evren
 */
public class InvitationsActivity extends Activity {

	private TableLayout tableLayout;
	private LayoutInflater inflater;

	private LinearLayout loadingLayout;
	private TextView noNewInvitationsText;
	
	private ImageView rightArrow;
	private ImageView leftArrow;

	private ArrayList<Invitation> invitationsList;
	private ArrayList<View> viewList;

	private Invitation selectedInvitation;

	private int invitationCount;
	private String facebookId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_invitations);

		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		}

		Bundle bundle = getIntent().getExtras();
		invitationCount = bundle.getInt("invitation_count");
		facebookId = bundle.getString("facebook_id");
		
		viewList = new ArrayList<View>();

		loadingLayout = (LinearLayout) this.findViewById(R.id.invitations_loadinglayout);
		noNewInvitationsText = (TextView) this.findViewById(R.id.invitations_nonewinvitationstext);

		rightArrow = (ImageView) this.findViewById(R.id.invitations_rightarrowimage);
		rightArrow.setVisibility(View.INVISIBLE);
		rightArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(selectedInvitation!=null){
					Intent intent = new Intent(InvitationsActivity.this, InvitationResponseActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("facebook_id", facebookId);
					bundle.putInt("invitation_id", selectedInvitation.getInvitationId());
					bundle.putInt("event_id", selectedInvitation.getEventId());
					TypedArray categoryNames = getResources().obtainTypedArray(R.array.category_names);
					String categoryName = categoryNames.getString(selectedInvitation.getCategoryId()-1);
					bundle.putString("category_name", categoryName);
					bundle.putString("location_name", selectedInvitation.getLocationName());
					bundle.putString("date_time", selectedInvitation.getDateTime());
					bundle.putInt("total_score", selectedInvitation.getScore());
					bundle.putString("founder_name", selectedInvitation.getFounderName());
					intent.putExtras(bundle);
					InvitationsActivity.this.startActivityForResult(intent, 15);
				}
			}
		});
		
		
		leftArrow = (ImageView) this.findViewById(R.id.invitations_leftarrowimage);
		leftArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				InvitationsActivity.this.finish();
			}
		});
		
		if(invitationCount==0){
			loadingLayout.setVisibility(View.INVISIBLE);
			noNewInvitationsText.setVisibility(View.VISIBLE);
		}
		else{
			selectedInvitation = null;

			tableLayout = (TableLayout) this.findViewById(R.id.invitations_tablelayout);
			inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

			invitationsList = new ArrayList<Invitation>();

			loadingLayout.setVisibility(View.VISIBLE);

			GetInvitationsTask getInvitationsTask = new GetInvitationsTask();
			getInvitationsTask.execute();
		}
	}
	
	/**
	 * Populates the rows with the event invitation data
	 */
	public void setInvitationRows() {
		TypedArray categoryIcons = getResources().obtainTypedArray(R.array.category_icons);
    	for(int i = 0; i<invitationsList.size(); i++){
    		final Invitation invitation = invitationsList.get(i);
        	View itemView = inflater.inflate(R.layout.event_rowview, null);
        	viewList.add(itemView);
        	RelativeLayout layout = (RelativeLayout) itemView.findViewById(R.id.event_rowview_innerlayout);
        	final int pos = i;

        	final TextView locationText = (TextView) itemView.findViewById(R.id.event_rowview_locationtext);
        	final TextView dateTimeText = (TextView) itemView.findViewById(R.id.event_rowview_datetimetext);
        	final TextView nameText = (TextView) itemView.findViewById(R.id.event_rowview_nametext);
        	final TextView nameLabel = (TextView) itemView.findViewById(R.id.event_rowview_namelabel);
        	final TextView scoreText = (TextView) itemView.findViewById(R.id.event_rowview_scoretext);
        	
        	locationText.setText(invitation.getLocationName());
        	dateTimeText.setText(invitation.getDateTime());
        	nameText.setText(invitation.getFounderName());
        	scoreText.setText(invitation.getScore()+" pts");
        	
        	DownloadPhotoThumbTask downloadPhotoThumbTask = new DownloadPhotoThumbTask();
        	downloadPhotoThumbTask.execute(i);
        	
        	layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					v.setBackgroundResource(R.drawable.menu_border_focused);
					locationText.setTextColor(getResources().getColor(R.color.text_white));
					nameText.setTextColor(getResources().getColor(R.color.text_white));
					scoreText.setTextColor(getResources().getColor(R.color.text_white));
					nameLabel.setTextColor(getResources().getColor(R.color.desc_text_selected));
					dateTimeText.setTextColor(getResources().getColor(R.color.desc_text_selected));
					radioButtonSelected(pos);
					selectedInvitation = invitation;
				}
			});
        	
        	TableLayout.LayoutParams tableRowParams=
        		  new TableLayout.LayoutParams
        		  (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
        	
        	int leftMargin = (int) getResources().getDimension(R.dimen.event_rowview_left);
        	int rightMargin = (int) getResources().getDimension(R.dimen.event_rowview_right);
        	int topMargin = (int) getResources().getDimension(R.dimen.event_rowview_top);
        	int bottomMargin = (int) getResources().getDimension(R.dimen.event_rowview_bottom);
        	
        	tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
        	tableLayout.addView(itemView, tableRowParams);
        }
	}
	
	/**
	 * Handles the selection of an invitation
	 * @param position - index of the event invitation
	 */
	private void radioButtonSelected(int position){
    	rightArrow.setVisibility(View.VISIBLE);
    	for(int i = 0; i<tableLayout.getChildCount(); i++){
    		View view = tableLayout.getChildAt(i);
    		RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.event_rowview_innerlayout);
    		TextView locationText = (TextView) view.findViewById(R.id.event_rowview_locationtext);
    		TextView nameText = (TextView) view.findViewById(R.id.event_rowview_nametext);
    		TextView scoreText = (TextView) view.findViewById(R.id.event_rowview_scoretext);
        	TextView dateTimeText = (TextView) view.findViewById(R.id.event_rowview_datetimetext);
        	TextView nameLabel = (TextView) view.findViewById(R.id.event_rowview_namelabel);
			RadioButton radioButton = (RadioButton) view.findViewById(R.id.event_rowview_radio);
    		if(i!=position){
    			radioButton.setChecked(false);
    			layout.setBackgroundResource(R.drawable.menu_border_selector);
    			locationText.setTextColor(getResources().getColor(R.color.text_gray));
    			nameText.setTextColor(getResources().getColor(R.color.text_gray));
    			scoreText.setTextColor(getResources().getColor(R.color.text_gray));
    			dateTimeText.setTextColor(getResources().getColor(R.color.desc_text_normal));
    			nameLabel.setTextColor(getResources().getColor(R.color.desc_text_normal));
    		}
    		else{
    			radioButton.setChecked(true);
    		}
    	}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_invitations, menu);
		return true;
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 15 && resultCode == 999){
        	InvitationsActivity.this.setResult(999);
        	InvitationsActivity.this.finish();
        }
    }

	/**
	 * Asynchronous task that gets the invitations of a user.
	 * @author Yunus Evren
	 */
	class GetInvitationsTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "GetInvitationsTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(InvitationsActivity.this)){
				Log.i(MainActivity.LOG, "GetInvitationsTask: Network unavailable");
				return false;
			}
			else{
				try{
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.GetInvitations?"
						+ "facebook_id=" + facebookId;
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
						Log.w(MainActivity.LOG, "GetInvitationsTask query returned no result.");
						return false;
					}

					for(String line : responseList){
						String[] tokens = line.split("\\|");
						invitationsList.add(new Invitation(Integer.parseInt(tokens[0]), tokens[1], tokens[2], 
								tokens[3], Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]),
								Integer.parseInt(tokens[6]), tokens[7]));
					}
					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetInvitationsTask Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetInvitationsTask MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetInvitationsTask IOException");
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Log.w(MainActivity.LOG, "GetInvitationsTask finished successfully.");
				if(invitationsList.size()>0){
					setInvitationRows();
				}
			}
			else{
				
			}
			loadingLayout.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Asynchronous task that downloads the thumbnail photo of an event location.
	 * @author Yunus Evren
	 */
	class DownloadPhotoThumbTask extends AsyncTask<Integer, Void, Boolean>{

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
			
			String photoURL = invitationsList.get(index).getPhotoURL();
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
					Log.i(MainActivity.LOG, "DownloadPhotoThumbTask: Photo downloaded successfully");
					bmp = ImageHelper.getRoundedCornerBitmap(bmp, 20);
					ImageView imageView = (ImageView) view.findViewById(R.id.event_rowview_image);
					imageView.setImageBitmap(bmp);
					imageView.setVisibility(View.VISIBLE);
				}
			}
			else{
				Log.i(MainActivity.LOG, "DownloadPhotoThumbTask download failed");
			}
			ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.event_rowview_progressbar);
			progressBar.setVisibility(View.GONE);
		}
	}
}
