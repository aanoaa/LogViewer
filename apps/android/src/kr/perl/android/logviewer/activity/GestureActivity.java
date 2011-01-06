package kr.perl.android.logviewer.activity;

import java.util.ArrayList;

import kr.perl.android.logviewer.R;
import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;

public class GestureActivity extends Activity {
	
	private static final String TAG = "GestureActivity";
	private GestureLibrary mLibrary;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture);
	    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	    init();
	    addHooks();
	}
	
	private void init() {
		mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!mLibrary.load()) {
			Log.d(TAG, "gesture load faild");
			setResult(RESULT_CANCELED);
			finish();
		}
	}
	
	private void addHooks() {
		Button button = (Button) findViewById(R.id.gesture_cancel_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "gesture canceled by user");
				GestureActivity.this.setResult(RESULT_CANCELED);
				GestureActivity.this.finish();
			}
		});
		
		GestureOverlayView gesture = (GestureOverlayView) findViewById(R.id.gesture_view);
		gesture.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
			@Override
			public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
				Log.d(TAG, "gesture performed");
				ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
				if (predictions.size() > 0) {
					Prediction prediction = predictions.get(0);
					if (prediction.score > 1.0) {
						Log.d(TAG, "predicated successfully (" + prediction.name + ")");
						Intent intent = new Intent();
						intent.putExtra("gesture", prediction.name);
						GestureActivity.this.setResult(RESULT_OK, intent);
					}
					else {
						Log.d(TAG, "there is no good predication");
						GestureActivity.this.setResult(RESULT_CANCELED);
					}
				}
				else {
					Log.d(TAG, "there is no predication");
					GestureActivity.this.setResult(RESULT_CANCELED);
				}
				GestureActivity.this.finish();
			}
		});
	}
}
