package hz.dodo.controls;

import hz.dodo.HZDodo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

// http://androidopentutorials.com/android-vertical-scrollbar-styling/

// 滚动到底部或顶部
// fullScroll(ScrollView.FOCUS_DOWN/ScrollView.FOCUS_UP)

// 滚动一段距离
// smoothScrollBy(offx, offy)

public class DScrollView extends ScrollView implements Handler.Callback
{
	final int MSG_SCROLL_CHANGED = 0;
	final int MSG_RELAYOUT = 1;
	
	protected
	Context
		ctx;
	
	protected
	Handler
		handler;
	
	protected
	VContent
		vContent;
	
	protected
	int
		vw,
		vh,
		tdx, tdy, tmx, tmy, tlx, tly, tux, tuy,
		mx, my, // 累计移动距离
		topy;
	
	boolean bMoved;

	public DScrollView(Context context)
	{
		super(context);
		init(context);
	}

	public DScrollView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public DScrollView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}
	public DScrollView(Context ctx, int vw, int vh)
	{
		super(ctx);
		this.vw = vw;
		this.vh = vh;
		
		init(ctx);
	}
	private void init(Context ctx)
	{
		this.ctx = ctx;
		
		handler = new Handler(this);
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		LinearLayout ll = new LinearLayout(ctx);
		addView(ll, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		vContent = new VContent(ctx, vw, vh);
		ll.addView(vContent, vw, vh);
//		setVerticalScrollBarEnabled(false); // 删除ScrollView拉到尽头（顶部、底部）,然后继续拉出现的阴影效果 

		// setScrollbarFadingEnabled(false); // 滚动条一直显示
		// setScrollBarFadeDuration(5000); // 滚动条从开始隐藏到完全看不见持续5秒钟
		// setScrollBarDefaultDelayBeforeFade(5000); // 滚动条5秒后隐藏
		// android:scrollbarThumbVertical="@drawable/scrollbar_vertical_track" // 背景
		// android:scrollbarTrackVertical="@drawable/scrollbar_vertical_thumb" // 滚动条
	}
	public void reLayout(int height)
	{
		Message msg = handler.obtainMessage();
		msg.what = MSG_RELAYOUT;
		msg.arg1 = height;
		msg.sendToTarget();
	}
	public int getTotalh()
	{
		return vContent != null ? vContent.getTotalh() : 0;
	}
	protected void onScrollChanged(int x, int y, int oldx, int oldy)
	{
		super.onScrollChanged(x, y, oldx, oldy);
		if (vContent != null)
		{
//			listener.onScrollChanged(this, x, y, oldx, oldy);
			onScrollChanged(-y);
			
			handler.removeMessages(MSG_SCROLL_CHANGED);
			handler.sendEmptyMessageDelayed(MSG_SCROLL_CHANGED, 20);
		}
	}
	public boolean handleMessage(Message msg)
	{
		if (vContent != null)
		{
			switch(msg.what)
			{
				case MSG_SCROLL_CHANGED:
					if(getScrollY() <= 0)
					{
						onScrollTop();
					}
					else
					{
						if(getScrollY() + getHeight() >= computeVerticalScrollRange())
						{
							onScrollBottom();
						}
					}
					break;
				case MSG_RELAYOUT:
					ViewGroup.LayoutParams lp = vContent.getLayoutParams();
					if(lp != null)
					{
						lp.height = msg.arg1;
						vContent.setLayoutParams(lp);
					}
					reDraw();
					break;
			}
		}

		return true;
	}
	protected void onDrawExt(Canvas cvs)
	{
		// 子类实现
	}
	protected void reDraw()
	{
		if(vContent != null)
		{
			vContent.reDraw();
		}
	}
	private void onTouchEventExt(MotionEvent ev)
	{
		switch (ev.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				tdx = (int) ev.getX();
				tdy = (int) ev.getY();
				tlx = tdx;
				tly = tdy;
				mx = 0;
				my = 0;
				bMoved = false;
				onTouchDown(tdx, tdy);
				break;
			case MotionEvent.ACTION_MOVE:
				tmx = (int) ev.getX();
				tmy = (int) ev.getY();

				mx += Math.abs(tmx - tlx); // 累计X轴移动距离
				my += Math.abs(tmy - tly); // 累计Y轴移动距离

				if (!bMoved)
				{
					if (mx > HZDodo.sill || my > HZDodo.sill)
					{
						bMoved = true;
						onTouchMoved();
					}
				}
				tlx = tmx;
				tly = tmy;

				break;
			case MotionEvent.ACTION_UP:
				tux = (int) ev.getX();
				tuy = (int) ev.getY();
				onTouchUp(tux, tuy, bMoved);
				break;
			case MotionEvent.ACTION_CANCEL:
				onTouchCancel();
				break;
		}
		// 子类实现
	}
	protected void onTouchDown(int tx, int ty)
	{
	}
	protected void onTouchMoved()
	{
	}
	protected void onTouchUp(int tx, int ty, boolean bMoved)
	{
	}
	protected void onTouchCancel()
	{
	}
	public void onScrollChanged(int y)
	{
		topy = y;
		reDraw();
	}
	public void onScrollTop()
	{
		// 子类实现
	}
	public void onScrollBottom()
	{
		// 子类实现
	}
	// -----------------------------
	private class VContent extends View
	{
		protected VContent(Context context)
		{
			super(context);
		}
		public VContent(Context ctx, int vw, int vh)
		{
			super(ctx);
		}
		public int getTotalh()
		{
			ViewGroup.LayoutParams lp = getLayoutParams();
			if(lp != null)
			{
				return lp.height;
			}
			return 0;
		}
		protected void onDraw(Canvas canvas)
		{
			onDrawExt(canvas);
		}
		@SuppressLint ("ClickableViewAccessibility")
		public boolean onTouchEvent(MotionEvent event)
		{
			onTouchEventExt(event);
			return true;
		}
		private void reDraw()
		{
			postInvalidate();
		}
		protected void onLayout(boolean changed, int left, int top, int right, int bottom)
		{
		}
	}
}
