package kr.perl.android.logviewer.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.net.Uri;

public final class HttpHelper {
	public static HttpResponse query(Uri uri) {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10 * 1000);
		HttpConnectionParams.setSoTimeout(httpParameters, 10 * 1000);

		HttpClient httpclient = new DefaultHttpClient(httpParameters);
		HttpGet httpget = new HttpGet(uri.toString());
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static String convertStreamToString(InputStream is) {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    	StringBuilder sb = new StringBuilder();
    	String line = null;
    	try {
    		while ((line = reader.readLine()) != null) {
        		sb.append(line + "\n");
        	}
    		
    		return sb.toString();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return null;
    }
}