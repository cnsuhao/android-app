package net.oschina.app.v2.activity.chat.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.chat.IMGroup;

/**
 * Created by Tonlin on 2015/6/2.
 */
public class GroupAdapter extends RecycleBaseAdapter {

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_chat_group,null);
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType,view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        ViewHolder vh = (ViewHolder)holder;

        IMGroup item = (IMGroup) getItem(position);
        ImageLoader.getInstance().displayImage(item.getPhoto(),vh.avatar);
        vh.name.setText(item.getName());
    }

    public static class ViewHolder extends RecycleBaseAdapter.ViewHolder {

        private TextView name;
        private ImageView avatar;

        public ViewHolder(int viewType, View v) {
            super(viewType, v);
            name = (TextView) v.findViewById(R.id.tv_name);
            avatar = (ImageView) v.findViewById(R.id.iv_avatar);
        }
    }
}
