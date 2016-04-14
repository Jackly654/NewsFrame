package hz.dodo.data;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.SensorManager;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

@SuppressLint ("NewApi")
public class DValue
{
	public interface Callback
	{
		// position当前值,offset较上次的偏移量
		public void update(final float position, final float offset);
		// 滑动完成
		public void end();
	}
	
	final float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));//滑动减速
	final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
	final float FRICATION = 0.84f;
	
	ValueAnimator animator;
	
	float density,
		  mPpi,
		  lastValue;

	public DValue(Activity at)
	{
		density = at.getResources().getDisplayMetrics().density;
		mPpi = density * 160.0f;
	}
	
	// 纵向滚动 duration / distance 如果为-1,则需要计算
	public void vFling(final Callback callback, final float velocity, int duration, int distance)
	{
		final int mDuration = duration > 0 ? duration : getSplineFlingDuration(velocity);
        final int mDistance = distance > 0 ? distance : (int) (getSplineFlingDistance(velocity) * Math.signum(velocity));
        
        lastValue = 0;
		animator = ValueAnimator.ofFloat(0, mDistance);
		animator.setInterpolator(new Interpolator()
	    {
	        public float getInterpolation(float t)
	        {
	        	t -= 1.0f;
	        	return t * t * t * t * t + 1.0f;
	        }
	    });
		animator.setDuration(mDuration).start();
		animator.addUpdateListener(new AnimatorUpdateListener()
		{
			public void onAnimationUpdate(ValueAnimator animation)
			{
				Float value = (Float) animation.getAnimatedValue();
				if(callback != null)
				{
					callback.update(value, value - lastValue);
					if(value == mDistance)
					{
						callback.end();
					}
				}
				lastValue = value;
			}
		});
	}
	public void vFling(final Callback callback, final float velocity)
	{
		vFling(callback, velocity, -1, -1);
	}
	public boolean vCancel()
	{
		if(animator != null)
		{
			if(animator.isRunning())
			{
				animator.cancel();
				return true;
			}
		}
		return false;
	}
	
	// 纵向滚动算法
	private int getSplineFlingDuration(float velocity)
	{  
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;  
        return (int) (1000.0 * Math.exp(l / decelMinusOne));  
    } 
	private double getSplineDeceleration(float velocity)
	{  
        return Math.log(INFLEXION * Math.abs(velocity) / (ViewConfiguration.getScrollFriction() * computeDeceleration(FRICATION)));
    }
	//减速  
    private float computeDeceleration(float friction)
    {
        return SensorManager.GRAVITY_EARTH   // g (m/s^2)  
                      * 39.37f               // inch/meter  
                      * mPpi                 // pixels per inch  
                      * friction;
    }
    
    private double getSplineFlingDistance(float velocity)
    {  
        final double l = getSplineDeceleration(velocity);  
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return ViewConfiguration.getScrollFriction() * computeDeceleration(FRICATION) * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }
}
