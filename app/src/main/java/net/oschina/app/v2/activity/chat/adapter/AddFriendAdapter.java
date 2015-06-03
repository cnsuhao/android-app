package net.oschina.app.v2.activity.chat.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.ui.AvatarView;

/**
 * Created by Tonlin on 2015/6/1.
 */
public class AddFriendAdapter extends RecycleBaseAdapter {

    public interface OnAddUserCallback {
        void onAddUser(IMUser user);
    }

    private OnAddUserCallback mCallback;

    public AddFriendAdapter(OnAddUserCallback callback) {
        mCallback = callback;
    }

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_chat_add_friend, null);
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);

        final IMUser user = (IMUser) getItem(position);

        ViewHolder vh = (ViewHolder) holder;
        vh.name.setText(user.getName());

        //ImageLoader.getInstance().displayImage(user.getPhoto(), vh.icon);
        vh.icon.setAvatarUrl(user.getPhoto());
        vh.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onAddUser(user);
                }
            }
        });
    }

    public static class ViewHolder extends RecycleBaseAdapter.ViewHolder {

        private TextView name, add;
        private AvatarView icon;

        public ViewHolder(int viewType, View v) {
            super(viewType, v);
            name = (TextView) v.findViewById(R.id.tv_name);
            add = (TextView) v.findViewById(R.id.tv_add);
            icon = (AvatarView) v.findViewById(R.id.iv_avatar);
        }
    }
}
