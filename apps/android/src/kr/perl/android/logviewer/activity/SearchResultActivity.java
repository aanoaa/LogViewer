package kr.perl.android.logviewer.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.adapter.LogSimpleAdapter;
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
import android.view.View;
import android.widget.ListView;

public class SearchResultActivity extends ListActivity {
	
	private static final String TAG = "SearchResultActivity";
	private static final String[] PROJECTION = new String[] { Logs._ID, Logs.CHANNEL, Logs.CREATED_ON, Logs.NICKNAME, Logs.MESSAGE, Logs.FAVORITE };
	
	private static final String SELECTION = Logs.CHANNEL + " = ? AND " + Logs.NICKNAME + "!= ? AND " + Logs.MESSAGE + " like ?";
	private static final String MENTION_SELECTION = Logs.CHANNEL + " = ? AND " + Logs.NICKNAME + "!= ? AND " + Logs.MESSAGE + " like ? AND date(" + Logs.CREATED_ON + ", 'unixepoch', 'localtime') = ?";
	private static final String FAVORITE_SELECTION = Logs.FAVORITE + " != ?";
	
	private static final String TIME_FORMAT = "HH:MM";
	private static final String[] ADD_PROJECTION = new String[] { Logs._ID, Logs.CHANNEL, TIME_FORMAT, Logs.CREATED_ON, Logs.NICKNAME, Logs.MESSAGE, Logs.FAVORITE };
	
	private Cursor mCursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		init();
		addHooks();
		handleIntent(getIntent());
	}
	
	@SuppressWarnings("unchecked")
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		if (l.getAdapter().getItemViewType(position) != SeparatedListAdapter.TYPE_SECTION_HEADER) {
			Map<String, String> map = (HashMap<String, String>) l.getAdapter().getItem(position);
			Date d = new Date((long) Integer.parseInt(map.get(Logs.CREATED_ON)) * 1000);
			String date = new SimpleDateFormat("yyyy-MM-dd").format(d);
			Intent intent = new Intent(this, ViewerActivity.class);
			intent.setAction(Intent.ACTION_VIEW);
			intent.putExtra("id", new StringBuilder().append(map.get(Logs._ID)).toString());
			intent.putExtra("strDate", date);
			intent.putExtra("channel", map.get(Logs.CHANNEL));
			startActivity(intent);
		} else {
			// TODO: Make Toast or Something Output for User.
		}
	}
	
	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		// TODO: AsyncTask
		if (Intent.ACTION_SEARCH.equals(action)) {
			// search
			String query = intent.getStringExtra(SearchManager.QUERY);
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
		    	  SearchHistoryProvider.AUTHORITY, SearchHistoryProvider.MODE);
		    suggestions.saveRecentQuery(query, null);
		    setTitle(String.format(getString(R.string.title_format_search), query));
		    
		    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String channel = prefs.getString(getString(R.string.pref_channel), getString(R.string.pref_channel_default));
			String[] selectionArgs = new String[] { channel, "", "%" + query + "%" };
			int result = search(Logs.CONTENT_URI, PROJECTION, SELECTION, selectionArgs, null);
			if (result == -1) {
				ContextUtil.toast(this, getString(R.string.error_internal));
				finish();
				return;
			} else if (result == 0) {
				ContextUtil.toast(this, String.format(getString(R.string.no_search_result, query)));
				finish();
				return;
			}
		} else if (Intent.ACTION_VIEW.equals(action)) {
			if (Logs.CONTENT_URI.equals(intent.getData())) {
				if (intent.getStringExtra(Logs.NICKNAME) == null) {
					// Favorite
				} else {
					// mention
					String nickname = intent.getStringExtra(Logs.NICKNAME);
					setTitle(String.format(getString(R.string.who_to_mention), nickname));
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
					String channel = prefs.getString(getString(R.string.pref_channel), getString(R.string.pref_channel_default));
					String[] selectionArgs = new String[] { channel, "", "%" + nickname + "%", intent.getStringExtra(Logs.CREATED_ON) };
					
					int result = search(intent.getData(), PROJECTION, MENTION_SELECTION, selectionArgs, null);
					if (result == -1) {
						ContextUtil.toast(this, getString(R.string.error_internal));
						finish();
						return;
					} else if (result == 0) {
						ContextUtil.toast(this, String.format(getString(R.string.no_mention_result), nickname));
						finish();
						return;
					}
				}
				
			} else {
				// one item
				int result = search(intent.getData(), PROJECTION, null, null, null);
				if (result <= 0) {
					ContextUtil.toast(this, getString(R.string.error_internal));
					finish();
					return;
				}
				
				String channel = mCursor.getString(mCursor.getColumnIndex(Logs.CHANNEL));
				long created_on = mCursor.getInt(mCursor.getColumnIndex(Logs.CREATED_ON));
				Date d = new Date((long) created_on * 1000);
				String datetime = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(d);
				setTitle(String.format("%s %s", channel, datetime));
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
		Intent intent = getIntent();
		if (Logs.CONTENT_URI.equals(intent.getData())) {
			if (intent.getStringExtra(Logs.NICKNAME) == null) {
				setTitle(getString(R.string.favorite));
				int result = search(intent.getData(), PROJECTION, FAVORITE_SELECTION, new String[] { "0" }, null);
				if (result == -1) {
					ContextUtil.toast(this, getString(R.string.error_internal));
					finish();
					return;
				} else if (result == 0) {
					ContextUtil.toast(this, getString(R.string.error_no_favorite));
					finish();
					return;
				}
			}
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
	
	/*
	 * [Return Value]
	 * -1: search failed
	 *  0: result count is 0
	 *  N: search rows
	 */
	private int search(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
		mCursor = managedQuery(uri, projection, selection, selectionArgs, orderBy);
		startManagingCursor(mCursor);
		if (mCursor.getCount() == 0) {
			return 0;
		}
		
		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		List<Map<String,?>> category = null;
		String prevDate = "";
		if (mCursor.moveToFirst()) {
			do {
				int id = mCursor.getInt(mCursor.getColumnIndex(Logs._ID));
				long created_on = mCursor.getInt(mCursor.getColumnIndex(Logs.CREATED_ON));
				Date d = new Date((long) created_on * 1000);
				String channel = mCursor.getString(mCursor.getColumnIndex(Logs.CHANNEL));
				String date = new SimpleDateFormat("yyyy-MM-dd").format(d);
				String time = new SimpleDateFormat("HH:mm").format(d);
				String nickname = mCursor.getString(mCursor.getColumnIndex(Logs.NICKNAME));
				String message = mCursor.getString(mCursor.getColumnIndex(Logs.MESSAGE));
				boolean isFavorite = mCursor.getInt(mCursor.getColumnIndex(Logs.FAVORITE)) != 0;
				
				if (mCursor.isFirst() || !prevDate.equals(date)) {
					category = new LinkedList<Map<String,?>>();
				}
				
				category.add(createItem(ADD_PROJECTION, new String[] {
						new StringBuilder().append(id).toString(), 
						channel, 
						time, 
						new StringBuilder().append(created_on).toString(), 
						nickname, 
						message, 
						new StringBuilder().append(isFavorite).toString()
				}));
				
				String nextDate = "";
				if (mCursor.isLast()) {
					adapter.addSection(date, new LogSimpleAdapter(this, category, R.layout.list_complex, null, null)); // from, to 가 의미없음
				} else {
					if (mCursor.moveToNext()) {
						nextDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date((long) mCursor.getInt(mCursor.getColumnIndex(Logs.CREATED_ON)) * 1000));
						mCursor.moveToPrevious();
					}
					
					if (!nextDate.equals(date)) {
						adapter.addSection(date, new LogSimpleAdapter(this, category, R.layout.list_complex, null, null)); // from, to 가 의미없음
					}
				}
				
				prevDate = date;
			} while (mCursor.moveToNext());
		}
		
		getListView().setAdapter(adapter);
		
		mCursor.moveToFirst();
		return mCursor.getCount();
	}
	
	private void init() {
	}
    
    private void addHooks() {
	}
}
