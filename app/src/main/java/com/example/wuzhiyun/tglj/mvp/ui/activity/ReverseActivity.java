package com.example.wuzhiyun.tglj.mvp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.example.wuzhiyun.tglj.mvp.model.api.service.CommonService;
import com.jess.arms.base.BaseActivity;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.integration.RepositoryManager;
import com.jess.arms.utils.ArmsUtils;

import com.example.wuzhiyun.tglj.di.component.DaggerReverseComponent;
import com.example.wuzhiyun.tglj.di.module.ReverseModule;
import com.example.wuzhiyun.tglj.mvp.contract.ReverseContract;
import com.example.wuzhiyun.tglj.mvp.presenter.ReversePresenter;

import com.example.wuzhiyun.tglj.R;


import java.util.prefs.Preferences;

import static com.jess.arms.utils.Preconditions.checkNotNull;


public class ReverseActivity extends BaseActivity<ReversePresenter> implements ReverseContract.View {
    public static final String PREFERENCES_FILE = "Reverse_preferences";
    public static final String KEY = "Reverse_did";
    @Override
    public void setupActivityComponent(@NonNull AppComponent appComponent) {
        DaggerReverseComponent //如找不到该类,请编译一下项目
                .builder()
                .appComponent(appComponent)
                .reverseModule(new ReverseModule(this))
                .build()
                .inject(this);
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_reverse; //如果你不需要框架帮你设置 setContentView(id) 需要自行设置,请返回 0
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences(PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        String did = sp.getString(KEY, "");
        if(TextUtils.isEmpty(did)) {
            mPresenter.getDid();
        }
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showMessage(@NonNull String message) {
        checkNotNull(message);
        ArmsUtils.snackbarText(message);
    }

    @Override
    public void launchActivity(@NonNull Intent intent) {
        checkNotNull(intent);
        ArmsUtils.startActivity(intent);
    }

    @Override
    public void killMyself() {
        finish();
    }
}
