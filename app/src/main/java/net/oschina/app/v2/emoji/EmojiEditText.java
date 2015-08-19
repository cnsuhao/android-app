package net.oschina.app.v2.emoji;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

import net.oschina.app.v2.utils.TLog;

import java.util.regex.Pattern;

public class EmojiEditText extends EditText {

    public static final Pattern EMOJI = Pattern
            .compile("\\[(([\u4e00-\u9fa5]+)|([a-zA-z]+))\\]");
    public static final Pattern EMOJI_PATTERN = Pattern
            .compile("\\[[(0-9)]+\\]");
    private static final java.lang.String TAG = EmojiEditText.class.getSimpleName();

    public EmojiEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EmojiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmojiEditText(Context context) {
        super(context);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start,
                                 int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
//		Spannable sp = getText();
//		String str = getText().toString();
//		TLog.log(TAG,"before matcher EMOJI_PATTERN"+ DateUtil.getDateStr(System.currentTimeMillis(),"HH:mm:sss"));
//		Matcher m = EMOJI_PATTERN.matcher(str);
//		TLog.log(TAG,"end matcher EMOJI_PATTERN"+ DateUtil.getDateStr(System.currentTimeMillis(),"HH:mm:sss"));
//		while (m.find()) {
//			int s = m.start();
//			int e = m.end();
//			String value = m.group();
//			Emoji emoji = EmojiHelper.getEmojiByNumber(value);
//			if (emoji != null) {
//				sp.setSpan(new EmojiSpan(value, 30, 1), s, e,
//						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//			}
//		}
//		TLog.log(TAG,"end find EMOJI_PATTERN "+ DateUtil.getDateStr(System.currentTimeMillis(),"HH:mm:sss"));
//
//		TLog.log(TAG,"before matcher EMOJI"+ DateUtil.getDateStr(System.currentTimeMillis(),"HH:mm:sss"));
//		Matcher m2 = EMOJI.matcher(str);
//		TLog.log(TAG,"end matcher EMOJI"+ DateUtil.getDateStr(System.currentTimeMillis(),"HH:mm:sss"));
//		while (m2.find()) {
//			int s = m2.start();
//			int e = m2.end();
//			String value = m2.group();
//			Emoji emoji = EmojiHelper.getEmoji(value);
//			if (emoji != null) {
//				sp.setSpan(new EmojiSpan(value, 30, 0), s, e,
//						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//			}
//		}
//		TLog.log(TAG,"end find EMOJI"+ DateUtil.getDateStr(System.currentTimeMillis(),"HH:mm:sss"));
    }

    public void insertEmoji(Emoji emoji) {
        if (emoji == null)
            return;
        int start = getSelectionStart();
        int end = getSelectionEnd();
        String value = emoji.getValue2();
        if (start < 0) {
            //Log.e(TAG,"append:"+value);
            append(getDisplayEmoji(value));
        } else {
            //Log.e(TAG, "replace:" + value);
            Spannable str = getDisplayEmoji(value);
            getText().replace(Math.min(start, end), Math.max(start, end),
                    str, 0, str.length());
        }
    }

    private Spannable getDisplayEmoji(String value) {
        //TLog.log(TAG,"getDisplayEmoji:"+value);
        Spannable span = new SpannableString(value);
        Emoji emoji = EmojiHelper.getEmoji(value);
        if(emoji != null){
            //TLog.log(TAG, "find emoji :" + emoji.getValue());
            span.setSpan(new EmojiSpan(value, 30, 0), 0, value.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if(emoji == null) {
            emoji = EmojiHelper.getEmojiByNumber(value);
            if(emoji != null) {
                //TLog.log(TAG, "find number emoji :" + emoji.getValue());
                span.setSpan(new EmojiSpan(value, 30, 1), 0, value.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return span;
    }

    public void delete() {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0,
                0, KeyEvent.KEYCODE_ENDCALL);
        dispatchKeyEvent(event);
    }
}
