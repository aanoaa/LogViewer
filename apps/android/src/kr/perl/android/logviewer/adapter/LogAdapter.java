package kr.perl.android.logviewer.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.perl.android.logviewer.R;
import kr.perl.android.logviewer.schema.LogSchema;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class LogAdapter extends SimpleCursorAdapter {
	
	private Context mContext;
	private int mResourceId;
	
	public LogAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		mContext = context;
		mResourceId = layout;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResourceId, null);
        }
        
        Cursor c = getCursor();
        int index;
        index = c.getColumnIndex(LogSchema.CREATED_ON);
        int created_on = c.getInt(index);
        index = c.getColumnIndex(LogSchema.NICKNAME);
        String nickname = c.getString(index);
        index = c.getColumnIndex(LogSchema.MESSAGE);
        String message = c.getString(index);
        
        Date date = new Date(created_on);
        SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm");
        String time = sDateFormat.format(date);
        
        TextView textview;
        textview = (TextView) row.findViewById(R.id.text1);
        textview.setText(time);
        
        textview = (TextView) row.findViewById(R.id.text2);
        textview.setText(nickname);
        
        textview = (TextView) row.findViewById(R.id.text3);
        textview.setText(message);
        return row;
	}
}
