package com.actidentification.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.actidentification.db.MySQLiteOpenHelper;
import com.actidentification.util.DataPoint;
import com.actidentification.util.Selector;
import com.actidentification.util.SpinnerAdapter;
import com.actidentification.util.Util;
import com.example.actidentification.R;

public class LoginActivity extends Activity implements SensorEventListener {

	float x,y,z;
	
	Button login;
	Spinner spinner;
	
	Map<String,ArrayList<Selector>> dataMap=new HashMap<String,ArrayList<Selector>>();
	ArrayList<DataPoint> identityPoints=new ArrayList<DataPoint>();
	
	SensorManager sm;
	
	int time=0;
	
	//�����
	String type=null;
	
	Vibrator vibrator;
	
	//��ʱtimer
	Timer timer;
	//�ռ���timer
	Timer mTimer;
	
	//��ʱhandler
	Handler handler;
	//�����ռ���handler
	Handler mHandler;
	
	MySQLiteOpenHelper dbOpenHelper;
	SQLiteDatabase db;
	
	ArrayList<String> testers=new ArrayList<String>() ;
	
	ActionBar actionBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		try {
			dbOpenHelper=new MySQLiteOpenHelper(this,"train.db3",1);
			db=dbOpenHelper.getReadableDatabase();
		} catch(Exception e) {
			Log.v("SQLiteException ","�����ݿ����");
			this.finish();
		}
		actionBar=getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		login=(Button)findViewById(R.id.login);
		spinner=(Spinner)findViewById(R.id.login_spinner);
		SpinnerAdapter adapter=new SpinnerAdapter(this);
		spinner.setAdapter(adapter);
		sm=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
		vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO �Զ����ɵķ������
				String str=spinner.getItemAtPosition(position).toString();
				if(str.equals("�ܲ�"))
					type="Run";
				if(str.equals("����"))
					type="Wave";
				if(str.equals("��·"))
					type="Walk";
				if(str.equals("��Ծ"))
					type="Jump";
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO �Զ����ɵķ������
				
			}
			
		});
		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(LoginActivity.this, "��������", Toast.LENGTH_SHORT).show();
				sm.registerListener(LoginActivity.this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
				timer=new Timer();
				//��ʱ
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						handler.sendEmptyMessage(0x123);
					}
				},1000,1000);
				
				//�ռ���
				mTimer=new Timer();
				mTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						mHandler.sendEmptyMessage(0x124);
					}
				}, 1000,50);
				login.setClickable(false);
			}
		});
		
		handler=new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==0x123) {
					time++;
					if(time>2) {
						mTimer.cancel();
						sm.unregisterListener(LoginActivity.this);
						vibrator.vibrate(1000);
						//����knn��֤
						addInDataMap(db);
						time=0;
						login.setClickable(true);
						login();
					}
				}
			}
		};
		
		mHandler=new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==0x124) {
					DataPoint point=new DataPoint(x,y,z);
					identityPoints.add(point);
				}
			}
		};
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v("resume", "resume");
		if(db==null||!Util.tableIsExist(db, "tester")) {
			Log.v("db is existed", db.toString());
			Toast.makeText(LoginActivity.this, "���ݿ���û�в�������Ϣ������ѵ��", Toast.LENGTH_SHORT).show();
		}
		else {
			try {
				//�����ݿ�����ȡѵ��������
				Cursor cursor=db.rawQuery("select * from tester", null);
				while(cursor.moveToNext()) {
					String name=cursor.getString(1);
					if(testers.size()==0)
						testers.add(name);
					else {
						//��ֹ����
						boolean isSame=false;
						for(int i=0;i<testers.size();i++) {
							if(testers.get(i).equals(name))
								isSame=true;
						}
						if(!isSame)
							testers.add(name);
					}
				}
				Log.v("testers", testers.toString());
			} catch(SQLiteException e) {
				Log.v("SQLiteException", "tester ���ѯ����");
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case android.R.id.home:
			Intent intent=new Intent(this,IdentityActivity.class);
			startActivity(intent);
		}
		return true;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO �Զ����ɵķ������
		x=event.values[0];
		y=event.values[1];
		z=event.values[2];
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO �Զ����ɵķ������
		
	}
	
	//�����������datamap
	private boolean addInDataMap(SQLiteDatabase db) {
		boolean result=false;
		if(testers.size()==0) {
			Toast.makeText(this, "û��ѵ���˼�¼ ,�����Ϸ�ѵ����ť��ת��ѵ������", Toast.LENGTH_SHORT).show();
			return false;
		}
		ArrayList<Selector> selectors=new ArrayList<Selector>();
		for(String tester:testers) {
			if(Util.tableIsExist(db, tester+"_"+type)) {
				Selector selector=new Selector(tester,type);
				selector.setOriginData(Util.selectFromDB(db, tester+"_"+type));
				selectors.add(selector);
				result=true;
			} 
		}
		Log.v("selectors", selectors.toString());
		//selectors����û������
		dataMap.put(type, selectors);
		return result;
	}

	//�����½
	private boolean login() {
		if(Util.knnDistance(dataMap, type, identityPoints)) {
			ArrayList<Selector> selectors=dataMap.get(type);
			Selector user=selectors.get(0);
			Log.v("selector size", selectors.size()+"");
			for(Selector selector:selectors) {
				if(selector.getKNNDataPoints().size()>user.getKNNDataPoints().size()) {	
					user=selector;
				}
			}
			/**
			 * ��Ҫ�ж�user�Ƿ�Ϊû�в���ѵ��������
			 * 
			 */
			Toast.makeText(this,"��½�ɹ�",Toast.LENGTH_SHORT).show();
			identityPoints.clear();
			Log.v("user ", user.getTesterName()+" "+user.getKNNDataPoints());
			Intent intent=new Intent(this,PersonActivity.class);
			intent.putExtra("user", user.getTesterName());
			startActivity(intent);
			timer.cancel();
			return true;
		}
		else 
			return false;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(timer!=null)
			timer.cancel();
		if(mTimer!=null)
			mTimer=null;
		identityPoints.clear();
		sm.unregisterListener(this);
		if(dbOpenHelper!=null)
			dbOpenHelper.close();
	}
}
