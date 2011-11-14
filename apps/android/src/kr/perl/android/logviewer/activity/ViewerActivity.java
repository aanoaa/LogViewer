package kr.perl.android.logviewer.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.perl.android.logviewer.Constants;
import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.adapter.LogCursorAdapter;
import kr.perl.android.logviewer.helper.HttpHelper;
import kr.perl.android.logviewer.preference.LogPreference;
import kr.perl.android.logviewer.util.ContextUtil;
import kr.perl.android.logviewer.util.JSONUtils;
import kr.perl.provider.LogViewer.Logs;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class ViewerActivity extends ListActivity {
	
	private static final String TAG = "ViewerActivity";
	private static final String[] PROJECTION = new String[] { Logs._ID, Logs.CREATED_ON, Logs.NICKNAME, Logs.MESSAGE };
	private static final String SELECTION = "date(" + Logs.CREATED_ON + ", 'unixepoch', 'localtime') = ? AND " + Logs.CHANNEL + " = ?";
	private static final int DATE_DIALOG_ID = 0;
	private static final int GESTURE_REQUEST = 0;
	
	private String mChannel;
	private String mStrDate;
	private Cursor mCursor;
	private ListView mList;
	
	SharedPreferences mPrefs;
	
	public Map<String,?> createItem(String[] keys, String[] values) {
		Map<String,String> item = new HashMap<String,String>();
		for (int i=0; i<keys.length; i++) item.put(keys[i], values[i]);
		return item;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.viewer);
		mList = (ListView) findViewById(android.R.id.list);
		
		addHooks();
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mStrDate = mPrefs.getString(getString(R.string.pref_latest_date), new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())));
		mChannel = mPrefs.getString(getString(R.string.pref_channel), getString(R.string.pref_channel_default));
		changeDate(mStrDate);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setTitle(String.format(getString(R.string.title_format2), mStrDate, mChannel));
	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(Logs.CONTENT_URI, id);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri); // mimeType => vnd.android.cursor.item/vnd.kr.perl.log
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.pref_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mention:
			mention();
			
			break;
		case R.id.today:
			String strDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
			changeDate(strDate);
			
			break;
		case R.id.search:
			onSearchRequested();
			break;
		
		case R.id.favorite:
			onFavoriteRequested();
			break;
			
	    case R.id.settings:
	    	startActivity(new Intent(this, LogPreference.class));
	    	break;
	    	
	    case R.id.pick:
	    	showDialog(DATE_DIALOG_ID);
	    	break;
	    	
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	    
	    return true;
	}
	
	private void onFavoriteRequested() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Logs.CONTENT_URI);  // mimeType => vnd.android.cursor.dir/vnd.kr.perl.log
		startActivity(intent);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			int year = Integer.parseInt(mStrDate.substring(0, 4));
			int month = Integer.parseInt(mStrDate.substring(5, 7));
			int day = Integer.parseInt(mStrDate.substring(8, 10));
			return new DatePickerDialog(this,
					mDateSetListener,
					year, month - 1, day);
		}
		return null;
	}
     
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult");
		if (resultCode != RESULT_OK) return;
		Log.d(TAG, "result ok");
		switch (requestCode) {
		case GESTURE_REQUEST:
			Bundle bundle = data.getExtras();
			String gestureName = bundle.getString("gesture");
			Log.d(TAG, gestureName);
			if (gestureName.equals("go_top")) {
				ContextUtil.toast(this, "top"); // activity can run on UI thread only right?
				mList.setSelection(0);
			}
			else if (gestureName.equals("go_bottom")) {
				ContextUtil.toast(this, "bottom");
				mList.setSelection(mCursor.getCount());
			}
			else if (gestureName.equals("go_left")) {
				ContextUtil.toast(this, "prev day");
				try {
					changeDate(prevDay(mStrDate));
				} catch (ParseException e) {
					e.printStackTrace();
					ContextUtil.toast(ViewerActivity.this, getString(R.string.error_internal));
				}
			}
			else if (gestureName.equals("go_right")) {
				ContextUtil.toast(this, "next day");
				try {
					changeDate(nextDay(mStrDate));
				} catch (ParseException e) {
					e.printStackTrace();
					ContextUtil.toast(ViewerActivity.this, getString(R.string.error_internal));
				}
			}
			else if (gestureName.equals("triangle")) {
				ContextUtil.toast(this, "today");
				String strDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
				changeDate(strDate);
			}
			else if (gestureName.equals("circle")) {
				ContextUtil.toast(this, "refresh");
				refresh();
			}
			break;
		}
	}
	
	private void mention() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Logs.CONTENT_URI);
		String nickname = mPrefs.getString(getString(R.string.pref_nickname), "");
		if (nickname.equals("")) {
			ContextUtil.toast(this, getString(R.string.setting_nickname_first));
			return;
		}
		
		intent.putExtra(Logs.NICKNAME, nickname);
		intent.putExtra(Logs.CREATED_ON, mStrDate);
		startActivity(intent);
	}
	
	private DatePickerDialog.OnDateSetListener mDateSetListener =
		new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, 
				int monthOfYear, int dayOfMonth) {
			
			String strDate = new StringBuilder().append(year)
				.append("-")
				.append(String.format("%02d", monthOfYear + 1))
				.append("-")
				.append(String.format("%02d", dayOfMonth)).toString();
			
			changeDate(strDate);
		}
	};
	
	public String prevDay (String strDate, int day) throws ParseException {
		if (day <= 0) return strDate;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = format.parse(strDate);
		date.setTime(date.getTime() - ((long) 1000 * 60 * 60 * 24 * day));
		return format.format(date);
	}
	
	public String prevDay(String strDate) throws ParseException {
		return prevDay(strDate, 1);
	}
	
	public String nextDay (String strDate, int day) throws ParseException {
		if (day <= 0) return strDate;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = format.parse(strDate);
		date.setTime(date.getTime() + ((long) 1000 * 60 * 60 * 24 * day));
		return format.format(date);
	}
	
	public String nextDay(String strDate) throws ParseException {
		return nextDay(strDate, 1);
	}
	
	private void addHooks() {
		ImageButton button;
		// prev, top, bottom, next, today, refresh
		button = (ImageButton) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					changeDate(prevDay(mStrDate));
				} catch (ParseException e) {
					e.printStackTrace();
					ContextUtil.toast(ViewerActivity.this, getString(R.string.error_internal));
				}
			}
		});
		
		button = (ImageButton) findViewById(R.id.button2);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mList.setSelection(0);
			}
		});
		
		button = (ImageButton) findViewById(R.id.button3);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mList.setSelection(mCursor.getCount());
			}
		});
		
		button = (ImageButton) findViewById(R.id.button4);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					changeDate(nextDay(mStrDate));
				} catch (ParseException e) {
					e.printStackTrace();
					ContextUtil.toast(ViewerActivity.this, getString(R.string.error_internal));
				}
			}
		});
		
		button = (ImageButton) findViewById(R.id.button5);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String strDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
				changeDate(strDate);
			}
		});
		
		button = (ImageButton) findViewById(R.id.button6);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refresh();
			}
		});
		
		button = (ImageButton) findViewById(R.id.gesture_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ViewerActivity.this, GestureActivity.class);
				startActivityForResult(intent, GESTURE_REQUEST);
			}
		});
		
		getContentResolver().registerContentObserver(Logs.CONTENT_URI, true, new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
			}
		});
	}
    
    private void changeDate(String strDate) {
		mStrDate = strDate;
		setTitle(String.format(getString(R.string.title_format2), mStrDate, mChannel));

		mCursor = managedQuery(Logs.CONTENT_URI, PROJECTION, SELECTION, new String[] { mStrDate, mChannel }, null);
		if (mCursor.getCount() == 0) {
			getListView().setEmptyView(findViewById(android.R.id.empty));
		} else {
			LogCursorAdapter adapter = new LogCursorAdapter(
				ViewerActivity.this, 
				R.layout.log_row, 
				mCursor
			);
			
			getListView().setAdapter(adapter);
		}
		
		refresh();
	}
    
    private void refresh() {
		int latestEpoch = 0;
		if (mCursor.getCount() != 0) {
			mCursor.moveToLast();
			int index = mCursor.getColumnIndex(Logs.CREATED_ON);
			if (!mCursor.isNull(index)) latestEpoch = mCursor.getInt(index);
		}
		
		new LogUpdateTask().execute(buildUri(mChannel, mStrDate, latestEpoch));
	}
	
	private Uri buildUri(String channel, String strDate, int epoch) {
		String ymd[] = strDate.split("-");
		Uri uri = Constants.API_HOST.buildUpon().appendPath(channel).build();
		for (String path : ymd) {
			uri = uri.buildUpon().appendPath(path).build();
		}
		
		if (epoch != 0) uri = uri.buildUpon().appendEncodedPath(String.valueOf(epoch)).build();
		return uri;
	}
	
	private class LogUpdateTask extends AsyncTask<Uri, Void, Boolean> {
		
		private String mMessage;
		
    	protected void onPreExecute () {
    		setProgressBarIndeterminateVisibility(true);
    		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout1);
    		layout.setEnabled(false);
    		ImageButton button = (ImageButton) findViewById(R.id.button6);
    		button.setEnabled(false);
    	}
    	
    	protected Boolean doInBackground(Uri... uris) {
    		for (Uri uri : uris) {
    			if (ContextUtil.isOnline(getApplicationContext())) {
    				HttpResponse res = HttpHelper.query(uri);
    				if (res == null) {
    					Log.e(TAG, "Request failed");
    					return null;
    				}
    				
    				int status = res.getStatusLine().getStatusCode();
    				Log.d(TAG, "HttpResponse Status: " + status);
    				
    				if (status != HttpStatus.SC_OK) {
    					return null;
    				}

    				HttpEntity resEntity = res.getEntity();
    				String content = "";
    				try {
    					content = EntityUtils.toString(resEntity);
    				} catch (Exception e) {
    					e.printStackTrace();
    					return null;
    				}
    				
    				try {
    					JSONObject json = JSONUtils.toJSON(content);
    					JSONArray data = json.getJSONArray("data");
    					List<ContentValues> values = new ArrayList<ContentValues>();
    					for (int i=0; i<data.length(); i++) {
    						JSONArray row = data.getJSONArray(i);
    						String nickname = row.getString(0);
    						String created_on = row.getString(1);
    						String message = row.getString(2);
    						ContentValues value = new ContentValues();
    						value.put(Logs.CHANNEL, mChannel);
    						value.put(Logs.NICKNAME, nickname);
    						value.put(Logs.MESSAGE, message);
    						value.put(Logs.CREATED_ON, created_on);
    						values.add(value);
    					}
    					
    					if (values.size() > 0) {
    						ContentValues[] hidden = values.toArray(new ContentValues[values.size()]);
    						int count = getApplicationContext().getContentResolver().bulkInsert(Logs.CONTENT_URI, hidden);
    						mMessage = String.format(getString(R.string.notify_add_row), count);
    					} else {
    						mMessage = String.format(getString(R.string.log_uptodate));
    					}
    					
    					return true;
    				} catch (Exception e) {
    					return null;
    				}
    			} else {
    				return null;
    			}
    		}

    		return null;
    	}

    	protected void onPostExecute(Boolean flag) {
    		setProgressBarIndeterminateVisibility(false);
    		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout1);
    		layout.setEnabled(true);
    		if (flag == null) {
    			ContextUtil.toast(getApplicationContext(), getString(R.string.error_connection));
    			return;
    		}
    		
    		ContextUtil.toast(getApplicationContext(), mMessage);
    		
    		mCursor = managedQuery(Logs.CONTENT_URI, PROJECTION, SELECTION, new String[] { mStrDate, mChannel }, null);
			if (mCursor.getCount() > 0) {
				LogCursorAdapter adapter = new LogCursorAdapter(
	        		ViewerActivity.this, 
	        		R.layout.log_row, 
	        		mCursor 
	        	);

	        	getListView().setAdapter(adapter);
			} else {
				getListView().setEmptyView(findViewById(android.R.id.empty));
			}
    	}
    }
}
