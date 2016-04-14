package hz.dodo.controls;

import hz.dodo.Logger;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

//http://blog.csdn.net/eclipsexys/article/details/38401641

public class DValueAnimator
{
	int vw, vh, tth,
		dx;
	public DValueAnimator(int vw, int vh, int tth)
	{
		this.vw = vw;
		this.vh = vh;
		this.tth = tth;
	}
	
	@SuppressLint ("NewApi")
	public void test(final int distance)
	{
		ValueAnimator animator = ValueAnimator.ofFloat(0, distance);
		// animator.setTarget(view);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(1000).start();
		animator.addUpdateListener(new AnimatorUpdateListener()
		{
			public void onAnimationUpdate(ValueAnimator animation)
			{
				Float value = (Float) animation.getAnimatedValue();
//				imageView.setTranslationY(value);
				Logger.i("value:" + value);
			}
		});
	}
	
	@SuppressLint ("NewApi")
	public void test2(final int distance, final int duration, final View v1, final View v2)
	{
		Logger.i("distance " + distance);
		dx = v2.getLeft();
		ValueAnimator animator = ValueAnimator.ofFloat(0, distance);
		animator.setInterpolator(new DecelerateInterpolator());
		animator.setDuration(duration).start();
		animator.addUpdateListener(new AnimatorUpdateListener()
		{
			public void onAnimationUpdate(ValueAnimator animation)
			{
				Float value = (Float) animation.getAnimatedValue();
//				Logger.i("value:" + value);
				
//				int dx = v1.getLeft();
//				v1.layout((int)(dx + value), tth, (int)(dx + vw + value), vh);
//				v2.layout((int)(dx + vw - value), tth, (int)(dx + vw*2 + value), vh);
				
//				Logger.i("V1 " + v1.getLeft());
//				Logger.i("V2 " + v2.getLeft());
				
				v2.setX(dx-value);
				Logger.i("V2 " + v2.getLeft());
			}
		});
	}
	
	// 抛物线
	@SuppressLint ("NewApi")
	public void parabola(View view, final int duration, final int disX, final int disY)
	{
		ValueAnimator animator = ValueAnimator.ofObject(new TypeEvaluator<PointF>()
		{
			public PointF evaluate(float fraction, PointF arg1, PointF arg2)
			{
				PointF p = new PointF();
				p.x = fraction * disX;
				p.y = fraction * fraction * 0.5f * disY * 4f * 0.5f;
				return p;
			}
		}, new PointF(0, 0));
		animator.setDuration(duration);
		animator.setInterpolator(new LinearInterpolator());
		animator.start();
		animator.addUpdateListener(new AnimatorUpdateListener()
		{
			public void onAnimationUpdate(ValueAnimator animator)
			{
				// PointF p = (PointF) animator.getAnimatedValue();
				// imageView.setTranslationX(p.x);
				// imageView.setTranslationY(p.y);
			}
		});
	}

	// 自由落体反弹效果
	@SuppressLint ("NewApi")
	public void freefall(View view, final int duration, final int distance)
	{
		final ValueAnimator animator = ValueAnimator.ofFloat(0, distance);
		animator.setTarget(view);
		animator.setInterpolator(new BounceInterpolator()); // 
		animator.setDuration(duration).start();
		animator.addUpdateListener(new AnimatorUpdateListener()
		{
			public void onAnimationUpdate(ValueAnimator animation)
			{
				Float value = (Float) animation.getAnimatedValue();
//				imageView.setTranslationY(value);
				Logger.i("value:" + value);
			}
		});
	}
	
	@SuppressLint ("NewApi")
	public void propertyValuesHolder(View view)
	{
		PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 1f);
		PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f, 0, 1f);
		PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f, 0, 1f);
		ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY, pvhZ).setDuration(1000).start();
	}

	@SuppressLint ("NewApi")
	public void alpha(final View view)
	{
		ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0f); // 从1.0变化到0
		animator.setDuration(1000);
		animator.start();
	}
	
//	AnimatorSet中有一系列的顺序控制方法：playTogether、playSequentially、animSet.play().with()、defore()、after()等。用来实现多个动画的协同工作方式。
	public void demo1()
	{
//		ObjectAnimator animator1 = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 2f);
//		ObjectAnimator animator2 = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 2f);
//		ObjectAnimator animator3 = ObjectAnimator.ofFloat(imageView, "translationY", 0f, 500f);
//		AnimatorSet set = new AnimatorSet();
//		set.setDuration(1000);
//		set.playTogether(animator1, animator2, animator3);
//		set.start();
	}
	
	// 生成自定义动画  
	@SuppressLint ("NewApi")
	private void setupCustomAnimations()
	{
		LayoutTransition mTransition = new LayoutTransition();
		// 动画：CHANGE_APPEARING
		// Changing while Adding
		PropertyValuesHolder pvhLeft = PropertyValuesHolder.ofInt("left", 0, 1);
		PropertyValuesHolder pvhTop = PropertyValuesHolder.ofInt("top", 0, 1);
		PropertyValuesHolder pvhRight = PropertyValuesHolder.ofInt("right", 0, 1);
		PropertyValuesHolder pvhBottom = PropertyValuesHolder.ofInt("bottom", 0, 1);
		PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0f, 1f);
		PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0f, 1f);

		final ObjectAnimator changeIn = ObjectAnimator.ofPropertyValuesHolder(this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScaleX, pvhScaleY).setDuration(mTransition.getDuration(LayoutTransition.CHANGE_APPEARING));
		mTransition.setAnimator(LayoutTransition.CHANGE_APPEARING, changeIn);
		changeIn.addListener(new AnimatorListenerAdapter()
		{
			public void onAnimationEnd(Animator anim)
			{
				View view = (View) ((ObjectAnimator) anim).getTarget();
				// View也支持此种动画执行方式了
				view.setScaleX(1f);
				view.setScaleY(1f);
			}
		});

		// 动画：CHANGE_DISAPPEARING
		// Changing while Removing
//		Keyframe 时间/值 对
		Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
		Keyframe kf1 = Keyframe.ofFloat(.9999f, 360f);
		Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
		PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
		final ObjectAnimator changeOut = ObjectAnimator.ofPropertyValuesHolder(this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhRotation).setDuration(mTransition.getDuration(LayoutTransition.CHANGE_DISAPPEARING));
		mTransition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, changeOut);
		changeOut.addListener(new AnimatorListenerAdapter()
		{
			public void onAnimationEnd(Animator anim)
			{
				View view = (View) ((ObjectAnimator) anim).getTarget();
				view.setRotation(0f);
			}
		});

		// 动画：APPEARING
		// Adding
		ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "rotationY", 90f, 0f).setDuration(mTransition.getDuration(LayoutTransition.APPEARING));
		mTransition.setAnimator(LayoutTransition.APPEARING, animIn);
		animIn.addListener(new AnimatorListenerAdapter()
		{
			public void onAnimationEnd(Animator anim)
			{
				View view = (View) ((ObjectAnimator) anim).getTarget();
				view.setRotationY(0f);
			}
		});

		// 动画：DISAPPEARING
		// Removing
		ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "rotationX", 0f, 90f).setDuration(mTransition.getDuration(LayoutTransition.DISAPPEARING));
		mTransition.setAnimator(LayoutTransition.DISAPPEARING, animOut);
		animOut.addListener(new AnimatorListenerAdapter()
		{
			public void onAnimationEnd(Animator anim)
			{
				View view = (View) ((ObjectAnimator) anim).getTarget();
				view.setRotationX(0f);
			}
		});
	}
}
