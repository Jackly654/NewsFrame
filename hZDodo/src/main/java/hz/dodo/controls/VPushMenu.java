package hz.dodo.controls;

import hz.dodo.HZDodo;
import hz.dodo.Logger;
import hz.dodo.PaintUtil;
import hz.dodo.data.HZDR;
import hz.dodo.media.DSound;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class VPushMenu extends View
{
	public interface Callback
	{
		public void onSelectedItem(final int tag, final String item);
		public void dismiss();
	}
	
	public static final int LOC_TOP_LEFT = 0;
	public static final int LOC_TOP_RIGHT = 1;
	public static final int LOC_BTM = 2;
	
	// 当前状态
	public static final int S_UNKOWN = -1;
	public static final int S_CLOSED = 0;
	public static final int S_OPENNING = 1;
	public static final int S_OPENED = 2;
	public static final int S_CLOSEING = 3;
	
	Context ctx;
	
	protected
	Callback callback;
	ValueAnimator animator;
	protected
	Paint paint, paintAlpha;
	
	protected
	int
		vw, vh,
		marginLR, // 左右边距
		unith, unith_h, unitw, unitw_h, tatalh,
		movedx, movedy,
		i1, // 循环变量
		dy, dx, // 菜单起点
		loc, // 左侧/右侧
		status, // 当前状态
		tag, // 外部弹起菜单的标志
		touchID; // 点击了菜单项
	
	protected
	float
		percent,
		tdx, tdy, tmx, tmy, tux, tuy, tlx, tly;
	
	protected
	boolean bmoved;
	
	protected
	String[] sArrItems;
	
	@Deprecated
	protected VPushMenu(Context ctx)
	{
		super(ctx);
	}
	
	public VPushMenu(Context ctx, Callback callback, int vw, int vh)
	{
		super(ctx);
		this.ctx = ctx;
		this.callback = callback;
		paint = PaintUtil.paint;
		paintAlpha = new Paint();
		paintAlpha.setColor(HZDR.CLR_TS);
		paintAlpha.setAlpha(0);
		
		this.vw = vw;
		this.vh = vh;
		
		unith = vh/12;
		unith_h = unith/2;
		marginLR = vw/32;
		unitw = vw/4;
		unitw_h = unitw/2;
		
		status = S_CLOSED;
		touchID = -1;
	}
	
	public void setItems(String[] items, int location, int tag)
	{
		sArrItems = items;
		loc = location;
		this.tag = tag;
		
		if(sArrItems == null)
		{
			tatalh = unith;
			unitw = vw/4;
			unitw_h = unitw/2;
		}
		else
		{
			if(location == LOC_BTM)
			{
				unitw = vw - marginLR*4;
				unitw_h = unitw/2;
			}
			else
			{
				paintAlpha.setTextSize(PaintUtil.fontS_3);
				int
					i1 = 0,
					maxw = vw/4; // 最小宽度
				float tmpw = 0;
				while(i1 < sArrItems.length)
				{
					if(sArrItems[i1] != null)
					{
						tmpw = paintAlpha.measureText(sArrItems[i1]);
						if(tmpw > maxw)
						{
							maxw = (int) tmpw;
						}
					}
					++i1;
				}
				
				unitw = maxw + PaintUtil.fontS_3*2;
				unitw_h = unitw/2;
			}
			
			tatalh = sArrItems.length * unith;
		}
		reDraw();
	}
	
	protected void onDraw(Canvas cvs)
	{
		cvs.setDrawFilter(PaintUtil.pfd);
		cvs.drawRect(0, 0, vw, vh, paintAlpha);
		
		if(sArrItems != null)
		{
			if(loc == LOC_BTM)
			{
				dy = (int) (vh - (tatalh*percent));
				dx = marginLR*2;
			}
			else
			{
				if(loc == LOC_TOP_LEFT)
				{
					dx = marginLR;
				}
				else
				{
					dx = vw - marginLR - unitw;
				}
				dy = (int) (tatalh*(percent - 1));
			}
			
			paint.setColor(HZDR.CLR_B1);
			cvs.drawRect(dx, dy, dx + unitw, dy + tatalh, paint);
			
			paint.setColor(HZDR.CLR_F3);
			paint.setTextSize(PaintUtil.fontS_3);
			i1 = 0;
			while(i1 < sArrItems.length)
			{
				if(touchID == i1)
				{
					paint.setColor(HZDR.CLR_B8);
					cvs.drawRect(dx, dy, dx + unitw, dy + unith, paint);
					paint.setColor(HZDR.CLR_F3);
				}
				cvs.drawText("" + sArrItems[i1], dx + PaintUtil.fontS_4, dy + unith_h + PaintUtil.fontHH_3, paint);
				if(i1 > 0)
				{
					cvs.drawLine(dx, dy, dx + unitw, dy, paint);
				}
				
				dy += unith;
				++i1;
			}
			
//			cvs.drawLine(dx + PaintUtil.fontS_4, 0, dx + PaintUtil.fontS_4, tatalh, paint);
//			cvs.drawLine(dx + PaintUtil.fontS_4 + paint.measureText(sArrItems[0]), 0, dx + PaintUtil.fontS_4 + paint.measureText(sArrItems[0]), tatalh, paint);
//			cvs.drawLine(dx + unitw, 0, dx + unitw, tatalh, paint);
		}
	}
	protected void reDraw()
	{
		postInvalidate();
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				tdx = event.getX();
				tdy = event.getY();
				tlx = tdx;
				tly = tdy;
				movedx = 0;
				movedy = 0;
				bmoved = false;
				
				if(status == S_OPENED)
				{
					if(loc == LOC_BTM)
					{
						if(tdy > vh - tatalh)
						{
							if(tdx > marginLR*2 && tdx < vh - marginLR*2)
							{
								touchID = (int) (tdy - (vh - tatalh))/unith;
							}
						}
					}
					else
					{
						if(tdy < tatalh)
						{
							if(loc == LOC_TOP_LEFT)
							{
								if(tdx < marginLR + unitw)
								{
									touchID = (int) (tdy/unith);
								}
							}
							else if(loc == LOC_TOP_RIGHT)
							{
								if(tdx > vw - marginLR - unitw)
								{
									touchID = (int) (tdy/unith);
								}
							}
						}
					}
				}
				reDraw();
				break;
			case MotionEvent.ACTION_MOVE:
				tmx = event.getX();
				tmy = event.getY();
				
				movedx += Math.abs(tmx - tlx); // 累计X轴移动距离
				movedy += Math.abs(tmy - tly); // 累计Y轴移动距离
				
				if(!bmoved)
				{
					if(movedy > HZDodo.sill || movedx > HZDodo.sill)
					{
						bmoved = true;
						touchID = -1;
						reDraw();
					}
				}
				
				tlx = tmx;
				tly = tmy;
				break;
			case MotionEvent.ACTION_UP:
				tux = event.getX();
				tuy = event.getY();
				
				if(status == S_OPENED)
				{
					if(!bmoved)
					{
						if(loc == LOC_TOP_LEFT)
						{
							if(tdy > tatalh || tdx > marginLR + unitw)
							{
								hide();
							}
						}
						else if(loc == LOC_TOP_RIGHT)
						{
							if(tdy > tatalh || tdx < vh - marginLR - unitw)
							{
								hide();
							}
						}
						else if(loc == LOC_BTM)
						{
							if(tdy < vh - tatalh || tdx < marginLR*2 || tdx > vw - marginLR*2)
							{
								hide();
							}
						}
						
						if(touchID >= 0)
						{
							if(sArrItems != null && touchID < sArrItems.length)
							{
								if(callback != null)
								{
									percent = 0;
									status = S_CLOSED;
									callback.dismiss();
									
									callback.onSelectedItem(tag, sArrItems[touchID]);
								}
							}
						}
					}
				}
				
				touchID = -1;
				reDraw();
				break;
		}
		return true;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				hide();
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@SuppressLint ("NewApi")
	public void show()
	{
		if(percent > 0) return;
		DSound.playTouchSound(ctx);
		touchID = -1;
		
		setFocusable(true);
		setFocusableInTouchMode(true); // 设置焦点
		requestFocus(); // 请求焦点
		
		Logger.i("show() ");
		
		if(animator != null) animator.cancel();
		status = S_OPENNING;

		animator = ValueAnimator.ofFloat(0.0f, 1.0f);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(200).start();
		animator.addUpdateListener(new AnimatorUpdateListener()
		{
			public void onAnimationUpdate(ValueAnimator animation)
			{
				Float value = (Float) animation.getAnimatedValue();
				percent = value.floatValue();
				paintAlpha.setAlpha((int)(80*percent));
				if(percent >= 1.0f)
				{
					status = S_OPENED;
				}
				reDraw();
			}
		});
	}
	
	@SuppressLint ("NewApi")
	public void hide()
	{
		if(status == S_CLOSEING || status == S_CLOSED) return;
		Logger.i("hide() ");
//		touchID = -1;
		status = S_CLOSEING;

		if(animator != null) animator.cancel();

		animator = ValueAnimator.ofFloat(1.0f, 0.0f);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(200).start();
		animator.addUpdateListener(new AnimatorUpdateListener()
		{
			public void onAnimationUpdate(ValueAnimator animation)
			{
				Float value = (Float) animation.getAnimatedValue();
				percent = value.floatValue();
				paintAlpha.setAlpha((int)(80*percent));
				if(percent <= 0)
				{
					status = S_CLOSED;
					
					if(callback != null) callback.dismiss();
				}
				reDraw();
			}
		});
	}
	
	public int getStatus()
	{
		return status;
	}
}
