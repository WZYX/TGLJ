package com.example.wuzhiyun.tglj.di.component;

import dagger.Component;

import com.jess.arms.di.component.AppComponent;

import com.example.wuzhiyun.tglj.di.module.ReverseModule;

import com.jess.arms.di.scope.ActivityScope;
import com.example.wuzhiyun.tglj.mvp.ui.activity.ReverseActivity;

@ActivityScope
@Component(modules = ReverseModule.class, dependencies = AppComponent.class)
public interface ReverseComponent {
    void inject(ReverseActivity activity);
}