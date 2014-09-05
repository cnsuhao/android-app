package net.oschina.app.v2.activity.friend.adapter;

import net.oschina.app.bean.FriendList.Friend;
import net.oschina.app.v2.base.ListBaseAdapter;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

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

		Friend item = (Friend) _data.get(position);

		vh.name.setText(item.getName());
		vh.desc.setText(item.getExpertise());
		vh.gender.setImageResource(item.getGender() == 1 ? R.drawable.list_male
				: R.drawable.list_female);

		return convertView;
	}

	static class ViewHolder {
		public TextView name, desc;
		public ImageView gender;
		public ImageView avatar;

		public ViewHolder(View view) {
			name = (TextView) view.findViewById(R.id.tv_name);
			desc = (TextView) view.findViewById(R.id.tv_desc);
			gender = (ImageView) view.findViewById(R.id.iv_gender);
			avatar = (ImageView) view.findViewById(R.id.iv_avatar);
		}
	}
}
