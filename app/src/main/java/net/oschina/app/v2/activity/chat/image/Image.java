package net.oschina.app.v2.activity.chat.image;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {

    private int id;
    private String title;
    private String displayName;
    private String mimeType;
    private String path;
    private String thumb;
    private long size;

    public Image(int id, String title, String displayName, String mimeType,
                 String path, long size) {
        super();
        this.id = id;
        this.title = title;
        this.displayName = displayName;
        this.mimeType = mimeType;
        this.path = path;
        this.size = size;
    }

    public Image() {
    }

    public Image(Parcel source) {
        path = source.readString();
        thumb = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(thumb);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }

        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }
    };
}
