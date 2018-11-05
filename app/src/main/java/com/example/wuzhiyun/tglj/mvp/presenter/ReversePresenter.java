package com.example.wuzhiyun.tglj.mvp.presenter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.wuzhiyun.tglj.MD5Util;
import com.example.wuzhiyun.tglj.mvp.model.entity.Did;
import com.example.wuzhiyun.tglj.mvp.model.entity.ResponseBaseEntity;
import com.example.wuzhiyun.tglj.mvp.ui.activity.ReverseActivity;
import com.jess.arms.integration.AppManager;
import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.mvp.BasePresenter;
import com.jess.arms.http.imageloader.ImageLoader;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.ErrorHandleSubscriber;

import javax.inject.Inject;

import com.example.wuzhiyun.tglj.mvp.contract.ReverseContract;
import com.jess.arms.utils.RxLifecycleUtils;

import java.util.List;


@ActivityScope
public class ReversePresenter extends BasePresenter<ReverseContract.Model, ReverseContract.View> {
    @Inject
    RxErrorHandler mErrorHandler;
    @Inject
    Application mApplication;
    @Inject
    ImageLoader mImageLoader;
    @Inject
    AppManager mAppManager;

    @Inject
    public ReversePresenter(ReverseContract.Model model, ReverseContract.View rootView) {
        super(model, rootView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mErrorHandler = null;
        this.mAppManager = null;
        this.mImageLoader = null;
        this.mApplication = null;
    }

    private String did;

    private String md5(String str) {
        return MD5Util.md5(str);
    }

    public void getDid() {
        String baseUrl = "lapi/did/app/channel?aid=android1&biz_type=12&channel_id=94&client_sys=android&time=";
        String time = String.valueOf(System.currentTimeMillis());
        String auth = md5(baseUrl + time + "vq47Hd9JUgfDCytC");
        String url = baseUrl + time + "&auth=" + auth;
        mModel.getDid(url)
                .subscribeOn(Schedulers.io())
                .compose(RxLifecycleUtils.bindToLifecycle(mRootView))//使用 Rxlifecycle,使 Disposable 和 Activity 一起销毁
                .subscribe(new ErrorHandleSubscriber<ResponseBaseEntity<Did>>(mErrorHandler) {
                    @Override
                    public void onNext(ResponseBaseEntity<Did> responseBaseEntity) {
                        String did = responseBaseEntity.getData().getDid();
                        SharedPreferences sp = mApplication.getSharedPreferences(ReverseActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = sp.edit();
                        if (TextUtils.isEmpty(did)) {
                            edit.remove(ReverseActivity.KEY);
                        } else {
                            edit.putString(ReverseActivity.KEY, did);
                        }
                        edit.apply();
                    }
                });
    }
}
