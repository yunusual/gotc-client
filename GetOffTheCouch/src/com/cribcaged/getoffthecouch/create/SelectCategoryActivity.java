package com.cribcaged.getoffthecouch.create;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.cribcaged.getoffthecouch.MainActivity;
import com.cribcaged.getoffthecouch.R;
import com.cribcaged.getoffthecouch.entities.Category;
import com.cribcaged.getoffthecouch.util.NetworkManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which asks the user to select a category.
 * @author Yunus Evren
 */
public class SelectCategoryActivity extends Activity {
	
	private String userId;
	private String fullName;

	private ImageView rightArrow;
	private ImageView leftArrow;
	
	private TableLayout tableLayout;
	private LayoutInflater inflater;
	
	private ArrayList<Category> categoryList;
	
	private LinearLayout loadingLayout;
	
	private Category selectedCategory;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_select_category);
        
        if (customTitleSupported) {
        	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        }
        
        Bundle bundle = getIntent().getExtras();
        userId = bundle.getString("user_id");
        fullName = bundle.getString("full_name");
        
        selectedCategory = null;
        
        rightArrow = (ImageView) this.findViewById(R.id.selectcategory_rightarrowimage);
        rightArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(selectedCategory!=null){
					Intent intent = new Intent(SelectCategoryActivity.this, SelectLocationActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("user_id", userId);
					bundle.putString("full_name", fullName);
					bundle.putInt("cat_id", selectedCategory.getId());
					bundle.putString("cat_name", selectedCategory.getName());
					intent.putExtras(bundle);
					SelectCategoryActivity.this.startActivityForResult(intent, 20);
				}
			}
		});
        rightArrow.setVisibility(View.INVISIBLE);
        
        leftArrow = (ImageView) this.findViewById(R.id.selectcategory_leftarrowimage);
        leftArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SelectCategoryActivity.this.finish();
			}
		});
        
        
        tableLayout = (TableLayout) this.findViewById(R.id.selectcategory_tablelayout);
        inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        
        loadingLayout = (LinearLayout) this.findViewById(R.id.selectcategory_loadinglayout);
        
        categoryList = new ArrayList<Category>();
        
        GetCategoriesTask getCategoriesTask = new GetCategoriesTask();
        getCategoriesTask.execute();
        
    }
    
    /**
     * Handles the selection of a radio button
     * @param position - index of the selected radio button
     */
    private void radioButtonSelected(int position){
    	rightArrow.setVisibility(View.VISIBLE);
    	for(int i = 0; i<tableLayout.getChildCount(); i++){
    		View view = tableLayout.getChildAt(i);
    		RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.category_rowview_innerlayout);
    		TextView text = (TextView) view.findViewById(R.id.category_rowview_text);
			RadioButton radioButton = (RadioButton) view.findViewById(R.id.category_rowview_radio);
    		if(i!=position){
    			radioButton.setChecked(false);
    			layout.setBackgroundResource(R.drawable.menu_border_selector);
				text.setTextColor(getResources().getColor(R.color.text_gray));
    		}
    		else{
    			radioButton.setChecked(true);
    		}
    	}
    }
    
    /**
     * Populates the rows according to the received category info
     */
    public void setCategoryRows() {
    	TypedArray categoryIcons = getResources().obtainTypedArray(R.array.category_icons);
    	for(int i = 0; i<categoryList.size(); i++){
    		final Category category = categoryList.get(i);
        	View itemView = inflater.inflate(R.layout.category_rowview, null);
        	RelativeLayout layout = (RelativeLayout) itemView.findViewById(R.id.category_rowview_innerlayout);
        	final int pos = i;

        	final TextView text = (TextView) itemView.findViewById(R.id.category_rowview_text);
        	ImageView image = (ImageView) itemView.findViewById(R.id.category_rowview_image);
        	image.setImageDrawable(categoryIcons.getDrawable(category.getId()-1));
        	text.setText(category.getName());
        	
        	layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					v.setBackgroundResource(R.drawable.menu_border_focused);
					text.setTextColor(getResources().getColor(R.color.text_white));
					radioButtonSelected(pos);
					selectedCategory = category;
				}
			});
        	
        	TableLayout.LayoutParams tableRowParams=
        		  new TableLayout.LayoutParams
        		  (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
        	
        	int leftMargin = (int) getResources().getDimension(R.dimen.category_rowview_left);
        	int rightMargin = (int) getResources().getDimension(R.dimen.category_rowview_right);
        	int topMargin = (int) getResources().getDimension(R.dimen.category_rowview_top);
        	int bottomMargin = (int) getResources().getDimension(R.dimen.category_rowview_bottom);
        	
        	tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
        	tableLayout.addView(itemView, tableRowParams);
        }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_createevent, menu);
        return true;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 999){
        	SelectCategoryActivity.this.setResult(999);
        	SelectCategoryActivity.this.finish();
        }
    }
    
    /**
     * Asynchronous task that gets the categories from the server.
     * @author Yunus Evren
     */
    class GetCategoriesTask extends AsyncTask<Void, Void, Boolean>{
    	
		@Override
		protected void onPreExecute() 
		{
			Log.i(MainActivity.LOG, "GetCategoriesTask executed");
			super.onPreExecute();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... unused) {
			if(!NetworkManager.isNetworkAvailable(SelectCategoryActivity.this)){
				Log.i(MainActivity.LOG, "GetCategoriesTask: Network unavailable");
				return false;
			}
			else{
				try{
					String link = "http://50.7.195.130:8080/gotc/servlet/com.cribcaged.getoffthecouch.server.GetCategories";
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
						Log.w(MainActivity.LOG, "GetCategoriesTask query returned no result.");
						return false;
					}
					
					for(String line : responseList){
						String[] tokens = line.split("\\|");
						categoryList.add(new Category(Integer.parseInt(tokens[0]), tokens[1]));
					}
					return true;
				}catch(NullPointerException e){
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetCategoriesTask Null Pointer Exception");
					return false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetCategoriesTask MalformedURLException");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.w(MainActivity.LOG, "GetCategoriesTask IOException");
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				if(categoryList.size()>0){
					setCategoryRows();
					loadingLayout.setVisibility(View.GONE);
				}
			}
			else{
				
			}
		}
	}
    
}
