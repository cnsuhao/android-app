package net.oschina.app.v2.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public final class aa {

	public static float a(TextView textview) {
		float f = 0.0F;
		if (textview != null) {
			float f1;
			CharSequence charsequence = textview.getText();
			if (TextUtils.isEmpty(charsequence)) {
				f1 = 0.0F;
				// break MISSING_BLOCK_LABEL_64;
			} else {
				f1 = textview.getPaint().measureText(charsequence.toString());
			}
			int i;
			int j;
			i = textview.getPaddingLeft();
			j = textview.getPaddingRight();
			f = f1 + (float) (j + i);
		}// goto _L2; else goto _L1
		return f;
	}

	public static int a(Context context) {
		return (int) TypedValue.applyDimension(1, 40F, context.getResources()
				.getDisplayMetrics());
	}

	public static void a(Activity activity, int ai[]) {
		if (ai != null && activity != null) {
			int i = ai.length;
			int j = 0;
			while (j < i) {
				View view = activity.findViewById(ai[j]);
				if (view != null)
					view.setOnClickListener((android.view.View.OnClickListener) activity);
				j++;
			}
		}
	}

	public static void a(Dialog dialog, int ai[]) {
		if (ai != null && dialog != null) {
			int i = ai.length;
			int j = 0;
			while (j < i) {
				View view = dialog.findViewById(ai[j]);
				if (view != null)
					view.setOnClickListener((android.view.View.OnClickListener) dialog);
				j++;
			}
		}
	}

	public static void a(View view) {
		if (view != null && (view.getParent() instanceof ViewGroup))
			((ViewGroup) view.getParent()).removeView(view);
	}

	public static void a(View view, int i) {
		if (view != null)
			view.setVisibility(i);
	}

	public static void a(View view,
			android.view.View.OnClickListener onclicklistener, int ai[]) {
		if (ai != null && view != null) {
			int i = ai.length;
			int j = 0;
			while (j < i) {
				View view1 = view.findViewById(ai[j]);
				if (view1 != null)
					view1.setOnClickListener(onclicklistener);
				j++;
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static Bitmap b(View view) {
		Bitmap bitmap = null;
		int i;
		int j;
		view.measure(android.view.View.MeasureSpec.makeMeasureSpec(0, 0),
				android.view.View.MeasureSpec.makeMeasureSpec(0, 0));
		i = view.getMeasuredWidth();
		j = view.getMeasuredHeight();
		view.layout(0, 0, i, j);
		view.setDrawingCacheEnabled(false);
		view.setWillNotCacheDrawing(true);
		view.setDrawingCacheEnabled(false);
		view.setWillNotCacheDrawing(true);
		Bitmap bitmap1;
		if (i <= 0 || j <= 0) {
			bitmap1 = null;
		} else {
			bitmap1 = Bitmap.createBitmap(i, j,
					android.graphics.Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap1);
			if (android.os.Build.VERSION.SDK_INT >= 11)
				view.setLayerType(1, null);
			view.draw(canvas);
		}// goto _L2; else goto _L1
		bitmap = bitmap1;
		return bitmap;
	}
}
