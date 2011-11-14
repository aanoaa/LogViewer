package kr.perl.android.logviewer.util;

import org.json.JSONObject;

public final class JSONUtils {

    private JSONUtils() { } // can not instantiate
    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";
    public static final String MESSAGE = "message";
	
	public static JSONObject toJSON(String strJSON) {
		JSONObject json;
		try {
			json = new JSONObject(strJSON);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return json;
	}
	
	public static boolean isSuccess(JSONObject json) {
		return json.has(SUCCESS);
	}
	
	public static boolean isFailed(JSONObject json) {
		return json.has(FAILED);
	}
	
	public static String message(JSONObject json) {
		try {
			return json.getString(MESSAGE);
		} catch (Exception e) {
			return "";
		}
	}
}
