package kr.perl.android.logviewer.thread;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.helper.HttpHelper;
import kr.perl.android.logviewer.util.ContextUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

public class ListThread extends Thread {
	
	private static final String TAG = "ListThread";
	
	private ListActivity mActivity;
	private Handler mHandler;
	private Uri mUri;
	
	private Runnable setEmptyContentRunnable = new Runnable() {
		public void run() {
			mActivity.setListAdapter(new ArrayAdapter<String>(mActivity.getApplicationContext(), android.R.layout.simple_list_item_1, new String [] { mActivity.getString(R.string.error_no_log) }));
		}
	};
	
	public ListThread(ListActivity activity, Uri uri) {
		mActivity = activity;
		mHandler = new Handler();
		mUri = uri;
	}
	
	public void run() {
		mActivity.setProgressBarIndeterminateVisibility(true);
		HttpResponse res = null;
		try {
			Log.d(TAG, mUri.toString());
			res = HttpHelper.query(mUri);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mActivity.setProgressBarIndeterminateVisibility(false);
		}
		
		if (res == null) {
			ContextUtil.toast(mActivity, mActivity.getApplicationContext().getString(R.string.error_connection));
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

		List<String> items = new ArrayList<String>();
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
		
			String item = null;
			try {
				item = row.getString(0);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			if (item == null) {
				ContextUtil.toast(mActivity, "cannot find entity"); // 이것도 바꿔야겟지..
				mHandler.post(setEmptyContentRunnable);
				return;
			}
			
			items.add(item);
		}
		
		// something here
		mActivity.setListAdapter(new ArrayAdapter<String>(mActivity.getApplicationContext(), android.R.layout.simple_list_item_1, items));
	}
}