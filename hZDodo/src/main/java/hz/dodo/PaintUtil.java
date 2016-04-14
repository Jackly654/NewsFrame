package hz.dodo;

import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Paint.FontMetrics;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class PaintUtil
{
	public static final int TXT_BIG = 0;
	public static final int TXT_NORMAL = 1;
	public static final int TXT_SMALL = 2;
	
	public static int // 贴图时垂直中心线 y + fontHH_* 即字体垂直居中	
			fontS_1, fontHH_1,
			fontS_2, fontHH_2,
			fontS_3, fontHH_3,
			fontS_4, fontHH_4,
			fontS_5, fontHH_5,
			fontS_6, fontHH_6;
	public static float density = 1.0f;
	
	// 画图抗锯齿在ondraw开头调用canvas.setDrawFilter(pfd);
	public static PaintFlagsDrawFilter pfd;
	
	// 画笔
	public static Paint paint;
	
	private static PaintUtil pu;
	
	static public PaintUtil getInstance(WindowManager wm)
	{
		if(pu == null) pu = new PaintUtil(wm);
		return pu;
	}
	
	private PaintUtil(WindowManager wm)
	{
		pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
		
		DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);
		density = metrics.density;
		
		paint = new Paint();
		paint.setAntiAlias(true); // 抗锯齿
		paint.setSubpixelText(true);  // 设置该项为true，将有助于文本在LCD屏幕上的显示效果 
		paint.setDither(true); // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰  
		
		// 在图形下面设置阴影层，产生阴影效果，radius为阴影的角度(0:无效果,值越大,越透明和发散)，dx和dy为阴影在x轴和y轴上的距离，color为阴影的颜色 
		// paint.setShadowLayer(float radius ,float dx,float dy,int color);
		// paint.clearShadowLayer();
		
//		paint.setFakeBoldText(boolean); // true为粗体，false为非粗体
//		paint.setTextSkewX(-0.5f); // float类型参数，负数表示右斜，正数左斜
//		paint.setUnderlineText(boolean); // true为下划线，false为非下划线
//		paint.setStrikeThruText(boolean); // true为删除线，false为非删除线
//		paint.setStrokeWidth(float); // 设置线宽，float型，如2.5f，默认绘文本无需设置（默认值为0），但假如设置了，再绘制文本的时候一定要恢复到0
		
		setTxtSize(TXT_NORMAL);
	}
	
	public void setTxtSize(int txtsize)
	{
		switch(txtsize)
		{
			case TXT_BIG:
				fontS_1 = 24;
				fontS_2 = 22;
				fontS_3 = 20;
				fontS_4 = 18;
				fontS_5 = 16;
				fontS_6 = 14;
				break;
			case TXT_NORMAL:
				fontS_1 = 22;
				fontS_2 = 20;
				fontS_3 = 18;
				fontS_4 = 16;
				fontS_5 = 14;
				fontS_6 = 12;
				break;
			case TXT_SMALL:
				fontS_1 = 20;
				fontS_2 = 18;
				fontS_3 = 16;
				fontS_4 = 14;
				fontS_5 = 12;
				fontS_6 = 10;
				break;
		}
		
		FontMetrics fontm;
		
		int btm;
		
		fontS_1 *= density;
		paint.setTextSize(fontS_1);
		fontm = paint.getFontMetrics();
		btm = (int) fontm.bottom;
		fontHH_1 = (fontS_1 - btm)/2;
		
		fontS_2 *= density;
		paint.setTextSize(fontS_2);
		fontm = paint.getFontMetrics();
		btm = (int) fontm.bottom;
		fontHH_2 = (fontS_2 - btm)/2;
		
		fontS_3 *= density;
		paint.setTextSize(fontS_3);
		fontm = paint.getFontMetrics();
		btm = (int) fontm.bottom;
		fontHH_3 = (fontS_3 - btm)/2;
		
		fontS_4 *= density;
		paint.setTextSize(fontS_4);
		fontm = paint.getFontMetrics();
		btm = (int) fontm.bottom;
		fontHH_4 = (fontS_4 - btm)/2;
		
		fontS_5 *= density;
		paint.setTextSize(fontS_5);
		fontm = paint.getFontMetrics();
		btm = (int) fontm.bottom;
		fontHH_5 = (fontS_5 - btm)/2;
		
		fontS_6 *= density;
		paint.setTextSize(fontS_6);
		fontm = paint.getFontMetrics();
		btm = (int) fontm.bottom;
		fontHH_6 = (fontS_6 - btm)/2;
	}
}
