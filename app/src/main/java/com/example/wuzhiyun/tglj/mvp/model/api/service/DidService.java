package com.example.wuzhiyun.tglj.mvp.model.api.service;

import com.example.wuzhiyun.tglj.mvp.model.entity.Did;
import com.example.wuzhiyun.tglj.mvp.model.entity.ResponseBaseEntity;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Url;

public interface DidService {
    @Headers({
            "User-Device: MTAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDE1MTF8djQuOS4x",
            "User-Agent: android/4.9.1 (android 4.4.4; ; Nexus+5)",
            "Cookie: acf_did=10000000000000000000000000001511",
            "Host: passport.douyu.com",
            "Connection: Keep-Alive",
            "Accept-Encoding: gzip"
    })
    @GET
    Observable<ResponseBaseEntity<Did>> getDid(@Url String url);
}
