package hz.dodo.controls;

import java.util.Timer;
import java.util.TimerTask;

import hz.dodo.data.HZDR;
import hz.dodo.Logger;
import hz.dodo.PaintUtil;
import hz.dodo.StrUtil;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class DProgress extends View
{
	public interface Callback
	{
		public void dismissed(final String tag);
	}
	
	public final int CLR_BACKGROUD = 0;
	public final int CLR_CONTENT = 1;
	
	ViewGroup vroot;
	Matrix matrix;
	Bitmap bm;
	Paint paint;
	Callback callback;

	Timer timer;
	TimerTask tt;
	
	RectF rectf;
	int radius,
		marginL,
		pw, ph,
		dx,
		clrBg, clrTxt;
	
	float imgCX, imgCY; // 图片中心点
	
	String strSrc, strDest, tag;
	
	boolean isRunning;
	
	private DProgress(Context ctx)
	{
		super(ctx);
	}

	public DProgress(Activity at, int fw, int fh)
	{
		super(at);
		setWillNotDraw(false);

		vroot = (ViewGroup) at.findViewById(android.R.id.content);
		rectf = new RectF();
		
		clrBg = HZDR.CLR_TS;
		clrTxt = HZDR.CLR_B7;
		
		PaintUtil.getInstance(at.getWindowManager());
		paint = PaintUtil.paint;
		
		int small = fw < fh ? fw : fh;
		int big = fw > fh ? fw : fh;
		radius = small/32;
		marginL = small/32;
		pw = small*2/3;
		ph = big*200/1846;
		
		strSrc = strDest = "请稍候…";
		initSize(fw, fh);
		
		matrix = new Matrix();
		timer = new Timer(true);
	}
	
	public void destroy()
	{
		cancel();
		if(timer != null)
		{
			timer.cancel();
			timer = null;
		}
	}
	
	private void initSize(int fw, int fh)
	{
		rectf.set((fw - pw)/2, (fh - ph)/2, (fw + pw)/2, (fh + ph)/2);
		calcStr(strSrc);
		
		if(bm != null)
		{
			imgCX = rectf.left + marginL + bm.getWidth()/2;
			imgCY = rectf.centerY();
			matrix.setTranslate(rectf.left + marginL, imgCY - bm.getHeight()/2);
		}
	}
	
	public void setImg(Bitmap bm)
	{
		this.bm = bm;
	}
	
	public void setColor(int clrType, int clrVal)
	{
		switch(clrType)
		{
			case CLR_BACKGROUD:
				clrBg = clrVal;
				break;
			case CLR_CONTENT:
				clrTxt = clrVal;
				break;
		}
	}
	public void setContent(final String cnt)
	{
		calcStr(strSrc = cnt);
	}
	private void calcStr(final String cnt)
	{
		strDest = StrUtil.breakText("" + cnt, (int)(rectf.width() - rectf.height() - PaintUtil.fontS_3), paint);
		if(strDest == null || strDest.length() <= 0)
		{
			strDest = "请稍候…";
		}
	}
	
	public void show(final Callback callback, final String tag, int fw, int fh)
	{
		this.callback = callback;
		this.tag = tag;
		initSize(fw, fh);
		setFocus();
		if (vroot != null && getParent() == null)
		{
			vroot.addView(this);
		}
		
		start();
	}
	
	public boolean isRunning()
	{
		return isRunning;
	}
	
	protected void onDraw(Canvas cvs)
	{
		cvs.setDrawFilter(PaintUtil.pfd);
		
		cvs.drawColor(HZDR.CLR_TS);
		paint.setColor(clrBg);
		cvs.drawRoundRect(rectf, radius, radius, paint);
		
		if(bm != null)
		{
			cvs.drawBitmap(bm, matrix, paint);
			dx = (int) (rectf.left + marginL*2 + bm.getWidth());
		}
		else
		{
			dx = (int) (rectf.left + marginL);
		}
		
		paint.setTextSize(PaintUtil.fontS_3);
		paint.setColor(clrTxt);
		cvs.drawText("" + strDest, dx, rectf.centerY() + PaintUtil.fontHH_3, paint);
	}
	
	private void reDraw()
	{
		postInvalidate();
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		return true;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				dismiss();
				return true;
			case KeyEvent.KEYCODE_MENU:
				return true;
			case KeyEvent.KEYCODE_SEARCH:
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setFocus()
	{
		setFocusable(true);
		setFocusableInTouchMode(true); // 设置焦点
		requestFocus(); // 请求焦点
	}
	
	public void dismiss()
	{
		try
		{
			if (vroot != null)
				vroot.removeView(this);
			
			isRunning = false;
			cancel();
			
			if(callback != null) callback.dismissed(tag);
		}
		catch (Exception e1)
		{
			Logger.e("DProgress dismiss() " + e1.toString());
		}
	}
	
	public void start()
	{
		cancel();
		isRunning = true;
		tt = new TimerTask()
		{
			public void run()
			{
				matrix.postRotate(30, imgCX, imgCY);
				reDraw();
			}
		};
		timer.schedule(tt, 0, 100);
	}
	
	public void cancel()
	{
		if(tt == null) return;
		tt.cancel();
		tt = null;
	}
}
