package net.oschina.app.v2.base;

import net.oschina.app.R;
import net.oschina.app.v2.ui.dialog.DialogControl;
import net.oschina.app.v2.ui.dialog.DialogHelper;
import net.oschina.app.v2.ui.dialog.BaseToast;
import net.oschina.app.v2.ui.dialog.WaitDialog;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

public class BaseActivity extends ActionBarActivity implements DialogControl,
		VisibilityControl, View.OnClickListener {
	private boolean _isVisible;
	private WaitDialog _waitDialog;

	@Override
	protected void onPause() {
		_isVisible = false;
		hideWaitDialog();
		super.onPause();
	}

	@Override
	protected void onResume() {
		_isVisible = true;
		super.onResume();
	}

	public void showToast(int msgResid, int icon, int gravity) {
		showToast(getString(msgResid), icon, gravity);
	}

	public void showToast(String message, int icon, int gravity) {
		BaseToast toast = new BaseToast(this);
		toast.setMessage(message);
		toast.setMessageIc(icon);
		toast.setLayoutGravity(gravity);
		toast.show();
	}

	@Override
	public WaitDialog showWaitDialog() {
		return showWaitDialog(R.string.loading);
	}

	@Override
	public WaitDialog showWaitDialog(int resid) {
		return showWaitDialog(getString(resid));
	}

	@Override
	public WaitDialog showWaitDialog(String message) {
		if (_isVisible) {
			if (_waitDialog == null) {
				_waitDialog = DialogHelper.getWaitDialog(this, message);
			}
			if (_waitDialog != null) {
				_waitDialog.setMessage(message);
				_waitDialog.show();
			}
			return _waitDialog;
		}
		return null;
	}

	@Override
	public void hideWaitDialog() {
		if (_isVisible && _waitDialog != null) {
			try {
				_waitDialog.dismiss();
				_waitDialog = null;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {
	}

	@Override
	public boolean isVisible() {
		return _isVisible;
	}
}
