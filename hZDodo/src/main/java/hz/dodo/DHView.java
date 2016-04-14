package hz.dodo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Scroller;

// 左右滑动
public class DHView extends View implements OnGestureListener
{
	private final static int SNAP_VELOCITY = 600;
	
	Scroller scroller;
	VelocityTracker vt, tmpvt;
	Paint paint;
	
	public int s_i, s_c, s_t;
	int velocityX, velocityY; // velocityX 移动速率
	public int tdx, tdy, tmx, tmy, tux, tuy, tlx, tly, fw, fh, dx;
	public int movedx, movedy; // 累计移动 / 移动的阀值
	
	public boolean moveing, moved;
	
	GestureDetector detector;
	
	String str_tmp = "";

	protected DHView(Context ctx)
	{
		super(ctx);
	}
	
	public DHView(Context ctx, int w, int h)
	{
		super(ctx);
		
		setWillNotDraw(false); // call ondraw
		
		detector = new GestureDetector(getContext(), this);
		scroller = new Scroller(ctx);
		
		fw = w;
		fh = h;
		
		paint = PaintUtil.paint;
		paint.setTextSize(PaintUtil.fontS_2);
		
		s_i = 0;
		s_c = 0;
		s_t = -1;
		
		moveing = false;
		moved = false;
	}

	protected void onDraw(Canvas canvas)
	{
		if(s_i >= 0 && s_i < s_c)
		{
			drawView(canvas, s_i, s_i*fw);
		}
		if(s_t >= 0 && s_t < s_c)
		{
			drawView(canvas, s_t, s_t*fw);
		}
		drawOver(canvas, dx);
	}
	
	// 画某一屏的数据
	protected void drawView(Canvas cvs, int s_d, int dx)
	{
		switch(s_d)
		{
			case 0:
				paint.setColor(Color.WHITE);
				break;
			case 1:
				paint.setColor(Color.BLUE);
				break;
			case 2:
				paint.setColor(Color.CYAN);
				break;
			case 3:
				paint.setColor(Color.GREEN);
				break;
			case 4:
				paint.setColor(Color.GRAY);
				break;
			default:
				paint.setColor(Color.RED);
				break;
		}
		
		cvs.drawRect(dx, 0, dx + fw, fh, paint);
		
		paint.setColor(Color.BLACK);
		cvs.drawText("这是第" + s_d + "屏", dx + fw / 2 - paint.measureText("这是第" + s_d + "屏")/2, fh/2, paint);
	}
		
	// view的上一层
	protected void drawOver(Canvas cvs, int dx)
	{
		paint.setColor(Color.BLACK);
		cvs.drawText("把我画到水平居中", dx + fw/2 - paint.measureText("把我画到水平居中")/2, fh - 10, paint);
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		detector.onTouchEvent(event);
		if (vt == null)
		{
			vt = VelocityTracker.obtain();
		}
		vt.addMovement(event);
		
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				// 如果屏幕的动画还没结束，你就按下了，我们就结束该动画
				if(scroller != null && !scroller.isFinished())
				{
					scroller.abortAnimation();
				}
				moveing = false;
				moved = false;
				tdx = (int) event.getX();
				tdy = (int) event.getY();
				tlx = tdx;
				tly = tdy;
				movedx = 0;
				movedy = 0;
				
				break;
			case MotionEvent.ACTION_MOVE:

				tmx = (int) event.getX();
				tmy = (int) event.getY();
				
				movedx += Math.abs(tmx - tlx);
				movedy += Math.abs(tmy - tly);
				
				if(movedx < HZDodo.sill)
				{
					tlx = tmx;
					tly = tmy;
					return true;
				}

				dx = getScrollX();
				int detaX = (int) (tlx - tmx);

				if (dx <= 0)
				{
					scrollBy(detaX / 4, 0);
				}
				else if (dx >= ((s_c - 1) * fw))
				{
					scrollBy(detaX / 4, 0);
				}
				else
				{
					scrollBy(detaX, 0);
				}
				
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
				
				if (vt != null)
				{
					vt.recycle();
					vt = null;
				}

				dx = getScrollX();
				if (dx <= 0)
				{
					scrollTo(0, 0);
				}
				else if (dx >= ((s_c - 1) * fw))
				{
					scrollTo((s_c - 1) * fw, 0);
				}
				else
				{
					//滑动速率达到了一个标准(快速向右滑屏，返回上一个屏幕) 马上进行切屏处理
					if (velocityX > SNAP_VELOCITY && s_i > 0)
					{
						snapToScreen(s_i - 1); // prev view
					}
					//快速向左滑屏，返回下一个屏幕)
					else if(velocityX < -SNAP_VELOCITY && s_i < (s_c-1))
					{
						snapToScreen(s_i + 1); // next view
					}
					else //我们是缓慢移动的，因此先判断是保留在本屏幕还是到下一屏幕
					{
						snapToDestination();
					}
				}
				
				if(movedx > HZDodo.sill)
				{
					moved = true;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				break;
		}

		return true;
	}

	public void snapToScreen(int which)
	{
		toScreen(which, -1);
	}
	
	public void snapToScreen(int which, int duration)
	{
		toScreen(which, duration);
	}
	
	private void toScreen(int which, int duration)
	{
		if(s_c == 1 || which < 0 || which > s_c - 1) return;
			
		moveing = true;
	    
	    if(which > s_c - 1)
	    	which = s_c - 1 ;
	    
	    int dx = which*fw - getScrollX();
	    if(duration < 0) duration = Math.abs(dx)/2;
	    scroller.startScroll(getScrollX(), 0, dx, 0, duration);
	    
	    //此时需要手动刷新View 否则没效果
	    postInvalidate();
	}
	
	private void snapToDestination()
	{
		//判断是否超过下一屏的中间位置，如果达到就抵达下一屏，否则保持在原屏幕	
		//直接使用这个公式判断是哪一个屏幕 前后或者自己
		//判断是否超过下一屏的中间位置，如果达到就抵达下一屏，否则保持在原屏幕
		// 这样的一个简单公式意思是：假设当前滑屏偏移值即 scrollCurX 加上每个屏幕一半的宽度，除以每个屏幕的宽度就是
		//  我们目标屏所在位置了。 假如每个屏幕宽度为320dip, 我们滑到了500dip处，很显然我们应该到达第二屏	
//		int destScreen = (getScrollX() + getWidth() / 2 ) / getWidth();
		
		int destScreen = (getScrollX() + fw / 2 ) / fw;
		System.out.println("目标屏:" + destScreen);
		snapToScreen(destScreen);
	}
	public void computeScroll()
	{
		dx = getScrollX();
		
		if (scroller.computeScrollOffset())
		{
			// 产生了动画效果 每次滚动一点
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			postInvalidate();
		}
		else
		{
			if(moveing)
			{
				// 移动完成
				moveing = false;
				s_t = -1;
				s_i = Math.abs(dx/fw);
				onComplete(s_i);
				System.out.println("当前第" + s_i + "屏, 起点坐标:" + dx);
			}
		}
	}
	
	protected void onComplete(int s_i)
	{
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		s_t = (l - s_i*fw) > 0 ? (s_i + 1) : (s_i - 1);
		dx = getScrollX();
		super.onScrollChanged(l, t, oldl, oldt);
	}
	
	@Override
	public boolean onDown(MotionEvent e)
	{
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		if(!scroller.isFinished())
		{
			scroller.abortAnimation();
			return false;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		if(!scroller.isFinished())
		{
			scroller.abortAnimation();
			return false;
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e)
	{
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		return false;
	}
	
	public void update(Object obj1, Object obj2)
	{
		setFocusable(true);
		setFocusableInTouchMode(true); // 设置焦点
		requestFocus(); // 请求焦点
		
		if(obj1 != null && obj1 instanceof Integer)
		{
			s_c = ((Integer)obj1).intValue();
			if(obj2 != null && obj2 instanceof Integer)
			{
				s_i = ((Integer)obj2).intValue();
			}
		}
		
		postInvalidate();
	}
}
