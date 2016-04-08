package pengpeng.com.newsframe.module;

import dagger.Module;
import dagger.Provides;
import pengpeng.com.newsframe.MainController;

/**
 * Created by Administrator on 2016/3/29.
 */
@Module
public class MainControllerModule {
    @Provides
    public MainController provideControllerModule(){
        return new MainController();
    }
}
