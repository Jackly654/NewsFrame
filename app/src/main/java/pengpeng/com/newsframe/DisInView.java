package pengpeng.com.newsframe;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import pengpeng.com.newsframe.utils.MeasureUtil;

/**
 * Created by Jackly on 16/4/10.
 */
public class DisInView extends View {
    private Paint mPaint;// 画笔
    private Bitmap bitmapDis,bitmapSrc;// 位图

    private int x, y;// 位图绘制时左上角的起点坐标
    private int screenW, screenH;// 屏幕尺寸
    private PorterDuffXfermode porterDuffXfermode;// 图形混合模式

    public DisInView(Context context) {
        this(context, null);
    }
    public DisInView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 实例化混合模式
        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

        // 初始化画笔  
        initPaint();

        // 初始化资源  
        initRes(context);
    }


    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    }

    private void initRes(Context context) {
        bitmapDis = BitmapFactory.decodeResource(context.getResources(),R.drawable.a3);
        bitmapSrc = BitmapFactory.decodeResource(context.getResources(), R.drawable.a3_mask);
        int[] screenSize = MeasureUtil.getScreenSize((Activity) context);

        screenW = screenSize[0];
        screenH = screenSize[1];
        /*
         * 计算位图绘制时左上角的坐标使其位于屏幕中心
         * 屏幕坐标x轴向左偏移位图一半的宽度
         * 屏幕坐标y轴向上偏移位图一半的高度
         */
        x = screenW/ 2 - bitmapDis.getWidth()/ 2;
        y = screenH/ 2 - bitmapDis.getHeight()/ 2;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);
        int sc = canvas.saveLayer(0, 0, screenW, screenH, null, Canvas.ALL_SAVE_FLAG);
        //canvas.drawBitmap(bitmapDis,x,y,mPaint);


        // 先绘制dis目标图
        canvas.drawBitmap(bitmapDis, x, y, mPaint);

        // 设置混合模式
        mPaint.setXfermode(porterDuffXfermode);

        // 再绘制src源图
        canvas.drawBitmap(bitmapSrc, x, y, mPaint);

        // 还原混合模式
        mPaint.setXfermode(null);

        // 还原画布
        canvas.restoreToCount(sc);

    }
}
