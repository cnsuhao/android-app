package net.oschina.app.v2.activity.question.adapter;

import net.oschina.app.R;
import net.oschina.app.bean.Post;
import net.oschina.app.common.StringUtils;
import net.oschina.app.v2.base.ListBaseAdapter;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class QuestionAdapter extends ListBaseAdapter {

	@SuppressLint("InflateParams")
	@Override
	protected View getRealView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		if (convertView == null || convertView.getTag() == null) {
			convertView = getLayoutInflater(parent.getContext()).inflate(
					R.layout.v2_list_cell_post, null);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		Post item = (Post) _data.get(position);

		vh.title.setText(item.getTitle());
		vh.source.setText(item.getAuthor());
		vh.avCount.setText(item.getAnswerCount()+"/"+item.getViewCount());
		vh.time.setText(StringUtils.friendly_time(item.getPubDate()));
		return convertView;
	}

	static class ViewHolder {
		public TextView title, source,avCount, time;
		public ImageView avatar;
		public ViewHolder(View view) {
			title = (TextView) view.findViewById(R.id.tv_title);
			source = (TextView) view.findViewById(R.id.tv_source);
			avCount = (TextView) view.findViewById(R.id.tv_answer_view_count);
			time = (TextView) view.findViewById(R.id.tv_time);
			avatar = (ImageView)view.findViewById(R.id.iv_avatar);
		}
	}
}
