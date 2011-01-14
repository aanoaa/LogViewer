package kr.perl.android.logviewer.util;

import kr.perl.android.logviewer.R;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
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
		final View view = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.custom_toast, null);
		((TextView) view.findViewById(R.id.text1)).setText(message);
		
		Toast toast = new Toast(activity.getApplicationContext());
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(view);
		toast.show();
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
