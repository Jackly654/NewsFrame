package pengpeng.com.newsframe;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import pengpeng.com.newsframe.utils.MeasureUtil;
/**
 * Created by Administrator on 2016/4/13.
 */
public class DreamEffectView extends View
{
	private Paint mBitmapPaint,mShaderPaint;
	private Bitmap mBitmap,darkCornerBitmap;
	private PorterDuffXfermode mXfermode;
	private int x,y;
	private int screenW,screenH;
	public DreamEffectView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		initRes(context);

		initPaint();

	}



	private void initRes(Context context)
	{
		// 获取位图
		mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.girl);

		// 实例化混合模式
		mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SCREEN);

		screenW = MeasureUtil.getScreenSize((Activity) context)[0];
		screenH = MeasureUtil.getScreenSize((Activity) context)[1];

		x = screenW / 2 - mBitmap.getWidth() / 2;
		y = screenH / 2 - mBitmap.getHeight() / 2;
	}
	private void initPaint()
	{
		// 实例化画笔
		mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// 去饱和、提亮、色相矫正
		mBitmapPaint.setColorFilter(new ColorMatrixColorFilter(new float[] { 0.8587F, 0.2940F, -0.0927F, 0, 6.79F, 0.0821F, 0.9145F, 0.0634F, 0, 6.79F, 0.2019F, 0.1097F, 0.7483F, 0, 6.79F, 0, 0, 0, 1, 0 }));
		// 实例化Shader图形的画笔
		mShaderPaint= new Paint();
		// 根据我们源图的大小生成暗角Bitmap
		darkCornerBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(darkCornerBitmap);
		// 计算径向渐变半径
		float radiu = canvas.getHeight() * (2F / 3F);
		RadialGradient radialGradient = new RadialGradient(canvas.getWidth()/2F,canvas.getHeight()/2F,radiu,new int[]{0,0,0xAA000000},new float[]{0F,0.7F,1.0F},Shader.TileMode.CLAMP);
		// 实例化一个矩阵
		Matrix matrix = new Matrix();
		// 设置矩阵的缩放
		matrix.setScale(canvas.getWidth() / (radiu * 2F), 1.0F);
		// 设置矩阵的预平移
		matrix.preTranslate(((radiu * 2F) - canvas.getWidth()) / 2F, 0);

		// 将该矩阵注入径向渐变
		radialGradient.setLocalMatrix(matrix);

		//mShaderPaint.setShader(new RadialGradient(screenW/2,screenH/2,mBitmap.getHeight()*7/8,Color.TRANSPARENT,Color.BLACK, Shader.TileMode.CLAMP));
		mShaderPaint.setShader(radialGradient);
		// 绘制矩形
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mShaderPaint);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawColor(Color.BLACK);
		int sc = canvas.saveLayer(x, y,x+mBitmap.getWidth(), y + mBitmap.getHeight(), null, Canvas.ALL_SAVE_FLAG);
		canvas.drawColor(0xcc1c093e);
		mBitmapPaint.setXfermode(mXfermode);
		canvas.drawBitmap(mBitmap, x, y, mBitmapPaint);
		mBitmapPaint.setXfermode(null);
		canvas.restoreToCount(sc);
		//canvas.drawRect(x,y,x+mBitmap.getWidth(),y+mBitmap.getHeight(),mShaderPaint);
		canvas.drawBitmap(darkCornerBitmap,x,y,null);
	}
}
