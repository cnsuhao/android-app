package net.oschina.app.v2.activity.comment.fragment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

import net.oschina.app.AppContext;
import net.oschina.app.bean.BlogCommentList;
import net.oschina.app.bean.Comment;
import net.oschina.app.bean.CommentList;
import net.oschina.app.bean.ListEntity;
import net.oschina.app.bean.Result;
import net.oschina.app.common.UIHelper;
import net.oschina.app.v2.activity.comment.adapter.CommentAdapter;
import net.oschina.app.v2.activity.comment.adapter.CommentAdapter.OnOperationListener;
import net.oschina.app.v2.activity.news.fragment.NewsFragment;
import net.oschina.app.v2.api.remote.NewsApi;
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.base.BaseListFragment;
import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.emoji.EmojiFragment;
import net.oschina.app.v2.emoji.EmojiFragment.EmojiTextListener;
import net.oschina.app.v2.ui.dialog.CommonDialog;
import net.oschina.app.v2.ui.dialog.DialogHelper;

import org.apache.http.Header;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tonlin.osc.happy.R;

public class CommentFrament extends BaseListFragment implements
		OnOperationListener, EmojiTextListener {

	public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";
	public static final String BUNDLE_KEY_BLOG = "BUNDLE_KEY_BLOG";
	public static final String BUNDLE_KEY_ID = "BUNDLE_KEY_ID";
	public static final String BUNDLE_KEY_OWNER_ID = "BUNDLE_KEY_OWNER_ID";
	protected static final String TAG = NewsFragment.class.getSimpleName();
	private static final String BLOG_CACHE_KEY_PREFIX = "blogcomment_list";
	private static final String CACHE_KEY_PREFIX = "comment_list";
	private int mId, mOwnerId;
	private boolean mIsBlogComment;

	private EmojiFragment mEmojiFragment;
	private Comment mCurrentReplyComment;

	private AsyncHttpResponseHandler mDeleteHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				Result res = Result.parse(new ByteArrayInputStream(arg2));
				if (res.OK()) {
					AppContext.showToastShort(R.string.delete_success);
				} else {
					AppContext.showToastShort(res.getErrorMessage());
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(arg0, arg1, arg2, e);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			AppContext.showToastShort(R.string.delete_faile);
		}
	};

	private AsyncHttpResponseHandler mReplyCommentHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				Result res = Result.parse(new ByteArrayInputStream(arg2));
				if (res.OK()) {
					AppContext.showToastShort(R.string.comment_success);
				} else {
					AppContext.showToastShort(res.getErrorMessage());
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(arg0, arg1, arg2, e);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			AppContext.showToastShort(R.string.comment_faile);
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		BaseActivity act = ((BaseActivity) activity);
		FragmentTransaction trans = act.getSupportFragmentManager()
				.beginTransaction();
		mEmojiFragment = new EmojiFragment();
		mEmojiFragment.setEmojiTextListener(this);
		trans.replace(R.id.emoji_container, mEmojiFragment);
		trans.commit();
		activity.findViewById(R.id.emoji_container).setVisibility(View.GONE);
	}

	protected int getLayoutRes() {
		return R.layout.v2_fragment_pull_refresh_listview;
	}

	public void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			mCatalog = args.getInt(BUNDLE_KEY_CATALOG, 0);
			mId = args.getInt(BUNDLE_KEY_ID, 0);
			mOwnerId = args.getInt(BUNDLE_KEY_OWNER_ID, 0);
			mIsBlogComment = args.getBoolean(BUNDLE_KEY_BLOG, false);
		}

		if (!mIsBlogComment && mCatalog == CommentList.CATALOG_POST) {
			((BaseActivity) getActivity())
					.setActionBarTitle(R.string.post_answer);
		}

		int mode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
				| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
		getActivity().getWindow().setSoftInputMode(mode);
	}

	@Override
	protected ListBaseAdapter getListAdapter() {
		return new CommentAdapter(this);
	}

	@Override
	protected String getCacheKeyPrefix() {
		String str = mIsBlogComment ? BLOG_CACHE_KEY_PREFIX : CACHE_KEY_PREFIX;
		return new StringBuilder(str).append("_").append(mId).append("_Owner")
				.append(mOwnerId).toString();
	}

	@Override
	protected ListEntity parseList(InputStream is) throws Exception {
		if (mIsBlogComment) {
			return BlogCommentList.parse(is);
		} else {
			return CommentList.parse(is);
		}
	}

	@Override
	protected ListEntity readList(Serializable seri) {
		if (mIsBlogComment)
			return ((BlogCommentList) seri);
		return ((CommentList) seri);
	}

	@Override
	public boolean onBackPressed() {
		if (mEmojiFragment != null) {
			return mEmojiFragment.onBackPressed();
		}
		return super.onBackPressed();
	}

	@Override
	protected void sendRequestData() {
		if (mIsBlogComment) {
			NewsApi.getBlogCommentList(mId, mCurrentPage, mHandler);
		} else {
			NewsApi.getCommentList(mId, mCatalog, mCurrentPage, mHandler);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	}

	@Override
	public void onMoreClick(final Comment comment) {
		final CommonDialog dialog = DialogHelper
				.getPinterestDialogCancelable(getActivity());
		String[] items = null;
		if (AppContext.instance().isLogin()
				&& AppContext.instance().getLoginUid() == comment.getAuthorId()) {
			items = new String[] { getString(R.string.reply),
					getString(R.string.delete) };
		} else {
			items = new String[] { getString(R.string.reply) };
		}
		dialog.setTitle(R.string.operation);
		dialog.setItemsWithoutChk(items, new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				dialog.dismiss();
				if (position == 0) {
					handleReplyComment(comment);
				} else if (position == 1) {
					handleDeleteComment(comment);
				}
			}

		});
		dialog.setNegativeButton(R.string.cancle, null);
		dialog.show();
	}

	private void handleReplyComment(Comment comment) {
		getActivity().findViewById(R.id.emoji_container).setVisibility(
				View.VISIBLE);
		mEmojiFragment.setInputHint(getString(R.string.reply_comment_to,
				comment.getAuthor()));
		mEmojiFragment.requestFocusInput();
		mCurrentReplyComment = comment;
	}

	private void handleDeleteComment(Comment comment) {
		if (!AppContext.instance().isLogin()) {
			UIHelper.showLogin(getActivity());
			return;
		}
		AppContext.showToastShort(R.string.deleting);
		if (mIsBlogComment) {
			NewsApi.deleteBlogComment(AppContext.instance().getLoginUid(), mId,
					comment.getId(), comment.getAuthorId(), mOwnerId,
					mDeleteHandler);
		} else {
			NewsApi.deleteComment(mId, mCatalog, comment.getId(),
					comment.getAuthorId(), mDeleteHandler);
		}
	}

	@Override
	public void onSendClick(String text) {
		if (mCurrentReplyComment == null)
			return;
		if (!AppContext.instance().isLogin()) {
			UIHelper.showLogin(getActivity());
			return;
		}
		if (mIsBlogComment) {
			NewsApi.replyBlogComment(mId, AppContext.instance().getLoginUid(),
					text, mCurrentReplyComment.getId(),
					mCurrentReplyComment.getAuthorId(), mReplyCommentHandler);
		} else {
			NewsApi.replyComment(mId, mCatalog, mCurrentReplyComment.getId(),
					mCurrentReplyComment.getAuthorId(), AppContext.instance()
							.getLoginUid(), text, mReplyCommentHandler);
		}
	}
}
