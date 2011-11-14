package kr.perl.android.logviewer.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import kr.perl.android.logviewer.Constants;
import kr.perl.android.logviewer.R;
import kr.perl.provider.LogViewer.Logs;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class LogCursorAdapter extends CursorAdapter {
	
	private int[] COLORS = new int[] {
		-6351338, -2205865, -6337258, -2205754, -6316778, -6858786,
		-9593066, -10397730, -13983978, -11032866, -15294633, -11018535,
		-15294566, -11018614, -15311457, -9773481, -15327073, -4989353,
		-12118369, -2172585, -7924065, -2188713, -6351238, -2205865, -6351338
	};
	
	private int mResourceId;
	private LayoutInflater mLayoutInflater;
	private Map<String, Integer> mNickname;
	private int mIndex;
	
	public LogCursorAdapter (Context context, int layout, Cursor c) {
        super(context, c);
        mResourceId = layout;
        mLayoutInflater = LayoutInflater.from(context);
        mNickname = new HashMap<String, Integer>();
    }
	
	@Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = mLayoutInflater.inflate(mResourceId, null);
    	ViewHolder viewHolder = new ViewHolder();
    	viewHolder.time = (TextView) v.findViewById(R.id.text1);
		viewHolder.nickname = (TextView) v.findViewById(R.id.text2);
		viewHolder.message = (TextView) v.findViewById(R.id.text3);
    	v.setTag(viewHolder);
    	return v;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {
    }
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
    	ViewHolder viewHolder;
    	if (convertView == null) {
    		v = mLayoutInflater.inflate(mResourceId, null);
    		viewHolder = new ViewHolder();
    		viewHolder.time = (TextView) v.findViewById(R.id.text1);
    		viewHolder.nickname = (TextView) v.findViewById(R.id.text2);
    		viewHolder.message = (TextView) v.findViewById(R.id.text3);
            v.setTag(viewHolder);
    	} else {
    		v = convertView;
    		viewHolder = (ViewHolder) v.getTag();
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
        
        viewHolder.time.setText(time);
        viewHolder.nickname.setText(nickname);
        viewHolder.message.setText(message);
        
        if (nickname.equals("")) {
        	viewHolder.nickname.setTextColor(Color.GRAY);
        	viewHolder.message.setTextColor(Color.GRAY);
        	return v;
        }

        viewHolder.message.setTextColor(Color.LTGRAY);
        if (!mNickname.containsKey(nickname)) {
        	Matcher m = Constants.PATTERN_WHITECAT.matcher(nickname);
        	if (m.find()) {
        		mNickname.put(nickname, -2302756);
        	} else {
        		if (mIndex == COLORS.length) mIndex = 0;
                mNickname.put(nickname, COLORS[mIndex++]);
        	}
        }

       	viewHolder.nickname.setTextColor(mNickname.get(nickname));
        return v;
	}
	
	private class ViewHolder {
		TextView time;
        TextView nickname;
        TextView message;
	}
}