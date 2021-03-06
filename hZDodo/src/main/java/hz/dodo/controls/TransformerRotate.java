package hz.dodo.controls;

import android.annotation.SuppressLint;
import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

public class TransformerRotate implements PageTransformer
{
	final float MAX_ROTATE = 90;

	@SuppressLint ("NewApi")
	public void transformPage(View view, float position)
	{
		if (position < -1)
		{ // [-Infinity,-1)
			// This page is way off-screen to the left.
			view.setAlpha(0);
		}
		else if (position <= 0)
		{ // [-1,0]
			// Use the default slide transition when
			// moving to the left page
//			view.setRotationX(360*(1 + position));
			view.setPivotX(view.getWidth());
			view.setRotationY(MAX_ROTATE*position);
			view.setAlpha(1 + position);
		}
		else if (position <= 1)
		{ // (0,1]
			// Fade the page out.
			view.setPivotX(0);
			view.setRotationY(MAX_ROTATE*position);
			view.setAlpha(1 - position);
		}
		else
		{ // (1,+Infinity]
			// This page is way off-screen to the right.
			view.setAlpha(0);
		}
	}
}
