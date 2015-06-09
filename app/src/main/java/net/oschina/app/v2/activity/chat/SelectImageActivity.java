package net.oschina.app.v2.activity.chat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.adapter.PhotoAdapter;
import net.oschina.app.v2.activity.chat.adapter.PhotoAlbumAdapter;
import net.oschina.app.v2.activity.chat.image.DirectoryEntity;
import net.oschina.app.v2.activity.chat.image.Image;
import net.oschina.app.v2.activity.chat.image.ImageProvider;
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.utils.ImageUtils;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Tonlin on 2015/6/9.
 */
public class SelectImageActivity extends BaseActivity implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {
    public static final String BUBDLE_KEY_PHOTOS = "bundle_key_photos";
    public static final String BUBDLE_KEY_FILE = "bundle_key_file";
    public static final String BUBDLE_KEY_FROM = "bundle_key_from";
    public static final String BUBDLE_KEY_NEED_CROP = "bundle_key_crop";
    public static final String BUBDLE_KEY_SINGLE_SELECT = "bundle_key_single";
    public static final String BUBDLE_KEY_MAX_SELECTED = "bundle_key_max";

    public static final int FROM_TYPE_LIB = 1;
    public static final int FROM_TYPE_CAMERA = 2;

    public static final int REQUEST_CODE_CAMERA = 101;
    public static final int REQUEST_CODE_CROP = 102;

    private static final int DEFAULT_MAX_SELECT_IMAGE = 10;
    private static final int LOADER_LIB = 1;
    private static final int LOADER_PHOTOS = 2;

    private ArrayList<DirectoryEntity> mDirList = new ArrayList<>();
    private ArrayList<Image> mSelectedImages = new ArrayList<>();
    private GridView mGvImage;
    private ImageProvider mImageProvider;
    private PhotoAlbumAdapter mAdapter;
    private boolean mSingleSelect = false;
    private int mMaxSelectedCount = DEFAULT_MAX_SELECT_IMAGE;
    private PhotoAdapter mPhotoAdapter;
    private DirectoryEntity mCurrentDir;
    private Spinner mSpinner;
    private Button mBtnOk;
    private TextView mTvPreview;
    private File mCameraFile;

    @Override
    protected int getLayoutId() {
        return R.layout.v2_activity_chat_select_image;
    }

    @Override
    protected int getActionBarTitle() {
        return R.string.chat_select_image;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    protected int getActionBarCustomView() {
        return R.layout.v2_actionbar_custom_chat_add_group;
    }

    @Override
    protected void initActionBar(Toolbar actionBar) {
        super.initActionBar(actionBar);
        mBtnOk = (Button) actionBar.findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);
        mBtnOk.setText(R.string.chat_send_zero);
        mBtnOk.setEnabled(false);
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        mTvPreview = (TextView) findViewById(R.id.tv_preview);
        mTvPreview.setOnClickListener(this);

        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(this);

        mGvImage = (GridView) findViewById(R.id.gv_image);
        mGvImage.setOnItemClickListener(this);

        mImageProvider = new ImageProvider(this);
        mAdapter = new PhotoAlbumAdapter(mDirList);

        mPhotoAdapter = new PhotoAdapter((int) TDevice.getScreenWidth(), mSingleSelect);
        mPhotoAdapter.setMaxSelectedCount(mMaxSelectedCount);
        mGvImage.setAdapter(mPhotoAdapter);

        mSpinner.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(LOADER_LIB, null, SelectImageActivity.this);
    }

    private void loadPhotos() {
        //mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
        getSupportLoaderManager().restartLoader(LOADER_PHOTOS, null, this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_preview) {
            UIHelper.showImagePreview(this, mPhotoAdapter.getSelectedImageUrls());
        } else if (v.getId() == R.id.btn_ok) {
            Intent data = new Intent();
            data.putExtra(BUBDLE_KEY_FROM, FROM_TYPE_LIB);
            data.putExtra(BUBDLE_KEY_PHOTOS, mPhotoAdapter.getSelectedPhotos());
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            selectPicFromCamera();
            return;
        }

        mPhotoAdapter.clearSelect();
        int check = mPhotoAdapter.toggleSelect(position - 1);
        mPhotoAdapter.toogleView(mGvImage, check > 0, position - 1);
        int size = mPhotoAdapter.getSelectedCount();
        String str;
        if (size > 0) {
            str = getString(R.string.chat_send_other, size);
        } else {
            str = getString(R.string.chat_send_zero);
        }
        //String str = getResources().getQuantityString(R.plurals.done_format_num, size == 0 ? 0 : 1, size);
        mBtnOk.setText(str);
        mBtnOk.setEnabled(size > 0);
        mTvPreview.setEnabled(size > 0);
        mTvPreview.setText(size > 0 ? getString(R.string.chat_preview_other, size) : getString(R.string.chat_preview_zero));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        if (arg0 == LOADER_LIB) {
            return new CursorLoader(getApplicationContext(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            "count(1) length",
                            MediaStore.Images.Media.BUCKET_ID,
                            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                            MediaStore.Images.Media.DATA
                    },
                    "1=1) GROUP BY " + MediaStore.Images.Media.BUCKET_ID + " -- (",
                    null,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC," +
                            MediaStore.Images.Media.DATE_MODIFIED + " DESC"
            );
        } else if (arg0 == LOADER_PHOTOS) {
            return new CursorLoader(
                    getApplicationContext(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Thumbnails.DATA // 图片地址
                    }, mCurrentDir.getId() == -1 ? null : MediaStore.Images.Media.BUCKET_ID
                    + "=" + mCurrentDir.getId(), null,
                    MediaStore.Images.Media.DATE_MODIFIED + " desc");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        if (arg0.getId() == LOADER_PHOTOS) {
            if (cursor.getCount() > 0) {
                ArrayList<Image> list = new ArrayList<Image>();
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    Image photo = new Image();
                    photo.setThumb(cursor.getString(cursor
                            .getColumnIndex(MediaStore.Images.Thumbnails.DATA)));
                    photo.setPath(cursor.getString(cursor
                            .getColumnIndex(MediaStore.Images.Media.DATA)));
                    list.add(photo);
                }
                mPhotoAdapter.setData(list);
                //mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
            } else {
                //mEmptyLayout.setErrorType(EmptyLayout.NODATA);
            }
        } else {
            mDirList.clear();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                String dirPath;
                int index = path.lastIndexOf('/');
                if (index > 0) {
                    dirPath = path.substring(0, index);
                } else {
                    dirPath = path;
                }
                DirectoryEntity dir = new DirectoryEntity();
                dir.setId(id);
                dir.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
                dir.setPath(dirPath);
                dir.setHeadImagePath(path);
                dir.setCount(cursor.getInt(cursor.getColumnIndex("length")));
                mDirList.add(dir);
            }
            mAdapter.notifyDataSetChanged();

            if (mDirList != null && mDirList.size() > 0) {
                mCurrentDir = (DirectoryEntity) mAdapter.getItem(0);
                //updateTitle();
                loadPhotos();
            } else {
                //王韬修改，修复了无图片时拍照Item没显示出来的情况
                mPhotoAdapter.setData(new ArrayList<Image>());
                //mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.setSelectedIdx(position);
        mCurrentDir = (DirectoryEntity) mAdapter.getItem(position);
        //updateTitle();
        loadPhotos();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void selectPicFromCamera() {
        //新建一个照片文件，拍照完之后把数据都写进去
        mCameraFile = new File(Constants.CACHE_DIR,
                System.currentTimeMillis() + ".j");

        mCameraFile.getParentFile().mkdirs();
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraFile)),
                REQUEST_CODE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && REQUEST_CODE_CAMERA == requestCode) {
            //拍照完回到页面
            if (mCameraFile != null && mCameraFile.exists()) {
                int degree = ImageUtils.getBitmapDegree(mCameraFile.getAbsolutePath());
                if (degree != 0) {
                    new RorateBitmapTask(degree).execute();
                } else {
                    Intent d = new Intent();
                    d.putExtra(BUBDLE_KEY_FROM, FROM_TYPE_CAMERA);
                    d.putExtra(BUBDLE_KEY_FILE, mCameraFile.getAbsolutePath());
                    setResult(Activity.RESULT_OK, d);
                    finish();
                }
            }
        }
    }

    private class RorateBitmapTask extends AsyncTask<Void, Void, Void> {

        private int degree;

        public RorateBitmapTask(int degree) {
            this.degree = degree;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showWaitDialog();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bitmap bitmap = ImageUtils.decodeFile(mCameraFile, 200, 200);
            //bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
            Bitmap target = ImageUtils.rotateBitmapByDegree(bitmap, degree);
            try {
                ImageUtils.saveImageToSD(SelectImageActivity.this,
                        mCameraFile.getAbsolutePath(), target, 100);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideWaitDialog();
            Intent d = new Intent();
            d.putExtra(BUBDLE_KEY_FROM, FROM_TYPE_CAMERA);
            d.putExtra(BUBDLE_KEY_FILE, mCameraFile.getAbsolutePath());
            setResult(Activity.RESULT_OK, d);
            finish();
        }
    }
}
