package hf.recycler;

import hf.recycler.data.DRCLR;
import hf.recycler.data.RAdapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

// 子类相当于List View
// 1.默认有下拉刷新功能
// 2.如果需要上拉加载,则调用openLoadMore()

public class FRefreshView extends SwipeRefreshLayout implements RAdapter.Callback, Handler.Callback
{
	public interface Callback
	{
		public void onViewChanged(final View view, final int status);
		public View onCreateItem();
		public void onInitItem(final View view, final int position, final Object obj);
		public void onLoadMore();
		public void onRefresh();
	}
	
	Callback
		callback;
	Handler
		handler;
	
	FRecyclerView
		rv;

	int
		vw, vh;

	public FRefreshView(Context ctx, int vw, int vh)
	{
		super(ctx);
		this.vw = vw;
		this.vh = vh;
		
		handler = new Handler(Looper.getMainLooper(), this);

		setColorSchemeColors(DRCLR.CLR_HOLO_BLUE_LIGHT, DRCLR.CLR_HOLO_RED_LIGHT, DRCLR.CLR_HOLO_GREEN_LIGHT, DRCLR.CLR_HOLO_ORANGE_LIGHT);
		setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			public void onRefresh()
			{
				if(callback != null)
				{
					callback.onRefresh();
				}
				else
				{
					FRefreshView.this.onRefresh();
				}
			}
		});
		
		rv = new FRecyclerView(ctx, vw, vh);
		rv.setOnListener(this);
		addView(rv);
	}
	public void setOnListener(final Callback callback)
	{
		this.callback = callback;
	}
	public void setData(final List<?> lt)
	{
		rv.setData(lt);
	}
	public void addHeadView(View view)
	{
		rv.addHeadView(view);
	}
	public void refreshResult(final boolean success)
	{
		handler.sendEmptyMessage(0);
	}
	public void openLoadMore()
	{
		rv.openLoadMore();
	}
	public void closeLoadMore()
	{
		rv.closeLoadMore();
	}
	public void loadMoreResult(final boolean success)
	{
		rv.loadMoreResult(success);
		reDraw();
	}
	public void reDraw()
	{
		rv.reDraw();
	}
	public void scrollToTop()
	{
		rv.scrollToTop();
	}
	public void scrollToBottom()
	{
		rv.scrollToBottom();
	}
	
	// 子类需要实现的方法
	public void onViewChanged(final View view, final int status)
	{
		if(callback != null)
		{
			callback.onViewChanged(view, status);
		}
	}
	public View onCreateItem()
	{
		if(callback != null) return callback.onCreateItem();
		return null;
	}
	public void onInitItem(final View view, final int position, final Object obj)
	{
		if(callback != null)
		{
			callback.onInitItem(view, position, obj);
		}
	}
	public void onLoadMore()
	{
		if(callback != null)
		{
			callback.onLoadMore();
		}
	}
	public void onRefresh()
	{
		if(callback != null)
		{
			callback.onRefresh();
		}
	}
	@Override
	public boolean handleMessage(Message msg)
	{
		setRefreshing(false);
		reDraw();
		return true;
	}
}
