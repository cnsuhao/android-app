package net.oschina.app.v2.activity.chat.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.chat.EMConversation;
import com.easemob.chat.MessageBody;
import com.easemob.chat.TextMessageBody;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;

/**
 * Created by Tonlin on 2015/5/27.
 */
public class ConversationAdapter extends RecycleBaseAdapter {

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_chat_conversation, null);
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        EMConversation item = (EMConversation) getItem(position);

        ViewHolder vh = (ViewHolder)holder;
        vh.name.setText(item.getUserName());
        MessageBody body =  item.getLastMessage().getBody();
        if(body instanceof TextMessageBody) {
            vh.message.setText(((TextMessageBody)body).getMessage());
        }
    }

    public static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
        private TextView name,message,time;
        public ViewHolder(int viewType, View v) {
            super(viewType, v);
            name = (TextView) v.findViewById(R.id.tv_name);
            message = (TextView)v.findViewById(R.id.tv_message);
            time = (TextView)v.findViewById(R.id.tv_time);
        }
    }
}
