package com.example.wuzhiyun.tglj.mvp.ui.adpter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.wuzhiyun.tglj.mvp.ui.fragment.ShareFragment;

import java.util.List;

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private List<String> data;
    private List<String> title;

    public FragmentAdapter(FragmentManager fm, List<String> data, List<String> title) {
        super(fm);
        this.data = data;
        this.title = title;
    }

    @Override
    public Fragment getItem(int position) {
        ShareFragment mFragmentMoment = new ShareFragment();
        Bundle bundle_fragment = new Bundle();
        bundle_fragment.putString("CODE", data.get(position));
        mFragmentMoment.setArguments(bundle_fragment);
        return mFragmentMoment;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return title.get(position);
    }
}
