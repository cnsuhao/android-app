package net.oschina.app.v2.activity.chat.text;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Toast;

import net.oschina.app.v2.model.URLs;

/**
 * Created by Tonlin on 2015/6/8.
 */
public class MyURLSpan extends ClickableSpan {
    String url;
    public MyURLSpan(String p_Url) {
        super();
        this.url = p_Url;
    }

    public void updateDrawState(TextPaint p_DrawState) {
        super.updateDrawState(p_DrawState);
        p_DrawState.setUnderlineText(false);
    }

    @Override
    public void onClick(View widget) {
        //super.onClick(widget);
        //Toast.makeText(widget.getContext(), "click:" + url, Toast.LENGTH_SHORT).show();
        if(url.startsWith("mailto:")){

        } else if(url.startsWith("http://")|| url.startsWith("https://")){

        } else if(url.startsWith("tel:")) {

        }

        Uri uri = Uri.parse(url);
        Context context = widget.getContext();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        context.startActivity(intent);
    }
}