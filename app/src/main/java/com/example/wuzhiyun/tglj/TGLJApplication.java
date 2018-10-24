package com.example.wuzhiyun.tglj;

import com.jess.arms.base.BaseApplication;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class TGLJApplication extends BaseApplication {
    private Realm mRealm;
    private static TGLJApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        instance = this;
    }

    public static TGLJApplication getInstance(){
        return instance;
    }


    public static Realm getRealm() {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("TGLJ.realm").schemaVersion(0).build();
        return Realm.getInstance(configuration);
    }
}
