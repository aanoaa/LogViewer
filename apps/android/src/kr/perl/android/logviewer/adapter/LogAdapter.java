package kr.perl.android.logviewer.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.perl.android.logviewer.R;
import kr.perl.provider.LogViewer.Logs;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class LogAdapter extends SimpleCursorAdapter {
	
	private int[] COLORS = new int[] {
		-6351338, -2205865, -6337258, -2205754, -6316778, -6858786,
		-9593066, -10397730, -13983978, -11032866, -15294633, -11018535,
		-15294566, -11018614, -15311457, -9773481, -15327073, -4989353,
		-12118369, -2172585, -7924065, -2188713, -6351238, -2205865, -6351338
	};
	
	private Context mContext;
	private int mResourceId;
	private Map<String, Integer> mNickname;
	private int mIndex;
	private Pattern pWhitecat;
	
	public LogAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		mContext = context;
		mResourceId = layout;
		mNickname = new HashMap<String, Integer>();
		mIndex = 0;
		pWhitecat = Pattern.compile("(whitecat|agcraft)", Pattern.CASE_INSENSITIVE);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View row;
		
		if (convertView == null) {
	        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        row = inflater.inflate(mResourceId, null);
		}
		else {
			row = convertView;
		}
		
        Cursor c = getCursor();
        c.moveToPosition(position);
        int index;
        index = c.getColumnIndex(Logs.CREATED_ON);
        int created_on = c.getInt(index);
        String time = new SimpleDateFormat("HH:mm").format(new Date((long) created_on * 1000));
        index = c.getColumnIndex(Logs.NICKNAME);
        String nickname = c.getString(index);
        index = c.getColumnIndex(Logs.MESSAGE);
        String message = c.getString(index);

        TextView tvTime = (TextView) row.findViewById(R.id.text1);
        TextView tvNickname = (TextView) row.findViewById(R.id.text2);
        TextView tvMessage = (TextView) row.findViewById(R.id.text3);
        
        tvTime.setText(time);
        tvNickname.setText(nickname);
        tvMessage.setText(message);
        
        if (nickname.equals("")) {
        	tvNickname.setTextColor(Color.GRAY);
        	tvMessage.setTextColor(Color.GRAY);
        	return row;
        }

        tvMessage.setTextColor(Color.LTGRAY);
        if (!mNickname.containsKey(nickname)) {
        	Matcher m = pWhitecat.matcher(nickname);
        	if (m.find()) {
        		mNickname.put(nickname, -2302756);
        	} else {
        		if (mIndex == COLORS.length) mIndex = 0;
                mNickname.put(nickname, COLORS[mIndex++]);
        	}
        }
       	tvNickname.setTextColor(mNickname.get(nickname));
        return row;
	}
}
