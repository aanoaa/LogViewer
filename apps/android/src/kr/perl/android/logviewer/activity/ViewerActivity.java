package kr.perl.android.logviewer.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.adapter.LogAdapter;
import kr.perl.android.logviewer.provider.LogProvider;
import kr.perl.android.logviewer.schema.LogSchema;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ViewerActivity extends ListActivity {
	
	private Context mContext;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer);
		init();
		//setContent();
		setCursorContent();
		addHooks();
	}
	
	private void init() {
		mContext = ViewerActivity.this;
	}
	
	private void setCursorContent() {
		Cursor cursor = managedQuery(LogSchema.CONTENT_URI, LogProvider.PROJECTION, null, null, LogSchema.CREATED_ON + " desc");
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
			mContext, 
			R.layout.log_row, 
			cursor, 
			new String[] {LogSchema.CREATED_ON, LogSchema.NICKNAME, LogSchema.MESSAGE}, 
			new int[] {R.id.text1, R.id.text2, R.id.text3}
		);
		
		adapter.notifyDataSetChanged();
		setListAdapter(adapter);
	}
	
	private void setContent() {
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        Map<String, String> map;
        map = new HashMap<String, String>();
        map.put("time", "10:10");
        map.put("nick", "a3r0");
        map.put("message", "안녕하세요");
        data.add(map);
        
        map = new HashMap<String, String>();
        map.put("time", "10:10");
        map.put("nick", "a3r0");
        map.put("message", "안녕하세요");
        data.add(map);
        
        map = new HashMap<String, String>();
        map.put("time", "10:10");
        map.put("nick", "a3r0");
        map.put("message", "안녕하세요");
        data.add(map);
        
        map = new HashMap<String, String>();
        map.put("time", "10:10");
        map.put("nick", "a3r0");
        map.put("message", "안녕하세요");
        data.add(map);
    
        setListAdapter(new LogAdapter(
        	this,
            data, 
            R.layout.log_row, 
            new String[] { "time", "nick", "message" },  
            new int[] { R.id.text1, R.id.text2, R.id.text3 })
        );
	}
	
	private void addHooks() {
		// nothing
	}
	
	@Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
    }
}