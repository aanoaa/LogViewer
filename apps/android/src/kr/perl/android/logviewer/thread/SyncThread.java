package kr.perl.android.logviewer.thread;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.helper.HttpHelper;
import kr.perl.android.logviewer.schema.LogSchema;

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
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class SyncThread extends Thread {
	
	private static final String TAG = "SyncThread";
	
	private ListActivity mActivity;
	private Handler mHandler;
	private SimpleCursorAdapter mAdapter;
	private String mDate;
	private int mLatestEpoch;
	
	private Runnable setEmptyContentRunnable = new Runnable() {
		public void run() {
			if (mAdapter.getCount() == 0) {
				mActivity.setListAdapter(new ArrayAdapter<String>(mActivity.getApplicationContext(), android.R.layout.simple_list_item_1, new String [] { mActivity.getString(R.string.error_no_log) }));
			}
		}
	};
	
	public SyncThread(ListActivity activity, SimpleCursorAdapter adapter, String strDate, int latestEpoch) {
		mActivity = activity;
		mHandler = new Handler();
		mAdapter = adapter;
		mDate = strDate;
		mLatestEpoch = latestEpoch;
	}
	
	private void toast(String errstr) {
		Toast.makeText(mActivity.getApplicationContext(), errstr, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void run() {
		mActivity.setProgressBarIndeterminateVisibility(true);
		HttpResponse res = null;
		try {
			String strUri = "http://192.168.0.209:3000/log/" + mDate;
			if (mLatestEpoch != 0) strUri += "/" + mLatestEpoch;
			Log.d(TAG, "uri: " + strUri);
			res = HttpHelper.query(Uri.parse(strUri));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mActivity.setProgressBarIndeterminateVisibility(false);
		}
		
		if (res == null) {
			toast(mActivity.getApplicationContext().getString(R.string.error_connection));
			return;
		}
		
		if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			toast(String.format(mActivity.getApplicationContext().getString(R.string.error_http_io), res.getStatusLine().getStatusCode(), res.getStatusLine().toString()));
		} else {
			JSONObject json = null;
			HttpEntity entity = res.getEntity();
			if (entity == null) {
				toast("none entity");
				return;
			} else {
				InputStream instream = null;
				try {
					instream = entity.getContent();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if (instream == null) {
					toast("Couldn't get Entity Content");
					return;
				}
				
				String result = HttpHelper.convertStreamToString(instream);
				try {
					json = new JSONObject(result);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if (json == null) {
					Toast.makeText(mActivity.getApplicationContext(), "Couldn't encode body text to JSON object", Toast.LENGTH_SHORT).show();
					toast("Couldn't encode body text to JSON object");
					return;
				}
				
				JSONArray rows = null;
				try {
					rows = json.getJSONArray("rows");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if (rows == null) {
					toast("Couldn't find rows JSONArray");
					mHandler.post(setEmptyContentRunnable);
					return;
				}
				
				List<ContentValues> values = new ArrayList<ContentValues>();
				for (int i=0; i<rows.length(); i++) {
					JSONObject row = null;
					try {
						row = rows.getJSONObject(i);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					if (row == null) {
						toast("Couldn't find " + i + "th row JSONObject");
						mHandler.post(setEmptyContentRunnable);
						return;
					}
					
					try {
						ContentValues value = new ContentValues();
						String nickname = row.getString("nickname");
						int created_on = row.getInt("created_on");
						String hostname = row.getString("hostname");
						String channel = row.getString("channel");
						String message = row.getString("message");
						String username = row.getString("username");
						
						value.put(LogSchema.CHANNEL, channel);
						value.put(LogSchema.NICKNAME, nickname);
						value.put(LogSchema.HOSTNAME, hostname);
						value.put(LogSchema.MESSAGE, message);
						value.put(LogSchema.USERNAME, username);
						value.put(LogSchema.CREATED_ON, created_on);
						values.add(value);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				if (values.size() != 0) {
					ContentValues[] hidden = values.toArray(new ContentValues[values.size()]);
					int count = mActivity.getContentResolver().bulkInsert(LogSchema.CONTENT_URI, hidden);
					Log.d(TAG, "bulk inserted " + count);
					mAdapter.notifyDataSetChanged();
					mHandler.post(setEmptyContentRunnable);
				}
			}
		}
	}
}
