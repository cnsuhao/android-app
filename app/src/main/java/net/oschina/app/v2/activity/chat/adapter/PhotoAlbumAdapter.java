package net.oschina.app.v2.activity.chat.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.chat.image.DirectoryEntity;

import java.util.ArrayList;
import java.util.List;

public class PhotoAlbumAdapter extends BaseAdapter {

    private List<DirectoryEntity> list = new ArrayList<DirectoryEntity>();
    private DisplayImageOptions options;
    private int mSelectedIdx;

    public PhotoAlbumAdapter(ArrayList<DirectoryEntity> fileList) {
        list = fileList;
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHodler vh = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.v2_list_cell_photo_album, null);
            vh = new ViewHodler(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHodler) convertView.getTag();
        }

        DirectoryEntity de = list.get(position);

        vh.name.setText(de.getName() + "(" + de.getCount() + ")");
        vh.desc.setText(de.getPath());

        //vh.name.setTextColor(parent.getResources().getColor(mSelectedIdx == position?R.color.main_green:R.color.black));

        //vh.icon.setBackgroundResource(R.drawable.ic_default_pic);
        ImageLoader.getInstance().displayImage(
                "file:///" + de.getHeadImagePath(), vh.icon, options,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                                                  Bitmap loadedImage) {
                        ((ImageView) view)
                                .setBackgroundResource(R.color.light_gray);
                    }
                });
        return convertView;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("NON_DROPDOWN")) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.
                    v2_list_cell_photo_album_bar, parent, false);
            convertView.setTag("NON_DROPDOWN");
        }
        TextView textView = (TextView) convertView.findViewById(R.id.tv_name);
        textView.setText(list.get(position).getName());
        return convertView;
    }

    public void setSelectedIdx(int i) {
        mSelectedIdx = i;
        notifyDataSetChanged();
    }

    static class ViewHodler {
        ImageView icon;
        TextView name, desc;

        public ViewHodler(View view) {
            icon = (ImageView) view.findViewById(R.id.iv_icon);
            name = (TextView) view.findViewById(R.id.tv_name);
            desc = (TextView) view.findViewById(R.id.tv_desc);
        }
    }
}
