package net.oschina.app.v2.activity.friend.adapter;

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

public class FriendAdapter extends ListBaseAdapter {

	@SuppressLint("InflateParams")
	@Override
	protected View getRealView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		if (convertView == null || convertView.getTag() == null) {
			convertView = getLayoutInflater(parent.getContext()).inflate(
					R.layout.v2_list_cell_friend, null);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

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
