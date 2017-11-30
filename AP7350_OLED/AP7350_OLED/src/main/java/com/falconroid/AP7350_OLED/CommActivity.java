package com.falconroid.AP7350_OLED;

import com.falconroid.service.BatteryReceiver;
import com.falconroid.service.CByteRecord;
import com.falconroid.service.CTranslate;
import com.falconroid.utils.FalconException;
import com.hyipc.app.AP7350_OLED.R;

import com.falconroid.comm.CommPort;
import com.falconroid.service.Platform;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;

//import com.android.internal.telephony.PhoneConstants;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Switch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.x;

public class CommActivity extends Activity implements OnClickListener {
	private int miBaud;
	private int miPort;
	private int mTick;

	private BatteryReceiver batteryReceiver;

	public static float currentBattery = 0;

	public Switch mSwitch;
	public Timer timer = new Timer();
	private  CTranslate s_trans;

	public static final String TAG = "COMM";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comm);

		mSwitch = (Switch) findViewById(R.id.switch1);
		mSwitch.setOnClickListener(this);

		batteryReceiver = new BatteryReceiver();
		IntentFilter batteryFilter = new IntentFilter();
		batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);//获取字体点阵数据
		registerReceiver(batteryReceiver, batteryFilter);

		s_trans = new CTranslate(this);
		//获取系统当前时间


		/*
		//GPIO
		Platform.initIO();

		Platform.SetGpioOutput(94);
		Platform.SetGpioDataLow(94);
		*/
		//UART2
		miPort = 2;
		miBaud = 115200;
		mTick=0;

		// open comm
		new OpenT().start();

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				mTick++;
				if(mTick==1) {

					TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);//取得相关系统服务

					switch (tm.getSimState()) { //getSimState()取得sim的状态 有下面6中状态
						case TelephonyManager.SIM_STATE_ABSENT:
							OledShowString(1, "Sim: no sim");
							break;
						case TelephonyManager.SIM_STATE_UNKNOWN:
							OledShowString(1, "Sim: unknow");
							break;
						case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
							OledShowString(1, "Sim: locked");
							break;
						case TelephonyManager.SIM_STATE_PIN_REQUIRED:
							OledShowString(1, "Sim: pin");
							break;
						case TelephonyManager.SIM_STATE_PUK_REQUIRED:
							OledShowString(1, "Sim: puk");
							break;
						case TelephonyManager.SIM_STATE_READY:
							OledShowString(1, "Sim: ok");
							break;
					}
				}

				if(mTick==2) {
					//Log.d(TAG, "timer excute");
					SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
					Date curDate = new Date(System.currentTimeMillis());
					String str = formatter.format(curDate);
					Log.d(TAG, str);
					OledShowString(2, "Tim: "+str);
				}
				if(mTick==3) {
					OledShowString(3, "Bat:"+currentBattery+ "%");
				}
				if(mTick==4) {
					TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
					String imei = telephonyManager.getDeviceId();
					//Log.d(TAG, "IM:"+imei);
					OledShowString(4, "Im:"+imei);
					mTick=0;
				}
			}
		}, 1000, 2000);
	}

	private class OpenT extends Thread {
		public void run() {
			try {
				CommPort.getInstance().doActionOpen(miPort, miBaud);
			} catch (FalconException fae) {

			}
		}
	}

	//row 1~4;str 16个ASCII字符，支持中文输入
	public void OledShowString(int row,String str)
	{
		int length;
		CByteRecord record = s_trans.getString(str,0,0);
		//Log.d(TAG,"record.m_iWidth:" + Integer.toHexString(record.m_iWidth));
		//Log.d(TAG,"record.m_iHeight:" + Integer.toHexString(record.m_iHeight));
		//Log.d(TAG,"record.m_bData.length:" + Integer.toHexString(record.m_bData.length));
		if(record.m_bData.length>256)
			length=256;
		else
			length=record.m_bData.length;

		byte[] bDataTemp = new byte[256];
		byte[] bDataRotate = new byte[256];

		for(int i=0;i<length;i++) {
			//Log.d(TAG, "bData[" + i + "]:" + Integer.toHexString(record.m_bData[i]&0xff));
		}

		int Height=0;
		Height=record.m_iHeight;
		if(record.m_iHeight>16)
			Height=16;

		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < (record.m_iWidth); j++) {
				bDataRotate[16 * i + j] = record.m_bData[record.m_iWidth * i + j];
			}
		}

		for(int k=0;k<16;k++) {
			for (int i = 0; i < 8; i++) {
				bDataTemp[k*8+i] = 0;
				for (int j = 0; j < 8; j++) {
					if ((bDataRotate[16 * j+k] & (0x80 >> i)) != 0) {
						bDataTemp[k*8+i] = (byte) (bDataTemp[k*8+i] + (1 << j));
					}
					if ((bDataRotate[128 + 16 * j+k] & (0x80 >> i)) != 0) {
						bDataTemp[k*8+128 + i] = (byte) (bDataTemp[k*8+128 + i] + (1 << j));
					}
				}
			}
		}

		byte[] bData = new byte[257];

		bData[0]=(byte)row;
		for(int i=0;i<256;i++) {
			bData[i+1] =  bDataTemp[i];
		}
		// send it
		try {
			CommPort.getInstance().doActionSend(bData);
		} catch (FalconException fae) {
			Log.d(TAG, "FalconException:" + fae.getMessage());
		} catch (java.io.IOException ioe) {
			Log.d(TAG, "IOException:" + ioe.getMessage());
		}
	}
	public void onClick(View view) {
		if (mSwitch.equals(view)) {

			if (mSwitch.isChecked()) {
				//OledShowString(1,"123456!");
				//Platform.SetGpioDataHigh(94);
				//mSwitch.setText("HOST	GPIO94=1");
			} else {
				//OledShowString(2,"abcdcf!");
				//Platform.SetGpioDataLow(94);
				//mSwitch.setText("SLAVE	GPIO94=0");
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Platform.deInitIO();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		Log.e("www", "onResume");

		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		Log.e("www", "onPause");

		super.onPause();
	}
}
