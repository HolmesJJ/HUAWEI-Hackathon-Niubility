package edu.nyp.deafapp;

import android.app.Application;

import me.yokeyword.fragmentation.Fragmentation;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 建议在Application里初始化
        Fragmentation.builder()
                // BUBBLE显示悬浮球 ; SHAKE: 摇一摇唤出 ;  NONE：隐藏
                .stackViewMode(Fragmentation.NONE)
                .debug(BuildConfig.DEBUG)
                .install();
    }
}
