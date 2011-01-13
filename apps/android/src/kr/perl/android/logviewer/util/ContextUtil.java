package kr.perl.android.logviewer.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public final class ContextUtil {

    private ContextUtil() { } // can not instantiate
	
	public static boolean isOnline(Activity activity) {
		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null) return false;
		return info.isConnectedOrConnecting();
	}
	
	public static void toast(Activity activity, String message) {
		Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
	public static void toastOnUiTread(Activity activity, String message) {
		final Activity a = activity;
		final String m = message;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				toast(a, m);
			}
		});
	}
}
