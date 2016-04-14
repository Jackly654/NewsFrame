package hz.dodo;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.view.View;

public class DGifView extends View
{
	Context ctx;
	Movie movie;
	String gif;
	
	int duraction;
	long curTime, movieStart;
	
	protected DGifView(Context ctx)
	{
		super(ctx);
	}
	
	public DGifView(Context ctx, int vw, int vh)
	{
		super(ctx);
		this.ctx = ctx;
	}
	
	protected void onDraw(Canvas cvs)
	{
		curTime = android.os.SystemClock.uptimeMillis();
		// 第一次播放
		if (movieStart == 0)
		{
			movieStart = curTime;
		}
		if (movie != null)
		{
			duraction = movie.duration();
			if(duraction > 0)
			{
				movie.setTime((int) ( (curTime - movieStart) % duraction));
				movie.draw(cvs, 0, 0);
			}
			postInvalidate();
		}
	}
	
	public void setImg(InputStream is)
	{
		if(is == null)
		{
			movie = null;
			return;
		}
		try
		{
			movie = Movie.decodeStream(is); 
		}
		catch(Exception e1)
		{
			Logger.e("DGifView::update()" + e1.toString());
		}
	}
//	public void update(final String assetGifName)
//	{
//		if(assetGifName == null) return;
//		
//		if(!assetGifName.equals("" + gif))
//		{
//			try
//			{
//				movie = Movie.decodeStream(ctx.getAssets().open(assetGifName)); 
//			}
//			catch(Exception e1)
//			{
//				Logger.e("DGifView::update()" + e1.toString());
//			}
//		}
//	}
}
