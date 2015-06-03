package net.oschina.app.v2.activity.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.chat.fragment.MySectionIndexer;
import net.oschina.app.v2.model.chat.Avatar;
import net.oschina.app.v2.model.chat.IMUser;
import net.oschina.app.v2.model.chat.UserRelation;
import net.oschina.app.v2.ui.AvatarView;
import net.oschina.app.v2.ui.pinned.PinnedHeaderListView;

import java.util.ArrayList;
import java.util.List;

public class AddGroupAdapter extends BaseAdapter implements PinnedHeaderListView.PinnedHeaderAdapter,
        OnScrollListener ,Filterable{

    private List<IMUser> mSourceList = new ArrayList<>();
    private List<IMUser> mList;
    private MySectionIndexer mIndexer;
    private Context mContext;
    private int mLocationPosition = -1;
    private LayoutInflater mInflater;
    private DisplayImageOptions options;
    private List<IMUser> mSelecteds = new ArrayList<>();
    private UserNameFilter mFilter;

    public AddGroupAdapter(List<IMUser> mList, MySectionIndexer mIndexer,
                           Context mContext) {
        this.mSourceList = mList;
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
        if (position == 0) {
            convertView = mInflater.inflate(R.layout.v2_list_cell_chat_simple_text, null);
            return convertView;
        }
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.v2_list_cell_chat_add_group, null);

            holder = new ViewHolder();
            holder.group_title = (TextView) view.findViewById(R.id.group_title);
            holder.name = (TextView) view.findViewById(R.id.tv_name);
            holder.icon = (AvatarView) view.findViewById(R.id.iv_icon);
            holder.item = view.findViewById(R.id.ly_item);
            holder.selected = (CheckBox) view.findViewById(R.id.cb_selected);
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

        holder.icon.setImageBitmap(null);
        holder.name.setText(item.getName());
        holder.icon.setAvatarUrl(item.getPhoto());
        //ImageLoader.getInstance().displayImage(item.getPhoto(), holder.icon, options);
        holder.selected.setChecked(item.isSelected());
        return view;
    }

    public void toggle(int position) {
        IMUser item = (IMUser) getItem(position);
        if(item.isSelected()){
            mSelecteds.remove(item);
        } else {
            mSelecteds.add(item);
        }
        item.setSelected(!item.isSelected());
    }

    public List<IMUser> getSelecteds(){
        return mSelecteds;
    }

    @Override
    public Filter getFilter() {
        if (null == mFilter) {
            mFilter = new UserNameFilter();
        }
        return mFilter;
    }

    public static class ViewHolder {
        public View item;
        public TextView group_title;
        public TextView name;
        public AvatarView icon;
        public CheckBox selected;
    }

    @Override
    public int getPinnedHeaderState(int position) {
        if (position == 0) {
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

    public List<IMUser> getSourcList(){
        return mSourceList;
    }

    public void setData(List<IMUser> data) {
        mSourceList = data;
        mList = data;
    }

    public void setFilter(List<IMUser> list) {
        mList = list;
        notifyDataSetChanged();
    }

    // 自定义Filter类
    class UserNameFilter extends Filter {
        @Override
        // 该方法在子线程中执行
        // 自定义过滤规则
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            List<IMUser> newValues = new ArrayList<>();
            String filterString = constraint.toString().trim()
                    .toLowerCase();

            // 如果搜索框内容为空，就恢复原始数据
            if (TextUtils.isEmpty(filterString)) {
                newValues = mSourceList;
            } else {
                // 过滤出新数据
                for (IMUser user : mSourceList) {
                    if (-1 != user.getName().toLowerCase().indexOf(filterString)) {
                        newValues.add(user);
                    }
                }
            }

            results.values = newValues;
            results.count = newValues.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            mList = (List<IMUser>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();  // 通知数据发生了改变
            } else {
                notifyDataSetInvalidated(); // 通知数据失效
            }
        }
    }
}
