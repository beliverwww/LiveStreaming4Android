package com.livestream.rtmpsend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SplashActivity extends Activity {

    private Button m_pressButton;
    private Runnable m_anim_runnable;
    private Thread   m_thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        m_pressButton = (Button) findViewById(R.id.longpress_button);

        m_pressButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(SplashActivity.this, ActivityMain.class);
                startActivity(intent);
                return false;
            }
        });
    }
}
