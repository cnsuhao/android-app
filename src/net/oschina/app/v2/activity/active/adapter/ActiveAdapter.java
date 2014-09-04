package net.oschina.app.v2.activity.active.adapter;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.oschina.app.R;
import net.oschina.app.bean.Active;
import net.oschina.app.bean.Tweet;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.ui.text.MyLinkMovementMethod;
import net.oschina.app.v2.ui.text.MyURLSpan;
import net.oschina.app.v2.ui.text.TweetTextView;
import android.annotation.SuppressLint;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ActiveAdapter extends ListBaseAdapter {
	private final static String AT_HOST_PRE = "http://my.oschina.net";
	private final static String MAIN_HOST = "http://www.oschina.net";

	@SuppressLint("InflateParams")
	@Override
	protected View getRealView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		if (convertView == null || convertView.getTag() == null) {
			convertView = getLayoutInflater(parent.getContext()).inflate(
					R.layout.v2_list_cell_active, null);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		Active item = (Active) _data.get(position);

		vh.name.setText(item.getAuthor());

		vh.action.setText(UIHelper.parseActiveAction2(item.getObjectType(),
				item.getObjectCatalog(), item.getObjectTitle()));

		if (TextUtils.isEmpty(item.getMessage())) {
			vh.body.setVisibility(View.GONE);
		} else {
			vh.body.setMovementMethod(MyLinkMovementMethod.a());
			vh.body.setFocusable(false);
			vh.body.setDispatchToParent(true);
			vh.body.setLongClickable(false);
			Spanned span = Html.fromHtml(modifyPath(item.getMessage()));
			vh.body.setText(span);
			MyURLSpan.parseLinkText(vh.body, span);
		}
		
		vh.time.setText(StringUtils.friendly_time(item.getPubDate()));

		vh.from.setVisibility(View.VISIBLE);
		switch (item.getAppClient()) {
		default:
			vh.from.setText("");
			break;
		case Tweet.CLIENT_MOBILE:
			vh.from.setText(R.string.from_mobile);
			break;
		case Tweet.CLIENT_ANDROID:
			vh.from.setText(R.string.from_android);
			break;
		case Tweet.CLIENT_IPHONE:
			vh.from.setText(R.string.from_iphone);
			break;
		case Tweet.CLIENT_WINDOWS_PHONE:
			vh.from.setText(R.string.from_windows_phone);
			break;
		case Tweet.CLIENT_WECHAT:
			vh.from.setText(R.string.from_wechat);
			break;
		}

		if (item.getCommentCount() > 0) {
			vh.commentCount.setText(String.valueOf(item.getCommentCount()));
			vh.commentCount.setVisibility(View.VISIBLE);
		} else {
			vh.commentCount.setVisibility(View.GONE);
		}
		if (item.getActiveType() == Active.CATALOG_OTHER) {
			vh.retweetCount.setVisibility(View.VISIBLE);
		} else {
			vh.retweetCount.setVisibility(View.GONE);
		}
		
		String faceURL = item.getFace();
		if (faceURL.endsWith("portrait.gif") || StringUtils.isEmpty(faceURL)) {
			vh.avatar.setImageBitmap(null);
		} else {
			ImageLoader.getInstance().displayImage(item.getFace(), vh.avatar);
		}
		
		return convertView;
	}

	private String modifyPath(String message) {
		message = message.replaceAll("(<a[^>]+href=\")/([\\S]+)\"", "$1"
				+ AT_HOST_PRE + "/$2\"");
		message = message.replaceAll(
				"(<a[^>]+href=\")http://m.oschina.net([\\S]+)\"", "$1"
						+ MAIN_HOST + "$2\"");
		return message;
	}

	static class ViewHolder {
		public TextView name, from, time, action, actionName, commentCount,
				retweetCount;
		public TweetTextView body;
		public ImageView avatar;

		public ViewHolder(View view) {
			name = (TextView) view.findViewById(R.id.tv_name);
			from = (TextView) view.findViewById(R.id.tv_from);
			body = (TweetTextView) view.findViewById(R.id.tv_body);
			time = (TextView) view.findViewById(R.id.tv_time);
			action = (TextView) view.findViewById(R.id.tv_action);
			actionName = (TextView) view.findViewById(R.id.tv_action_name);
			commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
			retweetCount = (TextView) view.findViewById(R.id.tv_retweet_count);
			avatar = (ImageView) view.findViewById(R.id.iv_avatar);
		}
	}
}
