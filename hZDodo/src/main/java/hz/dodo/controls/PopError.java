package hz.dodo.controls;

import java.util.ArrayList;
import java.util.List;

import hz.dodo.data.HZDR;
import hz.dodo.PaintUtil;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

// 加权限 <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
public class PopError implements OnKeyListener, OnTouchListener
{
	WindowManager wm;
	DPopView dpv;
	Paint paint;
	
	public PopError(Context ctx, final String errorLog)
	{
		wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		PaintUtil.getInstance(wm);
		paint = PaintUtil.paint;
		
		dpv = new DPopView(ctx, errorLog);
		dpv.setOnKeyListener(this);
		dpv.setOnTouchListener(this);
		
		new Handler(Looper.getMainLooper(), new Handler.Callback()
		{
			public boolean handleMessage(Message msg)
			{
				LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_ALT_FOCUSABLE_IM, PixelFormat.TRANSPARENT);
				wm.addView(dpv, layoutParams);
				return true;
			}
		}).sendEmptyMessage(0);
	}
	
	public boolean onKey(View v, int keyCode, KeyEvent event)
	{
		if(event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch(keyCode)
			{
				case KeyEvent.KEYCODE_BACK:
					wm.removeView(dpv);
					break;
				case KeyEvent.KEYCODE_MENU:
					break;
				case KeyEvent.KEYCODE_SEARCH:
					break;
			}
		}
		return true;
	}
	
	public boolean onTouch(View v, MotionEvent event)
	{
		switch(event.getAction())
		{
			case MotionEvent.ACTION_UP:
				if(dpv.canRemove(event.getY()))
				{
					wm.removeView(dpv);
				}
				break;
		}
		return true;
	}
	
	class DPopView extends View
	{
		int tth, radius, dy, i1;
		RectF rectf;
		String[] strArr;
		String pkg;

		public DPopView(Context ctx, final String errorLog)
		{
			super(ctx);
			setWillNotDraw(false);

			Rect rect = new Rect();
			getWindowVisibleDisplayFrame(rect);
			int fw = rect.width();
			int fh = rect.height();
			radius = fw/48;
			rectf = new RectF(radius, 0, fw - radius, 0);
			
			tth = fh*150/1846;
			
			pkg = "[" + ctx.getPackageName() + "]";
			
			paint.setTextSize(PaintUtil.fontS_3);
			strArr = getDesArray("" + errorLog, (int)(rectf.width() - PaintUtil.fontS_3*2), paint);
			if(strArr == null)
			{
				strArr = new String[]{"未正常获取到日志"};
			}
			
			int h = tth + (strArr.length + 3)*PaintUtil.fontS_3;
			rectf.top = (fh - h)/2;
			rectf.bottom = rectf.top + h;
		}
		
		protected void onDraw(Canvas cvs)
		{
			cvs.drawColor(HZDR.CLR_TS);
			paint.setColor(HZDR.CLR_B1);
			cvs.drawRoundRect(rectf, radius, radius, paint);
			
			paint.setColor(HZDR.CLR_B4);
			cvs.drawLine(rectf.left, rectf.top + tth, rectf.right, rectf.top + tth, paint);
			
			paint.setColor(Color.RED);
			paint.setTextSize(PaintUtil.fontS_1);
			cvs.drawText("崩溃日志", rectf.centerX() - paint.measureText("崩溃日志")/2, rectf.top + tth/2 + PaintUtil.fontHH_1, paint);
			
			paint.setTextSize(PaintUtil.fontS_3);
			paint.setColor(HZDR.CLR_F3);
			dy = (int) (rectf.top + tth + PaintUtil.fontS_3*2);
			cvs.drawText("" + pkg, rectf.left + PaintUtil.fontS_3, dy, paint);
			dy += PaintUtil.fontS_3;
			
			if(strArr != null)
			{
				i1 = 0;
				while(i1 < strArr.length)
				{
					cvs.drawText(strArr[i1], rectf.left + PaintUtil.fontS_3, dy, paint);
					dy += PaintUtil.fontS_3;
					++i1;
				}
			}
			else
			{
				cvs.drawText("未正常获取到日志", rectf.left + PaintUtil.fontS_3, dy, paint);
			}
		}
		
		public boolean canRemove(float tuy)
		{
			if(tuy < rectf.top || tuy > rectf.bottom)
			{
				return true;
			}
			return false;
		}
		
		private String[] getDesArray(final String res, final int maxwidth, final Paint paint)
		{
			try
			{
				if(res == null || res.length() <= 0 || maxwidth <= 0 || paint == null) return null;

				List<String> list = new ArrayList<String>();
				
				String str = res.trim();
				String si1;
				int breakCount, stri = 0, i1 = 0;
				
				while(true)
				{
					si1 = str.substring(stri);
					if(si1 == null || si1.length() <= 0) break;
					breakCount = paint.breakText(si1, true, maxwidth, null);
					list.add(si1.substring(0, breakCount));
					stri += breakCount;
				}

				String[] rsts = new String[list.size()];
				i1 = 0;
				while(i1 < list.size())
				{
					rsts[i1] = list.get(i1);
					i1++;
				}
				return rsts;
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
			return null;
		}
	}
}
