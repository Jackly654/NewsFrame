package hz.dodo.controls;

import hz.dodo.data.HZDR;
import hz.dodo.HZDodo;
import hz.dodo.Logger;
import hz.dodo.PaintUtil;
import hz.dodo.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * !!!一定要调用destroy()
 */
public class DText extends View
{
	public interface Callback
	{
		public void onClick(View v);
	}
	
	final int TICK_UNIT = 40;
	final int FPS = 1000/TICK_UNIT;
	final int DELAY_DURATION = FPS*2; // 每行停留2秒种

	Timer timer;
	TimerTask tt;
	Context ctx;
	Paint paint;
	
	Callback callback;

	int vw, vh, // 控件的宽/高
		tdx, tdy, tmx, tmy, tlx, tly, tux, tuy, // touch event
		orientation, // 滚动方向, 0横向,1纵向
		loop, loopTmp, // 循环次数
		txtWidthPix, // 串的长度,横向滚动会用到
		paddingLR, // 左右padding
		fonts, fonthh, baseLine,
		txtClr, txtNorClr, txtSctClr, // 字高,正常显示颜色,按下颜色
		bgClr, bgNorClr, bgSctClr, // 背景正常/按下颜色
		i1,
		dy1, dy2, dyTop1, dyTop2, dx1, dx2, // 贴图 dyTop == text top y
		delay, // 纵向滚动每行停留的时间
		movedx, movedy; // 累计移动 / 方向
	
	boolean isScroll, canScroll; // 是否需要滚动, 能否滚动(纵向滚动每行要停留时间)
	
	String strSrc, // 资源串
		   strBreak, // 静止不动的话,以...结尾的串
		   strDraw; // draw 的临时串
	List<String> ltStr; // 数组串,如果需要纵向滚动的话,会用到
	
	protected DText(Context context)
	{
		super(context);
	}
	/*protected DText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	protected DText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}*/
	
	public DText(Activity at, Callback callback, int vw, int vh)
	{
		super(at);
		setWillNotDraw(false); // call draw
		setFocusable(true);
		setFocusableInTouchMode(true); // 设置焦点
		requestFocus(); // 请求焦点

		this.callback = callback;
		this.vw = vw;
		this.vh = vh;
		Logger.i("vh:" + vh);
		
		PaintUtil.getInstance(at.getWindowManager());
		paint = PaintUtil.paint;
		
		orientation = 0;
		fonts = PaintUtil.fontS_3;
		fonthh = PaintUtil.fontHH_3;
		baseLine = vh/2 + fonthh;
		
		paddingLR = fonts/2;
		txtNorClr = HZDR.CLR_B1;
		txtSctClr = HZDR.CLR_F3;
		txtClr = txtNorClr;
		
		bgNorClr = HZDR.CLR_B3;
		bgSctClr = HZDR.CLR_B8;
		bgClr = bgNorClr;
		
		ltStr = new ArrayList<String>(5);
		canScroll = true;
		timer = new Timer(true);
	}
	
	public void destroy()
	{
		cancelTT();
		if(timer != null)
		{
			timer.cancel();
			timer = null;
		}
	}
	
	protected void onDraw(Canvas cvs)
	{
		cvs.setDrawFilter(PaintUtil.pfd);
		cvs.drawColor(bgClr);
		
		if(isScroll)
		{
			switch(orientation)
			{
				case 0:
					drawHorizontal(cvs);
					break;
				case 1:
					drawVertical(cvs);
					break;
			}
		}
		else
		{
			drawNormal(cvs);
		}
	}
	
	// 正常字符串
	private void drawNormal(Canvas cvs)
	{
		if(strBreak != null)
		{
			paint.setTextSize(fonts);
			paint.setColor(txtClr);
			cvs.drawText(strBreak, paddingLR, baseLine, paint);
		}
	}
	// 横向滚动
	private void drawHorizontal(Canvas cvs)
	{
		if(strSrc != null)
		{
			paint.setColor(txtClr);
			paint.setTextSize(fonts);
			if(dx1 < vw)
			{
				cvs.drawText(strSrc, dx1, baseLine, paint);
			}
			if(dx2 < vw)
			{
				cvs.drawText(strSrc, dx2, baseLine, paint);
			}
		}
	}
	// 纵向滚动
	private void drawVertical(Canvas cvs)
	{
		if(ltStr.size() <= 0)
		{
			if(isScroll)
			{
				cancelTT();
			}
		}
		else
		{
			dy1 = dyTop1;
			dy2 = dyTop2;
			
			paint.setColor(txtClr);
			paint.setTextSize(fonts);
			
			i1 = 0;
			while(i1 < ltStr.size())
			{
				if(null != (strDraw = ltStr.get(i1)))
				{
					if(dy1 + vh <= 0 || dy1 >= vh)
					{
					}
					else
					{
						cvs.drawText(ltStr.get(i1), paddingLR, dy1 + baseLine, paint);
					}
					
					dy1 += vh;
				}
				++i1;
			}
			
			i1 = 0;
			while(i1 < ltStr.size())
			{
				if(null != (strDraw = ltStr.get(i1)))
				{
					if(dy2 + vh <= 0 || dy2 >= vh)
					{
					}
					else
					{
						cvs.drawText(ltStr.get(i1), paddingLR, dy2 + baseLine, paint);
					}
					dy2 += vh;
				}
				++i1;
			}
		}
	}
	
	public void reDraw()
	{
		postInvalidate();
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		Logger.i("DText ontouch " + event.getAction());
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				tlx = tdx = (int) event.getX();
				tly = tdy = (int) event.getY();
				movedx = 0;
				movedy = 0;
				
				bgClr = bgSctClr;
				txtClr = txtSctClr;
				break;
			case MotionEvent.ACTION_MOVE:
				tmx = (int) event.getX();
				tmy = (int) event.getY();
				
				movedx += Math.abs(tmx - tlx); // 累计X轴移动距离
				movedy += Math.abs(tmy - tly); // 累计Y轴移动距离
				
				tlx = tmx;
				tly = tmy;
				
				break;
			case MotionEvent.ACTION_UP:
				tux = (int) event.getX();
				tuy = (int) event.getY();
				
				bgClr = bgNorClr;
				txtClr = txtNorClr;
				
				if(movedx > HZDodo.sill || movedy > HZDodo.sill)
				{
				}
				else
				{
					if(callback != null) callback.onClick(this);
				}
				break;
		}
		reDraw();
		return true;
	}
	
	// 滚动方向,默认/0横向,1纵向
	public void setOrientation(final int orientation)
	{
		this.orientation = orientation;
		reDraw();
	}
	public void setTxtSize(final int fonts, final int fonthh, final int paddingLR)
	{
		// 字号改变了的话,重新处理字符串
		if(this.fonts != fonts || this.paddingLR != paddingLR)
		{
			this.fonts = fonts;
			this.fonthh = fonthh;
			baseLine = vh/2 + fonthh;
			this.paddingLR = paddingLR;
			setTextSrc(strSrc, orientation);
		}
	}
	public void setTxtClr(final int normal, final int select)
	{
		txtNorClr = normal;
		txtSctClr = select;
	}
	public void setBackground(final int normal, final int select)
	{
		bgNorClr = normal;
		bgSctClr = select;
	}
	public void setTextSrc(final String txt, final int orientation)
	{
		if(txt == null || txt.equals("" + strSrc)) return;
		
		setOrientation(orientation);
		
		strSrc = txt;
		paint.setTextSize(fonts);
		txtWidthPix = (int) paint.measureText(strSrc);
		strBreak = StrUtil.breakText(strSrc, vw - paddingLR*2, paint);
		String[] strArrDraw = StrUtil.getDesArray(strSrc, vw - paddingLR*2, paint);
		
		ltStr.clear();
		if(strArrDraw != null)
		{
			int i1 = 0;
			while(i1 < strArrDraw.length)
			{
				ltStr.add(strArrDraw[i1]);
				++i1;
			}
		}
		
		if(orientation == 0) // 横向
		{
			if(txtWidthPix > vw - paddingLR*2)
			{
				startTT();
			}
			else
			{
				cancelTT();
			}
		}
		else if(orientation == 1) // 纵向
		{
			if(ltStr.size() > 0)
			{
				startTT();
			}
			else
			{
				cancelTT();
			}
		}
		else
		{
			cancelTT();
		}
			
		reDraw();
	}
	
	// 设置循环次数 0 为无限循环
	public void setLoop(final int loop)
	{
		this.loop = loop;
	}
	
	public void reScroll()
	{
		startTT();
	}
	
	void startTT()
	{
		cancelTT();
		tt = new TimerTask()
		{
			public void run()
			{
				if(isScroll)
				{
					switch(orientation)
					{
						case 0: // 横向
							if(dx1 + txtWidthPix < 0)
							{
								dx1 = dx2 + txtWidthPix + vw/2;
							}
							else
							{
								--dx1;
							}
							if(dx2 + txtWidthPix < 0)
							{
								dx2 = dx1 + txtWidthPix + vw/2;
							}
							else
							{
								--dx2;
							}
							
							if(loop > 0)
							{
								if(dx2 == paddingLR)
								{
									loopTmp++;
									if(loopTmp >= loop)
									{
										cancelTT();
									}
								}
							}
							break;
						case 1: // 纵向
							
							if(canScroll)
							{
								if(dyTop1 + ltStr.size()*vh < 0)
								{
									dyTop1 = dyTop2 + ltStr.size()*vh;
									Logger.i("top1 调整到top2 底下, top1:" + dyTop1);
								}
								else
								{
									--dyTop1;
								}
								
								if(dyTop2 + ltStr.size()*vh < 0)
								{
									dyTop2 = dyTop1 + ltStr.size()*vh;
									Logger.i("top2 调整到top1 底下, top2:" + dyTop2);
								}
								else
								{
									--dyTop2;
								}
								
								if(dyTop1%vh == 0)
								{
									canScroll = false;
								}
							}
							else
							{
								++delay;
								if(delay == DELAY_DURATION)
								{
									canScroll = true;
									delay = 0;
								}
							}
							break;
					}
					reDraw();
				}
			}
		};
		
		switch(orientation)
		{
			case 0: // 横向
				dx1 = paddingLR;
				dx2 = dx1 + txtWidthPix + vw/2;
				loopTmp = 0;
				break;
			case 1:
				dyTop1 = 0;
				dyTop2 = dyTop1 + ltStr.size()*vh;
				Logger.i("初始化top1:" + dyTop1 + ", top2:" + dyTop2);
				break;
		}

		isScroll = true;
		timer.schedule(tt, 1000, TICK_UNIT);
	}
	void cancelTT()
	{
		isScroll = false;
		reDraw();

		if(tt == null) return;
		tt.cancel();
		tt = null;
	}
}