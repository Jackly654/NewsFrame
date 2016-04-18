package hf.recycler.data;

import hf.recycler.VLoad;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;

public class RAdapter extends RecyclerView.Adapter<ViewHolder> implements Handler.Callback
{
	public interface Callback
	{
		public void onViewChanged(final View view, final int status);
		public View onCreateItem();
		public void onInitItem(final View view, final int position, final Object obj);
		public void onLoadMore();
	}
	
	public static final int VIEW_RECYCLED = 1; // 已经析构
	public static final int VIEW_ATTACHED = 2; // 已经依附在窗口
	public static final int VIEW_DETACHED = 3; // 已经从窗口分离
	
	protected static final int TYPE_HEAD = 0;
	protected static final int TYPE_ITEM = 1;
	protected static final int TYPE_FOOTER = 2;
	
	private final int MSG_OPEN_LOAD_MORE = 1;
	private final int MSG_CLOSE_LOAD_MORE = 2;
	private final int MSG_REDRAW = 3;

	Context ctx;
	Handler handler;
	List<?> lt;
	
	Callback callback;
	boolean
		hasHeadView,
		canLoadMore;
	
	ViewHolder
		vHeaderHolder;
	ViewHolder
		vFooterHolder;
	
	public RAdapter(Context ctx)
	{
		this.ctx = ctx;
		hasHeadView = false;
		handler = new Handler(Looper.getMainLooper(), this);
		closeLoadMore();
	}
	public void setOnListener(final Callback callback)
	{
		this.callback = callback;
	}
	public void setData(final List<?> lt)
	{
		this.lt = lt;
		refresh();
	}
	public void addHeadView(View view)
	{
		hasHeadView = true;
		vHeaderHolder = new FViewHolder(view);
	}
	public void openLoadMore(int vw, int vh)
	{
//		Message msg = handler.obtainMessage(MSG_OPEN_LOAD_MORE);
//		msg.arg1 = vw;
//		msg.arg2 = vh;
//		msg.sendToTarget();
		canLoadMore = true;
		if(vFooterHolder == null)
		{
			vFooterHolder = new FViewHolder(new VLoad(ctx, vw, vh));
		}
		refresh();
	}
	public void closeLoadMore()
	{
		canLoadMore = false;
		handler.sendEmptyMessage(MSG_CLOSE_LOAD_MORE);
	}
	public boolean canLoadMore()
	{
		return canLoadMore;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		ViewHolder holder = null;
		switch(viewType)
		{
			case TYPE_HEAD:
				holder = vHeaderHolder;
				break;
			case TYPE_ITEM:
				if(callback != null) holder = new FViewHolder(callback.onCreateItem());
				break;
			case TYPE_FOOTER:
				holder = vFooterHolder;
				break;
		}
		return holder;
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position)
	{
		if(lt == null || callback == null) return;

		if(position <= 0)
		{
			if(!hasHeadView)
			{
				if(lt.size() > 0)
				{
					callback.onInitItem(holder.itemView, position, lt.get(0));
				}
			}
		}
		else
		{
			int pos = position;
			if(hasHeadView)
			{
				pos -= 1;
			}
			if(pos >= 0 && pos < lt.size())
			{
				callback.onInitItem(holder.itemView, pos, lt.get(pos));
			}
		}
	}

	@Override
	public int getItemCount()
	{
		int size = lt != null ? lt.size() : 0;
		if(hasHeadView)
		{
			size += 1;
		}
		if(canLoadMore)
		{
			size += 1;
		}
		return size;
	}
	@Override
	public int getItemViewType(int position)
	{
		if(hasHeadView && position == 0)
		{
			return TYPE_HEAD;
		}
		// 最后一个item设置为footerView
		if(canLoadMore && position + 1 == getItemCount())
		{
			return TYPE_FOOTER;
		}
		
		return TYPE_ITEM;
	}
	@Override
	public void onViewRecycled(ViewHolder holder)
	{
		super.onViewRecycled(holder);
		if(callback != null)
		{
			callback.onViewChanged(holder.itemView, VIEW_RECYCLED);
		}
	}
	@Override
	public void onViewAttachedToWindow(ViewHolder holder)
	{
		super.onViewAttachedToWindow(holder);
		if(callback != null)
		{
			callback.onViewChanged(holder.itemView, VIEW_ATTACHED);
		}
	}
	@Override
	public void onViewDetachedFromWindow(ViewHolder holder)
	{
		super.onViewDetachedFromWindow(holder);
		if(callback != null)
		{
			callback.onViewChanged(holder.itemView, VIEW_DETACHED);
		}
	}
	public void refresh()
	{
		handler.removeMessages(MSG_REDRAW);
		handler.sendEmptyMessageDelayed(MSG_REDRAW, 200);
	}
	public void addData(int position)
	{
		notifyItemInserted(position);
	}
	public void removeData(int position)
	{
		notifyItemRemoved(position);
	}

	protected class FViewHolder extends ViewHolder
	{
		public FViewHolder(final View view)
		{
			super(view);
		}
	}
	@Override
	public boolean handleMessage(Message msg)
	{
		switch (msg.what)
		{
			case MSG_OPEN_LOAD_MORE:
				if(vFooterHolder == null)
				{
					vFooterHolder = new FViewHolder(new VLoad(ctx, msg.arg1, msg.arg2));
				}
				refresh();
				break;
			case MSG_CLOSE_LOAD_MORE:
//				if(vFooterHolder != null && vFooterHolder.itemView != null)
//				{
//					((VLoad)(vFooterHolder.itemView)).onDestroy();
//				}
//				vFooterHolder = null;
				refresh();
				break;
			case MSG_REDRAW:
				notifyDataSetChanged();
				break;
		}
		return true;
	}
}