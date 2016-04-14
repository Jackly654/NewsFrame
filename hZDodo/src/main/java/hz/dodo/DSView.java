package hz.dodo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

// 上下滑动
public class DSView extends View // implements Handler.Callback
{
	public final static int SNAP_VELOCITY = 600;

	public Scroller scroller;
	public VelocityTracker vt, tmpvt;

	protected int velocityX, velocityY, // 瞬时速度
			sl_w, sl_radius, sl_marginR, sl_marginTB, sl_marginB, // 滚动条的宽度,圆角
			movedx, movedy; // 累计移动 / 方向

	protected float sl_unith, sl_dy; // 滚动条一份的高度,起点

	protected RectF rectf;

	protected int tth, // 标题高度
			totalh, // 总高度
			topy, // 画布起点
			vw, vh; // 控件尺寸

	protected float tdx, tdy, tmx, tmy, tux, tuy, tlx, tly;

	protected boolean bmoved;

	protected DSView(Context ctx)
	{
		super(ctx);
	}

	public DSView(Context ctx, int vw, int vh)
	{
		super(ctx);
		setWillNotDraw(false); // call draw

		this.vw = vw;
		this.vh = vh;
		topy = 0;
		totalh = 0;
		tth = vh / 13;

		scroller = new Scroller(ctx);
		rectf = new RectF();
		sl_w = vw / 135;
		sl_marginR = vw / 216;
		sl_marginTB = 0;
		sl_radius = sl_w / 2;
	}

	protected void draw_scroller(Canvas cvs, Paint paint)
	{
		try
		{
			if (totalh <= 0 || totalh <= vh)
				return;
			
			sl_unith = (vh - sl_marginTB * 2 - tth - sl_marginB) * vh * 1.0f / totalh;
			sl_dy = (topy * (vh - tth - sl_marginB) * 1.0f) / totalh;

			rectf.set(vw - sl_w - sl_marginR, topy + tth + sl_dy + sl_marginTB, vw - sl_marginR, topy + tth + sl_dy + sl_unith - sl_marginTB);
			cvs.drawRoundRect(rectf, sl_radius, sl_radius, paint);
		}
		catch (Exception e1)
		{
			Logger.e("DSView draw scroller = " + e1.toString());
		}
	}

	protected void touch_event(MotionEvent event)
	{
		if (vt == null)
		{
			vt = VelocityTracker.obtain();
		}
		vt.addMovement(event);

		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				tdx = event.getX();
				tdy = event.getY();
				tlx = tdx;
				tly = tdy;
				movedx = 0;
				movedy = 0;
				bmoved = false;

				// 取消上一次
				// handler.removeMessages(msg_sl_delay_dismiss);

				// 如果屏幕的动画还没结束，你就按下了，我们就结束该动画
				if (scroller != null && !scroller.isFinished())
				{
					bmoved = true;
					scroller.abortAnimation();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				tmx = event.getX();
				tmy = event.getY();

				movedx += Math.abs(tmx - tlx); // 累计X轴移动距离
				movedy += Math.abs(tmy - tly); // 累计Y轴移动距离

				if (movedy > HZDodo.sill)
				{
					if (totalh > vh)
					{
						if (getScrollY() < 0)
							scrollBy(0, (int) ( (tly - tmy) / 10)); // 不要再往下了
						else if (getScrollY() > totalh - vh)
							scrollBy(0, (int) ( (tly - tmy) / 10)); // 不要再往上了
						else
							scrollBy(0, (int) (tly - tmy)); // 正常滑动
					}
				}

				tlx = tmx;
				tly = tmy;
				break;
			case MotionEvent.ACTION_UP:
				tux = event.getX();
				tuy = event.getY();

				tmpvt = vt;
				tmpvt.computeCurrentVelocity(1000);
				velocityX = (int) tmpvt.getXVelocity();
				velocityY = (int) tmpvt.getYVelocity();

				vt.recycle();
				vt = null;
				// tmpvt.recycle(); // 是不是疯了又一次recycle
				// tmpvt = null;

				if (totalh <= vh) // 不足一屏
				{
					scrollTo(0, 0);
				}
				else
				{
					if (getScrollY() < 0)
					{
						scrollTo(0, 0); // 不要再往下了
					}
					else if (getScrollY() > totalh - vh)
					{
						scrollTo(0, totalh - vh); // 不要再往上了
					}
					else
					{
						if (Math.abs(velocityY) > SNAP_VELOCITY && Math.abs(velocityY) > Math.abs(velocityX) && Math.abs(tuy - tdy) > Math.abs(tux - tdx))
						{
							scroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
							postInvalidate();
						}
					}
				}

				if (movedx > HZDodo.sill || movedy > HZDodo.sill)
				{
					bmoved = true;
				}

				// 取消滚动条
				// handler.sendEmptyMessageDelayed(msg_sl_delay_dismiss,
				// sl_delay_timer);

				break;
			case MotionEvent.ACTION_CANCEL:
				break;
		}
	}

	public void computeScroll()
	{
		if (scroller.computeScrollOffset())
		{
			// 产生动画
			if (getScrollY() < 0)
			{
				scrollTo(0, 0); // 不要再往下了
				scroller.abortAnimation();
			}
			else if (getScrollY() > totalh - vh)
			{
				scrollTo(0, totalh - vh); // 不要再往上了
				scroller.abortAnimation();
			}
			else
			{
				scrollTo(0, scroller.getCurrY());
			}
			postInvalidate();
		}
	}

	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		super.onScrollChanged(l, t, oldl, oldt);
		topy = t;
	}

	protected void update(Object obj)
	{
		setFocusable(true);
		setFocusableInTouchMode(true); // 设置焦点
		requestFocus(); // 请求焦点
		scrollTo(0, 0); // 归0
	}

	// public boolean handleMessage(Message msg)
	// {
	// switch(msg.what)
	// {
	// case msg_sl_delay_dismiss:
	// sl_draw = false;
	// postInvalidate();
	// break;
	// }
	// return true;
	// }
}