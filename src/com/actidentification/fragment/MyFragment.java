package com.actidentification.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actidentification.db.MySQLiteOpenHelper;
import com.actidentification.util.Util;
import com.example.actidentification.R;

public class MyFragment  extends Fragment implements SensorEventListener{

	protected SensorManager sm;
	
	public static EditText tester;
	protected Button start;
	protected Button stop;
	protected TextView notice;
	protected TextView result;
	
	protected float x=0;
	protected float y=0;
	protected float z=0;
	
	protected Handler handler;
	
	protected MySQLiteOpenHelper dbOpenHelper;
	protected static SQLiteDatabase db;
	
	
	protected Vibrator vibrator;
	
	//动态数组存放测试用户名
	public static ArrayList<String> testerNames=new ArrayList<String>();
	
//	protected ArrayList<DataPoint> points=new ArrayList<DataPoint>();

	/**
	public MyFragment() {
		dbOpenHelper=new MySQLiteOpenHelper(getActivity(),"train.db3",1);
		try {
			db=dbOpenHelper.getReadableDatabase();
		} catch(SQLiteException e) {
			Log.v("SQLiteException ", "打开数据库出错");
		}
	}
	*/
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sm=(SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
		vibrator=(Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		dbOpenHelper=new MySQLiteOpenHelper(getActivity(),"train.db3",1);
		try {
			db=dbOpenHelper.getReadableDatabase();
		} catch(SQLiteException e) {
			Log.v("SQLiteException ", "打开数据库出错");
		}
		if(db!=null) {
			if(!Util.tableIsExist(db, "tester")) {
				try {
					db.execSQL("create table tester (_id integer primary key autoincrement,testerName)");
				} catch(SQLiteException e) {
					Log.v("SQLiteException", "创建测试者数据表出错");
				}
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup viewGroup,Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.fragment_layout,null);
		
		tester=(EditText)view.findViewById(R.id.tester);
		start=(Button)view.findViewById(R.id.startTrain);
		stop=(Button)view.findViewById(R.id.stopTrain);
		notice=(TextView)view.findViewById(R.id.notice);
		result=(TextView)view.findViewById(R.id.result);
		
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(sm!=null) {
			sm.unregisterListener(this);
		}
		if(dbOpenHelper!=null)
			dbOpenHelper.close();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO 自动生成的方法存根
		x=event.values[0];
	    y=event.values[1];
		z=event.values[2];
}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO 自动生成的方法存根
		
	}

	public static SQLiteDatabase getDatabase() {
		return db;
	}
}
