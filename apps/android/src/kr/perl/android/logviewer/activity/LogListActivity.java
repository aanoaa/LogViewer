package kr.perl.android.logviewer.activity;

import java.util.ArrayList;

import kr.perl.android.logviewer.Constants;
import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.thread.ListThread;
import kr.perl.android.logviewer.util.ContextUtil;
import kr.perl.android.logviewer.util.StringUtil;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

public class LogListActivity extends ListActivity {
	
	private Intent mIntent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.log_list);
		init();
		addHooks();
	}
	
	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		String value = ((TextView) v).getText().toString();
		if (mIntent.getStringExtra(Constants.KEY_MONTH) != null) {
			mIntent.putExtra(Constants.KEY_DAY, value);
			mIntent.setClass(this, ViewerActivity.class);
		} else {
			for (String key : new String[] { Constants.KEY_CHANNEL, Constants.KEY_YEAR, Constants.KEY_MONTH }) {
				if (mIntent.getStringExtra(key) == null) {
					mIntent.putExtra(key, value);
					break;
				}
			}
			
			setContent();
		}
		
		startActivity(mIntent);
	}
	
	private void init() {
		mIntent = getIntent();
		mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		setContent();
	}
	
	private void addHooks() {
		
	}
	
	private void setContent() {
		ArrayList<String> uriPiece = new ArrayList<String>();
		String channel = mIntent.getStringExtra(Constants.KEY_CHANNEL);
		String year = mIntent.getStringExtra(Constants.KEY_YEAR);
		String month = mIntent.getStringExtra(Constants.KEY_MONTH);
		String day = mIntent.getStringExtra(Constants.KEY_DAY);
		
		if (channel != null && !channel.equals("")) uriPiece.add(channel);
		if (year != null && !year.equals("")) uriPiece.add(year);
		if (month != null && !month.equals("")) uriPiece.add(month);
		if (day != null && !day.equals("")) uriPiece.add(day);
		
		String path = StringUtil.join(uriPiece, "/");
		if (uriPiece.size() != 0) {
			setTitle(String.format(getString(R.string.title_format3), path));
		}
		
		if (uriPiece.size() == 0 || ContextUtil.isOnline(this)) {
			// fresh list from web
			new ListThread(this, Uri.parse(Constants.LOG_SERVER_DOMAIN + StringUtil.join(uriPiece, "/"))).run();
		} else {
			// local list from db(Content Provider)
		}
	}
}