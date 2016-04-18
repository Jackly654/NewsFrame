package hf.recycler;

import hf.recycler.data.RAdapter;
import hf.recycler.data.RStaggerdAdapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

// 相当于List View
// 1.如果需要上拉加载,则调用openLoadMore()

public class FRecyclerView extends RecyclerView implements Handler.Callback
{
	private final int TYPE_LINRAR = 0; // 单列
	private final int TYPE_STAGGEREDGRID = 1; // 多列
	
	LayoutManager
		lMng;
//	LinearLayoutManager
//		llMng;
//	StaggeredGridLayoutManager
//		sgMng;
	
	RAdapter
		adapter;

	RAdapter.Callback
		callback;

	Handler
		handler;
	
	int
		vw, vh,
		i1,
		iMax,
		iLastVisibleItem,
		style;
	
	int[]
		iSgArr;
	
	boolean
		isLoading;
	
	public FRecyclerView(Context ctx, int vw, int vh)
	{
		super(ctx);
		init(ctx, 1, vw, vh);
	}
	public FRecyclerView(Context ctx, int column, int vw, int vh)
	{
		super(ctx);
		init(ctx, column, vw, vh);
	}
	private void init(Context ctx, int column, int vw, int vh)
	{
		this.vw = vw;
		this.vh = vh;

		isLoading = false;
		setHasFixedSize(true);
		// 设置item动画
		setItemAnimator(new DefaultItemAnimator());
		
		handler = new Handler(Looper.getMainLooper(), this);
		
		if(column <= 1) // 单列
		{
			style = TYPE_LINRAR;
			
			setLayoutManager(lMng = new LinearLayoutManager(ctx));
			setAdapter(adapter = new RAdapter(ctx));
		}
		else // 多列
		{
			style = TYPE_STAGGEREDGRID;
			setLayoutManager(lMng = new StaggeredGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL));
			setAdapter(adapter = new RStaggerdAdapter(ctx));
		}
		setOnScrollListener(new RecyclerView.OnScrollListener()
		{
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState)
			{
				super.onScrollStateChanged(recyclerView, newState);
				/*if(!isLoading && adapter.canLoadMore())
				{
					if (newState == RecyclerView.SCROLL_STATE_IDLE && iLastVisibleItem + 1 == adapter.getItemCount())
					{
						if(callback != null)
						{
							isLoading = true;
							callback.onLoadMore();
						}
					}
				}*/
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy)
			{
				super.onScrolled(recyclerView, dx, dy);
				switch(style)
				{
					case TYPE_LINRAR:
						iLastVisibleItem = ((LinearLayoutManager)lMng).findLastVisibleItemPosition();
						break;
					case TYPE_STAGGEREDGRID:
						if(iSgArr == null)
						{
							iSgArr = new int[((StaggeredGridLayoutManager)lMng).getSpanCount()];
						}
						((StaggeredGridLayoutManager)lMng).findLastVisibleItemPositions(iSgArr);
						iLastVisibleItem = findMax(iSgArr);
						break;
					default:
						iLastVisibleItem = 0;
						break;
				}
				
				if(iLastVisibleItem + 1 == adapter.getItemCount() && adapter.canLoadMore() && !isLoading)
				{
					handler.removeMessages(0);
					handler.sendEmptyMessageDelayed(0, 200);
				}
			}
		});
	}
    private int findMax(int[] iArr)
    {
    	iMax = iArr[0];
        i1 = 0;
        while(i1 < iArr.length)
        {
        	if (iArr[i1] > iMax)
            {
        		iMax = iArr[i1];
            }
        	++i1;
        }
        return iMax;
    }
	public void setOnListener(final RAdapter.Callback callback)
	{
		this.callback = callback;
		adapter.setOnListener(callback);
	}
	public void setData(final List<?> lt)
	{
		adapter.setData(lt);
	}
	public void addHeadView(View view)
	{
		adapter.addHeadView(view);
	}
	public void openLoadMore()
	{
		handler.post(openMore);
	}
	Runnable openMore = new Runnable()
	{
		public void run()
		{
			adapter.openLoadMore(vw, vh);
			if(adapter.canLoadMore() && !isLoading)
			{
				toCallonLoadMore();
			}
		};
	};
	public void loadMoreResult(final boolean success)
	{
		isLoading = false;
	}
	public void closeLoadMore()
	{
		adapter.closeLoadMore();
		loadMoreResult(true);
	}
	public void scrollToTop()
	{
		lMng.scrollToPosition(0);
	}
	public void scrollToBottom()
	{
		lMng.scrollToPosition(adapter.getItemCount() - 1);
	}
	public void reDraw()
	{
		adapter.refresh();
	}
	@Override
	public boolean handleMessage(Message msg)
	{
		post(run);
		return true;
	}
	Runnable run = new Runnable()
	{
		@Override
		public void run()
		{
			if(iLastVisibleItem + 1 == adapter.getItemCount() && adapter.canLoadMore() && !isLoading)
			{
				toCallonLoadMore();
			}
		}
	};
	private void toCallonLoadMore()
	{
		isLoading = true;
		if(callback != null)
		{
			callback.onLoadMore();
		}
		else
		{
			onLoadMore();
		}
	}
	protected void onLoadMore()
	{
	}
}
