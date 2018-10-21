package com.example.wuzhiyun.tglj.di.module;

import com.jess.arms.di.scope.ActivityScope;

import dagger.Module;
import dagger.Provides;

import com.example.wuzhiyun.tglj.mvp.contract.LotteryContract;
import com.example.wuzhiyun.tglj.mvp.model.LotteryModel;


@Module
public class LotteryModule {
    private LotteryContract.View view;

    /**
     * 构建LotteryModule时,将View的实现类传进来,这样就可以提供View的实现类给presenter
     *
     * @param view
     */
    public LotteryModule(LotteryContract.View view) {
        this.view = view;
    }

    @ActivityScope
    @Provides
    LotteryContract.View provideLotteryView() {
        return this.view;
    }

    @ActivityScope
    @Provides
    LotteryContract.Model provideLotteryModel(LotteryModel model) {
        return model;
    }
}