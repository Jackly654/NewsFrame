package hz.dodo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Scroller;

// 可以左右滑动,也可以上下滑动
public class DGView extends View implements OnGestureListener
{
	private final static int SNAP_VELOCITY = 500;
	
	final int dir_na = -1;
	final int dir_downup = 0;
	final int dir_leftright = 1;
	
	Scroller scl_0;
	VelocityTracker vt, tmpvt;
	Paint paint;
	
	int s_i, s_c, s_t, velocityX, velocityY; // velocityX 移动速率
	int tdx, tdy, tmx, tmy, tux, tuy, tlx, tly, fw, fh, dx;
	int movedx, movedy; // 累计移动 / 移动的阀值
	int dir; // 方向
	int topy, totalh, titleh;
	int t_unitw;
	int i1;
	
	int[] dyArray, totalArray;
	
	boolean moveing, moved, drawing;
	
	GestureDetector detector;
	
	String str_tmp = "";
	
	Canvas cvs;
	Bitmap bm;

	protected DGView(Context ctx)
	{
		super(ctx);
	}
	
	public DGView(Context ctx, int w, int h)
	{
		super(ctx);
		
		setWillNotDraw(false); // call ondraw
		
		detector = new GestureDetector(getContext(), this);
		scl_0 = new Scroller(ctx);
		
		fw = w;
		fh = h;
		titleh = (int) (fh*0.08f);
		
		paint = PaintUtil.paint;
		paint.setTextSize(PaintUtil.fontS_2);
		
		s_i = 0;
		s_c = 0;
		s_t = -1;
		
		moveing = false;
		moved = false;
		drawing = false;
		
		dir = dir_na;
		
		cvs = new Canvas();
		bm = Bitmap.createBitmap(fw, fh - titleh, Config.RGB_565);
		cvs.setBitmap(bm);
	}

	protected void onDraw(Canvas canvas)
	{
		canvas.drawBitmap(bm, dx, topy + titleh, paint);
		if(dx < 0)
		{
			drawTitle(canvas, dx, topy);
		}
		else if(dx > (s_c-1)*fw)
		{
			drawTitle(canvas, dx + fw - t_unitw, topy);
		}
		else
		{
			drawTitle(canvas, dx + (int)((dx*1.0f)/s_c), topy);
		}
	}
	
	private void reDraw()
	{
		drawing = true;
		
		if(dx < 0)
		{
			if(s_i >= 0 && s_i < s_c)
			{
				drawView(cvs, s_i, 0, - dyArray[s_i]);
			}
			if(s_t >= 0 && s_t < s_c)
			{
				drawView(cvs, s_t, s_t*fw-dx, - dyArray[s_t]);
			}
		}
		else if(dx > (s_c-1)*fw)
		{
			if(s_i >= 0 && s_i < s_c)
			{
				drawView(cvs, s_i, 0, - dyArray[s_i]);
			}
			if(s_t >= 0 && s_t < s_c)
			{
				drawView(cvs, s_t, s_t*fw-dx, - dyArray[s_t]);
			}
		}
		else
		{
			if(s_i >= 0 && s_i < s_c)
			{
//				drawView(cvs, s_i, s_i*fw-dx, titleh - dyArray[s_i]);
				drawView(cvs, s_i, s_i*fw-dx, - dyArray[s_i]);
			}
			if(s_t >= 0 && s_t < s_c)
			{
//				drawView(cvs, s_t, s_t*fw-dx, titleh - dyArray[s_t]);
				drawView(cvs, s_t, s_t*fw-dx, - dyArray[s_t]);
			}
		}
		
//		drawOver(cvs);
		drawing = false;
		
		postInvalidate();
	}
	
	// 画某一屏的数据
	protected void drawView(Canvas cvs, int s_d, int dx, int dy)
	{
	}

	protected void drawTitle(Canvas cvs, int scrollerdx, int titledy)
	{
		paint.setColor(Color.RED);
		cvs.drawRect(dx, titledy, dx + fw, titledy + titleh, paint);
	}
	
	// view的上一层
	protected void drawOver(Canvas cvs)
	{
		paint.setColor(Color.MAGENTA);
		cvs.drawRect(0, 0, fw, titleh, paint);
		
		paint.setTextSize(PaintUtil.fontS_1);
		paint.setColor(Color.WHITE);
		cvs.drawText("index=" + s_i, fw/2 - paint.measureText("index=" + s_i)/2, fh*0.04f + PaintUtil.fontHH_1, paint);
	}

//	public boolean onTouchEvent(MotionEvent event)
	public boolean touchEvent(MotionEvent event)
	{
		if(moveing) return true; // 左右滑动过程中,禁止再touch
		
		if (vt == null)
		{
			vt = VelocityTracker.obtain();
		}
		vt.addMovement(event);
		
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				// 如果屏幕的动画还没结束，你就按下了，我们就结束该动画
				if(scl_0 != null && !scl_0.isFinished())
				{
					scl_0.abortAnimation();
				}
				
				moveing = false;
				moved = false;
				tdx = (int) event.getX();
				tdy = (int) event.getY();
				tlx = tdx;
				tly = tdy;
				movedx = 0;
				movedy = 0;
				
				dir = dir_na; // 初始化方向
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
				
				if(dir == dir_na)
				{
					if(Math.abs(tlx-tdx) > Math.abs(tly-tdy))
					{
						dir = dir_leftright;
					}
					else if(Math.abs(tlx-tdx) < Math.abs(tly-tdy))
					{
						dir = dir_downup;
					}
				}
				
				if(dir == dir_leftright)
				{
					dx = getScrollX();
					int detaX = (int) (tlx - tmx);

					if (dx < 0)
					{
						scrollBy((int)(detaX * 0.25f), 0);
					}
					else if (dx > ((s_c - 1) * fw))
					{
						scrollBy((int)(detaX * 0.25f), 0);
					}
					else
					{
						scrollBy(detaX, 0);
					}
				}
				else if(dir == dir_downup)
				{
					if (getScrollY() < 0)
					{
						scrollBy(0, (int)((tly - tmy)/10)); // 不要再往下了
					}
					else if(getScrollY() > totalh - fh)
					{
						scrollBy(0, (int)((tly - tmy)/10)); // 不要再往上了
					}
					else
					{
						scrollBy(0, (int)(tly - tmy)); // 正常滑动
					}
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

				if(dir == dir_leftright)
				{
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
							snapToScreen(s_i - 1, true); // prev view
						}
						//快速向左滑屏，返回下一个屏幕)
						else if(velocityX < -SNAP_VELOCITY && s_i < (s_c-1))
						{
							snapToScreen(s_i + 1, true); // next view
						}
						else //我们是缓慢移动的，因此先判断是保留在本屏幕还是到下一屏幕
						{
							snapToDestination();
						}
					}
				}
				else if(dir == dir_downup)
				{
//					log("up滚动条的x=" + scl_0.getCurrX());
					if(totalh <= fh) // 不足一屏
					{
						log("up 不足一屏");
						scrollTo(scl_0.getCurrX(), 0);
					}
					else
					{
						if (getScrollY() < 0)
						{
							log("up 不要再往下了");
							scrollTo(dx, 0); // 不要再往下了
						}
						else if(getScrollY() > totalh - fh)
						{
							log("up 不要再往上了");
							scrollTo(dx, totalh - fh); // 不要再往上了
						}
						else
						{
							if(Math.abs(velocityY) > SNAP_VELOCITY && Math.abs(velocityY) > Math.abs(velocityX) && Math.abs(tuy - tdy) > Math.abs(tux - tdx))
							{
								log("up 产生加速度");
//								scroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
								scl_0.fling(dx, getScrollY(), 0, -velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
//								reDraw();
							}
						}
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
	
	public void snapToScreen(int which, boolean duration)
	{
//		System.out.println("移动到:" + which);
		
		moveing = true;
	    
	    if(which > s_c - 1)
	    	which = s_c - 1 ;
	    
	    int dx = which*fw - getScrollX();
	    
	    if(duration)
	    {
	    	scl_0.startScroll(getScrollX(), 0, dx, 0, Math.abs(dx)/2);
	    }
	    else
	    {
	    	scl_0.startScroll(getScrollX(), 0, dx, 0, 10);
	    }
	    
	    //此时需要手动刷新View 否则没效果
//	    postInvalidate();
	    reDraw();
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
		snapToScreen(destScreen, true);
	}
	public void computeScroll()
	{
		if(dir == dir_leftright)
		{
			dx = getScrollX();
			
			if (scl_0.computeScrollOffset())
			{
				// 产生了动画效果 每次滚动一点
				scrollTo(scl_0.getCurrX(), scl_0.getCurrY());
				reDraw();
			}
			else
			{
				if(moveing)
				{
					s_t = -1;
					s_i = Math.abs(dx/fw);
//					System.out.println("当前第" + s_i + "屏, 起点坐标:" + dx);
					
					// 每次左右滑动以后,恢复画布的起点
					scrollTo(scl_0.getCurrX(), dyArray[s_i]);
					totalh = totalArray[s_i] + titleh;
					
//					log("更新总高度:" + totalh);
					reDraw();
					
					// 移动完成
					moveing = false;
					
					updatePage(s_i);
				}
			}
		}
		else if(dir == dir_downup)
		{
			if(scl_0.computeScrollOffset())
			{
				log("computeScroll() 继续滑动scroll x=" + scl_0.getCurrX() + "dx=" + dx);
				// 产生动画
				if (getScrollY() < 0)
				{
					scrollTo(dx, 0); // 不要再往下了
					scl_0.abortAnimation();
					reDraw();
				}
				else if(getScrollY() > totalh - fh)
				{
					log("停止向上移动");
					scrollTo(dx, totalh - fh); // 不要再往上了
					scl_0.abortAnimation();
					reDraw();
				}
				else
				{
//					scrollTo(0, scroller.getCurrY());
					scrollTo(dx, scl_0.getCurrY());
					reDraw();
				}
			}
		}
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		topy = getScrollY();
		
		if(dir == dir_leftright)
		{
			s_t = (l - s_i*fw) > 0 ? (s_i + 1) : (s_i - 1);
			dx = getScrollX();
		}
		else if(dir == dir_downup)
		{
			dyArray[s_i] = t;
		}
		reDraw();
	}
	
	@Override
	public boolean onDown(MotionEvent e)
	{
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		log("onFling()");
//		if(!scl_0.isFinished())
//		{
//			scl_0.abortAnimation();
////			return false;
//		}
//		return false;
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		log("onScroll()");
//		if(!scl_0.isFinished())
//		{
//			scl_0.abortAnimation();
////			return false;
//		}
//		return false;
		return true;
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
	
	// 滑动翻页完成的回调
	protected void updatePage(int index)
	{
		log("完成翻页,当前:" + index);
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
			t_unitw = (int) ((fw*1.0)/s_c);
		}
		
		reDraw();
	}
	
	public void log(String msg)
	{
		System.out.println("" + msg);
	}
}