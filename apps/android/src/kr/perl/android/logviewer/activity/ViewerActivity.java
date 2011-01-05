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
import kr.perl.android.logviewer.provider.SearchHistoryProvider;
import kr.perl.android.logviewer.thread.SyncThread;
import kr.perl.android.logviewer.util.ContextUtil;
import kr.perl.android.logviewer.util.StringUtil;
import kr.perl.provider.LogViewer.Logs;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ViewerActivity extends ListActivity {
	
	private static final String TAG = "ViewerActivity";
	private static final String[] PROJECTION = new String[] { Logs._ID, Logs.CREATED_ON, Logs.NICKNAME, Logs.MESSAGE };
	private static final String SELECTION = "date(" + Logs.CREATED_ON + ", 'unixepoch', 'localtime') = ? and " + Logs.CHANNEL + " = ?";
	
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
		handleIntent(getIntent());
		init();
		addHooks();
		setContent();
		refresh();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String channel = prefs.getString(getString(R.string.pref_channel), getString(R.string.pref_channel_default));
		if (!channel.equals(mChannel)) {
			mChannel = channel;
			setTitle(String.format(getString(R.string.title_format2), mStrDate, mChannel));
			setContent();
		}
	}
	
	@Override
	public boolean onSearchRequested() {
	    // pause some stuff here
		return super.onSearchRequested();
	}
	
	@Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
	
	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_VIEW.equals(action)) {
			mStrDate = intent.getStringExtra(Constants.KEY_YMD);
			mChannel = intent.getStringExtra(Constants.KEY_CHANNEL);
			if (mStrDate == null || !isValidDate(mStrDate) || mChannel == null) {
				Log.e(TAG, "Invalid argument");
				finish();
				return;
			}
		} else if (Intent.ACTION_MAIN.equals(action)) {
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
		    	  SearchHistoryProvider.AUTHORITY, SearchHistoryProvider.MODE);
		    suggestions.saveRecentQuery(query, null);
		    search(query);
		} else {
			Log.e(TAG, "Unknown action");
			finish();
			return;
		}
		
		if (getIntent().getData() == null) getIntent().setData(Logs.CONTENT_URI);
		if (mStrDate == null) mStrDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
		if (mChannel == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		    mChannel = prefs.getString(getString(R.string.pref_channel), getString(R.string.pref_channel_default));
		}
	}
	
	private void search(String query) {
		Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
		// 검색해서 결과를 새로 보여줘야 한다.
	}
	
	@Override
	public void onPause() {
		super.onPause();
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
	    case R.id.list:
	    	// FIXME: 이제 슬슬 구현해볼까
	    	Intent intent = new Intent();
	    	intent.setAction(Intent.ACTION_GET_CONTENT);
	    	intent.setDataAndType(Logs.CONTENT_URI, Logs.CONTENT_TYPE);
	    	intent.addCategory(Intent.CATEGORY_DEFAULT);
	    	try {
	    		startActivity(intent);
	    	} catch(Exception e) {
	    		ContextUtil.toast(this, "Cannot found activity");
	    	}
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
    
    private void setContent() {
    	setTitle(String.format(getString(R.string.title_format2), mStrDate, mChannel));
    	mCursor = managedQuery(getIntent().getData(), PROJECTION, SELECTION, new String[] { mStrDate, mChannel }, null);
		SimpleCursorAdapter adapter = new LogAdapter(
				this,
				R.layout.log_row, 
				mCursor, 
				new String[] { Logs.CREATED_ON, Logs.NICKNAME, Logs.MESSAGE }, 
				new int[] { R.id.text1, R.id.text2, R.id.text3 }
		);
			
		setListAdapter(adapter);
    }
	
	private void init() {
		mList = (ListView) findViewById(android.R.id.list);
	}
	
	private void refresh() {
		mList.setSelection(mCursor.getCount()); // refresh 누르면 바닥으로..
		if (SyncThread.isQuery()) return;
		if (mCursor.getCount() != 0) {
			mCursor.moveToLast();
			int index = mCursor.getColumnIndex(Logs.CREATED_ON);
			if (!mCursor.isNull(index)) mLatestEpoch = mCursor.getInt(index);
		}
		
		sync(buildUri(mChannel, mStrDate, mLatestEpoch), mChannel);
	}
	
	private Uri buildUri(String channel, String strDate, int epoch) {
		// FIXME: UriMatcher 추가하고 URI 로 해결하자
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
		
		getContentResolver().registerContentObserver(Logs.CONTENT_URI, true, new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				mList.setSelection(mCursor.getCount());
			}
		});
	}
}
