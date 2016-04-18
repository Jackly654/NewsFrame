package pengpeng.com.newsframe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import hz.dodo.ImgMng;
import hz.dodo.controls.DViewPager;

public class MainActivity extends AppCompatActivity {
    private PageTurnView mPageCurlView;// 翻页控件
    private FoldView mFoldView;// 折页控件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    foldPage();

    // turnPage();
}

    private void foldPage() {
        mFoldView = (FoldView) findViewById(R.id.main);
        mFoldView.setBitmaps(initBitmaps());
    }

    private void turnPage() {
        mPageCurlView = (PageTurnView) findViewById(R.id.main);
        mPageCurlView.setBitmaps(initBitmaps());
    }

    private List<Bitmap> initBitmaps() {
        Bitmap bitmap = null;
        List<Bitmap> bitmaps = new ArrayList<Bitmap>();

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.page_img_a);
        bitmaps.add(bitmap);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.page_img_b);
        bitmaps.add(bitmap);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.page_img_c);
        bitmaps.add(bitmap);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.page_img_d);
        bitmaps.add(bitmap);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.page_img_e);
        bitmaps.add(bitmap);

        return bitmaps;
    }

    @Override
    protected void onDestroy() {
        if (null != mFoldView) {
            mFoldView.slideStop();
            mFoldView.getSlideHandler().removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}

