package com.example.wuzhiyun.tglj.di.module;

import com.jess.arms.di.scope.ActivityScope;

import dagger.Module;
import dagger.Provides;

import com.example.wuzhiyun.tglj.mvp.contract.ReverseContract;
import com.example.wuzhiyun.tglj.mvp.model.ReverseModel;


@Module
public class ReverseModule {
    private ReverseContract.View view;

    /**
     * 构建ReverseModule时,将View的实现类传进来,这样就可以提供View的实现类给presenter
     *
     * @param view
     */
    public ReverseModule(ReverseContract.View view) {
        this.view = view;
    }

    @ActivityScope
    @Provides
    ReverseContract.View provideReverseView() {
        return this.view;
    }

    @ActivityScope
    @Provides
    ReverseContract.Model provideReverseModel(ReverseModel model) {
        return model;
    }
}