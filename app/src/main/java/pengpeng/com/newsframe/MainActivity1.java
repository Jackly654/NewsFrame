package pengpeng.com.newsframe;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import hz.dodo.ImgMng;
import hz.dodo.controls.DViewPager;

public class MainActivity1 extends AppCompatActivity {
    private LinearLayout llRoot;
    private CustomView mCustomView;
    private int radiu;
    private DViewPager mDViewPager;
//    private Context mContext;
    private Context mContext;
    private List<View> ltvs;
    private ImageView view1,view2,view3,view4;


    ImgMng
        im;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            mCustomView.setRadiu(radiu);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        //mCustomView = (CustomView) findViewById(R.id.main_cv);

        /*llRoot = (LinearLayout) findViewById(R.id.llroot);
        llRoot.addView(new CustomView(this));*/

       /* new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while(true)
                {
                    try
                    {
                        if(radiu <= 200)
                        {
                            radiu += 10;
                            mHandler.obtainMessage().sendToTarget();
                        }else
                        {
                            radiu = 0;
                        }
                        Thread.sleep(40);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();*/
        //new Thread(mCustomView).start();

        im = ImgMng.getInstance(this);

//        mContext = MainActivity.this;
        setList();
        initData(this, ltvs);

        setContentView(mDViewPager);

    }

    private void setList()
    {
        view1 = new ImageView(this);
        view2 = new ImageView(this);
        view3 = new ImageView(this);
        view4 = new ImageView(this);

        view1.setImageResource(R.drawable.a);
        view2.setImageResource(R.drawable.a2);
        view3.setImageResource(R.drawable.a3);
        view4.setImageResource(R.drawable.a3_mask);

//        view1.setImageDrawable(im.getBmId(R.drawable.a));
//        view2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.a2));
//        view3.setImageDrawable(mContext.getResources().getDrawable(R.drawable.a3));
//        view4.setImageDrawable(mContext.getResources().getDrawable(R.drawable.a3_mask));
        ltvs = new ArrayList<>(4);
        ltvs.add(view1);
        ltvs.add(view2);
        ltvs.add(view3);
        ltvs.add(view4);
    }

    private void initData(Context context,List<View> list)
    {
        mDViewPager = new DViewPager(context, null,list,0,0);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}

