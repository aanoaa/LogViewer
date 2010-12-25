package kr.perl.android.logviewer.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.perl.android.logviewer.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class LogAdapter extends SimpleAdapter {
	
	private Context mContext;
	private int mResourceId;
	
	public LogAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
		mContext = context;
		mResourceId = resource;
	}
	
	@SuppressWarnings("unchecked")
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResourceId, null);
        }
        
        Map<String, String> map = (HashMap) getItem(position);
        String time = map.get("time");
        String nick = map.get("nick");
        String message = map.get("message");
        
        TextView textview;
        textview = (TextView) row.findViewById(R.id.text1);
        textview.setText(time);
        
        textview = (TextView) row.findViewById(R.id.text2);
        textview.setText(nick);
        
        textview = (TextView) row.findViewById(R.id.text3);
        textview.setText(message);
        return row;
	}
}
