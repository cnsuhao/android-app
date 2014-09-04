package net.oschina.app.v2.activity.tweet.adapter;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.oschina.app.R;
import net.oschina.app.bean.Tweet;
import net.oschina.app.common.StringUtils;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.ui.text.MyLinkMovementMethod;
import net.oschina.app.v2.ui.text.MyURLSpan;
import net.oschina.app.v2.ui.text.TweetTextView;
import android.annotation.SuppressLint;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetAdapter extends ListBaseAdapter {

	@SuppressLint("InflateParams")
	@Override
	protected View getRealView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		if (convertView == null || convertView.getTag() == null) {
			convertView = getLayoutInflater(parent.getContext()).inflate(
					R.layout.v2_list_cell_tweet, null);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		Tweet item = (Tweet) _data.get(position);
		vh.name.setText(item.getAuthor());
		// vh.title.setLinkText(item.getBody());
		// vh.title.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());
		// vh.title.setLinkText(item.getBody());
		vh.title.setMovementMethod(MyLinkMovementMethod.a());
		vh.title.setFocusable(false);
		vh.title.setDispatchToParent(true);
		vh.title.setLongClickable(false);
		Spanned span = Html.fromHtml(item.getBody());
		vh.title.setText(span);
		MyURLSpan.parseLinkText(vh.title, span);

		vh.time.setText(StringUtils.friendly_time(item.getPubDate()));

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

		vh.commentCount.setText(String.valueOf(item.getCommentCount()));
		
		ImageLoader.getInstance().displayImage(item.getFace(), vh.avatar);
		return convertView;
	}

	static class ViewHolder {
		public TextView name, from, time, commentCount;
		public TweetTextView title;
		public ImageView avatar;

		public ViewHolder(View view) {
			name = (TextView) view.findViewById(R.id.tv_name);
			title = (TweetTextView) view.findViewById(R.id.tv_title);
			from = (TextView) view.findViewById(R.id.tv_from);
			time = (TextView) view.findViewById(R.id.tv_time);
			commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
			avatar = (ImageView) view.findViewById(R.id.iv_avatar);
		}
	}
}
