package hz.dodo.controls;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class AnimView
{
//	animation.setInterpolator(new OvershootInterpolator()); // 回弹效果
//	animation.setInterpolator(new AccelerateDecelerateInterpolator()); // 中间部分加速
//	animation.setInterpolator(new AnticipateInterpolator()); // 开始向后一点，然后，往前抛
//	animation.setInterpolator(new AnticipateOvershootInterpolator()); // 开始向后一点，往前抛过点，然后返回来 
//	animation.setInterpolator(new AccelerateInterpolator()); // 加速
//	animation.setInterpolator(new DecelerateInterpolator()); // 减速
//	animation.setInterpolator(new BounceInterpolator()); // 反弹插补
//	animation.setInterpolator(new LinearInterpolator()); // 匀速
//	animation.setInterpolator(new BounceInterpolator()); // 反弹插补
	
	public interface Callback
	{
		public void startAnim(View v, int index);
		public void endAnim(View v, int index);
	}
	
	final int DURATION = 250;
	final int LR = 0; // left or right
	final int TB = 1; // top or bottom
	
	AccelerateDecelerateInterpolator adi;
	
	public AnimView()
	{
		adi = new AccelerateDecelerateInterpolator();
	}
	public void slideLR(final Callback callback, final View v1, final int d1, final View v2, final int d2)
	{
		slide(callback, LR, v1, d1, v2, d2);
	}
	
	public void slideTB(final Callback callback, final View v1, final int d1, final View v2, final int d2)
	{
		slide(callback, TB, v1, d1, v2, d2);
	}
	
	private void slide(final Callback callback, final int type, final View v1, final int d1, final View v2, final int d2)
	{
		/**
		 * fromXDelta
		 * 在放缩，旋转，移动的过程中，有三种参数
		 * Animation.ABSOLUTE 绝对位置
		 * Animation.RELATIVE_TO_SELF 相对于自身的位置
		 * Animation.RELATIVE_TO_PARENT 相对于父控件的位置
		 * 
		 * toXDelta
		 * 移动的距离
		 */
		
		if(v1 != null)
		{
			v1.clearAnimation();
			TranslateAnimation anim1 = null; //new TranslateAnimation(Animation.RELATIVE_TO_PARENT, d1, Animation.RELATIVE_TO_PARENT, 0);
			if(LR == type)
			{
				anim1 = new TranslateAnimation(Animation.ABSOLUTE, d1, Animation.ABSOLUTE, 0);
			}
			else if(TB == type)
			{
				anim1 = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, d1);
			}
			anim1.setInterpolator(adi);
			anim1.setDuration(DURATION);
			anim1.setStartOffset(0);
			anim1.setAnimationListener(new Animation.AnimationListener()
			{
				public void onAnimationStart(Animation animation)
				{
//					Logger.i("onAnimationStart()");
					if(callback != null) callback.startAnim(v1, 1);
				}
				public void onAnimationRepeat(Animation animation)
				{
//					Logger.i("onAnimationRepeat()");
				}
				public void onAnimationEnd(Animation animation)
				{
//					Logger.i("onAnimationEnd()");
					if(LR == type)
					{
						int left = v1.getLeft() + d1;
						int right = left + v1.getWidth();
						v1.clearAnimation();
						v1.layout(left, v1.getTop(), right, v1.getBottom());
					}
					else if(TB == type)
					{
						int top = v1.getTop() + d1;
						int bottom = top + v1.getHeight();
						v1.clearAnimation();
						v1.layout(v1.getLeft(), top, v1.getRight(), bottom);
					}
					if(callback != null)
					{
						callback.endAnim(v1, 1);
					}
				}
			});
			v1.startAnimation(anim1);
		}
		
		if(v2 != null)
		{
			v2.clearAnimation();
			TranslateAnimation anim2 = null; // new TranslateAnimation(Animation.RELATIVE_TO_PARENT, d2, Animation.RELATIVE_TO_PARENT, 0);
			if(LR == type)
			{
				anim2 = new TranslateAnimation(Animation.ABSOLUTE, d2, Animation.ABSOLUTE, 0);
			}
			else if(TB == type)
			{
				anim2 = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, d2);
			}
			anim2.setInterpolator(adi);
			anim2.setDuration(DURATION);
			anim2.setStartOffset(0);
			anim2.setAnimationListener(new Animation.AnimationListener()
			{
				public void onAnimationStart(Animation animation)
				{
					if(callback != null) callback.startAnim(v2, 2);
				}
				public void onAnimationRepeat(Animation animation)
				{
				}
				public void onAnimationEnd(Animation animation)
				{
					if(LR == type)
					{
						int left = v2.getLeft() + d2;
						int right = left + v2.getWidth();
						v2.clearAnimation();
						v2.layout(left, v2.getTop(), right, v2.getBottom());
					}
					else if(TB == type)
					{
						int top = v2.getTop() + d2;
						int bottom = top + v2.getHeight();
						v2.clearAnimation();
						v2.layout(v2.getLeft(), top, v2.getRight(), bottom);
					}
					if(callback != null)
					{
						callback.endAnim(v2, 2);
					}
				}
			});
			v2.startAnimation(anim2);
		}
	}
	
	public void slideAny(final Callback callback, final View v, final int dtx, final int dty)
	{
		if(v != null)
		{
			v.clearAnimation();
			TranslateAnimation anim = new TranslateAnimation(Animation.ABSOLUTE, dtx, Animation.ABSOLUTE, dty); //new TranslateAnimation(Animation.RELATIVE_TO_PARENT, d1, Animation.RELATIVE_TO_PARENT, 0);
				
			anim.setInterpolator(adi);
			anim.setDuration(DURATION);
			anim.setStartOffset(0);
			anim.setAnimationListener(new Animation.AnimationListener()
			{
				public void onAnimationStart(Animation animation)
				{
//					Logger.i("slideAny onAnimationStart()");
					if(callback != null) callback.startAnim(v, 0);
				}
				public void onAnimationRepeat(Animation animation)
				{
//					Logger.i("slideAny onAnimationRepeat()");
				}
				public void onAnimationEnd(Animation animation)
				{
//					Logger.i("slideAny onAnimationEnd()");
					v.clearAnimation();
					
					int left = v.getLeft() + dtx;
					int right = left + v.getWidth();
					int top = v.getTop() + dty;
					int bottom = top + v.getHeight();
					
					v.layout(left, top, right, bottom);

					if(callback != null)
					{
						callback.endAnim(v, 0);
					}
				}
			});
			v.startAnimation(anim);
		}
	}
}
