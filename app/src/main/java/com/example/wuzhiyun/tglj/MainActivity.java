package com.example.wuzhiyun.tglj;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wuzhiyun.tglj.db.ShareCodeName;
import com.example.wuzhiyun.tglj.mvp.ui.adpter.FragmentAdapter;
import com.jess.arms.utils.ArmsUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.edit)
    EditText edit;
    @BindView(R.id.search)
    TextView search;

    private FragmentAdapter adapter;
    private List<String> title;
    private List<String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        ArmsUtils.obtainAppComponentFromContext(this).appManager();

        title = new ArrayList<>();
        Realm realm = TGLJApplication.getInstance().getRealm();
        RealmResults<ShareCodeName> realmResults = realm.where(ShareCodeName.class).findAllSorted("code", Sort.ASCENDING);
        data = new ArrayList<>();
        if (realmResults.size() == 0) {
//            data.add("000001");
//            data.add("399006");
//            data.add("399001");
            data.add("300456");
//            data.add("600643");
//            data.add("300131");
//            title.add("上证指数");
//            title.add("创业板指");
//            title.add("深圳成指");
            title.add("300456");
//            title.add("600643");
//            title.add("300131");
        } else {
            for (int i = 0; i < realmResults.size(); i++) {
                ShareCodeName shareCodeName = realmResults.get(i);
                //指数排前面
                if (shareCodeName.getCode().equals("000001") || shareCodeName.getCode().equals("399006") || shareCodeName.getCode().equals("399001")) {
                    data.add(0, realmResults.get(i).getCode());
                    title.add(0, realmResults.get(i).getName());
                } else {
                    data.add(realmResults.get(i).getCode());
                    title.add(realmResults.get(i).getName());
                }

            }
        }
        adapter = new FragmentAdapter(getSupportFragmentManager(), data, title);
        viewPager.setAdapter(adapter);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setTabsFromPagerAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateTitle(ShareCodeName shareCodeName) {
        for (int i = 0; i < title.size(); i++) {
            if (shareCodeName.getCode().equals(title.get(i))) {
                title.set(i, shareCodeName.getName());
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.search)
    public void onViewClicked() {
        if (!TextUtils.isEmpty(edit.getText().toString().trim())) {
            data.add(edit.getText().toString().trim());
            title.add(edit.getText().toString().trim());
            adapter.notifyDataSetChanged();
        }
    }
}
