package net.oschina.app.v2.activity.event.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.model.EventApplyData;
import net.oschina.app.v2.ui.dialog.CommonDialog;


public class EventApplyDialog extends CommonDialog implements
		android.view.View.OnClickListener {

	private EditText mName;
	private TextView mGender;
	private String[] genders;
	private EditText mMobile;
	private EditText mCompany;
	private EditText mJob;

	private EventApplyDialog(Context context, boolean flag, DialogInterface.OnCancelListener listener) {
		super(context, flag, listener);
	}

	@SuppressLint("InflateParams")
	private EventApplyDialog(Context context, int defStyle) {
		super(context, defStyle);
		View shareView = getLayoutInflater().inflate(
				R.layout.v2_dialog_event_apply, null);

		initView(shareView);
		setContent(shareView, 0);
		

	}
	
	private void initView(View view) {
		mName = (EditText) view.findViewById(R.id.et_name);
		mGender = (TextView) view.findViewById(R.id.tv_gender);
		mMobile = (EditText) view.findViewById(R.id.et_phone);
		mCompany = (EditText) view.findViewById(R.id.et_company);
		mJob = (EditText) view.findViewById(R.id.et_job);

		genders = getContext().getResources().getStringArray(
				R.array.gender);
		
		mGender.setText(genders[0]);
		
		mGender.setOnClickListener(this);
	}
	
	public EventApplyDialog(Context context) {
		this(context, R.style.dialog_bottom);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.tv_gender:
			selectGender();
			break;

		default:
			break;
		}
	}
	
	private void selectGender() {
		String gender = mGender.getText().toString();
		int idx = 0;
		for (int i = 0; i < genders.length; i++) {
			if (genders[i].equals(gender)) {
				idx = i;
				break;
			}
		}

		final CommonDialog dialog = new CommonDialog(getContext());
		dialog.setCanceledOnTouchOutside(true);
		dialog.setItems(genders, idx, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mGender.setText(genders[position]);
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	public EventApplyData getApplyData() {
		String name = mName.getText().toString();
		String gender = mGender.getText().toString();
		String phone = mMobile.getText().toString();
		String company = mCompany.getText().toString();
		String job = mJob.getText().toString();
		
		if (TextUtils.isEmpty(name)) {
			AppContext.showToast("请填写姓名");
			return null;
		}
		
		if (TextUtils.isEmpty(phone)) {
			AppContext.showToast("请填写电话");
			return null;
		}
		
		EventApplyData data = new EventApplyData();
		
		data.setName(name);
		data.setGender(gender);
		data.setPhone(phone);
		data.setCompany(company);
		data.setJob(job);
		
		return data;
	}
}
