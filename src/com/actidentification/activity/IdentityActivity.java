package com.actidentification.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actidentification.db.MySQLiteOpenHelper;
import com.actidentification.util.DataPoint;
import com.actidentification.util.Selector;
import com.actidentification.util.SpinnerAdapter;
import com.actidentification.util.Util;
import com.example.actidentification.R;

public class IdentityActivity extends Activity implements SensorEventListener {

	private SensorManager sm;
	
	Spinner spinner;
	
	TextView textView;
	Button startIdentify;
	
	MySQLiteOpenHelper dbOpenHelper;
	SQLiteDatabase db;
	
	//�����
	String type=null;
	
	//������
	ArrayList<String> testers=new ArrayList<String>();
	
	//������map
	Map<String,ArrayList<Selector>> dataMap=new HashMap<String,ArrayList<Selector>>();
	
	//�洢����֤�ĵ�
	ArrayList<DataPoint> identityPoints=new ArrayList<DataPoint>();
	
	Vibrator vibrator;
	
	//��֤�ɼ��Ƿ����
	boolean isFinish=false;
	
	float x,y,z;
	
	Handler handler;
	Handler knnHandler;
	
	Timer timer;
	
       //����һ������������ʾû�в��Թ����ݵ���
       public static Selector notTestedSelector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.identity_layout);
		sm=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
		vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		spinner=(Spinner)findViewById(R.id.spinner);
		SpinnerAdapter adapter=new SpinnerAdapter(this);
		textView=(TextView)findViewById(R.id.identity_notice);
		startIdentify=(Button)findViewById(R.id.startIdentify);
		spinner.setAdapter(adapter);
		
		//�����ݿ�
		dbOpenHelper=new MySQLiteOpenHelper(IdentityActivity.this,"train.db3",1);
		db=dbOpenHelper.getReadableDatabase();
		
		handler=new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==0x110) {
					DataPoint point=new DataPoint(x,y,z);
					identityPoints.add(point);
					if(identityPoints.size()>=200) {
						timer.cancel();
						sm.unregisterListener(IdentityActivity.this);
						isFinish=true;
						knnHandler.sendEmptyMessage(0x111);
					}
				}
			}
		};
		
		knnHandler=new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==0x111) {
					/**
					 * ���￪ʼ��knn�㷨��֤
					 */
					Log.v("identityPoints",identityPoints.toString());
					if(Util.knnDistance(dataMap, type, identityPoints)) {
						ArrayList<Selector> selectors=dataMap.get(type);
						Selector user=selectors.get(0);
						Log.v("selector size", selectors.size()+"");
						for(Selector selector:selectors) {
							//Log.v("identityPoints", identityPoints.toString());
							Log.v(selector.getTesterName()+"origin", selector.getOriginDataPoints().size()+"");
							Log.v(selector.getTesterName()+"origin", selector.getOriginDataPoints().toString());
							Log.v(selector.getTesterName(), selector.getKNNDataPoints().size()+"");
							if(selector.getKNNDataPoints().size()>user.getKNNDataPoints().size()) {	
								user=selector;
							}
						}
						vibrator.vibrate(1000);
						Log.v("knn point", user.getKNNDataPoints().toString());
						Toast.makeText(IdentityActivity.this, "knn points num "+user.getKNNDataPoints().size(),Toast.LENGTH_LONG).show();
						/**
						 * �����һ����֤��û�в����ѵ������
						 * ���ٸ�������ж��Ƿ�Ϊѵ����
						 */
						if(notTestedSelector.getKNNDataPoints().size()!=0&&notTestedSelector.getKNNDataPoints().size()>user.getKNNDataPoints().size()) {
							Toast.makeText(IdentityActivity.this,"��û������ѵ���������ѵ�����ڽ�����֤",Toast.LENGTH_SHORT).show();
                             return;
                        }
						Toast.makeText(IdentityActivity.this, "����"+user.getTesterName(), Toast.LENGTH_LONG).show();
						identityPoints.clear();
					}
				}
			}
		};
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO �Զ����ɵķ������
				String str=spinner.getItemAtPosition(position).toString();
				textView.setText("��ѡ����� "+str);
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
		
		
		
		startIdentify.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				timer=new Timer();
				//���dataMap
				if(addInDataMap(db)) {
					sm.registerListener(IdentityActivity.this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
					Toast.makeText(IdentityActivity.this, "��������", Toast.LENGTH_SHORT).show();
					timer.schedule(new TimerTask() {
					
						@Override
						public void run() {
							handler.sendEmptyMessage(0x110);
						}
					},2000,50);
					
				}
				else 
					Toast.makeText(IdentityActivity.this, "û�гɹ����ز���,�޷���֤", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v("resume", "resume");
		if(db==null||!Util.tableIsExist(db, "tester")) {
			Log.v("db is existed", db.toString());
			Toast.makeText(IdentityActivity.this, "���ݿ���û�в�������Ϣ������ѵ��", Toast.LENGTH_SHORT).show();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater=new MenuInflater(this);
		inflater.inflate(R.menu.identity_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.train:
			Intent intent=new Intent(this,TrainActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	//�����������datamap
	private boolean addInDataMap(SQLiteDatabase db) {
		boolean result=false;;
		if(testers.size()==0) {
			Toast.makeText(this, "û��ѵ���˼�¼ ,�����Ϸ�ѵ����ť��ת��ѵ������", Toast.LENGTH_SHORT).show();
			return false;
		}

              //ʵ����û�в��Թ���Selector
              notTestedSelector=new Selector("none",type);

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
	
	@Override
	public void onStop() {
		super.onStop();
		if(timer!=null)
			timer.cancel();
		identityPoints.clear();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(timer!=null)
			timer.cancel();
		identityPoints.clear();
		sm.unregisterListener(this);
		if(dbOpenHelper!=null)
			dbOpenHelper.close();
	}
}
