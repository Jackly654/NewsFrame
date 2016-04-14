package hz.dodo;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Graphic
{
	public static void drawRect(Canvas cvs, int left, int top, int right, int bottom, Paint paint)
	{
		cvs.drawRect(left, top, right, bottom, paint);
	}
	
	public static void drawText(Canvas cvs, int x, int y, String text, Paint paint)
	{
		cvs.drawText("" + text, x - paint.measureText("" + text)*0.5f, y, paint);
	}
}
