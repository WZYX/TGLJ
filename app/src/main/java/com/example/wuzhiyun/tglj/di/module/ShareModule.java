package com.example.wuzhiyun.tglj.di.module;

import com.jess.arms.di.scope.ActivityScope;

import dagger.Module;
import dagger.Provides;

import com.example.wuzhiyun.tglj.mvp.contract.ShareContract;
import com.example.wuzhiyun.tglj.mvp.model.ShareModel;


@Module
public class ShareModule {
    private ShareContract.View view;

    /**
     * 构建ShareModule时,将View的实现类传进来,这样就可以提供View的实现类给presenter
     *
     * @param view
     */
    public ShareModule(ShareContract.View view) {
        this.view = view;
    }

    @ActivityScope
    @Provides
    ShareContract.View provideShareView() {
        return this.view;
    }

    @ActivityScope
    @Provides
    ShareContract.Model provideShareModel(ShareModel model) {
        return model;
    }
}