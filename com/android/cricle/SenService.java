/**
 * 
 */
package com.android.cricle;

import java.io.FileWriter;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;

/**
 * @author yu
 *
 */
public class SenService extends Service implements Runnable {

	private SensorManager sensorManager;
	private Sensor sensor;
	private float x, y, z;
	private float accABS;// ÿ���ӵ�˲ʱ���Լ��ٶ�ֵ
	Time now;
	float sum; 
	FileWriter writer;
	public final static int MinutesPerHour = 60;
	//public final static String LogFilePath = "/Circle/AccData.txt";

	private SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			// �������SmartDevice Z1�ļ��ٶ�ֵ��������
			x = event.values[0];
			y = event.values[1];
			z = event.values[2];
			accABS = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)
					+ Math.pow(z, 2));
		}
	};
	private final IBinder binder = new MyBinder();

	public class MyBinder extends Binder {
		SenService getService() {
			return SenService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	//��ȡ��ǰ���ٶ�ֵ
	public float getAcc(){
		return this.accABS;
	}
	//��ȡϵͳ��¼�ļ�·��
	public String getLogFilePath(){
		return Environment.getExternalStorageDirectory().getPath() + "/Circle/AccData.txt";
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		now = new Time(); 
		// ��Ӧ�� YLC
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (sensor == null) {
			Log.v("SenService", "�����豸��֧�ָù��ܣ�");
			this.stopSelf();
		}
		// ע�������
		sensorManager.registerListener(sensorEventListener, sensor,
				SensorManager.SENSOR_DELAY_UI);
		new Thread(this).start();
	}

	// ����д�뺯��
	public void logToFile(String writestr) throws IOException {
		try {
			writer = new FileWriter(getLogFilePath(), true);
			writer.write(writestr);
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			Log.e("IOE", e.toString());
		} finally {
			writer.close();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(1000);

				now.setToNow();
				// ÿ��1����д��һ������
				if ( now.second == 0) {
					
					int time2int = now.hour % 12 * SenService.MinutesPerHour + now.minute;
					
					this.logToFile(Integer.valueOf(time2int).toString() + " "
							+ Float.valueOf(sum / SenService.MinutesPerHour).toString() + "\r\n"); 
					
					sum = 0.0f;
				} else {
					sum += this.accABS;
				}
				// Log.v("Sensor", now.toString() +
				// Float.valueOf(accABS).toString());

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				Log.e("IOERROR", e.toString());
			}
		}
	}
}
