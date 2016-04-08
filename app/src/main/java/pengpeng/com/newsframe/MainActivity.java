package pengpeng.com.newsframe;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;


public class MainActivity extends AppCompatActivity {
    private LinearLayout llRoot;
    private CustomView mCustomView;
    private int radiu;

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
        setContentView(R.layout.activity_main);

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
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}

