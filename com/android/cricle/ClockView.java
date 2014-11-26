package com.android.cricle;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.os.Environment;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @ClassName: ClockView
 * @author ���� E-mail: iylc@qq.com
 * @Description: TODO
 * @version
 */
public class ClockView extends View implements Runnable {

	CircleActivity circle;
	
	private int cx;//Բ��x��
	private int cy;//Բ��y��
	
	private float angel_sec;
	private float angel_min;
	private float angel_hour;

	private Matrix matrix_sec;
	private Matrix matrix_min;
	private Matrix matrix_hour;

	private Bitmap secBm;
	private Bitmap minBm;
	private Bitmap hourBm;
	
	private RectF oval_day;	//ÿ����ٶ�������ʾ���õ��ľ���
	private RectF oval_hour;//ÿСʱ���ٶ�������ʾ���õ��ľ���
	private RectF oval_min;//ÿ���Ӽ��ٶ�������ʾ���õ��ľ���
	Paint accp;		//���ٶ���ʾ���õ��Ļ���
	Paint border_paint;	//���ٶ���ʾ���߽��õ��Ļ���
	int accWidth;	//���ٶ���ʾ�����
	

	// ��¼ÿ����ٶ�����
	private float[] accArr; 
	private int[] transArr; // ͸��������
	// ��¼ÿСʱ���ٶ�����
	private float[] accArr_hour; 
	private int[] transArr_hour; // ͸��������
	// ��¼ÿ���Ӽ��ٶ�����
	private float[] accArr_min; 
	private int[] transArr_min; // ͸��������

	private Bitmap oilBm;
	private Bitmap bootBm;

	Time time_now = new Time();
	private String dataFileName;
/**/
	public ClockView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		circle = (CircleActivity)context;
		init();
	}

	public ClockView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		circle = (CircleActivity)context;
		init();
	}

	public ClockView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		circle = (CircleActivity)context;
		init();
	}

	private void loadData(){
		try {
			 
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFileName)));
			//������ȡ   AccData.txt�ļ��ĸ�ʽ��ÿ�д洢�������� 
			//��һ�������� ��¼�߲ɼ����ݵ�ʱ��ֵ   Сʱ*60 + ������ 
			//�ڶ�����������ƽ�����ٶ�ֵ
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] strArr = line.split(" ");
				//ʱ��
				int arrT = Integer.parseInt(strArr[0]);
				//���ٶȾ�ֵ
				float arrV = Float.parseFloat(strArr[1]);
				//ֻ��ȡż�����ӵ�ֵ
				if(arrT % 2 == 0)
					accArr[(int) arrT / 2] = arrV;
				
				time_now.setToNow();
				if(arrT > time_now.hour * 60){
					accArr_hour[(arrT - time_now.hour * 60) * 6] = arrV;
				}
			}
			br.close();
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			Log.e("IOERROR", e.toString());
		} /**/
	}
	
	private void init() {

		accArr = new float[360];
		transArr = new int[361];
		
		accArr_hour = new float[360];
		transArr_hour = new int[361];
		
		accArr_min = new float[360];
		transArr_min = new int[361];

		
		//dataFileName = this.circle.sensrv.getLogFilePath();
		dataFileName = Environment.getExternalStorageDirectory().getPath() + "/Circle/AccData.txt";
		for (int x = 0; x < 360; x++)
			transArr[x] = transArr_hour[x] = transArr_min[x] =80;
		matrix_sec = new Matrix();
		matrix_min = new Matrix();
		matrix_hour = new Matrix();

		secBm = BitmapFactory.decodeResource(getResources(),
				R.drawable.info_rt_base_needle_);
		minBm = BitmapFactory.decodeResource(getResources(), R.drawable.min);
		hourBm = BitmapFactory.decodeResource(getResources(), R.drawable.hour);

		bootBm = BitmapFactory.decodeResource(getResources(),
				R.drawable.info_rt_base_needle_boot_);
		oilBm = BitmapFactory.decodeResource(getResources(),
				R.drawable.info_rt_ins_oil_);
		

		//Բ��
		cx = oilBm.getWidth() / 2;
		cy = oilBm.getHeight() / 2;
		
		//��ȡ�ļ��еļ��ٶ�ֵ
		loadData();
		
		//���
		accWidth = 30;
		
		//ԭ��onDraw��  �ع�ʱ���˹���
		// ���û�������Ҫ�ľ���
		oval_day = new RectF();
		oval_day.left = 25;
		oval_day.top = 25;
		oval_day.right = oilBm.getWidth() - 25;
		oval_day.bottom = oilBm.getHeight() - 25;
		
		//one hour ����
		oval_hour = new RectF();
		oval_hour.left = oval_day.left + accWidth;
		oval_hour.top = oval_hour.left;
		oval_hour.right = oilBm.getWidth() - oval_hour.left;
		oval_hour.bottom = oilBm.getHeight() - oval_hour.left;
		
		//one min ����
		oval_min = new RectF();
		oval_min.left = oval_hour.left + accWidth;
		oval_min.top = oval_min.left;
		oval_min.right = oilBm.getWidth() - oval_min.left;
		oval_min.bottom = oilBm.getHeight() - oval_min.left;
		
		
		// ���û���
		accp = new Paint();
		accp.setStyle(Paint.Style.STROKE);
		accp.setStrokeWidth(accWidth);
		//��Ե
		border_paint = new Paint();
		border_paint.setStyle(Paint.Style.STROKE);
		border_paint.setStrokeWidth(1);
		
		new Thread(this).start();
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawBitmap(oilBm, 0, 0, null);

		// ��ȡ��ǰʱ��
		time_now.setToNow();
		int sec = time_now.second;
		int min = time_now.minute;
		int hou = time_now.hour;

		angel_sec = sec * 6 - 180;
		angel_min = min * 6 - 180;
		angel_hour = hou * 30 - 180 + min / 2;

		// second
		matrix_sec.reset();
		matrix_sec.preTranslate(oilBm.getWidth() / 2 - bootBm.getWidth() / 2
				+ 3, oilBm.getHeight() / 2 - bootBm.getHeight() / 2);
		matrix_sec.preRotate(angel_sec, secBm.getWidth() / 2,
				secBm.getHeight() / 6);

		// min
		matrix_min.reset();
		matrix_min.preTranslate(oilBm.getWidth() / 2 - bootBm.getWidth() / 2
				+ 3, oilBm.getHeight() / 2 - bootBm.getHeight() / 2);
		matrix_min.preRotate(angel_min, minBm.getWidth() / 2,
				minBm.getHeight() / 6);

		// hour
		matrix_hour.reset();
		matrix_hour.preTranslate(oilBm.getWidth() / 2 - bootBm.getWidth() / 2
				+ 3, oilBm.getHeight() / 2 - bootBm.getHeight() / 2);
		matrix_hour.preRotate(angel_hour, hourBm.getWidth() / 2,
				hourBm.getHeight() / 6);

		// draw
		canvas.drawBitmap(secBm, matrix_sec, null);
		canvas.drawBitmap(minBm, matrix_min, null);
		canvas.drawBitmap(hourBm, matrix_hour, null);

		canvas.drawBitmap(bootBm, oilBm.getWidth() / 2 - bootBm.getWidth() / 2,
				oilBm.getHeight() / 2 - bootBm.getHeight() / 2, null);

		// ÿ1����ȡһ��ֵ �������ݴ���ת�������accArr��accArr_hour����
		if (sec == 0) { 
			loadData(); 
		} 
		//��ȡ��һ��ļ��ٶ�ֵ������ÿ�ּ��ٶ�������
		accArr_min[sec*6] = this.circle.sensrv.getAcc();
		

		// ����͸��������
		for (int k = 0; k < 160; k++)
			transArr[(360 + (hou * 30 + (int)min / 2 - k)) % 360] = 240 - k;
		// ����͸��������
		for (int k = 0; k < 160; k++)
			transArr_hour[(360 + (min * 6 - k)) % 360] = 240 - k;
		// ����͸��������
		for (int k = 0; k < 160; k++)
			transArr_min[(360 + (sec * 6  - k)) % 360] = 240 - k;


		
		for (int i = 0; i < 360; i++) {
			// ���ݼ��ٶ�ֵ�����û�����ɫ
			float a2c = this.accArr[i];
			if (a2c > 30)
				accp.setARGB(transArr[i], 0, 0, (int) ((40 - a2c) / 10 * 255));
			else if (a2c > 20 && a2c <= 30)
				accp.setARGB(transArr[i], 0, (int) ((30 - a2c) / 10 * 255), 255);
			else if (a2c > 10 && a2c <= 20)
				accp.setARGB(transArr[i], (int) ((20 - a2c) / 10 * 255), 255,
						255);
			else if (a2c < 10)
				accp.setARGB(transArr[i], 255, 255, 255);

			canvas.drawArc(oval_day, i + 270, 1, false, accp);
		}	
		
		for (int i = 0; i < 360; i++){
			// ���ݼ��ٶ�ֵ�����û�����ɫ
			float a2c = this.accArr[i];
			if (a2c > 30)
				accp.setARGB(transArr_hour[i], 0, 0, (int) ((40 - a2c) / 10 * 255));
			else if (a2c > 20 && a2c <= 30)
				accp.setARGB(transArr_hour[i], 0, (int) ((30 - a2c) / 10 * 255), 255);
			else if (a2c > 10 && a2c <= 20)
				accp.setARGB(transArr_hour[i], (int) ((20 - a2c) / 10 * 255), 255,
						255);
			else if (a2c < 10)
				accp.setARGB(transArr_hour[i], 255, 255, 255);
			
			//one hour
			canvas.drawCircle(cx, cy, cx - oval_hour.left + accWidth/2, border_paint);
			canvas.drawArc(oval_hour, i + 270, 1, false, accp);
		}
		
		for (int i = 0; i < 360; i++){
			// ���ݼ��ٶ�ֵ�����û�����ɫ
			float a2c = this.accArr_min[i];
			if (a2c > 30)
				accp.setARGB(transArr_min[i], 0, 0, (int) ((40 - a2c) / 10 * 255));
			else if (a2c > 20 && a2c <= 30)
				accp.setARGB(transArr_min[i], 0, (int) ((30 - a2c) / 10 * 255), 255);
			else if (a2c > 10 && a2c <= 20)
				accp.setARGB(transArr_min[i], (int) ((20 - a2c) / 10 * 255), 255,
						255);
			else if (a2c < 10)
				accp.setARGB(transArr_min[i], 255, 255, 255);
			
			//one min
			canvas.drawCircle(cx, cy, cx - oval_min.left + accWidth/2, border_paint);
			canvas.drawArc(oval_min, i + 270, 1, false, accp);
		}

	}

	 
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			postInvalidate();
		}

	}
}
