package hz.dodo.controls;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class AnimTrans
{
	public interface Callback
	{
		public void startAnim();
		public void endAnim(final View v1, final View v2);
	}
	
	final int DURATION = 250;
	final int LR = 0; // left or right
	final int TB = 1; // top or bottom

	public void TO_LR(final Callback callback, final View v1, final View v2, final int d1)
	{
		final int trans1 = (int) v1.getTranslationX();
		final int trans2 = (int) v2.getTranslationX();

		ValueAnimator animator = ValueAnimator.ofInt(0, d1);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(DURATION).start();
		if(callback != null)
		{
			callback.startAnim();
		}
		animator.addUpdateListener(new AnimatorUpdateListener()
		{
			public void onAnimationUpdate(ValueAnimator animation)
			{
				int value = ((Integer) animation.getAnimatedValue()).intValue();
				v1.setTranslationX(trans1 + value); // 相对原始位置的便宜量
				v2.setTranslationX(trans2 + value);
				
				if(value == d1)
				{
					if(callback != null)
					{
						callback.endAnim(v1, v2);
					}
				}
			}
		});
	}
	
	public void TO_LR(final Callback callback, final View v1, final int d1, final View v2, final int d2)
	{
		final int trans1 = (int) v1.getTranslationX();
		final int trans2 = (int) v2.getTranslationX();
		
		ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(DURATION).start();
		if(callback != null)
		{
			callback.startAnim();
		}
		animator.addUpdateListener(new AnimatorUpdateListener()
		{
			public void onAnimationUpdate(ValueAnimator animation)
			{
				Float value = (Float) animation.getAnimatedValue();
				v1.setTranslationX(trans1 + value*d1);
				v2.setTranslationX(trans2 + value*d2);
				
				if(value == 1)
				{
					if(callback != null)
					{
						callback.endAnim(v1, v2);
					}
				}
			}
		});
	}
}
