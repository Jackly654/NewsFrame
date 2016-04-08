package pengpeng.com.newsframe.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import pengpeng.com.newsframe.NewsApplication;

/**
 * Created by Administrator on 2016/4/5.
 */
@Module
public class NewsApplicationModule {
    private final NewsApplication application;


    public NewsApplicationModule(NewsApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext(){
        return this.application;
    }
}
