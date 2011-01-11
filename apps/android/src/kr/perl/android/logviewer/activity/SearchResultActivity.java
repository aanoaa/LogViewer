package kr.perl.android.logviewer.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.adapter.SeparatedListAdapter;
import kr.perl.android.logviewer.provider.SearchHistoryProvider;
import kr.perl.android.logviewer.util.ContextUtil;
import kr.perl.provider.LogViewer.Logs;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.widget.SimpleAdapter;

public class SearchResultActivity extends ListActivity {
	
	private static final String TAG = "SearchResultActivity";
	private static final String[] PROJECTION = new String[] { Logs._ID, Logs.CREATED_ON, Logs.NICKNAME, Logs.MESSAGE };
	private static final String SELECTION = Logs.CHANNEL + " = ? AND " + Logs.NICKNAME + "!= ? AND " + Logs.MESSAGE + " like ?";
	
	private static final String[] MENTION_PROJECTION = new String[] { Logs._ID, Logs.CREATED_ON, Logs.NICKNAME, Logs.MESSAGE };
	private static final String MENTION_SELECTION = Logs.CHANNEL + " = ? AND " + Logs.NICKNAME + "!= ? AND " + Logs.MESSAGE + " like ? AND date(" + Logs.CREATED_ON + ", 'unixepoch', 'localtime') = ?";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		init();
		addHooks();
		handleIntent(getIntent());
	}
	
	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_SEARCH.equals(action)) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
		    	  SearchHistoryProvider.AUTHORITY, SearchHistoryProvider.MODE);
		    suggestions.saveRecentQuery(query, null);
		    setTitle(String.format(getString(R.string.title_format_search), query));
		    
		    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String channel = prefs.getString(getString(R.string.pref_channel), getString(R.string.pref_channel_default));
			String[] selectionArgs = new String[] { channel, "", "%" + query + "%" };
			
			Cursor cursor = managedQuery(Logs.CONTENT_URI, PROJECTION, SELECTION, selectionArgs, null);
			startManagingCursor(cursor);
			if (cursor.getCount() == 0) {
				ContextUtil.toast(this, String.format(getString(R.string.no_search_result, query)));
				finish();
				return;
			}
			
		    search(cursor);
		} else if (Intent.ACTION_VIEW.equals(action)) {
			Uri uri = intent.getData();
			String date = intent.getStringExtra("date");
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String channel = prefs.getString(getString(R.string.pref_channel), getString(R.string.pref_channel_default));
			String nickname = prefs.getString(getString(R.string.pref_nickname), "");
			if (nickname.equals("")) {
				ContextUtil.toast(this, getString(R.string.setting_nickname_first));
				finish();
				return;
			}
			
			String[] selectionArgs = new String[] { channel, "", "%" + nickname + "%", date };
			Cursor cursor = managedQuery(uri, MENTION_PROJECTION, MENTION_SELECTION, selectionArgs, null);
			startManagingCursor(cursor);
			if (cursor.getCount() == 0) {
				ContextUtil.toast(this, String.format(getString(R.string.no_mention_result), nickname));
				finish();
				return;
			}
			
			setTitle(String.format(getString(R.string.who_to_mention), nickname));
		    search(cursor);
		} else {
			Log.e(TAG, "Unknown action");
			finish();
			return;
		}
	}
	
	@Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
	
	private Map<String,?> createItem(String[] keys, String[] values) {
		Map<String,String> item = new HashMap<String,String>();
		for (int i=0; i<keys.length; i++) item.put(keys[i], values[i]);
		return item;
	}
	
	private void search(Cursor cursor) {
		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		List<Map<String,?>> category = null;
		String prevDate = "";
		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndex(Logs._ID));
				long created_on = cursor.getInt(cursor.getColumnIndex(Logs.CREATED_ON));
				Date d = new Date((long) created_on * 1000);
				String date = new SimpleDateFormat("yyyy-MM-dd").format(d);
				String time = new SimpleDateFormat("HH:mm").format(d);
				String nickname = cursor.getString(cursor.getColumnIndex(Logs.NICKNAME));
				String message = cursor.getString(cursor.getColumnIndex(Logs.MESSAGE));
				
				if (cursor.isFirst() || !prevDate.equals(date)) {
					category = new LinkedList<Map<String,?>>();
				}
				
				category.add(createItem(PROJECTION, new String[] { new StringBuilder().append(id).toString(), time, nickname, message }));	
				
				String nextDate = "";
				if (cursor.isLast()) {
					adapter.addSection(date, new SimpleAdapter(this, category, R.layout.list_complex, new String[] { Logs.NICKNAME, Logs.MESSAGE }, new int[] { R.id.list_complex_title, R.id.list_complex_caption }));
				} else {
					if (cursor.moveToNext()) {
						nextDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date((long) cursor.getInt(cursor.getColumnIndex(Logs.CREATED_ON)) * 1000));
						cursor.moveToPrevious();
					}
					
					if (!nextDate.equals(date)) {
						adapter.addSection(date, new SimpleAdapter(this, category, R.layout.list_complex, new String[] { Logs.NICKNAME, Logs.MESSAGE }, new int[] { R.id.list_complex_title, R.id.list_complex_caption }));
					}
				}
				
				prevDate = date;
			} while (cursor.moveToNext());
		}
		
		getListView().setAdapter(adapter);
	}
	
	private void init() {
	}
    
    private void addHooks() {
	}
}
