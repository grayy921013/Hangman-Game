package hk.vincent.hangman;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends Activity {
    @InjectView(R.id.word)
    TextView word;
    @InjectView(R.id.miss)
    TextView miss;
    @InjectView(R.id.numofword)
    TextView numofword_tv;
    @InjectView(R.id.numberOfGuess)
    TextView numofguess_tv;
    @InjectView(R.id.start_btn)
    Button start_btn;
    @InjectView(R.id.skip_btn)
    Button skip_btn;
    @InjectView(R.id.end_btn)
    Button end_btn;
    @InjectView(R.id.keyboard_helper)
    EditText keyboard_helper;
    @InjectView(R.id.hint_req)
    TextView hint;
    OkHttpClient client;
    Handler handler = new Handler();
    private static final String TAG = "main activity";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    public final String PLAYER_ID = "grayy921013@gmail.com";
    public final String REQUEST_URL = "https://strikingly-hangman.herokuapp.com/game/on";
    public final String HINT_URL = "http://www.hanginghyena.com/gateway/lookup";
    public String SESSION_ID;
    int totalNumOfWords;
    int numOfGuessAllowed;
    String wordNow = "";
    String missStr = "";
    Character lastInput = null;
    int numOfWordNow;
    int numOfGuessNow;
    //All the network request callbacks
    Callback startGameCallback = new Callback() {
        @Override
        public void onFailure(Request request, IOException throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onResponse(Response response) throws IOException {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            try {
                String s = response.body().string();
                Log.d(TAG, s);
                JSONObject jsonObject = new JSONObject(s);
                SESSION_ID = jsonObject.getString("sessionId");
                jsonObject = new JSONObject(jsonObject.getString("data"));
                totalNumOfWords = Integer.parseInt(jsonObject.getString("numberOfWordsToGuess"));
                numOfGuessAllowed = Integer.parseInt(jsonObject.getString("numberOfGuessAllowedForEachWord"));
                numOfWordNow = 0;
                numOfGuessNow = 0;
                updateNumber();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        start_btn.setVisibility(View.INVISIBLE);
                        skip_btn.setVisibility(View.VISIBLE);
                        end_btn.setVisibility(View.VISIBLE);
                    }
                });
                nextWord();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Callback submitScoreCallback = new Callback() {
        @Override
        public void onFailure(Request request, IOException e) {
            e.printStackTrace();
            SESSION_ID = null;
        }

        @Override
        public void onResponse(Response response) throws IOException {
            String s = response.body().string();
            Log.d(TAG, s);
            SESSION_ID = null;
        }
    };
    Callback endCallback = new Callback() {
        @Override
        public void onFailure(Request request, IOException throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onResponse(Response response) throws IOException {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            try {
                String s = response.body().string();
                Log.d(TAG, s);
                JSONObject jsonObject = new JSONObject(s);
                jsonObject = new JSONObject(jsonObject.getString("data"));
                String msg = "";
                msg += "totalWordCount: " + jsonObject.getString("totalWordCount") + "\n";
                msg += "correctWordCount: " + jsonObject.getString("correctWordCount") + "\n";
                msg += "totalWrongGuessCount: " + jsonObject.getString("totalWrongGuessCount") + "\n";
                msg += "score: " + jsonObject.getString("score") + "\n";
                msg += "upload score?";
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Game Over");
                builder.setMessage(msg);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        submitScore();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SESSION_ID = null;
                        dialog.dismiss();
                    }
                });
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        builder.show();
                        start_btn.setVisibility(View.VISIBLE);
                        skip_btn.setVisibility(View.INVISIBLE);
                        end_btn.setVisibility(View.INVISIBLE);
                        word.setText("");
                        miss.setText("");
                        numofguess_tv.setText("");
                        numofword_tv.setText("");
                        hint.setText(R.string.need_hint);
                        final ActionBar actionBar = getActionBar();
                        actionBar.setTitle(R.string.app_name);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Callback guessCallback = new Callback() {
        @Override
        public void onFailure(Request request, IOException throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onResponse(Response response) throws IOException {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            try {
                String s = response.body().string();
                Log.d(TAG, s);
                JSONObject jsonObject = new JSONObject(s);
                jsonObject = new JSONObject(jsonObject.getString("data"));
                String newWord = jsonObject.getString("word");
                if (lastInput != null && newWord.equals(wordNow)) {
                    missStr += lastInput;
                }
                wordNow = newWord;
                numOfWordNow = Integer.parseInt(jsonObject.getString("totalWordCount"));
                numOfGuessNow = Integer.parseInt(jsonObject.getString("wrongGuessCountOfCurrentWord"));
                updateWord();
                updateNumber();
                getResult();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        start_btn.setVisibility(View.INVISIBLE);
                        keyboard_helper.setText("");
                        hint.setText(R.string.need_hint);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Callback getResultCallback = new Callback() {
        @Override
        public void onFailure(Request request, IOException throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onResponse(Response response) throws IOException {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            try {
                String s = response.body().string();
                Log.d(TAG, s);
                JSONObject jsonObject = new JSONObject(s);
                jsonObject = new JSONObject(jsonObject.getString("data"));
                final ActionBar actionBar = getActionBar();
                final String score = jsonObject.getString("score");
                if (actionBar != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            actionBar.setTitle("Score Now: " + score);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Callback hintCallback = new Callback() {
        @Override
        public void onFailure(Request request, IOException e) {

        }

        @Override
        public void onResponse(Response response) throws IOException {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            try {
                String s = response.body().string();
                Log.d(TAG, s);
                JSONObject jsonObject = new JSONObject(s);
                jsonObject = new JSONObject(jsonObject.getString("recs"));
                Iterator<String> iterator = jsonObject.keys();
                String most_likely = null;
                double highest = 0;
                while(iterator.hasNext()) {
                    String key = iterator.next();
                    double rate = jsonObject.getDouble(key);
                    if (most_likely == null || rate > highest) {
                        most_likely = key;
                        highest = rate;
                    }
                }
                final String hint_str = most_likely;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (hint_str != null) {
                            hint.setText(getString(R.string.hint_prefix) + " " + hint_str);
                        } else {
                            hint.setText(R.string.no_hint);
                        }
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                hint.setText(R.string.no_hint);
            }
        }
    };

    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            numofword_tv.setText(totalNumOfWords - numOfWordNow + "");
            numofguess_tv.setText(numOfGuessAllowed - numOfGuessNow + "");
            if (numOfGuessAllowed - numOfGuessNow == 0) {
                nextWord();
            }
        }
    };
    Runnable updateWordRunnable = new Runnable() {
        @Override
        public void run() {
            word.setText(reformat(wordNow));
            miss.setText(reformat(missStr));
            if (!wordNow.contains("*")) {
                nextWord();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        client = new OkHttpClient();
        keyboard_helper.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(keyboard_helper, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
        keyboard_helper.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //here is your code
                if (count == 1) {
                    char a = s.charAt(start);
                    if (a >= 'a' && a <= 'z') {
                        a -= 32;
                    }
                    if (a >= 'A' && a <= 'Z') {
                        makeGuess(a);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            keyboard_helper.clearFocus();
            keyboard_helper.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            keyboard_helper.setSelection(keyboard_helper.getText().toString().length());
        }
    }
    @OnClick(R.id.hint_req)
    public void getHint() {
        try {
            post("pattern=" + wordNow + "&exclusions=" + missStr,hintCallback,HINT_URL);
        } catch (Exception e) {
            hint.setText(R.string.no_hint);
            e.printStackTrace();
        }
    }
    @OnClick(R.id.start_btn)
    public void startGame() {
        try {
            post("{\"playerId\":\"" + PLAYER_ID + "\", \"action\":\"startGame\"}", startGameCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void submitScore() {
        try {

            post("{\"sessionId\":\"" + SESSION_ID + "\", \"action\":\"submitResult\"}", submitScoreCallback);
        } catch (Exception e) {
            SESSION_ID = null;
            e.printStackTrace();
        }
    }

    @OnClick(R.id.end_btn)
    public void gameEnd() {
        try {

            post("{\"sessionId\":\"" + SESSION_ID + "\", \"action\":\"getResult\"}", endCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getResult() {
        try {

            post("{\"sessionId\":\"" + SESSION_ID + "\", \"action\":\"getResult\"}", getResultCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.skip_btn)
    public void nextWord() {
        try {
            missStr = "";
            lastInput = null;
            if (totalNumOfWords == numOfWordNow) {
                gameEnd();
                return;
            }
            post("{\"sessionId\":\"" + SESSION_ID + "\", \"action\":\"nextWord\"}", guessCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void makeGuess(char s) {
        try {
            if (wordNow.indexOf(s) >= 0 || missStr.indexOf(s) >= 0) {
                return;
            }
            lastInput = s;
            post("{\"sessionId\":\"" + SESSION_ID + "\", \"action\":\"guessWord\",\"guess\":\"" + s + "\"}", guessCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateNumber() {
        runOnUiThread(updateRunnable);
    }

    private void updateWord() {
        runOnUiThread(updateWordRunnable);
    }

    private String reformat(String s) {
        return s.replace("", " ").trim();
    }

    void post(String json, Callback callback) throws IOException {
        post(json, callback, REQUEST_URL);
    }
    void post(String json, Callback callback, String url) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
