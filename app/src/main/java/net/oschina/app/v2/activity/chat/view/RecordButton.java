package net.oschina.app.v2.activity.chat.view;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.chat.recoder.AudioMediaRecorder;
import net.oschina.app.v2.activity.chat.recoder.MediaRecorderOnErrorListener;
import net.oschina.app.v2.activity.chat.recoder.MediaRecorderOnProgressListener;
import net.oschina.app.v2.utils.RecordUtils;
import net.oschina.app.v2.utils.TLog;

import java.io.File;

/**
 * Created by Sim on 2015/6/4.
 */
public class RecordButton extends Button {
    private static final double MAX_RECORD_AUDIO_TIME = 120 * 1000;
    private static final java.lang.String TAG = "RecordButton";
    private long mStartTime;
    private Dialog mTipDialog;
    private AudioMediaRecorder mMediaRecorder;
    private String mRecordFilePath;
    private ObtainDecibelThread mAnimThread;
    private ShowVolumeHandler mAnimHandler;
    private OnFinishedRecordListener mCallback;
    private static int[] ANIM_RESOURCES = {
            R.drawable.ic_chat_recorder_v1,
            R.drawable.ic_chat_recorder_v2,
            R.drawable.ic_chat_recorder_v3,
            R.drawable.ic_chat_recorder_v4,
            R.drawable.ic_chat_recorder_v5,
            R.drawable.ic_chat_recorder_v6,
            R.drawable.ic_chat_recorder_v7
    };

    private MediaRecorderOnProgressListener onRecordProgressListener = new MediaRecorderOnProgressListener() {

        @Override
        public void onProgress(long progress) {

        }
    };

    private MediaRecorderOnErrorListener onRecordErrorListener = new MediaRecorderOnErrorListener() {

        @Override
        public void onError(MediaRecorder arg0, int arg1, int arg2) {
            handleError();
        }

        @Override
        public void onError(int i, String s) {
            handleError();
        }

        private void handleError() {

        }
    };


    public RecordButton(Context context) {
        this(context, null);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.v2_dialog_recorder, null);
        mTipDialog = new Dialog(context,R.style.chat_recorder_style);
        mTipDialog.setContentView(view);
        WindowManager.LayoutParams lp = mTipDialog.getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;

        ImageView mAnimView = (ImageView) view.findViewById(R.id.iv_anim);
        mAnimHandler = new ShowVolumeHandler(mAnimView);

        mMediaRecorder = new AudioMediaRecorder();
        mMediaRecorder.setMaxRecordTime(MAX_RECORD_AUDIO_TIME);
        mMediaRecorder.setOnProgressListener(onRecordProgressListener);
        mMediaRecorder.setOnErrorListener(onRecordErrorListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartTime = System.currentTimeMillis();
                // start record
                mTipDialog.show();
                mRecordFilePath = RecordUtils.getNewRecordFile();
                File file = new File(mRecordFilePath);
                File parent = file.getParentFile();
                parent.mkdirs();
                mMediaRecorder.start(mRecordFilePath);

                mAnimThread = new ObtainDecibelThread();
                mAnimThread.start();
                break;
            case MotionEvent.ACTION_UP:
                //finish record
                mAnimThread.exit();
                mTipDialog.dismiss();
                mMediaRecorder.stop();

                if(mCallback!= null){
                    mCallback.onFinishedRecord(mRecordFilePath);
                }
                break;
            case MotionEvent.ACTION_CANCEL:// 当手指移动到view外面，会cancel
                //cancelRecord();
                break;
        }
        return true;
    }

    public void setRecorderCallback(OnFinishedRecordListener callback){
        mCallback = callback;
    }

    private class ObtainDecibelThread extends Thread {

        private volatile boolean running = true;

        public void exit() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mMediaRecorder == null || !running) {
                    break;
                }
                int amplitude = mMediaRecorder.getMaxAmplitude();
                if (amplitude != 0) {
                   // int f = (int) (10 * Math.log(amplitude) / Math.log(10));
                    double f = 0.8f + ((2.0 * Math.log10(amplitude / 20)) / 10.0) * 9.0f;
                    TLog.log(TAG,"f:"+f);
                    if (f < 3.5)
                        mAnimHandler.sendEmptyMessage(0);
                    else if (f < 4)
                        mAnimHandler.sendEmptyMessage(1);
                    else if (f < 4.5)
                        mAnimHandler.sendEmptyMessage(2);
                    else if (f < 5.0)
                        mAnimHandler.sendEmptyMessage(3);
                    else if (f < 5.5)
                        mAnimHandler.sendEmptyMessage(4);
                    else if (f < 6)
                        mAnimHandler.sendEmptyMessage(5);
                    else
                        mAnimHandler.sendEmptyMessage(6);
                }
            }
        }
    }

    static class ShowVolumeHandler extends Handler {
        private ImageView animView;

        ShowVolumeHandler(ImageView animView){
            this.animView = animView;
        }

        @Override
        public void handleMessage(Message msg) {
            animView.setImageResource(ANIM_RESOURCES[msg.what]);
        }
    }

    public interface OnFinishedRecordListener {
        public void onFinishedRecord(String audioPath);
    }
}
