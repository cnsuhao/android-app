package net.oschina.app.v2.activity.blog.adapter;

import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Blog;
import net.oschina.app.v2.utils.DateUtil;
import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

public class BlogAdapter extends RecycleBaseAdapter {

    @Override
    public View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_news,null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType,view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder)holder;
        Blog item = (Blog) _data.get(position);
        vh.title.setText(item.getTitle());
        vh.source.setText(item.getAuthor());
        vh.time.setText(DateUtil.getFormatTime(item.getPubDate()));

        vh.tip.setVisibility(View.VISIBLE);
        if (item.getDocumentType() == Blog.DOC_TYPE_ORIGINAL) {
            vh.tip.setImageResource(R.drawable.ic_source);
        } else {
            vh.tip.setImageResource(R.drawable.ic_forward);
        }
        vh.commentCount.setText(vh.commentCount.getContext().getString(
                R.string.comment_count, item.getCommentCount()));

        vh.body.setText(TextUtils.isEmpty(item.getBody())?"":item.getBody().trim());
        if(TextUtils.isEmpty(item.getBody()))
            vh.body.setVisibility(View.GONE);
        else
            vh.body.setVisibility(View.VISIBLE);
    }

    static class ViewHolder extends RecycleBaseAdapter.ViewHolder{
		public TextView title, source, time, commentCount,body;
		public ImageView tip;

		public ViewHolder(int viewType,View view) {
            super(viewType,view);
            body = (TextView) view.findViewById(R.id.tv_body);
			title = (TextView) view.findViewById(R.id.tv_title);
			source = (TextView) view.findViewById(R.id.tv_source);
			time = (TextView) view.findViewById(R.id.tv_time);
			commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
			tip = (ImageView) view.findViewById(R.id.iv_tip);
		}
	}
}
