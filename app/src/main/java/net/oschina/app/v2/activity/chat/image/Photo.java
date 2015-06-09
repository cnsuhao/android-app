package net.oschina.app.v2.activity.chat.image;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Photo implements Parcelable {
    private String type;
    private String url;
    private String alt;
    private int width;
    private int height;

    public Photo() {
    }

    public Photo(Parcel source) {
        type = source.readString();
        url = source.readString();
        alt = source.readString();
        width = source.readInt();
        height = source.readInt();
    }

    public Photo(String path) {
        this.url = path;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(url);
        dest.writeString(alt);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public static Photo make(JSONObject json) throws JSONException {
        Photo photo = new Photo();
        photo.setType(json.optString("type"));
        photo.setUrl(json.getString("url"));
        photo.setAlt(json.optString("alt"));
        return photo;
    }

    public static ArrayList<Photo> makeAll(JSONArray array)
            throws JSONException {
        ArrayList<Photo> list = new ArrayList<Photo>();
        final int size = array.length();
        for (int i = 0; i < size; i++) {
            list.add(make(array.getJSONObject(i)));
        }
        return list;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }

        @Override
        public Photo createFromParcel(Parcel source) {
            return new Photo(source);
        }
    };
}
