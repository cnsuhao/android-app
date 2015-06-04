package net.oschina.app.v2.activity.chat.adapter;

import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewAnimator;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.ui.AvatarView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tonlin on 2015/6/4.
 */
public class ContactSearchAdapter extends ListBaseAdapter {

    private List<IMUser> mSelecteds = new ArrayList<>();

    public void toggle(int position) {
        IMUser item = (IMUser) getItem(position);
        if (item.isSelected()) {
            mSelecteds.remove(item);
        } else {
            mSelecteds.add(item);
        }
        item.setSelected(!item.isSelected());
        notifyDataSetChanged();
    }

    public List<IMUser> getSelecteds() {
        return mSelecteds;
    }

    public void setSelecteds(List<IMUser> list){
        mSelecteds = list;
    }

    @Override
    protected View getRealView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null || convertView.getTag() == null) {
            convertView = getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_chat_add_group, null);
            vh = new ViewHolder(convertView);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            vh.title.setText("联系人");
            vh.title.setVisibility(View.VISIBLE);
        } else {
            vh.title.setVisibility(View.GONE);
        }
        IMUser item = (IMUser) getItem(position);
        vh.name.setText(item.getName());
        vh.avatar.setAvatarUrl(item.getPhoto());
        vh.selected.setChecked(item.isSelected());
        return convertView;
    }

    static class ViewHolder {
        TextView name, title;
        AvatarView avatar;
        AppCompatCheckBox selected;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.tv_name);
            avatar = (AvatarView) view.findViewById(R.id.iv_avatar);
            title = (TextView) view.findViewById(R.id.group_title);
            selected = (AppCompatCheckBox) view.findViewById(R.id.cb_selected);
        }
    }
}
