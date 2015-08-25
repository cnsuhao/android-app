package net.oschina.app.v2.activity.friend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.model.Friend;
import net.oschina.app.v2.ui.AvatarView;

import java.util.List;

/**
 * Created by Tonlin on 2015/8/25.
 */
public class SearchFriendAdapter extends ListBaseAdapter {

    private final List<Friend> selecteds;

    public SearchFriendAdapter(List<Friend> selecteds) {
        this.selecteds = selecteds;
    }

    @Override
    protected View getRealView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.v2_list_cell_select_friend, null);

            holder = new ViewHolder();
            holder.group_title = (TextView) view.findViewById(R.id.group_title);
            holder.name = (TextView) view.findViewById(R.id.tv_name);
            holder.icon = (AvatarView) view.findViewById(R.id.iv_icon);
            holder.item = view.findViewById(R.id.ly_item);
            holder.selected = (CheckBox) view.findViewById(R.id.cb_selected);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.group_title.setVisibility(View.GONE);

        Friend item = (Friend) _data.get(position);

        holder.name.setText(item.getName());
        holder.icon.setAvatarUrl(item.getPortrait());

        holder.selected.setSelected(selecteds.contains(item));
        holder.selected.setVisibility(View.VISIBLE);
        return view;
    }

    public static class ViewHolder {
        public View item;
        public TextView group_title;
        public TextView name;
        public AvatarView icon;
        public CheckBox selected;
    }
}
