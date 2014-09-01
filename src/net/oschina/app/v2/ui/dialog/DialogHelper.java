package net.oschina.app.v2.ui.dialog;

import net.oschina.app.R;
import android.app.Activity;
import android.content.Context;

public class DialogHelper {
	
	public static BaseDialog getPinterestDialog(Context context) {
		return new BaseDialog(context, R.style.dialog_common);
	}

	public static BaseDialog getPinterestDialogCancelable(Context context) {
		BaseDialog dialog = new BaseDialog(context,
				R.style.dialog_common);
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	public static WaitDialog getWaitDialog(Activity activity, int message) {
		WaitDialog dialog = null;
		try {
			dialog = new WaitDialog(activity, R.style.dialog_waiting);
			dialog.setMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dialog;
	}

	public static WaitDialog getWaitDialog(Activity activity, String message) {
		WaitDialog dialog = null;
		try {
			dialog = new WaitDialog(activity, R.style.dialog_waiting);
			dialog.setMessage(message);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return dialog;
	}

	public static WaitDialog getCancelableWaitDialog(Activity activity,
			String message) {
		WaitDialog dialog = null;
		try {
			dialog = new WaitDialog(activity, R.style.dialog_waiting);
			dialog.setMessage(message);
			dialog.setCancelable(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return dialog;
	}

}
