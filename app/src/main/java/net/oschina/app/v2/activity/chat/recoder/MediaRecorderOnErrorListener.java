package net.oschina.app.v2.activity.chat.recoder;


public interface MediaRecorderOnErrorListener extends android.media.MediaRecorder.OnErrorListener {

	public abstract void onError(int i, String s);
}
