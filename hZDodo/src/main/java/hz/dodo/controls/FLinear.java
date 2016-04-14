package hz.dodo.controls;

import hz.dodo.HZDodo;
import hz.dodo.ImgMng;
import hz.dodo.PaintUtil;
import hz.dodo.data.HZDR;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class FLinear extends LinearLayout
{
	protected
	Paint paint;
	
	protected
	ImgMng im;
	
	protected
	int
		vw, vw_h, vh, vh_h,
		movedx, movedy;

	protected
	boolean
		bMoved;

	protected
	float
		tdx, tdy, tmx, tmy, tlx, tly, tux, tuy;
	
	public FLinear(Activity at, int vw, int vh)
	{
		super(at);
		setWillNotDraw(false);
		setBackgroundColor(HZDR.CLR_B7);

		PaintUtil.getInstance(at.getWindowManager());
		paint = PaintUtil.paint;
		im = ImgMng.getInstance(at);
		this.vw = vw;
		this.vh = vh;
		
		vw_h = vw/2;
		vh_h = vh/2;
	}
	public void reDraw()
	{
		postInvalidate();
	}
	@SuppressLint ("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				tdx = event.getX();
				tdy = event.getY();
				tlx = tdx;
				tly = tdy;
				movedx = 0;
				movedy = 0;
				bMoved = false;

				onTouchDown(tdx, tdy);
				
				break;
			case MotionEvent.ACTION_MOVE:
				tmx = event.getX();
				tmy = event.getY();
				
				movedx += Math.abs(tmx - tlx); // 累计X轴移动距离
				movedy += Math.abs(tmy - tly); // 累计Y轴移动距离
				
				if(!bMoved)
				{
					if(movedx > HZDodo.sill || movedy > HZDodo.sill)
					{
						bMoved = true;
						onTouchMoved();
					}
				}
				
				tlx = tmx;
				tly = tmy;
				break;
			case MotionEvent.ACTION_UP:
				tux = event.getX();
				tuy = event.getY();
				onTouchUp(bMoved, tux, tuy);
				break;
			case MotionEvent.ACTION_CANCEL:
				onTouchCancel();
				break;
		}
		
		return true;
	}
	protected void onTouchDown(final float tdx, final float tdy)
	{
	}
	protected void onTouchMoved()
	{
	}
	protected void onTouchUp(final boolean bMoved, final float tux, final float tuy)
	{
	}
	protected void onTouchCancel()
	{
	}
}
