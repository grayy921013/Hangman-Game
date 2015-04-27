package hk.vincent.hangman;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by admin on 4/10/15.
 */
public class CustomFontTextView extends TextView {
    public CustomFontTextView(Context context) {
        super(context);
        init();
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "GoodDog.otf");
            setTypeface(tf);
        }
    }
}
