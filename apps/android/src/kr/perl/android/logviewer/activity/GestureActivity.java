package kr.perl.android.logviewer.activity;

import kr.perl.android.logviewer.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;

public class GestureActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture);
	    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	    addHooks();
	}
	
	private void addHooks() {
		Button button = (Button) findViewById(R.id.gesture_cancel_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GestureActivity.this.finish();
			}
		});
	}
}
