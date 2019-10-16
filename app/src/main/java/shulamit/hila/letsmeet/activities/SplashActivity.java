package shulamit.hila.letsmeet.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.google.firebase.auth.FirebaseAuth;

import shulamit.hila.letsmeet.R;

public class SplashActivity extends Activity {
    private static final int DELAY_TIME = 2; // seconds
    private int timeLeft;
    private String userId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        userId = FirebaseAuth.getInstance().getUid();
        startTimer();
    }

    private void startTimer()
    {
        timeLeft = DELAY_TIME;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while(timeLeft>=0){
                    SystemClock.sleep(1000); //Thread.sleep(1000);
                    timeLeft--;
                }
                // decide here whether to navigate to Login or Main Activity
                Intent intent = new Intent(SplashActivity.this, userId == null ? LoginActivity.class: MainActivity.class);
                startActivity(intent);
                finish();
            }
        }).start();
    }
}