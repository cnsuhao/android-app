package net.oschina.app.v2.activity.chat.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.image.Image;
import net.oschina.app.v2.activity.chat.image.Photo;
import net.oschina.app.v2.utils.TDevice;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends BaseAdapter {

    private int screenWidth;
    private List<Image> list = new ArrayList<>();
    private ArrayList<Image> selectedList = new ArrayList<>();
    private DisplayImageOptions options;
    private boolean mSingleSelect = false;
    private int mMaxSelectedCount = -1;

    public PhotoAdapter(int screenWidth, boolean single) {
        this.screenWidth = screenWidth;
        this.mSingleSelect = single;
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    public void setMaxSelectedCount(int count) {
        mMaxSelectedCount = count;
    }

    @Override
    public int getCount() {
        return list.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(ArrayList<Image> data) {
        this.list = data;
        notifyDataSetChanged();
    }

    public boolean addImage(Image img) {
        boolean found = false;
        //if(mSingleSelect)
        //   selectedList.clear();
        for (Image i : selectedList) {
            if (i.getPath().equals(img.getPath())) {
                found = true;
                return true;
            }
        }
        if (!found) {
            selectedList.add(img);
        }
        //notifyDataSetChanged();
        return false;
    }

    public boolean removeImage(Image img) {
        for (Image i : selectedList) {
            if (i.getPath().equals(img.getPath())) {
                selectedList.remove(i);
                return true;
            }
        }
        //notifyDataSetChanged();
        return false;
    }

    public int toggleSelect(int position) {
        Image img = list.get(position);
        if (hasImage(img) != -1) {
            removeImage(img);
            return -1;
        } else {
            if (mMaxSelectedCount != -1
                    && getSelectedCount() == mMaxSelectedCount
                    && !mSingleSelect) {
                AppContext.showToastShort(R.string.tip_max_select_picture);
                return -1;
            }
            addImage(img);
            return 1;
        }
    }


    public void toogleView(GridView gv, boolean check, int itemIndex) {
        //        Image img = list.get(itemIndex);
        //        int visiblePosition = gv.getFirstVisiblePosition();
        //        //只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
        //        for(int i= visiblePosition;i<= itemIndex;i++) {
        //            //得到要更新的item的view
        //            View view = gv.getChildAt(itemIndex - visiblePosition);
        //            //从view中取得holder
        //            ViewHolder holder = (ViewHolder) view.getTag();
        //            Integer p = (Integer) holder.ck.getTag();
        //            if (itemIndex == p.intValue()) {
        //                AppContext.showToastShort(p + ""+ holder.ck.isChecked());
        //                holder.ck.setChecked(check);
        //            }
        //        }
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedList.size();
    }

    public ArrayList<Image> getSelectedImages() {
        return selectedList;
    }

    public ArrayList<Photo> getSelectedPhotos() {
        ArrayList<Photo> list = new ArrayList<Photo>();
        for (Image img : selectedList) {
            Photo photo = new Photo();
            photo.setUrl(img.getPath());
            list.add(photo);
        }
        return list;
    }

    public String[] getSelectedImageUrls() {
        String[] list = new String[selectedList.size()];
        int i = 0;
        for (Image img : selectedList) {
            list[i++] =  "file:///"+img.getPath();
        }
        return list;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.v2_grid_cell_select_pic, null);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        int widthAndHeight = (int) (screenWidth - TDevice.dpToPixel(2)) / 3;
        LayoutParams lp = new LayoutParams(widthAndHeight, widthAndHeight);
        convertView.setLayoutParams(lp);

        if (position == 0) {
            vh.icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            vh.icon.setBackgroundResource(R.color.light_gray);
            vh.icon.setImageResource(R.drawable.ic_photo_camera);
            vh.ck.setVisibility(View.GONE);
            return convertView;
        }

        vh.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        vh.icon.setBackgroundResource(R.color.transparent);
        vh.ck.setVisibility(View.VISIBLE);

        Image image = list.get(position - 1);
        int idx = hasImage(image);
        if (idx != -1) {
            //vh.mask.setVisibility(View.VISIBLE);
            // vh.count.setVisibility(View.VISIBLE);
            // vh.count.setText(String.valueOf(idx + 1));
            vh.ck.setChecked(true);
        } else {
            // vh.mask.setVisibility(View.GONE);
            // vh.count.setVisibility(View.GONE);
            // vh.count.setText("");
            vh.ck.setChecked(false);
        }
        vh.ck.setTag(position);

        vh.icon.setImageBitmap(null);
        //vh.icon.setBackgroundResource(R.drawable.ic_default_pic);
        String path = (String) vh.icon.getTag();
        String p = "file:///" + image.getThumb();
        //if (!p.equals(path) || vh.icon.getDrawable()==null) {
        ImageLoader.getInstance().displayImage(p,
                vh.icon, options, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                                                  Bitmap loadedImage) {
                        //((ImageView) view).setBackgroundResource(R.color.light_gray);
                    }
                });
        //}
        vh.icon.setTag(p);
        return convertView;
    }

    public int hasImage(int idx) {
        Image img = list.get(idx);
        return hasImage(img);
    }

    public int hasImage(Image img) {
        for (int i = 0; i < selectedList.size(); i++) {
            Image image = selectedList.get(i);
            if (image.getPath().equals(img.getPath())) {
                return i;
            }
        }
        return -1;
    }

    public void clearSelect() {
        if (mSingleSelect) {
            selectedList.clear();
        }
    }

    static class ViewHolder {

        ImageView icon;
        TextView count;
        View mask;
        View content;
        CheckBox ck;

        public ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.iv_pic);
            count = (TextView) view.findViewById(R.id.tv_count);
            mask = view.findViewById(R.id.mask);
            content = view.findViewById(R.id.rl_content);
            ck = (CheckBox) view.findViewById(R.id.cb_check);
        }
    }
}
