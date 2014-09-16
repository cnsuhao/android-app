package net.oschina.app.v2.activity.news.view;

import net.oschina.app.v2.ui.dialog.CommonDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.tonlin.osc.happy.R;

public class ShareDialog extends CommonDialog {

	private ShareDialog(Context context, boolean flag, OnCancelListener listener) {
		super(context, flag, listener);
	}

	private ShareDialog(Context context, int defStyle) {
		super(context, defStyle);
		View shareView = getLayoutInflater().inflate(
				R.layout.v2_dialog_cotent_share, null);
		setContent(shareView, 0);
	}

	public ShareDialog(Context context) {
		this(context, R.style.dialog_bottom);
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		getWindow().setGravity(Gravity.BOTTOM);

		WindowManager m = getWindow().getWindowManager();
		Display d = m.getDefaultDisplay();
		WindowManager.LayoutParams p = getWindow().getAttributes();
		p.width = d.getWidth();
		getWindow().setAttributes(p);
	}
}
