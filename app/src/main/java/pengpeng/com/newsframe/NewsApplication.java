package pengpeng.com.newsframe;

import android.app.Application;
import android.content.Context;

import pengpeng.com.newsframe.Component.ApplicationComponent;
import pengpeng.com.newsframe.Component.DaggerApplicationComponent;
import pengpeng.com.newsframe.module.NewsApplicationModule;

/**
 * Created by Administrator on 2016/3/29.
 */
public class NewsApplication extends Application {

    private ApplicationComponent applicationComponent;

   /* @inject private MainController mMainController;
    public static NewsApplication from(Context context){
        return (NewsApplication) context.getApplicationContext();
    }*/


    @Override
    public void onCreate() {
        super.onCreate();
        this.initInjector();
    }

    private void initInjector() {
        this.applicationComponent = DaggerApplicationComponent.builder().newsApplicationModule(new NewsApplicationModule(this)).build();
    }

    public ApplicationComponent getApplicationComponent(){
        return this.applicationComponent;
    }
}
