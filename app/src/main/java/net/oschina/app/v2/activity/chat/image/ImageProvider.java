package net.oschina.app.v2.activity.chat.image;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Images;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImageProvider implements AbstractProvider {

    public ImageProvider(Context context1) {
        context = context1;
    }

    public Map<String, ArrayList<Image>> getImages() {
        LinkedHashMap<String, ArrayList<Image>> maps = new LinkedHashMap<String, ArrayList<Image>>();
        if (context == null) {
            return maps;
        }
        Cursor cursor = context.getContentResolver().query(
                Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                Images.Media.BUCKET_DISPLAY_NAME + " asc,"
                        + Images.Media.DATE_MODIFIED + " desc");
        if (cursor == null) {
            return maps;
        }
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor
                    .getColumnIndexOrThrow(Images.Media.BUCKET_ID));
            String title = cursor.getString(cursor
                    .getColumnIndexOrThrow(Images.Media.TITLE));
            String path = cursor.getString(cursor
                    .getColumnIndexOrThrow(Images.Thumbnails.DATA));
            String thumb = cursor.getString(cursor
                    .getColumnIndexOrThrow(Images.Thumbnails.DATA));
            String displayName = cursor.getString(cursor
                    .getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME));
            String mimeType = cursor.getString(cursor
                    .getColumnIndexOrThrow(Images.Media.MIME_TYPE));
            long size = cursor.getLong(cursor
                    .getColumnIndexOrThrow(Images.Media.SIZE));
            Image image = new Image(id, title, displayName, mimeType, path, size);
            image.setThumb(thumb);
            if (new File(image.getPath()).exists()) {
                String p = image.getPath().substring(0, image.getPath().lastIndexOf("/"));
                if (!maps.containsKey(p)) {
                    ArrayList<Image> list = new ArrayList<Image>();
                    list.add(image);
                    maps.put(p, list);
                } else {
                    ArrayList<Image> list = maps.get(p);
                    Iterator<Image> iterator = list.iterator();
                    boolean found = false;
                    while (iterator.hasNext()) {
                        if (iterator.next().getPath().equals(image.getPath())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        maps.get(p).add(image);
                    }
                }
            }
        }
        cursor.close();
        return maps;
    }

    public List<Image> getList() {
        ArrayList<Image> list = new ArrayList<Image>();
        if (context == null) {
            return list;
        }
        Cursor cursor = context.getContentResolver().query(
                Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor
                        .getColumnIndexOrThrow(Images.Media.BUCKET_ID));
                String title = cursor.getString(cursor
                        .getColumnIndexOrThrow(Images.Media.TITLE));
                String data = cursor.getString(cursor
                        .getColumnIndexOrThrow(Images.Thumbnails.DATA));
                list.add(new Image(
                        id,
                        title,
                        cursor.getString(cursor
                                .getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME)),
                        cursor.getString(cursor
                                .getColumnIndexOrThrow(Images.Media.MIME_TYPE)),
                        data, cursor.getLong(cursor
                        .getColumnIndexOrThrow(Images.Media.SIZE))));
            }
            cursor.close();
        }
        return list;
    }

    private Context context;
}
