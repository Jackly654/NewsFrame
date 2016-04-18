package hf.recycler;

import hf.recycler.data.DRCLR;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class VLoad extends LinearLayout
{
	LinearLayout
		ll;
	ProgressBar
		pb;
	
	TextView
		tv;

	int
		DEAD;
	
	public VLoad(Context ctx, int vw, int vh)
	{
		super(ctx);
		
		DEAD = vw/72;
		int h = vh*80/1846;
		
		setLayoutParams(new LayoutParams(vw, h + DEAD));
		setGravity(Gravity.CENTER);
		
		ll = new LinearLayout(ctx);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.setGravity(Gravity.CENTER_VERTICAL);
		
		pb = new ProgressBar(ctx);
		pb.setLayoutParams(new LayoutParams(h, h));
		
		int[] iArr = new int[4];
		iArr[0] = DRCLR.CLR_HOLO_BLUE_LIGHT;
		iArr[1] = DRCLR.CLR_HOLO_RED_LIGHT;
		iArr[2] = DRCLR.CLR_HOLO_GREEN_LIGHT;
		iArr[3] = DRCLR.CLR_HOLO_ORANGE_LIGHT;
		
		pb.setIndeterminateDrawable(new FoldingCirclesDrawable(iArr));
		pb.setPadding(DEAD, DEAD, DEAD, DEAD);
		
		tv = new TextView(ctx);
		tv.setText("加载中");
		tv.setTextSize(12);
		tv.setTextColor(Color.GRAY);
		tv.setGravity(Gravity.CENTER_VERTICAL);

		ll.addView(pb);
		ll.addView(tv);
		
		addView(ll);
	}

	public void onDestroy()
	{
		removeAllViews();
		ll.removeAllViews();
		pb = null;
		tv = null;
	}
}
