package hz.dodo.controls;

import hz.dodo.HZDodo;
import hz.dodo.Logger;
import hz.dodo.data.DValue;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class VS extends View
{
	// 上下滑动
	protected Activity at;
	DValue DV;

	protected final static int SNAP_VELOCITY = 600;

	protected VelocityTracker vt, tmpvt;

	protected RectF rectf;

	protected int velocityX, velocityY, // 瞬时速度
			sl_w, sl_radius, // 滚动条的宽度,圆角
			movedx, movedy, // 累计移动 / 方向
			tth, // 标题高度
			totalh, // 总高度
			topy, // 画布起点
			vw, vh; // 控件尺寸

	protected int tdx, tdy, tmx, tmy, tux, tuy, tlx, tly, //
			sl_marginB, sl_marginTB, sl_marginR, sl_unith, sl_dy, // 滚动条一份的高度,起点
			sill;

	protected boolean bmoved;

	protected VS(Context ctx)
	{
		super(ctx);
	}

	public VS(Activity at, int vw, int vh)
	{
		super(at);
		setWillNotDraw(false); // call draw
		setFocusable(true);
		setFocusableInTouchMode(true); // 设置焦点
		requestFocus(); // 请求焦点

		this.at = at;
		this.vw = vw;
		this.vh = vh;
		topy = 0;
		totalh = 0;
		tth = 0;

		rectf = new RectF();

		sl_w = vw / 135;
		sl_marginR = 0;
		sl_marginTB = 0;
		sl_radius = sl_w / 2;

		sill = HZDodo.sill;
	}

	protected void onDraw(Canvas cvs)
	{
	}

	public void reDraw()
	{
		postInvalidate();
	}

	protected void drawScroller(Canvas cvs, Paint paint)
	{
		try
		{
			if (totalh <= vh)
				return;

			sl_unith = (int) ( (vh - sl_marginTB * 2 - tth - sl_marginB) * vh * 1.0f / totalh);
			sl_dy = (int) ( (-topy * (vh - tth - sl_marginB) * 1.0f) / totalh);

			rectf.set(vw - sl_w - sl_marginR, tth + sl_dy + sl_marginTB, vw - sl_marginR, tth + sl_dy + sl_unith - sl_marginTB);
			cvs.drawRoundRect(rectf, sl_radius, sl_radius, paint);
		}
		catch (Exception e1)
		{
			Logger.e("VS draw scroller = " + e1.toString());
		}
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		if (vt == null)
		{
			vt = VelocityTracker.obtain();
		}
		vt.addMovement(event);

		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				tdx = (int) event.getX();
				tdy = (int) event.getY();
				tlx = tdx;
				tly = tdy;
				movedx = 0;
				movedy = 0;

				bmoved = cancel();

				break;
			case MotionEvent.ACTION_MOVE:
				tmx = (int) event.getX();
				tmy = (int) event.getY();

				movedx += Math.abs(tmx - tlx); // 累计X轴移动距离
				movedy += Math.abs(tmy - tly); // 累计Y轴移动距离

				if (!bmoved)
				{
					if (movedx > sill || movedy > sill)
					{
						bmoved = true;
					}
				}

				if (movedy > sill)
				{
					if (totalh >= vh)
					{
						topy += (int) (tmy - tly);

						if (topy > 0)
						{
							topy = 0;
						}
						else if (topy < vh - totalh)
						{
							topy = vh - totalh;
						}
						reDraw();
					}
				}

				move(tmx, tlx, tmy, tly);

				tlx = tmx;
				tly = tmy;

				break;
			case MotionEvent.ACTION_UP:
				tux = (int) event.getX();
				tuy = (int) event.getY();

				tmpvt = vt;
				tmpvt.computeCurrentVelocity(1000);
				velocityX = (int) tmpvt.getXVelocity();
				velocityY = (int) tmpvt.getYVelocity();

				// Logger.i("X:" + velocityX + ", Y:" + velocityY);

				vt.recycle();
				vt = null;

				if (totalh <= vh) // 不足一屏
				{
					if (topy > 0)
					{
						topy = 0;
					}
				}
				else
				{
					if (topy > 0)
					{
						topy = 0;
					}
					else if (topy < vh - totalh)
					{
						topy = vh - totalh;
					}
					else
					{
						if (Math.abs(velocityY) > SNAP_VELOCITY && Math.abs(velocityY) > Math.abs(velocityX) && Math.abs(tuy - tdy) > Math.abs(tux - tdx))
						{
							bmoved = true;
							fling(velocityY);
						}
					}
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				break;
		}

		return true;
	}

	protected void move(float tmx, float tlx, float tmy, float tly)
	{
	}

	protected void topyChanged()
	{
	}

	protected void fling(int velocity)
	{
		if (DV == null)
			DV = new DValue(at);

		DV.vFling(new DValue.Callback()
		{
			public void update(float position, float offset)
			{
				topy += offset;
				if (topy > 0)
				{
					topy = 0;
					cancel();
				}
				else if (topy < vh - totalh)
				{
					topy = vh - totalh;
					cancel();
				}
				topyChanged();
				reDraw();
			}

			public void end()
			{
			}
		}, velocity);
	}

	protected boolean cancel()
	{
		if (DV != null)
		{
			return DV.vCancel();
		}
		return false;
	}
}
