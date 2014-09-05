package net.oschina.app.v2.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ServerTaskUtils {

	public static void publicComment(Context context, PublicCommentTask task) {
		Intent intent = new Intent(ServerTaskService.ACTION_PUBLIC_COMMENT);
		Bundle bundle = new Bundle();
		bundle.putParcelable(ServerTaskService.BUNDLE_PUBLIC_COMMENT_TASK, task);
		intent.putExtras(bundle);
		context.startService(intent);
	}
}
