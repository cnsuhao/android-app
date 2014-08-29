package net.oschina.app.v2.activity.news.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.oschina.app.R;
import net.oschina.app.v2.base.ListBaseAdapter;

public class NewsAdapter extends ListBaseAdapter {

	@Override
	public int getCount() {
		return 20;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater infalter = LayoutInflater.from(parent.getContext());
		if(convertView == null){
			convertView = infalter.inflate(R.layout.v2_list_cell_news, null);
		}
		return convertView;
	}
}
