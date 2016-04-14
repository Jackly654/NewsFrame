package hz.dodo.controls;

import hz.dodo.Logger;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class DViewPager extends ViewPager
{
	public interface Callback
	{
		public void onInitView(View v, int pos);
	}

	public static final int TRANSFORMER_NA = 0; // 默认
	public static final int TRANSFORMER_ALPHA = 1; // 渐变
	public static final int TRANSFORMER_DEPTH = 2; // 右侧页缩放
	public static final int TRANSFORMER_ROTATE = 3; // 旋转
	public static final int TRANSFORMER_STACKED = 4; // 层叠

	DAdapter adapter;
	Callback callback;
	
	int
		vw, vh;

	protected DViewPager(Context ctx)
	{
		super(ctx);
	}

	public DViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public DViewPager(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs);
	}

	public DViewPager(final Context ctx, Callback callback, List<View> ltvs, int vw, int vh)
	{
		super(ctx);

		// setChildrenDrawingOrderEnabled(true);
		this.callback = callback;
		setAdapter(adapter = new DAdapter(ltvs));
		this.vw = vw;
		this.vh = vh;

		// 如果想自定义滚动持续时间的话,使用以下代码
		// try
		// {
		// Field : import java.lang.reflect.Field;
		// Field mField = ViewPager.class.getDeclaredField("mScroller");
		// mField.setAccessible(true);
		// FixedSpeedScroller scl = new
		// FixedSpeedScroller(viewPager.getContext(), new
		// AccelerateDecelerateInterpolator());
		// mField.set(viewPager, scl);
		// }
		// catch (Exception e1)
		// {
		// Logger.e("getDeclaredField() " + e1.toString());
		// }
	}

	// 设置切换效果
	public void setTransformer(final int transformer)
	{
		switch (transformer)
		{
			case TRANSFORMER_NA:
				setPageTransformer(true, null);
				break;
			case TRANSFORMER_ALPHA:
				setPageTransformer(true, new TransformerAlpha());
				break;
			case TRANSFORMER_DEPTH:
				setPageTransformer(true, new TransformerDepth());
				break;
			case TRANSFORMER_ROTATE:
				setPageTransformer(true, new TransformerRotate());
				break;
			case TRANSFORMER_STACKED:
				setPageTransformer(true, new TransformerStacked());
				break;
		}
	}
	
	// 获取当前显示的view
	public View getPrimaryItem()
	{
		return adapter.getPrimaryItem();
	}

	public void moveTo(int pos, boolean smoothScroll)
	{
		setCurrentItem(pos, smoothScroll);
	}

	public void addV(View v)
	{
		adapter.addV(v);
	}

	public void remV(View v)
	{
		adapter.remV(v);
	}
	public List<View> getChildList()
	{
		if(adapter != null)
		{
			return adapter.ltv;
		}
		return null;
	}
	public void destroy()
	{
		destroyDrawingCache();
		removeAllViews();
	}

	/**
	 * DAdapter
	 */
	public class DAdapter extends PagerAdapter
	{
		private List<View> ltv;
		View mCurDisplayView;

		public DAdapter(List<View> ltv)
		{
			this.ltv = ltv;
		}

		public int getCount()
		{
			return ltv.size();
		}

		public void addV(View v)
		{
			ltv.add(v);
			notifyDataSetChanged();
		}

		public void remV(View v)
		{
			ltv.remove(v);
			notifyDataSetChanged();
		}

		public void destroyItem(ViewGroup container, int position, Object object)
		{
			container.removeView(ltv.get(position));
		}

		public Object instantiateItem(ViewGroup container, int position)
		{
			View v = ltv.get(position);
			if (v.getParent() == null)
			{
				container.addView(v);
//				v.layout(position*vw, 0, (position + 1)*vw, vh);
			}
			if (callback != null)
			{
				callback.onInitView(v, position);
			}
			return v;

			// container.addView(ltv.get(position));
			// return ltv.get(position);
		}

		public boolean isViewFromObject(View arg0, Object arg1)
		{
			return arg0 == arg1;
		}

		@SuppressWarnings ("deprecation")
		public void finishUpdate(View container)
		{
			super.finishUpdate(container);
		}

		public int getItemPosition(Object object)
		{
			return POSITION_NONE;
		}

		public void setPrimaryItem(ViewGroup container, int position, Object object)
		{
			mCurDisplayView = (View) object;
		}

		// 获取当前显示的view
		public View getPrimaryItem()
		{
			return mCurDisplayView;
		}
	}

	/**
	 * 调整滑动持续时间的话会用到 FixedSpeedScroller
	 */
	public class FixedSpeedScroller extends Scroller
	{
		private int mDuration = 1500;

		public FixedSpeedScroller(Context context)
		{
			super(context);
		}

		public FixedSpeedScroller(Context context, Interpolator interpolator)
		{
			super(context, interpolator);
		}

		public void startScroll(int startX, int startY, int dx, int dy, int duration)
		{
			super.startScroll(startX, startY, dx, dy, mDuration);
			Logger.i("startX:" + startX + ", dx:" + dx);
		}

		public void startScroll(int startX, int startY, int dx, int dy)
		{
			super.startScroll(startX, startY, dx, dy, mDuration);
			Logger.i("startX:" + startX + ", dx:" + dx);
		}

		public void setmDuration(int time)
		{
			mDuration = time;
		}

		public int getmDuration()
		{
			return mDuration;
		}
	}
}
