package pengpeng.com.newsframe;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.view.View;

import pengpeng.com.newsframe.utils.MeasureUtil;

/**
 * Created by Administrator on 2016/4/7.
 */
public class CustomView extends View implements Runnable{
    private Paint mPaint;
    private Context mContext;// 上下文环境引用
    private int radiu;// 圆环半径
    private Bitmap bitmap;// 位图
    private
    int x,
        y;
    private
    boolean
    isClick;
    public CustomView(Context context) {
        this(context, null);
       // super(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initPaint();

        initRes(context);
       /* setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                *//**
                 * 判断是否被点击过
                  *//*
                if(isClick)
                {
                    // 如果已经被点击了则点击时设置颜色过滤为空还原本色
                    mPaint.setColorFilter(null);
                    isClick = false;
                }else{
                    // 如果未被点击则点击时设置颜色过滤后为黄色
                    mPaint.setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0X00FFFF00));
                    isClick = true;
                }

                invalidate();
            }
        });*/
    }

    private void initRes(Context context)
    {
         bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.a);

         /**
         * 计算位图绘制时左上角的坐标使其位于屏幕中心
         * 屏幕坐标x轴向左偏移位图一半的宽度
         * 屏幕坐标y轴向上偏移位图一半的高度
         */
        x = MeasureUtil.getScreenSize((Activity) mContext)[0] / 2 - bitmap.getWidth() / 2;
        y = MeasureUtil.getScreenSize((Activity) mContext)[1]/2 - bitmap.getHeight()/2;

       /* x = MeasureUtil.getScreenSize((Activity) mContext)[0] - bitmap.getWidth() ;
        y = MeasureUtil.getScreenSize((Activity) mContext)[1] - bitmap.getHeight();*/
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

         /**
         * 设置画笔样式为描边，圆环嘛……当然不能填充不然就么意思了
         *
         * 画笔样式分三种：
         * 1.Paint.Style.STROKE：描边
         * 2.Paint.Style.FILL_AND_STROKE：描边并填充
         * 3.Paint.Style.FILL：填充
         */

        mPaint.setStyle(Paint.Style.FILL);
        //mPaint.setColor(Color.argb(255, 255, 128, 103));
         /**
         * 设置描边的粗细，单位：像素px 注意：当setStrokeWidth(0)的时候描边宽度并不为0而是只占一个像素
         */
        mPaint.setStrokeWidth(100000);
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
                -1, 0, 0, 1, 1,
                0, -1, 0, 1, 1,
                0, 0, -1, 1, 1,
                0, 0, 0, 1, 0,
        });
        //mPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

       // mPaint.setColorFilter(new LightingColorFilter(0xFFFF00FF, 0x00000000));

        mPaint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.DARKEN));
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
       //canvas.drawCircle(MeasureUtil.getScreenSize((Activity) mContext)[0] / 2, MeasureUtil.getScreenSize((Activity) mContext)[1]/2,200,mPaint);
        canvas.drawBitmap(bitmap,x,y,mPaint);
    }

    public synchronized void setRadiu(int radiu){
        this.radiu =radiu;

        invalidate();
    }

    @Override
    public void run()
    {
        while (true) {
            try {
                /*
                 * 如果半径小于200则自加否则大于200后重置半径值以实现往复
                 */
                if (radiu <= 200) {
                    radiu += 10;

                    // 刷新View
                    postInvalidate();
                } else {
                    radiu = 0;
                }

                // 每执行一次暂停40毫秒
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
