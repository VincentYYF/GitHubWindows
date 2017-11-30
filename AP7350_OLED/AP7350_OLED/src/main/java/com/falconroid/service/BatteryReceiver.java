package com.falconroid.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.falconroid.AP7350_OLED.CommActivity;

/*
"present"��������(boolean)  ...
"level"����������  (int)������ �����ʣ������
"scale"��������    (int)������ ��������ֵ��ͨ��Ϊ100��
"icon-small"����  (int)  ����  ��ͼ��ID��
"voltage"��������(int)������  ����صĵ�ѹ�����أ�
"temperature"�� (int)������  ����ص��¶ȣ�0.1�ȵ�λ������ ��ʾ197��ʱ����˼Ϊ19.7�ȡ� 
"technology"����(String)������������ͣ����磬Li-ion�ȵȡ�

"plugged"������  (int)     ��  ����緽ʽ��
��������������������������BatteryManager.BATTERY_PLUGGED_AC��AC��硣
��������������������������BatteryManager.BATTERY_PLUGGED_USB��USB��硣

"status"��������  (int)������ �����״̬��
��������������������������BatteryManager.BATTERY_STATUS_CHARGING�����״̬��
��������������������������BatteryManager.BATTERY_STATUS_DISCHARGING���ŵ�״̬��
��������������������������BatteryManager.BATTERY_STATUS_NOT_CHARGING��δ������
��������������������������BatteryManager.BATTERY_STATUS_FULL�������硣
��������������������������BatteryManager.BATTERY_STATUS_UNKNOWN��δ֪״̬��


"health"��������  (int)������ ������״̬��
��������������������������BatteryManager.BATTERY_HEALTH_GOOD��״̬���á�
��������������������������BatteryManager.BATTERY_HEALTH_DEAD�����û�е硣
��������������������������BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE����ص�ѹ���ߡ�
��������������������������BatteryManager.BATTERY_HEALTH_OVERHEAT����ع��ȡ�
��������������������������BatteryManager.BATTERY_HEALTH_UNKNOWN��δ֪״̬��
*/


public class BatteryReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//�ж����Ƿ���Ϊ�����仯��Broadcast Action
		 if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
			//��ȡ��ǰ����
			int level = intent.getIntExtra("level", 0);  //���ʣ������
			//�������̶ܿ�
			int max = intent.getIntExtra("scale", 100); //������ֵ��ͨ��Ϊ100
	
			float scale = (level*100)/max;
			 CommActivity.currentBattery = scale;
			/*Intent intent1 = new Intent();
			intent.putExtra("scale", scale);
			context.sendBroadcast(intent1);*/
		}
	}

}
