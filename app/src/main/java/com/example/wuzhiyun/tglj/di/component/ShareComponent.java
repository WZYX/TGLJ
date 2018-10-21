package com.example.wuzhiyun.tglj.di.component;

import dagger.Component;

import com.jess.arms.di.component.AppComponent;

import com.example.wuzhiyun.tglj.di.module.ShareModule;

import com.jess.arms.di.scope.ActivityScope;
import com.example.wuzhiyun.tglj.mvp.ui.activity.ShareActivity;

@ActivityScope
@Component(modules = ShareModule.class, dependencies = AppComponent.class)
public interface ShareComponent {
    void inject(ShareActivity activity);
}