package hz.dodo.controls;

import android.annotation.SuppressLint;
import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

public class TransformerStacked implements PageTransformer
{
	@SuppressLint ("NewApi")
	public void transformPage(View view, float position)
	{
		if (position <= 1 && position >= 0)
		{
			view.setX(view.getLeft() - view.getWidth()*position);
		}
	}
}
