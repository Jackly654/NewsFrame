package pengpeng.com.newsframe;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
/**
 * Created by Administrator on 2016/4/14.
 */
public class MultiCircleView extends View
{
	private static final float STROKE_WIDTH = 1F/256F,
			SPACE = 1F / 64F,// 大圆小圆线段两端间隔占比
	LINE_LENGTH = 3F/32F,
	CIRCLE_LARGER_RADIU = 3F/32F,
	CIRCLE_SMALL_RADIU = 5F/64F,
	ARC_RADIU = 1F/8F,// 弧半径
	ARC_TEXT_RADIU = 5F/32F;// 弧围绕文字半径
	private Paint strokePaint,textPaint,arcPaint;
	private int size;//控件边长
	private float strokeWidth;//描边宽度
	private float ccX,ccY;//中心圆圆心坐标
	private float largeCircleRadiu,smallCricleRadiu;//大小圆半径
	private float lineLength;// 线段长度
	private float space;// 大圆小圆线段两端间隔
	private float textOffsetY;// 文本的Y轴偏移值
	public MultiCircleView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initPaint(context);
	}

	private void initPaint(Context context)
	{
		strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setColor(Color.WHITE);
		strokePaint.setStrokeCap(Paint.Cap.ROUND);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG|Paint.SUBPIXEL_TEXT_FLAG);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(20);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textOffsetY = (textPaint.descent()+textPaint.ascent())/2;

		arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		arcPaint.setStrokeCap(Paint.Cap.ROUND);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		//强制宽高一致
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		//获取控件边长
		size = w;
		//参数计算
		calculation();
	}

	private void calculation()
	{
		//计算描边宽度
		strokeWidth = STROKE_WIDTH * size;
		//计算中心圆圆心
		largeCircleRadiu = CIRCLE_LARGER_RADIU * size;

		// 计算小圆半径
		smallCricleRadiu = size * CIRCLE_SMALL_RADIU;
		//计算线段长度
		lineLength = LINE_LENGTH * size;

		// 计算大圆小圆线段两端间隔
		space = size * SPACE;
		//计算中心圆圆心坐标
		ccX = size/2;
		ccY = size/2+CIRCLE_LARGER_RADIU*size;
		//设置参数
		setPara();
	}

	private void setPara()
	{
		strokePaint.setStrokeWidth(strokeWidth);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawColor(0xFFF29B76);
		//绘制中心圆
		canvas.drawCircle(ccX, ccY, largeCircleRadiu, strokePaint);
		canvas.drawText("Dr.pengpeng",ccX,ccY-textOffsetY,textPaint);
		// 绘制左上方图形
		drawTopLeft(canvas);
		// 绘制右上方图形
		drawTopRight(canvas);

		// 绘制左下方图形
		drawBottomLeft(canvas);

		// 绘制下方图形
		drawBottom(canvas);

		// 绘制右下方图形
		drawBottomRight(canvas);
	}

	private void drawBottomRight(Canvas canvas)
	{
		canvas.save();
		canvas.translate(ccX, ccY);
		canvas.rotate(100);
		canvas.drawLine(0, -largeCircleRadiu, 0, -lineLength * 2, strokePaint);
		canvas.drawCircle(0, -largeCircleRadiu - smallCricleRadiu - lineLength, smallCricleRadiu, strokePaint);
		canvas.restore();
	}

	private void drawBottom(Canvas canvas)
	{
		canvas.save();
		canvas.translate(ccX, ccY);
		canvas.drawLine(0, largeCircleRadiu, 0, lineLength * 2, strokePaint);
		canvas.drawCircle(0, largeCircleRadiu + smallCricleRadiu + lineLength, smallCricleRadiu, strokePaint);
		canvas.restore();
	}

	private void drawBottomLeft(Canvas canvas)
	{
		canvas.save();
		canvas.translate(ccX, ccY);
		canvas.rotate(-100);
		canvas.drawLine(0, -largeCircleRadiu, 0, -lineLength * 2, strokePaint);
		canvas.drawCircle(0, -largeCircleRadiu - smallCricleRadiu - lineLength, smallCricleRadiu, strokePaint);
		canvas.restore();
	}

	private void drawTopRight(Canvas canvas)
	{
		float cricleY = -lineLength * 3;
		canvas.save();
		canvas.translate(ccX, ccY);
		canvas.rotate(30);
		canvas.drawLine(0, -largeCircleRadiu, 0, -lineLength * 2, strokePaint);
		canvas.drawCircle(0, -largeCircleRadiu * 2 - lineLength, largeCircleRadiu, strokePaint);

		// 画弧形
		drawTopRightArc(canvas, cricleY);

		canvas.restore();
	}

	private void drawTopRightArc(Canvas canvas, float cricleY)
	{
		canvas.save();
		canvas.translate(0, cricleY);
		canvas.rotate(-30);
		float arcRadiu = size * ARC_RADIU;
		RectF oval = new RectF(-arcRadiu, -arcRadiu, arcRadiu, arcRadiu);
		arcPaint.setStyle(Paint.Style.FILL);
		arcPaint.setColor(0x55EC6941);
		canvas.drawArc(oval,-22.5F,-135,true, arcPaint);

		arcPaint.setStyle(Paint.Style.STROKE);
		arcPaint.setColor(Color.WHITE);
		canvas.drawArc(oval, -22.5F, -135, false, arcPaint);

		canvas.restore();
	}

	private void drawTopLeft(Canvas canvas)
	{
		//锁定画布
		canvas.save();
		//平移和旋转画布
		canvas.translate(ccX,ccY);
		canvas.rotate(-30);
		// 依次画：线-圈-线-圈
		canvas.drawLine(0,-largeCircleRadiu,0,-lineLength * 2, strokePaint);
		canvas.drawCircle(0, -largeCircleRadiu * 2 - lineLength, largeCircleRadiu, strokePaint);
		canvas.drawLine(0, -largeCircleRadiu * 3 - lineLength, 0, -largeCircleRadiu * 3 - lineLength * 2, strokePaint);
		canvas.drawCircle(0, -largeCircleRadiu * 4 - lineLength*2,largeCircleRadiu,strokePaint);

		canvas.restore();
	}
}
