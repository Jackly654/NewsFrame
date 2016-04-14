package pengpeng.com.newsframe;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
/**
 * Created by Administrator on 2016/4/14.
 */
public class LayerView extends View
{
	private Bitmap mBitmap;// 位图对象
	private Paint mPaint;// 画笔对象

	private int mViewWidth, mViewHeight;// 控件宽高

	public LayerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// 从资源中获取位图对象
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.z);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        /*
         * 获取控件宽高
         */
		mViewWidth = w;
		mViewHeight = h;
		// 缩放位图与控件一致
		mBitmap = Bitmap.createScaledBitmap(mBitmap, mViewWidth, mViewHeight, true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
	/*	 *//*
     * 绘制一个红色矩形
     *//*
		mPaint.setColor(Color.RED);
		canvas.drawRect(mViewWidth / 2F - 200, mViewHeight / 2F - 200, mViewWidth / 2F + 200, mViewHeight / 2F + 200, mPaint);

    *//*
     * 保存画布并绘制一个蓝色的矩形
     *//*
		canvas.saveLayer(0, 0, mViewWidth, mViewHeight, null, Canvas.ALL_SAVE_FLAG);
		mPaint.setColor(Color.BLUE);

		// 旋转画布
		canvas.rotate(30);
		canvas.drawRect(mViewWidth / 2F - 100, mViewHeight / 2F - 100, mViewWidth / 2F + 100, mViewHeight / 2F + 100, mPaint);
		canvas.restore();*/
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(0.8F, 0.35F, mViewWidth, 0);
		canvas.drawBitmap(mBitmap, 0, 0, null);
		canvas.restore();

	}
}
