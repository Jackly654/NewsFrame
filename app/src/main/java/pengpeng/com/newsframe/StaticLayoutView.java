package pengpeng.com.newsframe;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
/**
 * Created by Administrator on 2016/4/11.
 */
public class StaticLayoutView extends View
{
	private static final String TEXT = "This is used by widgets to control text layout. You should not need\n" +
			"  to use this class directly unless you are implementing your own widget\n" +
			"  or custom display object, or would be tempted to call\n" +
			"  {@link android.graphics.Canvas#drawText(java.lang.CharSequence, int, int,\n" +
			"  float, float, android.graphics.Paint)\n" +
			"  Canvas.drawText()} directly.";

	private static final String TEXT1 = "日照香炉生紫烟，遥看瀑布挂前川";
	private TextPaint mTextPaint;
	private StaticLayout mStaticLayout;

	public StaticLayoutView(Context context)
	{
		this(context, null);
	}
	public StaticLayoutView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initPaint(context);
	}

	private void initPaint(Context context)
	{
		mTextPaint = new TextPaint();
		mTextPaint.setTextSize(66.46F);
		mTextPaint.setColor(Color.BLACK);

		Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/MFKeKe_Noncommercial-Regular.ttf");
		mTextPaint.setTypeface(typeface);
		mTextPaint.setTextSkewX(-0.5F);

	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		mStaticLayout = new StaticLayout(TEXT1,mTextPaint,canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL,1.0F,0.0F,false);
		mStaticLayout.draw(canvas);
		canvas.restore();
	}
}
