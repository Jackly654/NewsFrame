package hf.recycler.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.StaggeredGridLayoutManager;

public class RStaggerdAdapter extends RAdapter
{
	StaggeredGridLayoutManager.LayoutParams
		lp;
	
	int
		type;
	
	public RStaggerdAdapter(Context ctx)
	{
		super(ctx);
	}
	
	@Override
	public void onViewAttachedToWindow(ViewHolder holder)
	{
		super.onViewAttachedToWindow(holder);
		handleFooter(holder);
	}
	private void handleFooter(ViewHolder holder)
	{
		if(holder == null) return;
		
		type = getItemViewType(holder.getLayoutPosition());
		if(type == TYPE_FOOTER || type == TYPE_HEAD)
		{
			lp = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
			if(lp != null)
			{
				lp.setFullSpan(true);
			}
		}
	}
}
