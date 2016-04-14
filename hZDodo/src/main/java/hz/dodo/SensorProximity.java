package hz.dodo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

// 距离感应器
public class SensorProximity implements SensorEventListener
{
	public interface Callback
	{
		public void onSensorChanged(boolean bNear);
		public void onProximityIsSupport(final boolean support);
	}
	
	SensorManager sm;
	Sensor sensor;
	Callback callback;
	
	boolean bRegistered;
	
	public SensorProximity(Context ctx, final Callback callback, final boolean start)
	{
		this.callback = callback;

		sm = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
		sensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY); // 获得距离感应器的引用
		if(sensor != null && start) // 如果为空,说明不支持
		{
			start();
		}
		if(callback != null)
		{
			callback.onProximityIsSupport(sensor != null ? true : false);
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
				Logger.e("register Proximity sensor " + ext.toString());
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
				Logger.e("unregister Proximity sensor " + ext.toString());
			}
		}
	}
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}

	public void onSensorChanged(SensorEvent event)
	{
		if(Sensor.TYPE_PROXIMITY == event.sensor.getType())
		{
			float[] its = event.values;
	        if (its != null)
	        {
	        	Logger.i("its[0]:" + its[0]);
	            //经过测试，当手贴近距离感应器的时候its[0]返回值为3.0，当手离开时返回10.0
	        	if(callback != null)
	        	{
	        		callback.onSensorChanged(its[0] == sensor.getMaximumRange() ? false : true);
	        	}
	        }
		}
	}
}
