package kr.perl.android.logviewer.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class ContextUtil {
	public static boolean isOnline(Activity activity) {
		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	public static void toast(Activity activity, String message) {
		Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
}
