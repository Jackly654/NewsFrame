package pengpeng.com.newsframe;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
/**
 * Created by Administrator on 2016/4/13.
 */
public class BrickView extends View
{
	private Paint mFillPaint,mStrokePaint;//填充和描边的画笔
	private BitmapShader mBitmapShader;
	private float posX,posY;//触摸点坐标

	public BrickView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initPaint();
	}

	private void initPaint()
	{
		mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		mStrokePaint.setColor(0xFF000000);
		mStrokePaint.setStyle(Paint.Style.STROKE);
		mStrokePaint.setStrokeWidth(5);

		//实例化填充画笔
		mFillPaint = new Paint();
		/**
		 * 生成shader
		 */
		Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.brick);
		mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mFillPaint.setShader(mBitmapShader);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		/*
         * 手指移动时获取触摸点坐标并刷新视图
         */
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			posX = event.getX();
			posY = event.getY();

			invalidate();
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawColor(Color.DKGRAY);
		canvas.drawCircle(posX,posY,300,mFillPaint);
		canvas.drawCircle(posX,posY,300,mStrokePaint);
	}
}
