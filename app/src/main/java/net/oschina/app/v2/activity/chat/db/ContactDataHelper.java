package net.oschina.app.v2.activity.chat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import net.oschina.app.v2.model.chat.IMGroup;
import net.oschina.app.v2.model.chat.IMUser;

import java.util.HashMap;
import java.util.Map;


public class ContactDataHelper {

    public static final String TABLE_NAME_USER = "user_table";

    public static final String COLUMN_U_ID = "id";//
    public static final String COLUMN_U_NAME_NICK = "nick"; //
    public static final String COLUMN_U_PHOTO = "photo";
    public static final String COLUMN_IM_USERNAME = "im_username";
    public static final String COLUMN_DB_USERNAME = "db_username";
    public static final String COLUMN_U_CACHE_TIME = "cache_time";

    public static final String TABLE_NAME_GROUP = "groups_table";

    public static final String COLUMN_GROUP_ID = "group_id";//群组ID
    public static final String COLUMN_GROUP_NAME = "group_name";//群组名称
    public static final String COLUMN_GROUP_PHOTO = "photo";//群组名称
    public static final String COLUMN_GROUP_IMID = "group_im_id";
    public static final String COLUMN_GROUP_CACHE_TIME = "cache_time";


    private Context mContext;
    private EsDatabaseHelper.DatabaseWrapper mDb;

    public ContactDataHelper(Context context) {
        mContext = context;
        mDb = null;
    }

    public Context getContext() {
        return mContext;
    }

    public void initialize() {
        if (mDb == null) {
            mDb = EsDatabaseHelper.getDatabaseHelper(mContext)
                    .getWritableDatabaseWrapper();
        }
    }

    public void beginTransaction() {
        initialize();
        mDb.beginTransaction();
    }

    public void setTransactionSuccessful() {
        mDb.setTransactionSuccessful();
    }

    public void endTransaction() {
        if (mDb != null)
            mDb.endTransaction();
    }

    public void yieldTransaction() {
        mDb.yieldTransaction();
    }

    public void addOrUpdateUser(IMUser user) {
        if (user != null) {
            IMUser f = queryUserByIMUsername(user.getImUserName());
            if (f != null) {
                updateUser(user);
                return;
            }
            ContentValues values = userToContentValue(user);
            mDb.insert(TABLE_NAME_USER, null, values);
        }
    }

    public void updateUser(IMUser user) {
        ContentValues values = userToContentValue(user);
        mDb.update(TABLE_NAME_USER, values, COLUMN_IM_USERNAME + "=?", new String[]{user.getImUserName()});
    }

    public int deleteUserByIMUsername(String id) {
        return mDb.delete(TABLE_NAME_USER, COLUMN_IM_USERNAME + "=?",
                new String[]{id});
    }

    public IMUser queryUserByIMUsername(String id) {
        Cursor cursor = null;
        try {
            cursor = mDb.query(TABLE_NAME_USER, null, COLUMN_IM_USERNAME + "=?",
                    new String[]{id}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    return cursorToUser(cursor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public Map<String, IMUser> queryAllUsers() {
        Map<String, IMUser> list = new HashMap<>();
        Cursor cursor = null;
        try {
            cursor = mDb.query(TABLE_NAME_USER, null, null, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        IMUser user = cursorToUser(cursor);
                        list.put(user.getImUserName(), user);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }


    private IMUser cursorToUser(Cursor cursor) {
        IMUser user = new IMUser();


        return user;
    }

    private ContentValues userToContentValue(IMUser user) {
        ContentValues values = new ContentValues();


        return values;
    }


    /**
     * **************************************************************************
     */

    public void addOrUpdateGroup(IMGroup group) {
        if (group != null) {
            IMGroup g = queryGroupByImId(group.getImId());
            if (g != null) {
                updateGroup(group);
                return;
            }
            ContentValues values = groupToContentValue(group);
            mDb.insert(TABLE_NAME_GROUP, null, values);
        }
    }

    public void updateGroup(IMGroup group) {
        ContentValues values = groupToContentValue(group);
        mDb.update(TABLE_NAME_GROUP, values, COLUMN_GROUP_IMID + "=?", new String[]{group.getImId()});
    }

    public int deleteGroupByImId(String id) {
        return mDb.delete(TABLE_NAME_GROUP, COLUMN_GROUP_IMID + "=?",
                new String[]{id});
    }

    public IMGroup queryGroupByImId(String id) {
        Cursor cursor = null;
        try {
            cursor = mDb.query(TABLE_NAME_GROUP, null, COLUMN_GROUP_IMID + "=?",
                    new String[]{id}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    return cursorToGroup(cursor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private IMGroup cursorToGroup(Cursor cursor) {
        IMGroup group = new IMGroup();


        return group;
    }

    private ContentValues groupToContentValue(IMGroup group) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_ID, group.getId());
        values.put(COLUMN_GROUP_NAME, group.getName());
        return values;
    }
}
