package net.oschina.app.v2.activity.chat.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.Invite;
import net.oschina.app.v2.ui.AvatarView;

/**
 * Created by Tonlin on 2015/6/1.
 */
public class NewFriendAdapter extends RecycleBaseAdapter {
    public interface OnOperationCallback {
        void onAcceptUser(Invite invite);
    }

    private OnOperationCallback mCallback;

    public NewFriendAdapter(OnOperationCallback mCallback) {
        this.mCallback = mCallback;
    }

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_chat_new_friend, null);
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        final Invite item = (Invite) getItem(position);

        ViewHolder vh = (ViewHolder) holder;
        vh.name.setText(item.getFrom().getName());
        vh.message.setText(item.getMessage());

        //ImageLoader.getInstance().displayImage(item.getFrom().getPhoto(), vh.avatar);
        vh.avatar.setAvatarUrl(item.getFrom().getPhoto());

        vh.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onAcceptUser(item);
                }
            }
        });
    }

    public static class ViewHolder extends RecycleBaseAdapter.ViewHolder {

        private TextView name, accept,message;
        private AvatarView avatar;

        public ViewHolder(int viewType, View v) {
            super(viewType, v);
            accept = (TextView) v.findViewById(R.id.tv_accept);
            avatar = (AvatarView) v.findViewById(R.id.iv_avatar);
            name = (TextView) v.findViewById(R.id.tv_name);
            message = (TextView) v.findViewById(R.id.tv_message);
        }
    }
}
