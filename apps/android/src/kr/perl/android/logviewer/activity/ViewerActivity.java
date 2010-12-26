package kr.perl.android.logviewer.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.adapter.LogAdapter;
import kr.perl.android.logviewer.provider.LogProvider;
import kr.perl.android.logviewer.schema.LogSchema;
import kr.perl.android.logviewer.thread.SyncThread;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ViewerActivity extends ListActivity {
	
	private static final String TAG = "ViewerActivity";
	public static final String KEY_DATE = "date";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.viewer);
		if (isOnline()) sync();
		init();
		addHooks();
	}
	
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	private void sync() {
		new SyncThread(this).run();
	}
	
	private void init() {
		String strDate = getIntent().getStringExtra(KEY_DATE);
		if (strDate == null || isValidDate(strDate)) {
			strDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
			strDate = "2010-12-10";
		}
		
		setTitle(String.format(getString(R.string.title_format1), strDate));
		
		String selection = "date(" + LogSchema.CREATED_ON + ", 'unixepoch') = ?";
		String[] selectionArgs = new String[] { strDate };
		Cursor c = managedQuery(LogSchema.CONTENT_URI, LogProvider.PROJECTION, selection, selectionArgs, null);
		if (c.getCount() == 0) {
			setEmptyContent();
			return;
		}
		
		setContent(c);
	}
	
	private void setEmptyContent() {
		setListAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new String [] { getString(R.string.error_no_log) }));
	}
	
	private void setContent(Cursor cursor) {
		SimpleCursorAdapter adapter = new LogAdapter(
			getApplicationContext(), 
			R.layout.log_row, 
			cursor, 
			new String[] {LogSchema.CREATED_ON, LogSchema.NICKNAME, LogSchema.MESSAGE}, 
			new int[] {R.id.text1, R.id.text2, R.id.text3}
		);
		
		adapter.notifyDataSetChanged();
		setListAdapter(adapter);
	}
	
	private void addHooks() {
		// nothing
	}
	
	@Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
    }
	
	/*
	 * FIXME:
	 * 	MMdd 에 대해 더 엄격한 유효성 검사가 필요함
	 * 	MM은 12 이상일 수 없고, dd 도 음수나 32 이상이 될 수 없기에..
	 */
	private boolean isValidDate(String strDate) {
		Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
		Matcher m = pattern.matcher(strDate);
		if (m.find()) {
			return true;
		}
	
		Log.w(TAG, "invalid " + strDate + " set to date as today");
		return false;
	}
}