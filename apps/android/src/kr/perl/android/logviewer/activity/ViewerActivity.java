package kr.perl.android.logviewer.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kr.perl.android.logviewer.Constants;
import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.adapter.LogAdapter;
import kr.perl.android.logviewer.preference.LogPreference;
import kr.perl.android.logviewer.thread.SyncThread;
import kr.perl.android.logviewer.util.ContextUtil;
import kr.perl.android.logviewer.util.StringUtil;
import kr.perl.provider.LogViewer.Logs;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
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
		init();
		addHooks();
		handleIntent(getIntent());
	}
	
	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mStrDate = mPrefs.getString(getString(R.string.pref_latest_date), new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())));
		mChannel = mPrefs.getString(getString(R.string.pref_channel), getString(R.string.pref_channel_default));
		if (Intent.ACTION_MAIN.equals(action)) {
			mCursor = managedQuery(Logs.CONTENT_URI, PROJECTION, SELECTION, new String[] { mStrDate, mChannel }, null);
	    	startManagingCursor(mCursor);
	    	setListAdapter(getAdapter());
			refresh();
			mList.setSelection(mPrefs.getInt(getString(R.string.pref_latest_position), 0));
		} else if (Intent.ACTION_VIEW.equals(action)) {
			mStrDate = intent.getStringExtra("strDate");
			mChannel = intent.getStringExtra("channel");
			Log.d(TAG, "mStrDate:" + mStrDate);
			Log.d(TAG, "mChannel:" + mChannel);
			int id = Integer.parseInt(intent.getStringExtra("id"));
			mCursor = managedQuery(Logs.CONTENT_URI, PROJECTION, SELECTION, new String[] { mStrDate, mChannel }, null);
			startManagingCursor(mCursor);
	    	setListAdapter(getAdapter());
	    	refresh();
	    	
	    	if (mCursor.moveToFirst()) {
	    		do {
	    			int _id = mCursor.getInt(mCursor.getColumnIndex(Logs._ID));
	    			if (id == _id) {
	    				mList.setSelection(mCursor.getPosition());
	    				break;
	    			}
	    		} while (mCursor.moveToNext());
	    	}
		} else {
			Log.e(TAG, "Unknown action");
			finish();
			return;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setTitle(String.format(getString(R.string.title_format2), mStrDate, mChannel));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		int latest_position = mList.getLastVisiblePosition() - 10;
		
		if (latest_position < 0) latest_position = 0;
		mPrefs.edit().putInt(getString(R.string.pref_latest_position), latest_position).commit();
		mPrefs.edit().putString(getString(R.string.pref_latest_date), mStrDate).commit();
	}
	
	@Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(Logs.CONTENT_URI, id);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
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
			refresh();
			
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
		Intent intent = new Intent(Intent.ACTION_VIEW, Logs.CONTENT_URI);
		intent.putExtra("isFavorite", true);
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
					refresh();
				} catch (ParseException e) {
					e.printStackTrace();
					ContextUtil.toast(ViewerActivity.this, getString(R.string.error_internal));
				}
			}
			else if (gestureName.equals("go_right")) {
				ContextUtil.toast(this, "next day");
				try {
					changeDate(nextDay(mStrDate));
					refresh();
				} catch (ParseException e) {
					e.printStackTrace();
					ContextUtil.toast(ViewerActivity.this, getString(R.string.error_internal));
				}
			}
			else if (gestureName.equals("triangle")) {
				ContextUtil.toast(this, "today");
				String strDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
				changeDate(strDate);
				refresh();
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
		intent.putExtra("date", mStrDate);
		startActivity(intent);
	}
	
	private void sync(final Uri uri, final String channel) {
		Thread thread = new SyncThread(this, uri, channel);
		thread.start();
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
			refresh();
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
	
	private void init() {
		mList = (ListView) findViewById(android.R.id.list);
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
					refresh();
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
					refresh();
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
				refresh();
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
		if (mStrDate.equals(strDate)) {
			return;
		}
		
		mStrDate = strDate;
		setTitle(String.format(getString(R.string.title_format2), mStrDate, mChannel));
		mCursor = managedQuery(Logs.CONTENT_URI, PROJECTION, SELECTION, new String[] { mStrDate, mChannel }, null);
		setListAdapter(getAdapter());
	}
    
    private LogAdapter getAdapter() {
		return new LogAdapter(
				this, 
				R.layout.log_row, 
				mCursor, 
				new String[] { Logs.CREATED_ON, Logs.NICKNAME, Logs.MESSAGE }, 
				new int[] { R.id.text1, R.id.text2, R.id.text3 }
		);
	}
	
	private void refresh() {
		if (SyncThread.isQuery()) {
			mList.setSelection(mCursor.getCount());
			return;
		}
		
		int latestEpoch = 0;
		if (mCursor.getCount() != 0) {
			mCursor.moveToLast();
			int index = mCursor.getColumnIndex(Logs.CREATED_ON);
			if (!mCursor.isNull(index)) latestEpoch = mCursor.getInt(index);
		}
		
		sync(buildUri(mChannel, mStrDate, latestEpoch), mChannel);
	}
	
	private Uri buildUri(String channel, String strDate, int epoch) {
		String ymd[] = strDate.split("-");
		ArrayList<String> pieces = new ArrayList<String>(ymd.length);
		for (String item : ymd) {
			pieces.add(item);
		}
		
		String path = channel + "/" + StringUtil.join(pieces, "/");
		if (epoch != 0) {
			path += "/" + epoch;
		}
		
		return Uri.parse(Constants.LOG_SERVER_DOMAIN + path);
	}
}
