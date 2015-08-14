package net.oschina.app.v2.activity.event.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Event;
import net.oschina.app.v2.utils.ImageUtils;

public class EventAdapter extends RecycleBaseAdapter {
    private DisplayImageOptions options;

    public EventAdapter() {
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(
                R.layout.v2_list_cell_event, null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        ViewHolder vh = (ViewHolder)holder;
        final Event item = (Event) _data.get(position);

        ImageLoader.getInstance().displayImage(item.getCover(),vh.image, options);

        vh.title.setText(item.getTitle());
        vh.time.setText(item.getStartTime());
        vh.spot.setText(item.getSpot());
    }

	static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
		public TextView title, time, spot;
		public ImageView image;
		public ImageView status;

		public ViewHolder(int viewType,View view) {
            super(viewType,view);
            title = (TextView) view.findViewById(R.id.tv_event_title);
            time = (TextView) view.findViewById(R.id.tv_event_time);
            spot = (TextView) view.findViewById(R.id.tv_event_spot);
            image = (ImageView) view.findViewById(R.id.iv_event_img);
            status = (ImageView) view.findViewById(R.id.iv_event_status);
		}
	}
}
