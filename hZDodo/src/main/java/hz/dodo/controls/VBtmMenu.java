package hz.dodo.controls;

import hz.dodo.PaintUtil;
import hz.dodo.StrUtil;
import hz.dodo.controls.FView;
import hz.dodo.data.Empty;
import hz.dodo.data.HZDR;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.ViewGroup;

public class VBtmMenu extends FView
{
	public interface Callback
	{
		public void onClick(final int pos, final String item, final Object tag);
	}
	
	private final int DELAY = 100;
	
	private final int MAX_COLUMN = 4; // 最多列数

	private final int STYLE_IMG = 0; // 仅显示图标
	private final int STYLE_IMG_TXT = 1; // 显示图标+文字

	ViewGroup vroot;
	Timer timer;
	TimerTask tt;
	Callback callback;
	Object obj;

	Rect
		winRect;
	Bitmap
		bm;
	
	int
		iStyle,
		i1,
		dy, dx,
		iRow, // 几行
		unitw, unitw_h, unith, unith_h,
		imgdy, txtdy, // 图标/文字对齐线

		iLooperItem, // 动画
		touchid, // 按下
		
		itr, itc; // touch row / touch column 
	
	int[]
		iArrIds;
	String[]
		sArrItems;

	public VBtmMenu(Activity at, Callback callback, int vw, int vh)
	{
		super(at, vw, vh);
		this.callback = callback;
		setBackgroundColor(HZDR.CLR_TS);
		vroot = (ViewGroup) at.findViewById(android.R.id.content);
		winRect = new Rect();
		vroot.getWindowVisibleDisplayFrame(winRect);
		this.vh = winRect.height();
		this.vw = winRect.width();
		
		unith = this.vh/7;
		unith_h = unith/2;
		
		touchid = -1;
	}
	public void registerListener(Callback callback)
	{
		this.callback = callback;
	}
	public void onDestroy()
	{
		cancel();
		if(timer != null)
		{
			timer.cancel();
			timer = null;
		}
	}
	@Override
	protected void onDraw(Canvas cvs)
	{
		cvs.setDrawFilter(PaintUtil.pfd);
		paint.setColor(HZDR.CLR_B7);
		cvs.drawRect(winRect.left, winRect.bottom - iRow*unith, winRect.right, winRect.bottom, paint);
		
		if(!Empty.isEmpty(iArrIds))
		{
			paint.setTextSize(PaintUtil.fontS_5);
			
			dx = -unitw;
			dy = winRect.bottom - (iRow + 1)*unith;
			
			i1 = 0;
			while(i1 < iArrIds.length && i1 <= iLooperItem)
			{
				if(i1 == MAX_COLUMN)
				{
					dx = 0;
				}
				else
				{
					dx += unitw;
				}
				
				if(i1%MAX_COLUMN == 0)
				{
					dy += unith;
				}
				
				if(i1 == touchid)
				{
					paint.setColor(HZDR.CLR_B8);
					cvs.drawRect(dx, dy, dx + unitw, dy + unith, paint);
				}
				
				if(null != (bm = im.getBmId(iArrIds[i1])))
				{
					cvs.drawBitmap(bm, dx + unitw_h - bm.getWidth()/2, dy + imgdy - bm.getHeight()/2, null);
				}
				if(iStyle == STYLE_IMG_TXT)
				{
					if(!Empty.isEmpty(sArrItems) && i1 < sArrItems.length && !Empty.isEmpty(sArrItems[i1]))
					{
						paint.setColor(HZDR.CLR_F3);
						cvs.drawText(sArrItems[i1], dx + unitw_h - StrUtil.pixHalf(paint, sArrItems[i1]), dy + txtdy + PaintUtil.fontHH_5, paint);
					}
				}
				
				paint.setColor(HZDR.CLR_B4);
				cvs.drawLine(dx, dy + unith, dx + unitw, dy + unith, paint);
				cvs.drawLine(dx + unitw, dy, dx + unitw, dy + unith, paint);

				++i1;
			}
		}
	}
	// sArrItems:文字
	// iArrIds:图标索引
	// sArrItems & iArrIds 须一一对应
	public void setItems(final String[] sArrItems, final int[] iArrIds)
	{
		this.sArrItems = sArrItems;
		this.iArrIds = iArrIds;
		iStyle = STYLE_IMG_TXT;
		
		imgdy = unith*2/5;
		txtdy = unith*4/5;
		
		reCalc();
		reDraw();
	}
	public void setItems(final int[] iArrIds)
	{
		this.sArrItems = null;
		this.iArrIds = iArrIds;
		iStyle = STYLE_IMG;
		
		imgdy = unith_h;
		txtdy = -vh;
		
		reCalc();
		reDraw();
	}
	private void reCalc()
	{
		if(!Empty.isEmpty(iArrIds))
		{
			unitw = iArrIds.length > MAX_COLUMN ? vw/MAX_COLUMN : vw/iArrIds.length;
			unitw_h = unitw/2;
			iRow = iArrIds.length/MAX_COLUMN + (iArrIds.length%MAX_COLUMN == 0 ? 0 : 1);
		}
	}
	public void show(final Object tag)
	{
		setFocus();
		obj = tag;
		if(vroot != null && getParent() == null)
		{
			vroot.addView(this);
			iLooperItem = 0;
			animItem();
		}
	}
	public void dismiss()
	{
		if(vroot != null && getParent() == vroot)
		{
			vroot.removeView(this);
			iLooperItem = 0;
		}
	}
	private void animItem()
	{
		if(Empty.isEmpty(iArrIds)) return;
		start();
	}
	private void start()
	{
		cancel();
		if(timer == null) timer = new Timer(true);
		
		tt = new TimerTask()
		{
			public void run()
			{
				if(Empty.isEmpty(iArrIds))
				{
					cancel();
				}
				else
				{
					if(iLooperItem < iArrIds.length)
					{
						++iLooperItem;
						reDraw();
					}
					else
					{
						cancel();
					}
				}
			}
		};
		timer.schedule(tt, DELAY, DELAY);
	}
	private void cancel()
	{
		if(tt == null) return;
		tt.cancel();
		tt = null;
	}
	private void setFocus()
	{
		setFocusable(true);
		setFocusableInTouchMode(true); // 设置焦点
		requestFocus(); // 请求焦点
	}
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				dismiss();
				break;
		}
		return true;
	}
	@Override
	protected void onTouchDown(float tdx, float tdy)
	{
		if(Empty.isEmpty(iArrIds)) return;
		
		if(tdy > winRect.bottom - iRow*unith)
		{
			itc = (int) (tdx/unitw);
			itr = (int) ((tdy - (winRect.bottom - iRow*unith))/unith);
			touchid = itc + itr*MAX_COLUMN;
			reDraw();
		}
	}
	@Override
	protected void onTouchMoved()
	{
		touchid = -1;
		reDraw();
	}
	@Override
	protected void onTouchCancel()
	{
		touchid = -1;
		reDraw();
	}
	@Override
	protected void onTouchUp(boolean bMoved, float tux, float tuy)
	{
		if(!bMoved)
		{
			if(!Empty.isEmpty(iArrIds) && touchid >= 0 && touchid < iArrIds.length)
			{
				if(callback != null)
				{
					String item = null;
					if(!Empty.isEmpty(sArrItems) && touchid >= 0 && touchid < sArrItems.length)
					{
						item = sArrItems[touchid];
					}
					callback.onClick(touchid, item, obj);
				}
			}
			else
			{
				if(callback != null)
				{
					callback.onClick(-1, null, null);
				}
			}
		}
		touchid = -1;
		reDraw();
	}
}
