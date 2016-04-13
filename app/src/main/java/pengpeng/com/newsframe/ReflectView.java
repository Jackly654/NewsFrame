package pengpeng.com.newsframe;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.LinearGradient;
import android.util.AttributeSet;
import android.view.View;

import pengpeng.com.newsframe.utils.MeasureUtil;
/**
 * Created by Administrator on 2016/4/13.
 */
public class ReflectView extends View
{
	private Bitmap mSrcBitmap ,mRefBitmap;
	private Paint mPaint;
	private PorterDuffXfermode mXfermode;
	private int x,y;

	public ReflectView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// 初始化资源
		initRes(context);
	}

	private void initRes(Context context)
	{
		mSrcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.girl);
		//实例化一个矩阵对象
		Matrix matrx = new Matrix();
		matrx.setScale(1F,-1F);
		//生成倒影图
		mRefBitmap = Bitmap.createBitmap(mSrcBitmap,0,0,mSrcBitmap.getWidth(),mSrcBitmap.getHeight(),matrx,true);

		int screenW = MeasureUtil.getScreenSize((Activity) context)[0];
		int screenH = MeasureUtil.getScreenSize((Activity) context)[1];

		x = screenW / 2 - mSrcBitmap.getWidth() / 2;
		y = screenH / 2 - mSrcBitmap.getHeight() / 2;

		mPaint = new Paint();
		mPaint.setShader(new LinearGradient(x, y + mSrcBitmap.getHeight(),x, y + mSrcBitmap.getHeight()+ mSrcBitmap.getHeight() / 4,0xAA000000, Color.TRANSPARENT, Shader.TileMode.CLAMP));
		mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawColor(Color.BLACK);
		canvas.drawBitmap(mSrcBitmap,x,y,null);
		int sc = canvas.saveLayer(x,y+mSrcBitmap.getHeight(),x+mRefBitmap.getWidth(),y+mSrcBitmap.getHeight()*2,null, Canvas.ALL_SAVE_FLAG);
		canvas.drawBitmap(mRefBitmap,x,y+mSrcBitmap.getHeight(),null);
		mPaint.setXfermode(mXfermode);
		canvas.drawRect(x,y+mSrcBitmap.getHeight(),x+mRefBitmap.getWidth(),y+mSrcBitmap.getHeight()*2,mPaint);
		mPaint.setXfermode(null);
		canvas.restoreToCount(sc);
	}
}
