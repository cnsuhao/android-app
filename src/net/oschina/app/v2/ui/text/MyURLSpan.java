package net.oschina.app.v2.ui.text;

import net.oschina.app.bean.URLs;
import net.oschina.app.common.UIHelper;
import android.os.Parcel;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

public class MyURLSpan extends URLSpan {

	public MyURLSpan(Parcel src) {
		super(src);
	}

	public MyURLSpan(String url) {
		super(url);
	}

	@Override
	public void onClick(View widget) {
		URLs urls = URLs.parseURL(getURL());
		if (urls != null) {
			UIHelper.showLinkRedirect(widget.getContext(), urls.getObjType(),
					urls.getObjId(), urls.getObjKey());
		} else {
			UIHelper.openBrowser(widget.getContext(), getURL());
		}
	}

	public static void parseLinkText(TextView widget, Spanned spanhtml) {
		CharSequence text = widget.getText();
		if (text instanceof Spannable) {
			int end = text.length();
			Spannable sp = (Spannable) widget.getText();
			URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);

			URLSpan[] htmlurls = spanhtml != null ? spanhtml.getSpans(0, end,
					URLSpan.class) : new URLSpan[] {};

			if (urls.length == 0 && htmlurls.length == 0)
				return;

			SpannableStringBuilder style = new SpannableStringBuilder(text);
			// style.clearSpans();// 这里会清除之前所有的样式
			for (URLSpan url : urls) {
				if (!isNormalUrl(url)) {
					style.removeSpan(url);// 只需要移除之前的URL样式，再重新设置
					NoLinkURLSpan span = new NoLinkURLSpan(url.getURL());
					style.setSpan(span, sp.getSpanStart(url),
							sp.getSpanEnd(url),
							Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					continue;
				}
				style.removeSpan(url);// 只需要移除之前的URL样式，再重新设置
				MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
				style.setSpan(myURLSpan, sp.getSpanStart(url),
						sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			for (URLSpan url : htmlurls) {
				style.removeSpan(url);// 只需要移除之前的URL样式，再重新设置
				MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
				style.setSpan(myURLSpan, spanhtml.getSpanStart(url),
						spanhtml.getSpanEnd(url),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			widget.setText(style);
		}
	}

	public static boolean isNormalUrl(URLSpan url) {
		String urlStr = url.getURL();
		if (urlStr.endsWith(".sh")) {
			return false;
		}
		return true;
	}
}
