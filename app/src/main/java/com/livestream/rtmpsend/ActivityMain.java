package com.livestream.rtmpsend;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ActivityMain extends Activity {

	private EditText edtUrl;
	private Button btnStart;
	private int finishbutton_count = 0;
	private long click_time = -1;

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		edtUrl		= (EditText)findViewById(R.id.ID_EDT_URL);
		btnStart	= (Button)findViewById(R.id.ID_BTN_START);
		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FFmpegFrameRecorder.filename = edtUrl.getText().toString();
				Intent intent = new Intent(ActivityMain.this, ActivityFFmpegRecorder.class);

				startActivity(intent);
			}
		});

		int requestCode = 0;
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
			== PackageManager.PERMISSION_DENIED)
		requestPermissions(new String[] {Manifest.permission.CAMERA}, requestCode);
	}

	@Override
	protected void onResume() {
		super.onResume();
		finishbutton_count = 0;
		click_time = -1;
	}

	@Override
	public void onBackPressed() {
		checkFinish();
	}

	private void checkFinish() {
		if (finishbutton_count == 0) {
			finishbutton_count = 1;
			click_time = System.currentTimeMillis();
			Toast.makeText(getApplicationContext(),R.string.pressback_twice,Toast.LENGTH_SHORT).show();
		} else if (finishbutton_count == 1) {
			if (System.currentTimeMillis() - click_time <= 1000)
				super.onBackPressed();
			else {
				click_time = System.currentTimeMillis();
				Toast.makeText(getApplicationContext(),R.string.pressback_twice,Toast.LENGTH_SHORT).show();
			}
		}
	}
}
