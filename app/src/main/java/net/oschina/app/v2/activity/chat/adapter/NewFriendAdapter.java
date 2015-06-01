package net.oschina.app.v2.activity.chat.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.chat.Invite;

/**
 * Created by Tonlin on 2015/6/1.
 */
public class NewFriendAdapter extends RecycleBaseAdapter {
    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_contact, null);
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType,view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        Invite item = (Invite) getItem(position);

        ViewHolder vh = (ViewHolder)holder;
        vh.name.setText(item.getFrom().getName());
        ImageLoader.getInstance().displayImage(item.getFrom().getPhoto(), vh.icon);
        vh.title.setVisibility(View.GONE);
    }

    public static class ViewHolder extends RecycleBaseAdapter.ViewHolder {

        private TextView title, name;
        private ImageView icon;

        public ViewHolder(int viewType, View v) {
            super(viewType, v);
            title = (TextView) v.findViewById(R.id.group_title);
            icon = (ImageView) v.findViewById(R.id.iv_icon);
            name = (TextView) v.findViewById(R.id.tv_name);
        }
    }
}
