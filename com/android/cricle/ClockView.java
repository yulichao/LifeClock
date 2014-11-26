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
 * @author 作者 E-mail: iylc@qq.com
 * @Description: TODO
 * @version
 */
public class ClockView extends View implements Runnable {

	CircleActivity circle;
	
	private int cx;//圆心x轴
	private int cy;//圆心y轴
	
	private float angel_sec;
	private float angel_min;
	private float angel_hour;

	private Matrix matrix_sec;
	private Matrix matrix_min;
	private Matrix matrix_hour;

	private Bitmap secBm;
	private Bitmap minBm;
	private Bitmap hourBm;
	
	private RectF oval_day;	//每天加速度描述显示弧用到的矩形
	private RectF oval_hour;//每小时加速度描述显示弧用到的矩形
	private RectF oval_min;//每分钟加速度描述显示弧用到的矩形
	Paint accp;		//加速度显示弧用到的画笔
	Paint border_paint;	//加速度显示弧边界用到的画笔
	int accWidth;	//加速度显示弧宽度
	

	// 记录每天加速度数组
	private float[] accArr; 
	private int[] transArr; // 透明度数组
	// 记录每小时加速度数组
	private float[] accArr_hour; 
	private int[] transArr_hour; // 透明度数组
	// 记录每分钟加速度数组
	private float[] accArr_min; 
	private int[] transArr_min; // 透明度数组

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
			//逐行提取   AccData.txt文件的格式是每行存储两个数字 
			//第一个是整数 记录者采集数据的时间值   小时*60 + 分钟数 
			//第二个浮点数是平均加速度值
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] strArr = line.split(" ");
				//时间
				int arrT = Integer.parseInt(strArr[0]);
				//加速度均值
				float arrV = Float.parseFloat(strArr[1]);
				//只提取偶数分钟的值
				if(arrT % 2 == 0)
					accArr[(int) arrT / 2] = arrV;
				
				time_now.setToNow();
				if(arrT > time_now.hour * 60){
					accArr_hour[(arrT - time_now.hour * 60) * 6] = arrV;
				}
			}
			br.close();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
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
		

		//圆心
		cx = oilBm.getWidth() / 2;
		cy = oilBm.getHeight() / 2;
		
		//获取文件中的加速度值
		loadData();
		
		//宽度
		accWidth = 30;
		
		//原在onDraw中  重构时移了过来
		// 设置画弧所需要的矩形
		oval_day = new RectF();
		oval_day.left = 25;
		oval_day.top = 25;
		oval_day.right = oilBm.getWidth() - 25;
		oval_day.bottom = oilBm.getHeight() - 25;
		
		//one hour 矩形
		oval_hour = new RectF();
		oval_hour.left = oval_day.left + accWidth;
		oval_hour.top = oval_hour.left;
		oval_hour.right = oilBm.getWidth() - oval_hour.left;
		oval_hour.bottom = oilBm.getHeight() - oval_hour.left;
		
		//one min 矩形
		oval_min = new RectF();
		oval_min.left = oval_hour.left + accWidth;
		oval_min.top = oval_min.left;
		oval_min.right = oilBm.getWidth() - oval_min.left;
		oval_min.bottom = oilBm.getHeight() - oval_min.left;
		
		
		// 设置画笔
		accp = new Paint();
		accp.setStyle(Paint.Style.STROKE);
		accp.setStrokeWidth(accWidth);
		//边缘
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

		// 获取当前时间
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

		// 每1分钟取一次值 将的数据处理转换后存入accArr和accArr_hour数组
		if (sec == 0) { 
			loadData(); 
		} 
		//获取这一秒的加速度值，置入每分加速度数组中
		accArr_min[sec*6] = this.circle.sensrv.getAcc();
		

		// 设置透明度数组
		for (int k = 0; k < 160; k++)
			transArr[(360 + (hou * 30 + (int)min / 2 - k)) % 360] = 240 - k;
		// 设置透明度数组
		for (int k = 0; k < 160; k++)
			transArr_hour[(360 + (min * 6 - k)) % 360] = 240 - k;
		// 设置透明度数组
		for (int k = 0; k < 160; k++)
			transArr_min[(360 + (sec * 6  - k)) % 360] = 240 - k;


		
		for (int i = 0; i < 360; i++) {
			// 根据加速度值，设置画笔颜色
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
			// 根据加速度值，设置画笔颜色
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
			// 根据加速度值，设置画笔颜色
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
