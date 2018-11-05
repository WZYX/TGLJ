package com.example.wuzhiyun.tglj.mvp.model;

import android.app.Application;

import com.example.wuzhiyun.tglj.mvp.model.api.service.DidService;
import com.example.wuzhiyun.tglj.mvp.model.entity.Did;
import com.example.wuzhiyun.tglj.mvp.model.entity.ResponseBaseEntity;
import com.google.gson.Gson;
import com.jess.arms.integration.IRepositoryManager;
import com.jess.arms.mvp.BaseModel;

import com.jess.arms.di.scope.ActivityScope;

import javax.inject.Inject;

import com.example.wuzhiyun.tglj.mvp.contract.ReverseContract;

import io.reactivex.Observable;


@ActivityScope
public class ReverseModel extends BaseModel implements ReverseContract.Model {
    @Inject
    Gson mGson;
    @Inject
    Application mApplication;

    @Inject
    public ReverseModel(IRepositoryManager repositoryManager) {
        super(repositoryManager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mGson = null;
        this.mApplication = null;
    }

    @Override
    public Observable<ResponseBaseEntity<Did>> getDid(String url) {
        return mRepositoryManager.obtainRetrofitService(DidService.class).getDid(url);
    }
}