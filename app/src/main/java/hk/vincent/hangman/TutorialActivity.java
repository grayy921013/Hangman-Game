package hk.vincent.hangman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class TutorialActivity extends Activity {
    public static final String SHOW_TUTORIAL_PREF_KEY = "show_tutorial_pref_key";
    public static final String SHOW_TUTORIAL_KEY = "show_tutorial_key";
    @InjectView(R.id.checkbox)
    CheckBox checkBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        ButterKnife.inject(this);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPref = getSharedPreferences(
                        SHOW_TUTORIAL_PREF_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(SHOW_TUTORIAL_KEY,!isChecked);
                editor.apply();
            }
        });
    }
    @OnClick(R.id.skip_btn)
    public void skip() {
        Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }
}
