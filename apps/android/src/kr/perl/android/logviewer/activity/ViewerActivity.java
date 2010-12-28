package kr.perl.android.logviewer.activity;

import java.util.ArrayList;

import kr.perl.android.logviewer.Constants;
import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.adapter.LogAdapter;
import kr.perl.android.logviewer.provider.LogProvider;
import kr.perl.android.logviewer.schema.LogSchema;
import kr.perl.android.logviewer.thread.SyncThread;
import kr.perl.android.logviewer.util.ContextUtil;
import kr.perl.android.logviewer.util.StringUtil;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ViewerActivity extends ListActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.viewer);
		init();
		addHooks();
	}
	
	private void sync(final String strUri, final int latest_epoch, final String channel) {
		new SyncThread(this, strUri, latest_epoch, channel).run();
	}
	
	private void init() {
		Intent intent = getIntent();
		ArrayList<String> uriPiece = new ArrayList<String>();
		uriPiece.add(intent.getStringExtra(Constants.KEY_CHANNEL));
		uriPiece.add(intent.getStringExtra(Constants.KEY_YEAR));
		uriPiece.add(intent.getStringExtra(Constants.KEY_MONTH));
		uriPiece.add(intent.getStringExtra(Constants.KEY_DAY));
		
		String path = StringUtil.join(uriPiece, "/");
		setTitle(String.format(getString(R.string.title_format3), path));
		
		String created_on_arg = uriPiece.get(1) + "-" + uriPiece.get(2) + "-" + uriPiece.get(3);
		String selection = "date(" + LogSchema.CREATED_ON + ", 'unixepoch', 'localtime') = ? and " + LogSchema.CHANNEL + " = ?";
		String[] selectionArgs = new String[] { created_on_arg, uriPiece.get(0)	};
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
		
		if (ContextUtil.isOnline(this)) sync(Constants.LOG_SERVER_DOMAIN + path, created_on, uriPiece.get(0));
	}
	
	private void addHooks() {
		// nothing
	}
	
	@Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
    }
}