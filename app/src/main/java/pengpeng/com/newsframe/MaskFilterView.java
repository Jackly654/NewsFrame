package pengpeng.com.newsframe;
import android.app.Activity;
import android.content.Context;
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
public class MaskFilterView extends View
{
	private static final int RECT_SIZE = 400;
	private Paint mPaint;
	private Context mContext;

	private int left,top,right,bottom;

	public MaskFilterView(Context context)
	{
		this(context, null);
	}
	public MaskFilterView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext =context;
		initPaint();
		initRes(context);

	}

	private void initPaint()
	{
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(0xFF603811);

		//设置画笔遮罩滤镜
		mPaint.setMaskFilter(new BlurMaskFilter(20,BlurMaskFilter.Blur.SOLID));
	}

	private void initRes(Context context)
	{
         /**
         * 计算位图绘制时左上角的坐标使其位于屏幕中心
         */
		left = MeasureUtil.getScreenSize((Activity) mContext)[0] / 2 - RECT_SIZE / 2;
		top = MeasureUtil.getScreenSize((Activity) mContext)[1] / 2 - RECT_SIZE / 2;
		right = MeasureUtil.getScreenSize((Activity) mContext)[0] / 2 + RECT_SIZE / 2;
		bottom = MeasureUtil.getScreenSize((Activity) mContext)[1] / 2 + RECT_SIZE / 2;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		canvas.drawColor(Color.GRAY);
		canvas.drawRect(left,top,right,bottom,mPaint);
		setLayerType(LAYER_TYPE_SOFTWARE,null);
	}
}
