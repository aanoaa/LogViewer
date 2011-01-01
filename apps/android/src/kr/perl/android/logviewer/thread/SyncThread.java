package kr.perl.android.logviewer.thread;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.helper.HttpHelper;
import kr.perl.android.logviewer.util.ContextUtil;
import kr.perl.provider.LogViewer.Logs;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.ContentValues;
import android.net.Uri;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;

public class SyncThread extends Thread {
	
	private ListActivity mActivity;
	private Uri mUri;
	private String mChannel;
	
	private final Runnable threadEmptyContentRunnable = new Runnable() {
		@Override
		public void run() {
			mActivity.setProgressBarIndeterminate(false);
			if (mActivity.getListAdapter().getCount() == 0) {
				mActivity.setListAdapter(new ArrayAdapter<String>(mActivity.getApplicationContext(), android.R.layout.simple_list_item_1, new String [] { mActivity.getString(R.string.error_no_log) }));
			}
		}
	};
	
	private final Runnable threadLoadingBarStart = new Runnable() {
		@Override
		public void run() {
			mActivity.setProgressBarIndeterminateVisibility(true);
		}
	};

	private final Runnable threadLoadingBarStop = new Runnable() {
		@Override
		public void run() {
			mActivity.setProgressBarIndeterminateVisibility(false);
		}
	};
		
	public SyncThread(ListActivity activity, Uri uri, String channel) {
		mActivity = activity;
		mUri = uri;
		mChannel = channel;
	}
	
	private void runUiThread(Runnable thread) {
		mActivity.runOnUiThread(thread);
	}

	private void runSync() {
		HttpResponse res = null;
		try {
			res = HttpHelper.query(mUri);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (res == null) {
			ContextUtil.toastOnUiTread(mActivity, mActivity.getApplicationContext().getString(R.string.error_connection));
			runUiThread(threadEmptyContentRunnable);
			return;
		}
		
		if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			ContextUtil.toastOnUiTread(mActivity, String.format(mActivity.getApplicationContext().getString(R.string.error_http_io), res.getStatusLine().getStatusCode(), res.getStatusLine().toString()));
			runUiThread(threadEmptyContentRunnable);
			return;
		} 
		
		JSONObject json = null;
		HttpEntity entity = res.getEntity();
		if (entity == null) {
			ContextUtil.toastOnUiTread(mActivity, "none entity"); 
			return;
		}
		
		InputStream instream = null;
		try {
			instream = entity.getContent();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (instream == null) {
			ContextUtil.toastOnUiTread(mActivity, "Couldn't get Entity Content");
			return;
		}

		String response_body = HttpHelper.convertStreamToString(instream);
		if (response_body == null) {
			ContextUtil.toastOnUiTread(mActivity, "response body is null");
			return;
		}
		
		try {
			json = new JSONObject(response_body);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (json == null) {
			ContextUtil.toastOnUiTread(mActivity, "Couldn't encode body text to JSON object");
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
			ContextUtil.toastOnUiTread(mActivity, "interal error");
			runUiThread(threadEmptyContentRunnable);
			return;
		}

		if (result != HttpStatus.SC_OK) {
			ContextUtil.toastOnUiTread(mActivity, "interal error"); // 처리를 달리해줘야 할 것 같은데..
			runUiThread(threadEmptyContentRunnable);
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
				ContextUtil.toastOnUiTread(mActivity, "Couldn't find JSONArray[" + i + "]");
				runUiThread(threadEmptyContentRunnable);
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
				ContextUtil.toastOnUiTread(mActivity, "cannot find entity"); // 이것도 바꿔야겟지..
				runUiThread(threadEmptyContentRunnable);
				return;
			}
			
			ContentValues value = new ContentValues();
			value.put(Logs.CHANNEL, mChannel);
			value.put(Logs.NICKNAME, nickname);
			value.put(Logs.MESSAGE, message);
			value.put(Logs.CREATED_ON, created_on);
			values.add(value);
		}

		if (values.size() != 0) {
			ContentValues[] hidden = values.toArray(new ContentValues[values.size()]);
			int count = mActivity.getContentResolver().bulkInsert(Logs.CONTENT_URI, hidden);
			((SimpleCursorAdapter) mActivity.getListAdapter()).notifyDataSetChanged();
			ContextUtil.toastOnUiTread(mActivity, "added " + count + " rows");
		} else {
			ContextUtil.toastOnUiTread(mActivity, mActivity.getString(R.string.log_uptodate));
			runUiThread(threadEmptyContentRunnable);
		}
	}
	
	@Override
	public void run() {
		try {
			runUiThread(threadLoadingBarStart);
			runSync();
			runUiThread(threadLoadingBarStop);
		} catch (Exception e) {
			runUiThread(threadLoadingBarStop);
		}
	}
}
