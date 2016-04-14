package hz.dodo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

// 翻转传感器
public class SensorRotation implements SensorEventListener
{
	public interface Callback
	{
		public void onSensorRotationChanged(final float x, final float y, final float z, final float v);
		public void onSensorRotationIsSupport(final boolean support);
	}
	
	SensorManager sm;
	Sensor sensor;
	Callback callback;
	
	boolean bRegistered;

	@SuppressLint ("InlinedApi")
	public SensorRotation(Context ctx, final Callback callback, final boolean start)
	{
		this.callback = callback;

		sm = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
		sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR); // 获得翻转传感器的引用
		if(sensor != null && start) // 如果为空,说明不支持
		{
			start();
		}
		if(callback != null)
		{
			callback.onSensorRotationIsSupport(sensor != null ? true : false);
		}
	}
	public void onDestroy()
	{
		stop();
	}
	
	public void start()
	{
		if(!bRegistered && sensor != null)
		{
			try
			{
				bRegistered = true;
				sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
			}
			catch(Exception ext)
			{
				bRegistered = false;
				Logger.e("register light sensor " + ext.toString());
			}
		}
	}
	public void stop()
	{
		if(bRegistered && sensor != null)
		{
			try
			{
				bRegistered = false;
				sm.unregisterListener(this, sensor);
			}
			catch(Exception ext)
			{
				bRegistered = true;
				Logger.e("unregister light sensor " + ext.toString());
			}
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if(Sensor.TYPE_ROTATION_VECTOR == event.sensor.getType())
		{
//			SensorEvent.values[0]	旋转向量沿 x 轴的部分（x * sin(θ/2)）。
//			SensorEvent.values[1]	旋转向量沿 y 轴的部分（y * sin(θ/2)）。
//			SensorEvent.values[2]	旋转向量沿 z 轴的部分（z * sin(θ/2)）。
//			SensorEvent.values[3]	旋转向量的数值部分（(cos(θ/2)）1。
			
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			float v = event.values[3];
					
			if(callback != null)
			{
				callback.onSensorRotationChanged(x, y, z, v);
			}
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}
}
