package kr.perl.android.logviewer.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.util.ContextUtil;
import kr.perl.provider.LogViewer.Logs;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class StatusActivity extends Activity {
	
	private static final String[] PROJECTION = new String[] { Logs._ID, Logs.CHANNEL, Logs.NICKNAME, Logs.MESSAGE, Logs.FAVORITE, Logs.CREATED_ON };
	
	private CheckBox mFavorite;
	private int mId;
	private String mStrDate;
	private String mChannel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);
		init();
		addHooks();
		
		Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null, null);
		if (cursor.getCount() == 0) {
			ContextUtil.toast(this, getString(R.string.error_internal));
			finish();
			return;
		}
		
		if (cursor.moveToFirst()) {
			mId = cursor.getInt(cursor.getColumnIndex(Logs._ID));
			boolean isFavorite = cursor.getInt(cursor.getColumnIndex(Logs.FAVORITE)) != 0; 
			long created_on = cursor.getInt(cursor.getColumnIndex(Logs.CREATED_ON));
			Date d = new Date((long) created_on * 1000);
			mChannel = cursor.getString(cursor.getColumnIndex(Logs.CHANNEL));
			mStrDate = new SimpleDateFormat("yyyy-MM-dd").format(d);
			String datetime = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(d);
			String nickname = cursor.getString(cursor.getColumnIndex(Logs.NICKNAME));
			String message = cursor.getString(cursor.getColumnIndex(Logs.MESSAGE));
			
			setTitle(String.format("%s %s", mChannel, datetime));
			TextView textview;
			textview = (TextView) findViewById(R.id.text1);
			textview.setText(nickname);
			textview = (TextView) findViewById(R.id.text2);
			textview.setText(message);
			mFavorite.setChecked(isFavorite);
		}
	}
	
	private void init() {
		mFavorite = (CheckBox) findViewById(R.id.checkbox1);
	}
	
	private void addHooks() {
		mFavorite.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				ContentValues cv = new ContentValues();
				cv.put(Logs.FAVORITE, isChecked ? 1 : 0);
				getContentResolver().update(getIntent().getData(), cv, null, null);
			}
		});
		
		TextView textview = (TextView) findViewById(R.id.text2);
		textview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(StatusActivity.this, ViewerActivity.class);
				intent.setAction(Intent.ACTION_VIEW);
				intent.putExtra("id", new StringBuilder().append(mId).toString());
				intent.putExtra("strDate", mStrDate);
				intent.putExtra("channel", mChannel);
				startActivity(intent);
			}
		});
	}
}