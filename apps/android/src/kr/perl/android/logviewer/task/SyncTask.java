package kr.perl.android.logviewer.task;

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
import android.os.AsyncTask;
import android.widget.SimpleCursorAdapter;

// Params, Progress, Result
public class SyncTask extends AsyncTask<Uri, Void, Boolean> {
	
	private ListActivity mActivity;
	private String mChannel;
	
	public static boolean IS_QUERYING = false;
	
	private final Runnable PROGRESS_BAR_START = new Runnable() {
		@Override
		public void run() {
			mActivity.setProgressBarIndeterminateVisibility(true);
		}
	};
	
	private final Runnable PROGRESS_BAR_STOP = new Runnable() {
		@Override
		public void run() {
			mActivity.setProgressBarIndeterminateVisibility(false);
		}
	};

	public SyncTask(ListActivity activity, String channel) {
		mActivity = activity;
		mChannel = channel;
		IS_QUERYING = false;
	}
	
	protected Boolean doInBackground(Uri... uris) {
		// uri 로 부터 HTTP IO 작업 후에 DB 작업
		if (IS_QUERYING) {
			return false;
		}
		
		IS_QUERYING = true;
		mActivity.runOnUiThread(PROGRESS_BAR_START);
		
		for (Uri uri : uris) {
			if (ContextUtil.isOnline(mActivity)) {
				if (!sync(uri)) {
					return false;
				}
			} else {
				ContextUtil.toastOnUiTread(mActivity, mActivity.getString(R.string.error_connection));
				return false;
			}
		}
		
		return true;
	}

	protected void onPostExecute(Boolean flag) {
		IS_QUERYING = false;
		mActivity.runOnUiThread(PROGRESS_BAR_STOP);
	}
	
	private boolean sync(Uri uri) {
		HttpResponse res = null;
		try {
			res = HttpHelper.query(uri);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (res == null) {
			ContextUtil.toastOnUiTread(mActivity, mActivity.getString(R.string.error_connection));
			return false;
		}
		
		if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			ContextUtil.toastOnUiTread(mActivity, String.format(mActivity.getApplicationContext().getString(R.string.error_http_io), res.getStatusLine().getStatusCode(), res.getStatusLine().toString()));
			return false;
		} 
		
		JSONObject json = null;
		HttpEntity entity = res.getEntity();
		if (entity == null) {
			ContextUtil.toastOnUiTread(mActivity, "none entity"); 
			return false;
		}
		
		InputStream instream = null;
		try {
			instream = entity.getContent();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (instream == null) {
			ContextUtil.toastOnUiTread(mActivity, "Couldn't get Entity Content");
			return false;
		}

		String response_body = HttpHelper.convertStreamToString(instream);
		if (response_body == null) {
			ContextUtil.toastOnUiTread(mActivity, "response body is null");
			return false;
		}
		
		try {
			json = new JSONObject(response_body);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (json == null) {
			ContextUtil.toastOnUiTread(mActivity, "Couldn't encode body text to JSON object");
			return false;
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
			return false;
		}

		if (result != HttpStatus.SC_OK) {
			ContextUtil.toastOnUiTread(mActivity, "interal error"); // 처리를 달리해줘야 할 것 같은데..
			return false;
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
				return false;
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
				return false;
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
			ContextUtil.toastOnUiTread(mActivity, String.format(mActivity.getString(R.string.notify_add_row), count));
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					((SimpleCursorAdapter) mActivity.getListAdapter()).notifyDataSetChanged();
				}
			});
		} else {
			if (mActivity.getListAdapter().getCount() != 0) {	
				ContextUtil.toastOnUiTread(mActivity, mActivity.getString(R.string.log_uptodate));
			}
		}
		
		return true;
	}
	
	
}