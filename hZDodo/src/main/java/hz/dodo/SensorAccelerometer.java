package hz.dodo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

// 三轴加速度感应器
public class SensorAccelerometer implements SensorEventListener
{
	public interface Callback
	{
		public void onSensorChanged(final float x, final float y, final float z);
		public void onAccelerometerIsSupport(final boolean support);
	}
	
	SensorManager sm;
	Sensor sensor;
	Callback callback;
	
	boolean bRegistered;

	public SensorAccelerometer(Context ctx, final Callback callback, final boolean start)
	{
		this.callback = callback;

		sm = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
		sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // 获得三轴加速度感应器的引用
		if(sensor != null && start) // 如果为空,说明不支持
		{
			start();
		}
		if(callback != null)
		{
			callback.onAccelerometerIsSupport(sensor != null ? true : false);
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
		if(Sensor.TYPE_ACCELEROMETER == event.sensor.getType())
		{
            float x_lateral = event.values[0];
            float y_longitudinal = event.values[1];
            float z_vertical = event.values[2];
			
			if(callback != null)
			{
				callback.onSensorChanged(x_lateral, y_longitudinal, z_vertical);
			}
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}
}
