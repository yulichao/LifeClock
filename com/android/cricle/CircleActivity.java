package com.android.cricle;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class CircleActivity extends Activity {
	/** Called when the activity is first created. */

	
	public SenService sensrv;
	private ServiceConnection sc = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) { // connect Service
			sensrv = ((SenService.MyBinder) (service)).getService();  
		}

		@Override
		public void onServiceDisconnected(ComponentName name) { // disconnect  Service
			sensrv = null;
		}
	}; 

	// �������ļ��к��ļ��Ƿ���ڣ�����������д���
	public void createEnv() {
		File dir = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/Circle");
		if (!dir.exists()) {
			dir.mkdir(); 
			File file = new File(sensrv.getLogFilePath());
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				Log.e("IOE", e.toString());
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		connection();
		createEnv();
		
		// ����view

		new ClockView(this);
	}

	private void connection() {
		Intent intent = new Intent("yu.Service.SenService");
		bindService(intent, sc, Context.BIND_AUTO_CREATE); // bindService
		//startService(intent);
	}

}