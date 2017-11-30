package com.falconroid.comm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.util.Log;

import com.huayu.io.EMgpio;
import com.huayu.io.SerialPort;

import com.falconroid.service.Platform;
import com.falconroid.utils.*;

public class CommPort {
	protected SerialPort mSerialPort = null;
	protected OutputStream mOutputStream = null;
	private InputStream mInputStream = null;
	
	private ICommListener mListener = null;
	//private static boolean mIoInit = false;
	
	byte[] mBuffer = new byte[1024];
	int mBufferLength = 0;

	Thread mCommT = null;

	private boolean mCommExitFlag = true;
	
	static{
		EMgpio.GPIOInit();
	}
	
	private static CommPort sInstance = null;
	
	//get instance
	public static CommPort getInstance(){
		if(sInstance == null){
			sInstance = new CommPort();
		}
		return sInstance;
	}
	
	//open comm port: baudrate+port
	private void initComm(int iPort,int iBaudrate) throws FalconException{
    	try{    		    		
    		//mSerialPort = new SerialPort(new File("/dev/ttyMT1"),115200, 0);
    		mSerialPort = new SerialPort(new File("/dev/ttyMT"+iPort),iBaudrate, 0);
    		mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();			
    	}catch (SecurityException e) {
    		throw new FalconException("SecurityException:"+e.getMessage());
		} catch (IOException e) {
			throw new FalconException("IOException:"+e.getMessage());			
		} catch (InvalidParameterException e) {
			throw new FalconException("InvalidParameterException:"+e.getMessage());
		}
    }
	
	//close comm port
	private void deInitComm() {
		// kill thread
		mCommExitFlag = true;

		if (mOutputStream != null) {
			try {
				mOutputStream.close();
			} catch (java.io.IOException ioe) {
			}

			mOutputStream = null;
		}

		if (mInputStream != null) {
			try {
				mInputStream.close();
			} catch (java.io.IOException ioe) {
			}
			mInputStream = null;
		}

		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}
	
	public void doActionOpen(int iPort,int iBaudrate) throws FalconException{
		//mListener = null;
		
		deInitComm();
					
		initComm(iPort,iBaudrate);
						
		//start new thread
		mCommT = new Thread(mCommDameo);
		mCommT.start();		
	}
	
	public void addListener(ICommListener listener){

		mListener = listener;
	}
	
	public void doActionClose(){
		deInitComm();
		
		mListener = null;
	}
	
	//send 
	public void doActionSend(byte[] bData) throws FalconException,IOException{
		if(mOutputStream == null){
			throw new FalconException("outputstream null");
		}
		mOutputStream.write(bData);
	}
	
	//recv
	private Runnable mCommDameo = new Runnable(){
		public void run(){
			int iAvailable = 64;
			
			mCommExitFlag = false;
			Log.e("wwm mCommExitFlag"," mCommExitFlag:"+mCommExitFlag);
			
			try{
				while (!mCommExitFlag){
					iAvailable = mInputStream.available();
					
					if (iAvailable > 0){
						
						//Log.e("wwm iAvailable"," iAvailable:"+iAvailable);
						
						mBufferLength = mInputStream.read(mBuffer, 0,
								iAvailable);
						Log.e("wwm mBufferLength"," mBufferLength:"+mBufferLength);
						Log.e("wwm mBuffer"," mBuffer:"+ new String(mBuffer));
						if (mBufferLength > 0){
														
							if(mListener != null){
								//Log.d("COMM", "buffer comin:"+mBufferLength);
								mListener.onCommDataIn(mBuffer,0,mBufferLength);
							}
						}
					}else{
						//mBufferLength = mInputStream.read(mBuffer, 0,10);
						//Log.e("wwm mBufferLength"," mBufferLength:"+mBufferLength);
					}
				}
			}catch(java.io.IOException ioe){
				Log.e("wwm comm"," comm err : "+ioe.getMessage());
				
				if(mListener != null){
					mListener.onExceptionHappen("wwm Recv IOException:"+ioe.getMessage());
				}
			}
		}
	};
}
