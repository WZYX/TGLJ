package com.example.wuzhiyun.tglj.di.component;

import dagger.Component;

import com.jess.arms.di.component.AppComponent;

import com.example.wuzhiyun.tglj.di.module.LotteryModule;

import com.jess.arms.di.scope.ActivityScope;
import com.example.wuzhiyun.tglj.mvp.ui.activity.LotteryActivity;

@ActivityScope
@Component(modules = LotteryModule.class, dependencies = AppComponent.class)
public interface LotteryComponent {
    void inject(LotteryActivity activity);
}