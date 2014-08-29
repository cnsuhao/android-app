package net.oschina.app.v2.activity.news.adapter;

import net.oschina.app.R;
import net.oschina.app.bean.News;
import net.oschina.app.common.StringUtils;
import net.oschina.app.v2.base.ListBaseAdapter;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NewsAdapter extends ListBaseAdapter {

	@SuppressLint("InflateParams")
	@Override
	protected View getRealView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		if (convertView == null || convertView.getTag() == null) {
			convertView = getLayoutInflater(parent.getContext()).inflate(
					R.layout.v2_list_cell_news, null);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		News news = (News) _data.get(position);

		vh.title.setText(news.getTitle());
		vh.source.setText(news.getAuthor());
		vh.time.setText(StringUtils.friendly_time(news.getPubDate()));
		return convertView;
	}

	static class ViewHolder {
		public TextView title, source, time;

		public ViewHolder(View view) {
			title = (TextView) view.findViewById(R.id.tv_title);
			source = (TextView) view.findViewById(R.id.tv_source);
			time = (TextView) view.findViewById(R.id.tv_time);
		}
	}
}
