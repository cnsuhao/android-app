package net.oschina.app.v2.activity.chat.text;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Layout;
import android.text.Selection;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Tonlin on 2015/6/8.
 */
public class SuperTextView extends TextView {

    public SuperTextView(Context context) {
        super(context);
    }

    public SuperTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SuperTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SuperTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        TextView widget = this;
        CharSequence text = getText();
        if(text instanceof SpannableString) {
            SpannableString buffer = (SpannableString) text;
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= getTotalPaddingLeft();
                y -= getTotalPaddingTop();

                x += getScrollX();
                y += getScrollY();
                Layout layout = getLayout();
                int line = layout.getLineForVertical(y);
                int offset = layout.getOffsetForHorizontal(line, x);

                float width = layout.getLineWidth(line);
                if (y > width) {
                    offset = y;
                }

                ClickableSpan[] link = buffer.getSpans(offset, offset, ClickableSpan.class);
                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(buffer, buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                        //return true;
                    }
                } else {
                    Selection.removeSelection(buffer);
                   // if (action == MotionEvent.ACTION_UP && listener != null) {
                   //     listener.onTextClicked();
                   // }
                   // return false;
                }
            }
        }
        return super.onTouchEvent(event);
    }

}
