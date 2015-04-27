package hk.vincent.hangman;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by admin on 4/10/15.
 */
public class CustomFontButton extends Button {
    public CustomFontButton(Context context) {
        super(context);
        init();
    }

    public CustomFontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomFontButton(Context context, AttributeSet attrs, int defStyleAttr) {
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
