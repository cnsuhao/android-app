package net.oschina.app.v2.activity.comment.adapter;

import net.oschina.app.R;
import net.oschina.app.bean.Comment;
import net.oschina.app.bean.Tweet;
import net.oschina.app.common.StringUtils;
import net.oschina.app.v2.base.ListBaseAdapter;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CommentAdapter extends ListBaseAdapter {

	@SuppressLint("InflateParams")
	@Override
	protected View getRealView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		if (convertView == null || convertView.getTag() == null) {
			convertView = getLayoutInflater(parent.getContext()).inflate(
					R.layout.v2_list_cell_comment, null);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		Comment item = (Comment) _data.get(position);

		vh.name.setText(item.getAuthor());
		vh.content.setText(item.getContent());
		vh.time.setText(StringUtils.friendly_time(item.getPubDate()));

		vh.from.setVisibility(View.VISIBLE);
		switch (item.getAppClient()) {
		default:
			vh.from.setText("");
			vh.from.setVisibility(View.GONE);
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

		return convertView;
	}

	static class ViewHolder {
		TextView name, content, time, from;

		ViewHolder(View view) {
			name = (TextView) view.findViewById(R.id.tv_name);
			content = (TextView) view.findViewById(R.id.tv_content);
			time = (TextView) view.findViewById(R.id.tv_time);
			from = (TextView) view.findViewById(R.id.tv_from);
		}
	}
}
