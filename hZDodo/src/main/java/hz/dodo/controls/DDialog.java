package hz.dodo.controls;

import hz.dodo.data.HZDR;
import hz.dodo.HZDodo;
import hz.dodo.Logger;
import hz.dodo.PaintUtil;
import hz.dodo.StrUtil;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class DDialog extends View
{
	// 点击按钮类型/返回
	public static final int CLICK_LEFT = 0;
	public static final int CLICK_RIGHT = 1;
	public static final int CLICK_KEYBACK = 2;
	public static final int CLICK_OUTSIDE = 3;

	// 设置颜色类型
	public static final int SET_CLR_VIEW_BG = 0; // 弹出框的背景
	public static final int SET_CLR_DIVIDER_LINE = 1; // 分割线颜色
	public static final int SET_CLR_TXT_TITLE = 2; // 标题文字颜色
	public static final int SET_CLR_TXT_LEFT_BTN = 3; // 按钮文字颜色
	public static final int SET_CLR_TXT_RIGHT_BTN = 4; // 按钮文字颜色
	public static final int SET_CLR_TXT_CNT = 5; // 内容文字颜色
	public static final int SET_CLR_TXT_BTN_PRESS = 6; // 按钮按下时,文字颜色
	
	// 设置title/btn渐变色
	public static final int SET_CLR_TITLE_BG = 20;
	public static final int SET_CLR_BTN = 21; // 只有一个按钮时
	public static final int SET_CLR_BTN_LEFT = 22;
	public static final int SET_CLR_BTN_RIGHT = 23;

	// touch 用到
	private final int PRESS_NA = 0;
	private final int PRESS_LEFT = 1; // 按下左侧
	private final int PRESS_RIGHT = 2; // 按下右侧

	public interface Callback
	{
		public void onClick(final int type, final String tag);
	}

	Activity at;
	protected Callback callback;
	ViewGroup vroot;

	protected Paint paint;
	protected RectF rectf;

	GradientDrawable gdT, gdLB, gdRB, gdB; // 标题,左下,右下,下

	protected
	int fw, fh,
	// 颜色
			clr_v_bg, // 内容背景色
			clr_trs_bg, // 透明背景
			clr_title, // title色
			clr_cnt, // 内容颜色
			// clr_left_n, clr_left_s, clr_right_n, clr_right_s, // 按钮文字颜色
			clr_txt_btn_press, // 按钮按下时文字颜色
			clr_divider, // 线条色
			clr_press, // 按钮按下颜色

			// 高度
			tth, btmh, cnth, // 标题/按钮/内容
			marginTB, marginLR, // 内容上下左右留边
			dividerh, // 文字两行垂直中心线间隔
			mixCnth, // 内容区最低高度
			mixTB, // 最小上下边距
			dialogWidth, // dialog的宽度

			radius, // 圆角

			i1, dy, btnPress, // 按下了哪个按钮

			tdx, tdy, tmx, tmy, tlx, tly, tux, tuy, movedx, movedy, // 累计移动 / 方向

			orientation; // 当前屏幕方向
	
	protected
	boolean
		bMoved;

	protected
	String
		sTitle,
		sLeft,
		sRight,
		tag;

	protected
	String[]
		sCnt;

	@Deprecated
	protected DDialog(Context ctx)
	{
		super(ctx);
	}

	public DDialog(Activity at, int fw, int fh)
	{
		super(at);
		setWillNotDraw(false);
		this.at = at;
		vroot = (ViewGroup) at.findViewById(android.R.id.content);

		orientation = at.getRequestedOrientation();

		PaintUtil.getInstance(at.getWindowManager());
		paint = PaintUtil.paint;

		this.fw = fw;
		this.fh = fh;

		if (fw > fh)
		{
			fw ^= fh;
			fh ^= fw;
			fw ^= fh;
		}

		clr_v_bg = HZDR.CLR_B1;
		clr_trs_bg = HZDR.CLR_TS;
		clr_title = HZDR.CLR_B1;
		clr_cnt = HZDR.CLR_F3;
		// clr_left_n = HZDR.CLR_F1;
		// clr_right = HZDR.CLR_B2;
		clr_divider = HZDR.CLR_B4;
		clr_press = HZDR.CLR_B8;
		clr_txt_btn_press = HZDR.CLR_B1;

		sTitle = "提示";
		sLeft = "取消";
		sRight = "确定";

		dialogWidth = getw(914);
		rectf = new RectF( (fw - dialogWidth) / 2, 0, fw - ( (fw - dialogWidth) / 2), 0);

		tth = geth(150);
		btmh = geth(150);
		dividerh = geth(100);
		marginLR = (int) (rectf.width() * 60 / 914);
		marginTB = mixTB = PaintUtil.fontS_3;
		cnth = mixCnth = geth(300);

		radius = getw(36);

		initGdT(new int[] { HZDR.CLR_B6, HZDR.CLR_B2 });
		initBtnL(new int[] { clr_press, clr_press, clr_press });
		initBtnR(new int[] { clr_press, clr_press, clr_press });
		initBtn(new int[] { clr_press, clr_press, clr_press });

		setRect();
	}
	
	// 初始化title背景
	private void initGdT(final int[] clrs)
	{
		gdT = new GradientDrawable(Orientation.TOP_BOTTOM, clrs);
		gdT.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		gdT.setCornerRadii(new float[] { radius, radius, radius, radius, 0, 0, 0, 0 });
	}
	// 初始化左下角按钮背景
	private void initBtnL(final int[] clrs)
	{
		gdLB = new GradientDrawable(Orientation.BL_TR, clrs);
		gdLB.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		gdLB.setCornerRadii(new float[] { 0, 0, 0, 0, 0, 0, radius, radius });
	}
	// 初始化右下角按钮背景
	private void initBtnR(final int[] clrs)
	{
		gdRB = new GradientDrawable(Orientation.BL_TR, clrs);
		gdRB.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		gdRB.setCornerRadii(new float[] { 0, 0, 0, 0, radius, radius, 0, 0 });
	}
	// 初始化单按钮背景
	private void initBtn(final int[] clrs)
	{
		gdB = new GradientDrawable(Orientation.BL_TR, clrs);
		gdB.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		gdB.setCornerRadii(new float[] { 0, 0, 0, 0, radius, radius, radius, radius });
	}
	
	private int getw(int w)
	{
		return fw * w / 1080;
	}

	private int geth(int h)
	{
		return fh * h / 1845;
	}

	private void setFocus()
	{
		setFocusable(true);
		setFocusableInTouchMode(true); // 设置焦点
		requestFocus(); // 请求焦点
	}

	public void setTitle(final String title)
	{
		sTitle = title;
		if (sTitle == null)
		{
			tth = 0;
		}
		else
		{
			paint.setTextSize(PaintUtil.fontS_3);
			sTitle = StrUtil.breakText(title, (int) (rectf.width() - PaintUtil.fontS_3 * 2), paint);
			tth = (fh > fw ? fh : fw) * 150 / 1846;
		}
		setRect();
	}

	public void setBtn(final String left, final String right)
	{
		sLeft = left;
		sRight = right;
		if (getBtnCount() > 0)
		{
			btmh = (fh > fw ? fh : fw) * 150 / 1846;
		}
		else
		{
			btmh = 0;
		}
		setRect();
	}

	private int getBtnCount()
	{
		return (sLeft != null && sRight != null) ? 2 : ( (sLeft != null || sRight != null) ? 1 : 0);
	}

	public void setColor(final int dest, final int color)
	{
		switch (dest)
		{
			case SET_CLR_VIEW_BG:
				clr_v_bg = color;
				break;
			case SET_CLR_DIVIDER_LINE:
				clr_divider = color;
				break;
			case SET_CLR_TXT_TITLE:
				clr_title = color;
				break;
			case SET_CLR_TXT_LEFT_BTN:
				// clr_left = color;
				break;
			case SET_CLR_TXT_RIGHT_BTN:
				// clr_right = color;
				break;
			case SET_CLR_TXT_CNT:
				clr_cnt = color;
				break;
			case SET_CLR_TXT_BTN_PRESS:
				clr_txt_btn_press = color;
				break;
		}
		reDraw();
	}
	public void setColor(final int dest, final int[] clrs)
	{
		if(clrs == null) return;

		switch(dest)
		{
			case SET_CLR_TITLE_BG:
				initGdT(clrs);
				break;
			case SET_CLR_BTN_LEFT:
				initBtnL(clrs);
				break;
			case SET_CLR_BTN:
				initBtn(clrs);
				break;
			case SET_CLR_BTN_RIGHT:
				initBtnR(clrs);
				break;
		}
		reDraw();
	}

	public void setContent(final String cnt)
	{
		if (cnt == null || cnt.length() <= 0)
		{
			sCnt = null;
			setRect();
			return;
		}

		paint.setTextSize(PaintUtil.fontS_4);
		sCnt = StrUtil.getDesArray(cnt, (int) (rectf.width() - marginLR * 2), paint);
		if (sCnt != null)
		{
			int h = dividerh * sCnt.length;
			if (h + mixTB * 2 >= mixCnth)
			{
				marginTB = mixTB;
			}
			else
			{
				marginTB = (mixCnth - h) / 2;
			}
			cnth = marginTB * 2 + h;
			setRect();
		}
		else
		{
			cnth = mixCnth;
			setRect();
		}
	}

	/**
	 * @param cnt
	 *            内容
	 * @param enter
	 *            以什么作为换行符,如果传null,就以"\n"
	 */
	public void setContent(final String cnt, final String enter)
	{
		if (cnt == null || cnt.length() <= 0)
		{
			sCnt = null;
			setRect();
			return;
		}

		String split = enter == null ? "\n" : enter;
		String[] strs = cnt.split(split), sArr;
		List<String> lt = new ArrayList<String>();
		int i1 = 0, i2;

		paint.setTextSize(PaintUtil.fontS_4);
		while (i1 < strs.length)
		{
			if (null != (sArr = StrUtil.getDesArray(strs[i1], (int) (rectf.width() - marginLR * 2), paint)))
			{
				i2 = 0;
				while (i2 < sArr.length)
				{
					lt.add(sArr[i2]);
					++i2;
				}
			}
			++i1;
		}

		if (lt.size() > 0)
		{
			sCnt = new String[lt.size()];
			i1 = 0;
			while (i1 < lt.size())
			{
				sCnt[i1] = lt.get(i1);
				++i1;
			}

			int h = dividerh * sCnt.length;
			if (h + mixTB * 2 >= mixCnth)
			{
				marginTB = mixTB;
			}
			else
			{
				marginTB = (mixCnth - h) / 2;
			}
			cnth = marginTB * 2 + h;
			// cnth = marginTB*2 + dividerh*sCnt.length;
			setRect();
		}
		else
		{
			sCnt = null;
			setRect();
		}
	}

	protected void setRect() // 有几行文字
	{
		rectf.top = (fh - (tth + btmh + cnth)) / 2;
		rectf.bottom = rectf.top + tth + btmh + cnth;

		rectf.left = (fw - dialogWidth) / 2;
		rectf.right = rectf.left + dialogWidth;

		// 标题
		gdT.setBounds((int) rectf.left, (int) rectf.top, (int) rectf.right, (int) rectf.top + tth);

		// 左按钮
		gdLB.setBounds((int) rectf.left, (int) rectf.bottom - btmh, (int) rectf.centerX(), (int) rectf.bottom);
		// 右按钮
		gdRB.setBounds((int) rectf.centerX(), (int) rectf.bottom - btmh, (int) rectf.right, (int) rectf.bottom);
		// 只有一个按钮
		gdB.setBounds((int) rectf.left, (int) rectf.bottom - btmh, (int) rectf.right, (int) rectf.bottom);
	}

	public void show(Callback callback, final String tag)
	{
//		if (vroot.getWidth() > 0 && vroot.getHeight() > 0)
//		{
//			fw = vroot.getWidth();
//			fh = vroot.getHeight();
//		}

		DisplayMetrics metrics = new DisplayMetrics();
		at.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		fh = metrics.heightPixels;
		fw = metrics.widthPixels;

		orientation = at.getRequestedOrientation();
		if (fw > fh) // 横屏
		{
			at.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		else
		{
			at.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		if (cnth + tth + btmh > fh)
		{
			cnth = fh - tth - btmh;
		}
		setRect();

		setFocus();
		this.callback = callback;
		this.tag = tag;
		if (vroot != null && getParent() == null)
		{
			vroot.addView(this);
		}
	}
	
	public boolean isShowing()
	{
		return getParent() != null;
	}

	public void dismiss()
	{
		try
		{
			if (vroot != null)
				vroot.removeView(this);
			at.setRequestedOrientation(orientation);
		}
		catch (Exception e1)
		{
			Logger.e("DDialog dismiss() " + e1.toString());
		}
	}

	protected void onDraw(Canvas cvs)
	{
		cvs.setDrawFilter(PaintUtil.pfd);
		cvs.drawColor(clr_trs_bg);

		paint.setColor(clr_v_bg);
		cvs.drawRoundRect(rectf, radius, radius, paint);

		// 标题
		cvs.save();
		gdT.draw(cvs);
		cvs.restore();

		paint.setColor(clr_divider);
		// 标题分割线
		cvs.drawLine(rectf.left, rectf.top + tth, rectf.right, rectf.top + tth, paint);
		// 按钮分割线
		cvs.drawLine(rectf.left, rectf.bottom - btmh, rectf.right, rectf.bottom - btmh, paint);
		if (getBtnCount() >= 2)
		{
			cvs.drawLine(rectf.centerX(), rectf.bottom, rectf.centerX(), rectf.bottom - btmh, paint);
		}

		// cvs.drawLine(rectf.left, rectf.centerY(), rectf.right,
		// rectf.centerY(), paint);
		// paint.setColor(clr_left);
		// cvs.drawLine(rectf.left, rectf.top + tth + marginTB, rectf.right,
		// rectf.top + tth + marginTB, paint);
		// cvs.drawLine(rectf.left, rectf.bottom - btmh - marginTB, rectf.right,
		// rectf.bottom - btmh - marginTB, paint);

		// 标题
		if (sTitle != null)
		{
			paint.setColor(clr_title);
			paint.setTextSize(PaintUtil.fontS_4);
			cvs.drawText("" + sTitle, rectf.centerX() - paint.measureText("" + sTitle) / 2, rectf.top + tth / 2 + PaintUtil.fontHH_4, paint);
		}

		// 按钮按下效果
		if (getBtnCount() > 0)
		{
			cvs.save();
			if (btnPress == PRESS_RIGHT)
			{
				if (getBtnCount() == 1)
				{
					gdB.draw(cvs);
				}
				else
				{
					gdRB.draw(cvs);
				}
			}
			else if (btnPress == PRESS_LEFT)
			{
				gdLB.draw(cvs);
			}
			cvs.restore();
		}

		// 按钮
		paint.setTextSize(PaintUtil.fontS_4);
		if (getBtnCount() == 2)
		{
			// paint.setColor(clr_left);
			if (btnPress == PRESS_LEFT)
			{
//				paint.setColor(HZDR.CLR_B1);
				paint.setColor(clr_txt_btn_press);
			}
			else
			{
				paint.setColor(HZDR.CLR_F1);
			}
			cvs.drawText("" + sLeft, rectf.left + rectf.width() / 4 - paint.measureText("" + sLeft) / 2, rectf.bottom - btmh / 2 + PaintUtil.fontHH_4, paint);

			// 粗体
			// paint.setTypeface(Typeface.create(Typeface.SANS_SERIF,
			// Typeface.BOLD));
			// paint.setColor(clr_right);
			if (btnPress == PRESS_RIGHT)
			{
//				paint.setColor(HZDR.CLR_B1);
				paint.setColor(clr_txt_btn_press);
			}
			else
			{
				paint.setColor(HZDR.CLR_B2);
			}
			// paint.setFakeBoldText(true);
			cvs.drawText("" + sRight, rectf.right - rectf.width() / 4 - paint.measureText("" + sRight) / 2, rectf.bottom - btmh / 2 + PaintUtil.fontHH_4, paint);
			// paint.setFakeBoldText(false);
			// paint.setTypeface(Typeface.create(Typeface.SANS_SERIF,
			// Typeface.NORMAL));
		}
		else if (getBtnCount() == 1)
		{
			// paint.setColor(clr_right);
			if (btnPress == PRESS_RIGHT)
			{
//				paint.setColor(HZDR.CLR_B1);
				paint.setColor(clr_txt_btn_press);
			}
			else
			{
				paint.setColor(HZDR.CLR_B2);
			}
			cvs.drawText("" + sRight, rectf.centerX() - paint.measureText("" + sRight) / 2, rectf.bottom - btmh / 2 + PaintUtil.fontHH_4, paint);
		}

		drawCnt(cvs);
	}
	
	protected void drawCnt(Canvas cvs)
	{
		// 内容
		if (sCnt != null)
		{
			paint.setColor(clr_cnt);
			paint.setTextSize(PaintUtil.fontS_4);
			dy = (int) (rectf.top + tth + marginTB + dividerh / 2 + PaintUtil.fontHH_4);

			if (sCnt.length == 1)
			{
				cvs.drawText(sCnt[0], rectf.centerX() - paint.measureText(sCnt[0]) / 2, dy, paint);
			}
			else
			{
				i1 = 0;
				while (i1 < sCnt.length && dy <= rectf.bottom - btmh)
				{
					cvs.drawText(sCnt[i1], rectf.left + marginLR, dy, paint);
					dy += dividerh;
					++i1;
				}
			}
		}
	}

	protected void reDraw()
	{
		postInvalidate();
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:

				tdx = (int) event.getX();
				tdy = (int) event.getY();
				tlx = tdx;
				tly = tdy;
				movedx = 0;
				movedy = 0;
				bMoved = false;

				if (getBtnCount() > 0)
				{
					if (tdy > rectf.bottom - btmh && tdy < rectf.bottom)
					{
						switch (getBtnCount())
						{
							case 1:
								btnPress = PRESS_RIGHT;
								break;
							case 2:
								if (tdx < rectf.centerX())
								{
									btnPress = PRESS_LEFT;
								}
								else
								{
									btnPress = PRESS_RIGHT;
								}
								break;
						}
					}
				}

				reDraw();
				break;
			case MotionEvent.ACTION_MOVE:

				tmx = (int) event.getX();
				tmy = (int) event.getY();

				movedx += Math.abs(tmx - tlx); // 累计X轴移动距离
				movedy += Math.abs(tmy - tly); // 累计Y轴移动距离
				
				if(!bMoved)
				{
					if(movedx > HZDodo.sill || movedy > HZDodo.sill)
					{
						bMoved = true;
						reDraw();
					}
				}

				tlx = tmx;
				tly = tmy;
				break;
			case MotionEvent.ACTION_UP:

				tux = (int) event.getX();
				tuy = (int) event.getY();

				if (!bMoved/*movedx < HZDodo.sill && movedy < HZDodo.sill*/)
				{
					if(null != rectf)
					{
						// 框外
						if (tuy < rectf.top || tuy > rectf.bottom || tux < rectf.left || tux > rectf.right)
						{
							if (null != callback)
								callback.onClick(CLICK_OUTSIDE, tag);
						}
						// 框内
						else
						{
							if (getBtnCount() > 0)
							{
								if (tuy > rectf.bottom - btmh && tuy < rectf.bottom)
								{
									switch (getBtnCount())
									{
										case 1:
											toCallback(CLICK_RIGHT);
											break;
										case 2:
											if (tux < rectf.centerX())
											{
												toCallback(CLICK_LEFT);
											}
											else
											{
												toCallback(CLICK_RIGHT);
											}
											break;
									}
								}
							}
							else
							{
								if (tuy < rectf.top || tuy > rectf.bottom)
								{
									toCallback(CLICK_KEYBACK);
								}
							}
						}
					}
				}

				btnPress = PRESS_NA;
				reDraw();
				break;
		}
		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				toCallback(CLICK_KEYBACK);
				break;
		}
		return true;
	}

	private void toCallback(final int type)
	{
		if (callback != null)
			callback.onClick(type, tag);
		dismiss();
	}
}
