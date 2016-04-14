package hz.dodo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

// 光线感应器
public class SensorLight implements SensorEventListener
{
	public interface Callback
	{
		public void onSensorChanged(final String description, final float value);
		public void onSensorLightIsSupport(final boolean support);
	}
	
	SensorManager sm;
	Sensor sensor;
	Callback callback;
	
	boolean bRegistered;

	public SensorLight(Context ctx, final Callback callback, final boolean start)
	{
		this.callback = callback;

		sm = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
		sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT); // 获得光线感应器的引用
		if(sensor != null && start) // 如果为空,说明不支持
		{
			start();
		}
		if(callback != null)
		{
			callback.onSensorLightIsSupport(sensor != null ? true : false);
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
//		Accuracy:精确值
//	    Sensor:发生变化的感应器
//	    Timestamp:发生的时间，单位是纳秒
//	    Values:发生变化后的值,这个是一个长度为3数组
//	           光线感应器只需要values[0]的值，其他两个都为0.而values[0]就是我们开发光线感应器所需要的，单位是：lux照度单位
		if(Sensor.TYPE_LIGHT == event.sensor.getType())
		{
			float value = event.values[0];
			String s1 = "";
			if(value >= SensorManager.LIGHT_SUNLIGHT)
			{
				// 阳光
				s1 = "阳光";
			}
			else if(value >= SensorManager.LIGHT_SHADE)
			{
				// 阴
				s1 = "阴";
			}
			else if(value >= SensorManager.LIGHT_OVERCAST)
			{
				// 灰蒙蒙
				s1 = "灰蒙蒙";
			}
			else if(value >= SensorManager.LIGHT_SUNRISE)
			{
				// 日出
				s1 = "日出";
			}
			else if(value >= SensorManager.LIGHT_CLOUDY)
			{
				// 多云
				s1 = "多云";
			}
			else if(value >= SensorManager.LIGHT_FULLMOON)
			{
				// 满月
				s1 = "满月";
			}
			else if(value >= SensorManager.LIGHT_NO_MOON)
			{
				// 没月亮
				s1 = "没月亮";
			}
			else
			{
				// 伸手不见五指
				s1 = "伸手不见五指";
			}
			
			if(callback != null)
			{
				callback.onSensorChanged(s1, value);
			}
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}
}
