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

	// 检查程序文件夹和文件是否存在，不存在则进行创建
	public void createEnv() {
		File dir = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/Circle");
		if (!dir.exists()) {
			dir.mkdir(); 
			File file = new File(sensrv.getLogFilePath());
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
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
		
		// 表盘view

		new ClockView(this);
	}

	private void connection() {
		Intent intent = new Intent("yu.Service.SenService");
		bindService(intent, sc, Context.BIND_AUTO_CREATE); // bindService
		//startService(intent);
	}

}