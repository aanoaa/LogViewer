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
		init();
		addHooks();
	}
	
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	private void sync(final SimpleCursorAdapter adapter, final String date, final int latest_epoch) {
		new SyncThread(this, adapter, date, latest_epoch).run();
	}
	
	private void init() {
		String strDate = getIntent().getStringExtra(KEY_DATE);
		if (strDate == null || isValidDate(strDate)) {
			strDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
		}
		
		setTitle(String.format(getString(R.string.title_format1), strDate));
		
		String selection = "date(" + LogSchema.CREATED_ON + ", 'unixepoch', 'localtime') = ?";
		String[] selectionArgs = new String[] { strDate };
		Cursor c = managedQuery(LogSchema.CONTENT_URI, LogProvider.PROJECTION, selection, selectionArgs, null);
		int created_on = 0;
		if (c.getCount() != 0) {
			c.moveToLast();
			int index = c.getColumnIndex(LogSchema.CREATED_ON);
			if (!c.isNull(index)) created_on = c.getInt(index);
		}
		
		SimpleCursorAdapter adapter = new LogAdapter(
			getApplicationContext(), 
			R.layout.log_row, 
			c, 
			new String[] {LogSchema.CREATED_ON, LogSchema.NICKNAME, LogSchema.MESSAGE}, 
			new int[] {R.id.text1, R.id.text2, R.id.text3}
		);
			
		adapter.notifyDataSetChanged();
		setListAdapter(adapter);
		
		if (isOnline()) sync(adapter, strDate, created_on);
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