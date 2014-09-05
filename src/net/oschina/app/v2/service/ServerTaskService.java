package net.oschina.app.v2.service;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpResponseHandler;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.v2.api.remote.NewsApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompatExtras;
import android.support.v4.app.NotificationManagerCompat;
import android.util.SparseIntArray;

public class ServerTaskService extends IntentService {

	public static final String ACTION_PUBLIC_COMMENT = "net.oschina.app.v2.ACTION_PUBLIC_COMMENT";

	public static final String BUNDLE_PUBLIC_COMMENT_TASK = "BUNDLE_PUBLIC_COMMENT_TASK";

	// public static SparseIntArray pendings = new SparseIntArray();

	private AsyncHttpResponseHandler mPublicCommentHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			int id = 1;// task.getId() * task.getUid();
			// cancellNotification(id);
			notifySimpleNotifycation(id, "成功发表评论", "评论", "成功发表评论", false, true);
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					cancellNotification(1);
				}
			}, 3000);
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			int id = 1;// task.getId() * task.getUid()
			notifySimpleNotifycation(id, "发布评论失败", "评论", "发布评论失败", false, true);
		}

		public void onFinish() {
			stopSelf();
		}
	};

	public ServerTaskService() {
		this("ServerTaskService");
	}

	public ServerTaskService(String name) {
		super(name);
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if (ACTION_PUBLIC_COMMENT.equals(action)) {
			PublicCommentTask task = intent
					.getParcelableExtra(BUNDLE_PUBLIC_COMMENT_TASK);
			if (task != null) {
				publicComment(task);
			}
		}
	}

	private void publicComment(final PublicCommentTask task) {
		// task.getId() * task.getUid()
		notifySimpleNotifycation(1, "正在发表你的评论..", "评论", "正在发布评论", true, false);
		NewsApi.publicComment(task.getCatalog(), task.getId(), task.getUid(),
				task.getContent(), task.getIsPostToMyZone(),
				mPublicCommentHandler);
	}

	private void notifySimpleNotifycation(int id, String ticker, String title,
			String content, boolean ongoing, boolean autoCancel) {
		Notification notification = new NotificationCompat.Builder(this)
				.setTicker(ticker).setContentTitle(title)
				.setContentText(content).setAutoCancel(true)
				.setOngoing(false)
				.setDefaults(Notification.DEFAULT_SOUND)
				.setOnlyAlertOnce(true)
				.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0))
				.setSmallIcon(R.drawable.icon).build();
		// if (autoCancel) {
		// notification.flags = Notification.FLAG_AUTO_CANCEL;
		//notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		// }
		// NotificationManagerCompat.from(this).notify(id, notification);
		NotificationManager notifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifyMgr.notify(id, notification);
	}

	private void cancellNotification(int id) {
		NotificationManagerCompat.from(this).cancel(id);
	}
}
