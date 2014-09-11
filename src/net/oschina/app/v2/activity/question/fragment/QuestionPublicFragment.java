package net.oschina.app.v2.activity.question.fragment;

import net.oschina.app.AppContext;
import net.oschina.app.bean.Post;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.service.ServerTaskUtils;
import net.oschina.app.v2.ui.dialog.CommonDialog;
import net.oschina.app.v2.ui.dialog.DialogHelper;
import net.oschina.app.v2.utils.TDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

public class QuestionPublicFragment extends BaseFragment {

	private EditText mEtTitle, mEtContent;
	private TextView mTvCategory;
	private CheckBox mCbLetMeKnow;

	private String[] mCategoryOptions;
	private int mCategory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		int mode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
				| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
		getActivity().getWindow().setSoftInputMode(mode);
		mCategoryOptions = getResources().getStringArray(
				R.array.post_pub_options);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.public_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.public_menu_send:
			handleSubmit();
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.v2_fragment_question_public,
				container, false);

		initView(view);
		return view;
	}

	private void initView(View view) {
		mEtTitle = (EditText) view.findViewById(R.id.et_title);
		mTvCategory = (TextView) view.findViewById(R.id.tv_category);
		mCategory = AppContext.getLastQuestionCategoryIdx();
		mTvCategory.setText(getString(R.string.question_public_category,
				mCategoryOptions[mCategory]));
		view.findViewById(R.id.ly_category).setOnClickListener(this);
		mEtContent = (EditText) view.findViewById(R.id.et_content);
		mCbLetMeKnow = (CheckBox) view.findViewById(R.id.cb_let_me_know);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ly_category) {
			handleShowCategory();
		}
	}

	private void handleShowCategory() {
		final CommonDialog dialog = DialogHelper
				.getPinterestDialogCancelable(getActivity());
		dialog.setTitle(R.string.category);
		dialog.setItems(mCategoryOptions, mCategory, new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				dialog.dismiss();
				mCategory = position;
				mTvCategory.setText(getString(
						R.string.question_public_category,
						mCategoryOptions[mCategory]));
			}
		});
		dialog.setNegativeButton(R.string.cancle, null);
		dialog.show();
	}

	private boolean prepareForSubmit() {
		if (!TDevice.hasInternet()) {
			AppContext.showToastShort(R.string.tip_network_error);
			return false;
		}
		if (!AppContext.instance().isLogin()) {
			UIHelper.showLogin(getActivity());
			return false;
		}
		String title = mEtTitle.getText().toString().trim();
		if (TextUtils.isEmpty(title.trim())) {
			AppContext.showToastShort(R.string.tip_title_empty);
			mEtTitle.requestFocus();
			return false;
		}
		String content = mEtContent.getText().toString().trim();
		if (TextUtils.isEmpty(content.trim())) {
			AppContext.showToastShort(R.string.tip_content_empty);
			mEtContent.requestFocus();
			return false;
		}
		return true;
	}

	private void handleSubmit() {
		if (!prepareForSubmit()) {
			return;
		}
		String title = mEtTitle.getText().toString().trim();
		String content = mEtContent.getText().toString().trim();
		Post post = new Post();
		post.setAuthorId(AppContext.instance().getLoginUid());
		post.setTitle(title);
		post.setBody(content);
		post.setCatalog(mCategory + 1);
		if (mCbLetMeKnow.isChecked())
			post.setIsNoticeMe(1);
		ServerTaskUtils.publicPost(getActivity(), post);
		getActivity().finish();
	}
}
