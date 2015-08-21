package net.oschina.app.v2.activity.user.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Blog;
import net.oschina.app.v2.utils.StringUtils;

/**
 * Created by Tonlin on 2015/8/21.
 */
public class UserBlogAdapter extends RecycleBaseAdapter {

    public UserBlogAdapter(){
    }

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_news, null);
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        UserBlogAdapter.ViewHolder vh = (ViewHolder) holder;
        Blog item = (Blog) _data.get(position);

        vh.title.setText(item.getTitle());
        vh.source.setText(item.getAuthor());
        vh.time.setText(StringUtils.friendly_time(item.getPubDate()));
    }

    public static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
        public TextView title, source, time;

        public ViewHolder(int viewType,View view) {
            super(viewType,view);
            title = (TextView) view.findViewById(R.id.tv_title);
            source = (TextView) view.findViewById(R.id.tv_source);
            time = (TextView) view.findViewById(R.id.tv_time);
        }
    }
}
