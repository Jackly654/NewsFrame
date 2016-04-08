package pengpeng.com.newsframe.Component;

import dagger.Component;
import pengpeng.com.newsframe.MainController;
import pengpeng.com.newsframe.NewsApplication;
import pengpeng.com.newsframe.module.MainControllerModule;

/**
 * Created by Administrator on 2016/4/5.
 */
@Component(modules = MainControllerModule.class)
public interface MainControllerComponent {
    void inject(NewsApplication newsApplication);
}
