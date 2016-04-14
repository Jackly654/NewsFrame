package pengpeng.com.newsframe;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
/**
 * Created by Administrator on 2016/4/14.
 */
public class PathView extends View
{
	private Path mPath;// 路径对象
	private Paint mPaint;// 路径画笔对象
	private TextPaint mTextPaint;// 文本画笔对象
	public PathView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		/*
         * 实例化画笔并设置属性
         */
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.CYAN);
		mPaint.setStrokeWidth(5);

		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
		mTextPaint.setColor(Color.DKGRAY);
		mTextPaint.setTextSize(20);

		// 实例化路径
		mPath = new Path();
		RectF oval = new RectF(100,100,300,400);
		mPath.addOval(oval,Path.Direction.CW);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawPath(mPath,mPaint);
		canvas.drawTextOnPath("有人识得其中意，虎退羊走返家邦", mPath, 0, 0, mTextPaint);
	}
}
