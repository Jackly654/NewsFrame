package hz.dodo.graphic;

import hz.dodo.data.HZDR;
import hz.dodo.Logger;
import hz.dodo.PaintUtil;

import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.view.View;

/**
 * GifView<br>
 * 本类可以显示一个gif动画，其使用方法和android的其它view（如imageview)一样。<br>
 * 如果要显示的gif太大，会出现OOM的问题。
 * 
 * @author liao
 * 
 */
public class GifView extends View implements GifDecoder.Callback, Handler.Callback
{
	public interface Callback
	{
		public void parseOk(boolean parseStatus,int frameIndex);
	}
	
	/** gif解码器 */
	protected GifDecoder gifDecoder = null;
	/** 当前要画的帧的图 */
	protected Bitmap currentImage = null;

	protected int showWidth = -1;
	protected int showHeight = -1;
	protected RectF rectf;

	Handler handler;
	Callback callback;
	Paint paint;

	private GifImageType animationType = GifImageType.SYNC_DECODER;

	/**
	 * 解码过程中，Gif动画显示的方式<br>
	 * 如果图片较大，那么解码过程会比较长，这个解码过程中，gif如何显示
	 * 
	 * @author liao
	 * 
	 */
	public enum GifImageType
	{
		/**
		 * 在解码过程中，不显示图片，直到解码全部成功后，再显示
		 */
		WAIT_FINISH (0),
		/**
		 * 和解码过程同步，解码进行到哪里，图片显示到哪里
		 */
		SYNC_DECODER (1),
		/**
		 * 在解码过程中，只显示第一帧图片
		 */
		COVER (2);

		GifImageType(int i)
		{
			nativeInt = i;
		}

		final int nativeInt;
	}

	protected GifView(Context ctx)
	{
		super(ctx);
	}
	
	public GifView(Activity at, Callback callback)
	{
		super(at);
		this.callback = callback;
		PaintUtil.getInstance(at.getWindowManager());
		paint = PaintUtil.paint;
		rectf = new RectF();
	}

//	public GifView(Context context, AttributeSet attrs)
//	{
//		this(context, attrs, 0);
//	}
//
//	public GifView(Context context, AttributeSet attrs, int defStyle)
//	{
//		super(context, attrs, defStyle);
//
//	}
	
	public void destory()
	{
	}

	/**
	 * 设置图片，开始解码
	 * 
	 * @param is
	 *            要设置的图片
	 */
	private void setGifDecoderImage(InputStream is)
	{
		Logger.i("设置图片");
		
		try
		{
			if(handler == null) handler = new Handler(this);
			else handler.removeMessages(0);
			rectf.setEmpty();
			
			if (gifDecoder != null)
			{
				gifDecoder.free();
				gifDecoder = null;
			}
			gifDecoder = new GifDecoder(is, this);
			gifDecoder.start();
		}
		catch(Exception e1)
		{
			Logger.e("setGifDecoderImage()=" + e1.toString());
		}
	}

	/**
	 * 以字节流形式设置gif图片
	 * 
	 * @param is
	 *            图片
	 */
	public void setGifImage(final InputStream is)
	{
		try
		{
			setGifDecoderImage(is);
		}
		catch(Exception e1)
		{
			Logger.e("setGifImage()=" + e1.toString());
		}
	}

	public int getGifWidth()
	{
		if(gifDecoder != null) return gifDecoder.width;
		return 0;
	}
	public int getGifHeight()
	{
		if(gifDecoder != null) return gifDecoder.height;
		return 0;
	}
	protected void onDraw(Canvas canvas)
	{
		try
		{
			canvas.setDrawFilter(PaintUtil.pfd);
			
			canvas.drawColor(HZDR.CLR_B6);
			
			if (gifDecoder == null) return;
			if (currentImage == null) currentImage = gifDecoder.getImage();
			if (currentImage == null) return;

			paint.setColor(HZDR.CLR_B1);
			canvas.drawBitmap(currentImage, null, rectf, paint);
			
			Logger.i("gif onDraw()");
		}
		catch(Exception e1)
		{
			Logger.e("GifView onDraw()" + e1.toString());
		}
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		try
		{
			int pleft = getPaddingLeft();
			int pright = getPaddingRight();
			int ptop = getPaddingTop();
			int pbottom = getPaddingBottom();

			int widthSize;
			int heightSize;

			int w;
			int h;

			if (gifDecoder == null)
			{
				w = 1;
				h = 1;
			}
			else
			{
				w = gifDecoder.width;
				h = gifDecoder.height;
			}

			w += pleft + pright;
			h += ptop + pbottom;

			w = Math.max(w, getSuggestedMinimumWidth());
			h = Math.max(h, getSuggestedMinimumHeight());

			widthSize = resolveSize(w, widthMeasureSpec);
			heightSize = resolveSize(h, heightMeasureSpec);

			setMeasuredDimension(widthSize, heightSize);
		}
		catch(Exception e1)
		{
			Logger.e("onMeasure()" + e1.toString());
		}
	}

	/**
	 * 设置gif在解码过程中的显示方式<br>
	 * <strong>本方法只能在setGifImage方法之前设置，否则设置无效</strong>
	 * 
	 * @param type
	 *            显示方式
	 */
	public void setGifImageType(GifImageType type)
	{
		if (gifDecoder == null)
			animationType = type;
	}

	/**
	 * 设置要显示的图片的大小<br>
	 * 当设置了图片大小 之后，会按照设置的大小来显示gif（按设置后的大小来进行拉伸或压缩）
	 * 
	 * @param width
	 *            要显示的图片宽
	 * @param height
	 *            要显示的图片高
	 */
	public void setShowDimension(int width, int height)
	{
		if (width > 0 && height > 0)
		{
			showWidth = width;
			showHeight = height;
			rectf = new RectF();
			rectf.left = 0;
			rectf.top = 0;
			rectf.right = width;
			rectf.bottom = height;
		}
	}

	public void parseOk(boolean parseStatus, int frameIndex)
	{
		Logger.i("parseOk(): " + parseStatus);
		if(callback != null) callback.parseOk(parseStatus, frameIndex);
		if (parseStatus)
		{
			if (gifDecoder != null)
			{
				Logger.i("animationType: " + animationType);
				switch (animationType)
				{
					case WAIT_FINISH:
						if (frameIndex == -1)
						{
							if (gifDecoder.getFrameCount() > 1)
							{ // 当帧数大于1时，启动动画线程
								sendMsg();
							}
							else
							{
								postInvalidate();
							}
						}
						break;
					case COVER:
						if (frameIndex == 1)
						{
							currentImage = gifDecoder.getImage();
							postInvalidate();
						}
						else if (frameIndex == -1)
						{
							if (gifDecoder.getFrameCount() > 1)
							{
								sendMsg();
							}
							else
							{
								postInvalidate();
							}
						}
						break;
					case SYNC_DECODER:
						Logger.i("frameIndex: " + frameIndex);
						if (frameIndex == 1)
						{
							if(null != (currentImage = gifDecoder.getImage()))
							{
								if(rectf.isEmpty())
								{
									initRect(currentImage, rectf);
								}
							}
							postInvalidate();
						}
						else if (frameIndex == -1)
						{
							postInvalidate();
						}
						else
						{
							sendMsg();
						}
						break;
				}
			}
		}
		else
		{
			Logger.d("parseOk() 解码失败");
		}
	}
	
	private void initRect(Bitmap bm, RectF rtf)
	{
		if(bm == null) return;
		rtf.left = (getWidth() - bm.getWidth())*0.5f;
		rtf.right = (getWidth() + bm.getWidth())*0.5f;
		
		float imgh = bm.getHeight();
		float imgw = bm.getWidth();
		
		if(bm.getHeight() > getHeight())
		{
			rtf.top = 0;
			rtf.bottom = getHeight();
			
			float tmp = getHeight()*imgw/imgh;
			rtf.left = (getWidth() - tmp)*0.5f;
			rtf.right = (getWidth() + tmp)*0.5f;
		}
		else
		{
			rtf.top = (getHeight() - imgh)*0.5f;
			rtf.bottom = (getHeight() + imgh)*0.5f;
			
			rtf.left = (getWidth() - imgw)*0.5f;
			rtf.right = (getWidth() + imgw)*0.5f;
		}
	}

	public boolean handleMessage(Message msg)
	{
		try
		{
			switch(msg.what)
			{
				case 0:
					if (gifDecoder != null)
					{
						GifFrame frame = gifDecoder.next();
						if(frame != null)
						{
							currentImage = frame.image;
							if(rectf.isEmpty()) initRect(currentImage, rectf);
							postInvalidate();
							handler.postDelayed(run, frame.delay);
						}
					}
					break;
				default:
					postInvalidate();
					break;
			}
		}
		catch(Exception e1)
		{
			Logger.e("handleMessage()=" + e1.toString());
		}
		return true;
	}
	
	Runnable run = new Runnable()
	{
		public void run()
		{
			sendMsg();
		}
	};

	private void sendMsg()
	{
		handler.removeMessages(0);
		handler.sendEmptyMessage(0);
	}
}
