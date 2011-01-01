package kr.perl.android.logviewer.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.perl.android.logviewer.Constants;
import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.adapter.LogAdapter;
import kr.perl.android.logviewer.preference.LogPreference;
import kr.perl.android.logviewer.provider.LogProvider;
import kr.perl.android.logviewer.schema.LogSchema;
import kr.perl.android.logviewer.thread.SyncThread;
import kr.perl.android.logviewer.util.ContextUtil;
import kr.perl.android.logviewer.util.StringUtil;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ViewerActivity extends ListActivity {
	
	private static final String TAG = "ViewerActivity";
	
	private String mChannel;
	private String mStrDate;
	private int mLatestEpoch;
	private Cursor mCursor;
	private ListView mList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.viewer);
		init();
		addHooks();
		refresh();
	}
	
	@Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
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
	    case R.id.settings:
	    	startActivity(new Intent(getApplicationContext(), LogPreference.class));
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	private void sync(final Uri uri, final String channel) {
		Thread thread = new SyncThread(this, uri, channel);
		thread.start();
	}
	
	/*
     * FIXME:
     *      MMdd 에 대해 더 엄격한 유효성 검사가 필요함
     *      MM은 12 이상일 수 없고, dd 도 음수나 32 이상이 될 수 없기에..
     */
    private boolean isValidDate(String strDate) {
    	Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    	Matcher m = pattern.matcher(strDate);
    	if (m.find()) {
    		return true;
    	}
    
    	return false;
    }
	
	private void init() {
		Intent intent = getIntent();
		mList = (ListView) findViewById(android.R.id.list);
		mStrDate = intent.getStringExtra(Constants.KEY_YMD);
		mChannel = intent.getStringExtra(Constants.KEY_CHANNEL);
		if (mStrDate == null || !isValidDate(mStrDate)) {
			Log.w(TAG, "invalid " + mStrDate + " set to date as today");
            mStrDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
		}
		
		if (mChannel == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			mChannel = prefs.getString(getString(R.string.pref_channel), getString(R.string.pref_channel_default));
		}
		
		setTitle(String.format(getString(R.string.title_format2), mStrDate, mChannel));
		mCursor = getLogCursor(mChannel, mStrDate, null);
		mLatestEpoch = 0;
		
		SimpleCursorAdapter adapter = getAdapter(mCursor);
		adapter.notifyDataSetChanged();
		setListAdapter(adapter);
	}
	
	private void refresh() {
		if (mCursor.getCount() != 0) {
			mCursor.moveToLast();
			int index = mCursor.getColumnIndex(LogSchema.CREATED_ON);
			if (!mCursor.isNull(index)) mLatestEpoch = mCursor.getInt(index);
		}
		
		if (ContextUtil.isOnline(this)) {
			sync(buildUri(mChannel, mStrDate, mLatestEpoch), mChannel);
		}
		
		mList.setSelection(mCursor.getCount()); // insert 된 row 가 없어도 마지막으로 보내주자
	}
	
	private Cursor getLogCursor(String channel, String strDate, String orderBy) {
		String selection = "date(" + LogSchema.CREATED_ON + ", 'unixepoch', 'localtime') = ? and " + LogSchema.CHANNEL + " = ?";
		String[] selectionArgs = new String[] { strDate, channel };
		return managedQuery(LogSchema.CONTENT_URI, LogProvider.PROJECTION, selection, selectionArgs, orderBy);
	}
	
	private SimpleCursorAdapter getAdapter(Cursor cursor) {
		return new LogAdapter(
			this, 
			R.layout.log_row, 
			cursor, 
			new String[] { LogSchema.CREATED_ON, LogSchema.NICKNAME, LogSchema.MESSAGE }, 
			new int[] { R.id.text1, R.id.text2, R.id.text3 }
		);
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
	
	private void addHooks() {
		Button button;
		button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mList.setSelection(0);
			}
		});
		
		button = (Button) findViewById(R.id.button2);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refresh();
			}
		});
		
		getContentResolver().registerContentObserver(LogSchema.CONTENT_URI, true, new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				mList.setSelection(mCursor.getCount());
			}
		});
		
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals(getString(R.string.pref_channel))) {
					String channel = sharedPreferences.getString(getString(R.string.pref_channel), getString(R.string.pref_channel_default));
					if (mChannel.equals(channel)) return;
					// do something
				}
			}
		});
	}
}