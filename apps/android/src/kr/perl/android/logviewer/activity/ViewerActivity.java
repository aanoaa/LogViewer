package kr.perl.android.logviewer.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.adapter.LogAdapter;
import kr.perl.android.logviewer.provider.LogProvider;
import kr.perl.android.logviewer.schema.LogSchema;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ViewerActivity extends ListActivity {
	
	public static final String KEY_DATE = "date";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer);
		init();
		addHooks();
	}
	
	private void init() {
		String strDate = getIntent().getStringExtra(KEY_DATE);
		if (strDate == null) {
			strDate = new SimpleDateFormat("yyyyMMdd").format(new Date(System.currentTimeMillis()));
		}
		
		// 로그에 기록된 마지막날짜를 알아내서 그날에 해당하는 로그를 보여준다
		Cursor c = managedQuery(LogSchema.CONTENT_URI, new String[] { LogSchema.CREATED_ON }, null, null, LogSchema.CREATED_ON + " desc");
		c.moveToFirst();
		if (c.getColumnCount() == 0) {
			setEmptyContent();
		}
		
		
		
		/*int index = c.getColumnIndex(LogSchema.CREATED_ON);
		int latest_created_on = c.getInt(index);
		Date date = new Date(latest_created_on);*/
		
	}
	
	private void setEmptyContent() {
		setListAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new String [] { getString(R.string.error_no_log) }));
	}
	
	private void setContent(int epoch) {
		Cursor cursor = managedQuery(LogSchema.CONTENT_URI, LogProvider.PROJECTION, null, null, LogSchema.CREATED_ON + " desc");
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
}