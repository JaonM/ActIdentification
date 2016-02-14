package com.actidentification.util;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.actidentification.R;

public class SpinnerAdapter extends BaseAdapter {

	Activity activity;
	String [] activities=new String [] {"跑步","挥手","走路","跳跃"};
	
	public SpinnerAdapter(Activity activity) {
		this.activity=activity;
	}
	
	@Override
	public int getCount() {
		// TODO 自动生成的方法存根
		return activities.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO 自动生成的方法存根
		return activities[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		// TODO 自动生成的方法存根
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO 自动生成的方法存根
		View view=activity.getLayoutInflater().inflate(R.layout.spinner_layout, null);
		TextView text=(TextView)view.findViewById(R.id.spinnerText);
		text.setText(activities[arg0]);
		return view;
	}

	
}
