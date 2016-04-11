package pengpeng.com.newsframe;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
/**
 * Created by Administrator on 2016/4/11.
 */
public class FontView extends View
{
	private static final String TEXT = "ap彭彭ξτβбпшㄎㄊěǔぬも┰┠№＠↓";
	private Paint textPaint, linePaint;// 文本的画笔和中心线的画笔
	private Paint.FontMetrics mFontMetrics;//文本测量对象
	private int baseX, baseY;// Baseline绘制的XY坐标

	public FontView(Context context)
	{
		this(context,null);
	}
	public FontView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initPaint();//初始化画笔
	}

	private void initPaint()
	{
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(70);
		textPaint.setColor(Color.BLACK);
		textPaint.setTextAlign(Paint.Align.CENTER);

		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(1);
		linePaint.setColor(Color.RED);
/*

		mFontMetrics = mPaint.getFontMetrics();

		Log.d("Aige", "ascent：" + mFontMetrics.ascent);
		Log.d("Aige", "top：" + mFontMetrics.top);
		Log.d("Aige", "leading：" + mFontMetrics.leading);
		Log.d("Aige", "descent：" + mFontMetrics.descent);
		Log.d("Aige", "bottom：" + mFontMetrics.bottom);
*/

	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		//canvas.drawText(TEXT,0,Math.abs(mFontMetrics.top),mPaint);
		baseX = (int) (canvas.getWidth()/2 - textPaint.measureText(TEXT)/2);
		baseY = (int) (canvas.getHeight()/2 - ((textPaint.descent()+textPaint.ascent())/2));
		//canvas.drawText(TEXT,baseX,baseY,textPaint);
		canvas.drawText(TEXT,canvas.getWidth()/2,baseY,textPaint);
		canvas.drawLine(0,canvas.getHeight()/2,canvas.getWidth(),canvas.getHeight()/2,linePaint);
	}
}
