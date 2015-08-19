package net.oschina.app.v2.activity.user.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.WriterException;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.ui.dialog.CommonDialog;
import net.oschina.app.v2.utils.QrCodeUtils;

/**
 * Created by Tonlin on 2015/8/19.
 */
public class QrCodeDialog extends CommonDialog {
    private ImageView mIvCode;
    private Bitmap bitmap;

    public QrCodeDialog(Context context) {
        super(context, R.style.dialog_common);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }


    @Override
    protected void init(Context context) {
        super.init(context);
        View view = getLayoutInflater()
                .inflate(R.layout.v2_dialog_my_qrcode, null);

        mIvCode = (ImageView) view.findViewById(R.id.iv_qr_code);
        try {
            bitmap = QrCodeUtils.Create2DCode(String.format(
                    "http://my.oschina.net/u/%s", AppContext.instance()
                            .getLoginUid()));
            mIvCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (bitmap != null && !bitmap.isRecycled())
                    bitmap.recycle();
            }
        });
        setContent(view, 0);
    }
}
