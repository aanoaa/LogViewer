package kr.perl.android.logviewer.thread;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.helper.HttpHelper;
import kr.perl.android.logviewer.schema.LogSchema;
import kr.perl.android.logviewer.util.ContextUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class SyncThread extends Thread {
	
	private ListActivity mActivity;
	private Handler mHandler;
	private Uri mUri;
	private String mChannel;
	
	private Runnable setEmptyContentRunnable = new Runnable() {
		public void run() {
			mActivity.setProgressBarIndeterminateVisibility(false);
			if (mActivity.getListAdapter().getCount() == 0) {
				mActivity.setListAdapter(new ArrayAdapter<String>(mActivity.getApplicationContext(), android.R.layout.simple_list_item_1, new String [] { mActivity.getString(R.string.error_no_log) }));
			}
		}
	};
	
	public SyncThread(ListActivity activity, Uri uri, String channel) {
		mActivity = activity;
		mHandler = new Handler();
		mUri = uri;
		mChannel = channel;
	}
	
	@Override
	public void run() {
		HttpResponse res = null;
		try {
			res = HttpHelper.query(mUri);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (res == null) {
			ContextUtil.toast(mActivity, mActivity.getApplicationContext().getString(R.string.error_connection));
			mHandler.post(setEmptyContentRunnable);
			return;
		}
		
		if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			ContextUtil.toast(mActivity, String.format(mActivity.getApplicationContext().getString(R.string.error_http_io), res.getStatusLine().getStatusCode(), res.getStatusLine().toString()));
			mHandler.post(setEmptyContentRunnable);
			return;
		} 
		
		JSONObject json = null;
		HttpEntity entity = res.getEntity();
		if (entity == null) {
			ContextUtil.toast(mActivity, "none entity"); 
			return;
		}
		
		InputStream instream = null;
		try {
			instream = entity.getContent();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (instream == null) {
			ContextUtil.toast(mActivity, "Couldn't get Entity Content");
			return;
		}

		String response_body = HttpHelper.convertStreamToString(instream);
		if (response_body == null) {
			ContextUtil.toast(mActivity, "response body is null");
			return;
		}
		
		try {
			json = new JSONObject(response_body);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (json == null) {
			ContextUtil.toast(mActivity, "Couldn't encode body text to JSON object");
			return;
		}

		int result = 0;
		try {
			result = json.getInt("result");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		JSONArray data = null;
		try {
			data = json.getJSONArray("data");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (data == null) {
			ContextUtil.toast(mActivity, "interal error");
			mHandler.post(setEmptyContentRunnable);
			return;
		}

		if (result != HttpStatus.SC_OK) {
			ContextUtil.toast(mActivity, "interal error"); // 처리를 달리해줘야 할 것 같은데..
			mHandler.post(setEmptyContentRunnable);
			return;
		}

		List<ContentValues> values = new ArrayList<ContentValues>();
		for (int i=0; i<data.length(); i++) {
			JSONArray row = null;
			try {
				row = data.getJSONArray(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (row == null) {
				ContextUtil.toast(mActivity, "Couldn't find JSONArray[" + i + "]");
				mHandler.post(setEmptyContentRunnable);
				return;
			}
		
			String nickname = null;
			int created_on = 0;
			String message = null;
			try {
				nickname = row.getString(0);
				created_on = row.getInt(1);
				message = row.getString(2);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			if (nickname == null || message == null) {
				ContextUtil.toast(mActivity, "cannot find entity"); // 이것도 바꿔야겟지..
				mHandler.post(setEmptyContentRunnable);
				return;
			}
			
			ContentValues value = new ContentValues();
			value.put(LogSchema.CHANNEL, mChannel);
			value.put(LogSchema.NICKNAME, nickname);
			value.put(LogSchema.MESSAGE, message);
			value.put(LogSchema.CREATED_ON, created_on);
			values.add(value);
		}

		if (values.size() != 0) {
			ContentValues[] hidden = values.toArray(new ContentValues[values.size()]);
			int count = mActivity.getContentResolver().bulkInsert(LogSchema.CONTENT_URI, hidden);
			((SimpleCursorAdapter) mActivity.getListAdapter()).notifyDataSetChanged();
			Toast.makeText(mActivity, "added " + count + " rows", Toast.LENGTH_SHORT).show();
			mActivity.setProgressBarIndeterminateVisibility(false);
		} else {
			Toast.makeText(mActivity, mActivity.getString(R.string.log_uptodate), Toast.LENGTH_SHORT).show();
			mHandler.post(setEmptyContentRunnable);
		}
	}
}
