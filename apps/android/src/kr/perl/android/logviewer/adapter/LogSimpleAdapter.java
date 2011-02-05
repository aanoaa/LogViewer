package kr.perl.android.logviewer.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import kr.perl.android.logviewer.Constants;
import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.activity.SearchResultActivity;
import kr.perl.android.logviewer.activity.ViewerActivity;
import kr.perl.provider.LogViewer.Logs;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class LogSimpleAdapter extends SimpleAdapter {
	
	private static final String TAG = "LogSimpleAdapter";
	
	private int[] COLORS = new int[] {
		-6351338, -2205865, -6337258, -2205754, -6316778, -6858786,
		-9593066, -10397730, -13983978, -11032866, -15294633, -11018535,
		-15294566, -11018614, -15311457, -9773481, -15327073, -4989353,
		-12118369, -2172585, -7924065, -2188713, -6351238, -2205865, -6351338
	};
	
	private SearchResultActivity mActivity;
	private int mResourceId;
	private Map<String, Integer> mNickname;
	private int mIndex;
	
	public LogSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
		mActivity = (SearchResultActivity) context;
		mResourceId = resource;
		mNickname = new HashMap<String, Integer>();
		mIndex = 0;
	}
	
	@SuppressWarnings("unchecked")
	public View getView(int position, View convertView, ViewGroup parent) {
		View row;
		
		if (convertView == null) {
	        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        row = inflater.inflate(mResourceId, null);
		} else {
			row = convertView;
		}
		
		Map<String, String> map = (HashMap<String, String>) getItem(position);
		Date d = new Date((long) Integer.parseInt(map.get(Logs.CREATED_ON)) * 1000);
		final String ymd = new SimpleDateFormat("yyyy-MM-dd").format(d);
		final String hm = new SimpleDateFormat("HH:mm").format(d);;
		final String nickname = map.get(Logs.NICKNAME);
		final String message = map.get(Logs.MESSAGE);
		final String channel = map.get(Logs.CHANNEL);
		final int id = Integer.parseInt(map.get(Logs._ID));
		boolean isFavorite = map.get(Logs.FAVORITE).equals("true");
		
		TextView tvTime = (TextView) row.findViewById(R.id.text1);
        TextView tvNickname = (TextView) row.findViewById(R.id.text2);
        TextView tvMessage = (TextView) row.findViewById(R.id.text3);
        CheckBox cbFavorite = (CheckBox) row.findViewById(R.id.checkbox1);
        
        tvTime.setText(hm);
        tvNickname.setText(nickname);
        tvMessage.setText(message);
        
        if (nickname.equals("")) {
        	tvNickname.setTextColor(Color.GRAY);
        	tvMessage.setTextColor(Color.GRAY);
        	return row;
        }

        tvMessage.setTextColor(Color.LTGRAY);
        if (!mNickname.containsKey(nickname)) {
        	Matcher m = Constants.PATTERN_WHITECAT.matcher(nickname);
        	if (m.find()) {
        		mNickname.put(nickname, -2302756);
        	} else {
        		if (mIndex == COLORS.length) mIndex = 0;
                mNickname.put(nickname, COLORS[mIndex++]);
        	}
        }
       	tvNickname.setTextColor(mNickname.get(nickname));
       	
       	cbFavorite.setChecked(isFavorite);
       	cbFavorite.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				ContentValues cv = new ContentValues();
				cv.put(Logs.FAVORITE, isChecked ? 1 : 0);
				Uri uri = ContentUris.withAppendedId(Logs.CONTENT_URI, id);
				mActivity.getContentResolver().update(uri, cv, null, null);
			}
		});
       	
       	tvNickname.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mActivity, ViewerActivity.class);
				intent.setAction(Intent.ACTION_VIEW);
				intent.putExtra("id", new StringBuilder().append(id).toString());
				intent.putExtra("strDate", ymd);
				intent.putExtra("channel", channel);
				mActivity.startActivity(intent);
				mActivity.finish();
			}
		});
       	
        return row;
	}
}