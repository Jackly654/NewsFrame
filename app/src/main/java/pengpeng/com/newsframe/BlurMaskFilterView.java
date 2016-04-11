package pengpeng.com.newsframe;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import pengpeng.com.newsframe.utils.MeasureUtil;
/**
 * Created by Administrator on 2016/4/11.
 */
public class BlurMaskFilterView extends View
{
	private Paint shadowPaint;
	private Context mContext;
	private Bitmap srcBitmap,shadowBitmap;
	private int x, y ;

	public BlurMaskFilterView(Context context)
	{
		this(context,null);
	}
	public BlurMaskFilterView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
		// 记得设置模式为SOFTWARE
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		initPaint();
		initRes(context);
	}



	private void initPaint()
	{
		// 实例化画笔
		shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		shadowPaint.setColor(Color.DKGRAY);
		shadowPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));
	}
	private void initRes(Context context)
	{
		// 获取位图
		srcBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.a);
		shadowBitmap = srcBitmap.extractAlpha();

		/*
         * 计算位图绘制时左上角的坐标使其位于屏幕中心
         */
		x = MeasureUtil.getScreenSize((Activity) mContext)[0] / 2 - srcBitmap.getWidth() / 2;
		y = MeasureUtil.getScreenSize((Activity) mContext)[1] / 2 - srcBitmap.getHeight() / 2;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawBitmap(shadowBitmap,x,y,shadowPaint);
		canvas.drawBitmap(srcBitmap,x,y,null);
	}
}
