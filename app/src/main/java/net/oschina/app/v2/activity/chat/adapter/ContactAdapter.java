package net.oschina.app.v2.activity.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.fragment.MySectionIndexer;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.UserRelation;
import net.oschina.app.v2.ui.AvatarView;
import net.oschina.app.v2.ui.pinned.PinnedHeaderListView;

import java.util.List;

public class ContactAdapter extends BaseAdapter implements PinnedHeaderListView.PinnedHeaderAdapter,
        OnScrollListener {

    private List<IMUser> mList;
    private MySectionIndexer mIndexer;
    private Context mContext;
    private int mLocationPosition = -1;
    private LayoutInflater mInflater;
    private DisplayImageOptions options;

    public ContactAdapter(List<IMUser> mList, MySectionIndexer mIndexer,
                          Context mContext) {
        this.mList = mList;
        this.mIndexer = mIndexer;
        this.mContext = mContext;
        mInflater = LayoutInflater.from(mContext);
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.v2_list_cell_chat_contact, null);

            holder = new ViewHolder();
            holder.group_title = (TextView) view.findViewById(R.id.group_title);
            holder.name = (TextView) view.findViewById(R.id.tv_name);
            holder.icon = (AvatarView) view.findViewById(R.id.iv_icon);
            holder.item = view.findViewById(R.id.ly_item);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        IMUser item = mList.get(position);

        int section = mIndexer.getSectionForPosition(position);
        if (mIndexer.getPositionForSection(section) == position) {
            holder.group_title.setVisibility(View.VISIBLE);
            holder.group_title.setText(item.getSortKey());
        } else {
            holder.group_title.setVisibility(View.GONE);
        }

        if (position == 0) {
            holder.group_title.setVisibility(View.GONE);
            holder.name.setText(R.string.new_friend);
            holder.icon.setImageBitmap(null);
            holder.icon.setImageResource(R.drawable.ic_contact_add);
        } else if (position == 1) {
            holder.group_title.setVisibility(View.GONE);
            holder.name.setText(R.string.group_chat);
            holder.icon.setImageBitmap(null);
            holder.icon.setImageResource(R.drawable.ic_contact_group);
        } else {
            holder.name.setText(item.getName());
            //ImageLoader.getInstance().displayImage(displayUser.getPhoto(), holder.icon, options);
            holder.icon.setAvatarUrl(item.getPhoto());
        }
        return view;
    }

    public static Uri getResourceUri(int resId) {
        return Uri.parse("android.resource://" + AppContext.instance().getPackageName() + "/" + resId);
    }

    public static class ViewHolder {
        public View item;
        public TextView group_title;
        public TextView name;
        public AvatarView icon;
    }

    @Override
    public int getPinnedHeaderState(int position) {
        if (position == 0 || position == 1) {
            return PINNED_HEADER_GONE;
        }
        int realPosition = position;
        if (realPosition < 0
                || (mLocationPosition != -1 && mLocationPosition == realPosition)) {
            return PINNED_HEADER_GONE;
        }
        mLocationPosition = -1;
        int section = mIndexer.getSectionForPosition(realPosition);
        int nextSectionPosition = mIndexer.getPositionForSection(section + 1);
        if (nextSectionPosition != -1
                && realPosition == nextSectionPosition - 1) {
            return PINNED_HEADER_PUSHED_UP;
        }
        return PINNED_HEADER_VISIBLE;
    }

    @Override
    public void configurePinnedHeader(View header, int position, int alpha) {
        // TODO Auto-generated method stub
        int realPosition = position;
        int section = mIndexer.getSectionForPosition(realPosition);
        if (section > 0) {
            String title = (String) mIndexer.getSections()[section];
            ((TextView) header).setText(title);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
        if (view instanceof PinnedHeaderListView) {
            ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
        }
    }

    public void setIndexer(MySectionIndexer indexer) {
        mIndexer = indexer;
    }

    public void setData(List<IMUser> data) {
        mList = data;
    }
}
