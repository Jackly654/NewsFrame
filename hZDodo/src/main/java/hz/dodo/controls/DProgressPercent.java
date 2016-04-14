package hz.dodo.controls;

import hz.dodo.Logger;
import hz.dodo.PaintUtil;
import hz.dodo.StrUtil;
import hz.dodo.data.HZDR;
import hz.dodo.graphic.BmNine;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class DProgressPercent extends View
{

	public interface Callback
	{
		public void dismissed(final String tag);
	}
	
	public final int CLR_BACKGROUD = 0;
	public final int CLR_CONTENT = 1;
	
	ViewGroup vroot;
	Bitmap bmbg;
	Paint paint;
	Callback callback;
	
	NinePatch np;

	RectF
		rectfBg,
		rectfCnt,
		rectfPgBg,
		rectfPgCur;
		
	int
		radius, // 背景框的圆角
		radiusx, // 进度条圆角
		radiusy, // 进度条圆角
		marginL,
		pw, ph,
		iDdescriptionDy, // 描述文字坐标
		iProgressDy, // 进度条坐标
		iProgressH, // 进度条高度
		iPercentDy, // 百分比坐标
		dx,
		clrBg, clrTxt;
	
	String sSrc, sDest, sProgress, tag;
	
	@Deprecated
	private DProgressPercent(Context ctx)
	{
		super(ctx);
	}

	public DProgressPercent(Activity at, int fw, int fh)
	{
		super(at);
		setWillNotDraw(false);

		vroot = (ViewGroup) at.findViewById(android.R.id.content);
		rectfBg = new RectF();
		rectfCnt = new RectF();
		rectfPgBg = new RectF();
		rectfPgCur = new RectF();
		
		clrBg = HZDR.CLR_TS;
		clrTxt = HZDR.CLR_B7;
		
		PaintUtil.getInstance(at.getWindowManager());
		paint = PaintUtil.paint;
		
		int small = fw < fh ? fw : fh;
		int big = fw > fh ? fw : fh;
		radius = small/32;
		marginL = PaintUtil.fontS_1;
		pw = small*2/3;
		ph = big*400/1846;
		marginL = PaintUtil.fontS_3;
		
		sProgress = "0%";
		sSrc = sDest = "请稍候…";
		
		rectfBg.set((fw - pw)/2, (fh - ph)/2, (fw + pw)/2, (fh + ph)/2);
		rectfCnt.set(rectfBg);
		
		iProgressH = (int) (rectfBg.height()/20);
		radiusx = iProgressH/4;
		radiusy = iProgressH/2;
	}
	
	private void initSize(final int fw, final int fh)
	{
		sDest = StrUtil.breakText("" + sSrc, (int)(rectfCnt.width() - PaintUtil.fontS_3), paint);
		if(sDest == null || sDest.length() <= 0)
		{
			sDest = "请稍候…";
		}
		iDdescriptionDy = (int) (rectfCnt.top + rectfCnt.height()*50/160);
		iProgressDy = (int) (rectfCnt.top + rectfCnt.height()*100/160);
		iPercentDy = (int) (rectfCnt.top + rectfCnt.height()*120/160);
		
		rectfPgBg.set(rectfCnt.left + marginL, iProgressDy - iProgressH/2, rectfCnt.right - marginL, iProgressDy + iProgressH/2);
		rectfPgCur.set(rectfPgBg);
		rectfPgCur.right = rectfPgCur.left;
	}
	
	public void destroy()
	{
	}
	
	public void setImg(Bitmap bmbg)
	{
		this.bmbg = bmbg;
		np = null;
		if(bmbg != null)
		{
			BmNine bn = new BmNine();
			int[] iArr = bn.getPadding(bmbg);
			if(iArr != null && iArr.length == 4)
			{
				rectfCnt.set(rectfBg.left + iArr[0], rectfBg.top + iArr[2], rectfBg.right - iArr[1], rectfBg.bottom - iArr[3]);

				byte[] bArr = bmbg.getNinePatchChunk();
				np = new NinePatch(bmbg, bArr, null);
			}
		}
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
	// progress:[0,1], ival:小数点后保留几位
	public void setProgress(final float progress, final int ival)
	{
		float ft = progress < 0 ? 0 : (progress > 1 ? 1 : progress);
		rectfPgCur.right = rectfPgBg.left + rectfPgBg.width()*ft;
		sProgress = StrUtil.getStr(ft*100, ival) + "%";
		reDraw();
	}
	public void setContent(final String cnt)
	{
		sSrc = cnt;
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
		
		reDraw();
	}
	
	public boolean isRunning()
	{
		return getParent() != null;
	}
	
	protected void onDraw(Canvas cvs)
	{
		cvs.setDrawFilter(PaintUtil.pfd);
		// 半透明背景
		cvs.drawColor(HZDR.CLR_TS);
		
		// 框背景
		if(np != null)
		{
			np.draw(cvs, rectfBg);
		}
		else
		{
			paint.setColor(clrBg);
			cvs.drawRoundRect(rectfBg, radius, radius, paint);
		}
		
		// 提示文字
		paint.setTextSize(PaintUtil.fontS_3);
		paint.setColor(clrTxt);
		dx = (int) (rectfCnt.centerX() - paint.measureText("" + sDest)/2);
		cvs.drawText("" + sDest, dx, iDdescriptionDy + PaintUtil.fontHH_3, paint);
		
		// 进度条
		paint.setColor(HZDR.CLR_B1);
		cvs.drawRoundRect(rectfPgBg, radiusx, radiusy, paint);
		paint.setColor(HZDR.CLR_B2);
		cvs.drawRoundRect(rectfPgCur, radiusx, radiusy, paint);
		
		// 百分数
		paint.setColor(HZDR.CLR_B1);
		paint.setTextSize(PaintUtil.fontS_3);
		dx = (int) (rectfCnt.centerX() - paint.measureText("" + sProgress)/2);
		cvs.drawText("" + sProgress, dx, iPercentDy + PaintUtil.fontHH_3, paint);
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
			
			if(callback != null) callback.dismissed(tag);
		}
		catch (Exception e1)
		{
			Logger.e("DProgress dismiss() " + e1.toString());
		}
	}
}
