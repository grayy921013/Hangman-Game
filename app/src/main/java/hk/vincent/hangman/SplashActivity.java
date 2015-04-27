package hk.vincent.hangman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;


public class SplashActivity extends Activity {
    Handler handler = new Handler();
    Runnable transitionRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SharedPreferences sharedPref = getSharedPreferences(
                TutorialActivity.SHOW_TUTORIAL_PREF_KEY, Context.MODE_PRIVATE);
        final boolean shouldOpenTuto = sharedPref.getBoolean(TutorialActivity.SHOW_TUTORIAL_KEY,true);
        transitionRunnable = new Runnable() {
            @Override
            public void run() {
                if (shouldOpenTuto) {
                    Intent intent = new Intent(SplashActivity.this,TutorialActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(transitionRunnable,2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(transitionRunnable);
    }
}
