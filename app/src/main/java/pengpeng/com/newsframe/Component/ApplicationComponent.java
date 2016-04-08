package pengpeng.com.newsframe.Component;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import pengpeng.com.newsframe.BaseActivity;
import pengpeng.com.newsframe.module.NewsApplicationModule;

/**
 * Created by Administrator on 2016/4/5.
 */
@Singleton
@Component(modules = NewsApplicationModule.class)
public interface ApplicationComponent {
    void inject(BaseActivity baseActivity);

    Context context();
}
