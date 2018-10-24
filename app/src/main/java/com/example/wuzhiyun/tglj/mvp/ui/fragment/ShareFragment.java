package com.example.wuzhiyun.tglj.mvp.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wuzhiyun.tglj.CalendarUtil;
import com.example.wuzhiyun.tglj.R;
import com.example.wuzhiyun.tglj.TGLJApplication;
import com.example.wuzhiyun.tglj.db.ShareRealm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ShareFragment extends Fragment {
    View mView;
    private String name;//股票名称
    private String code;//股票代码
    private SparseArray<ShareRealm> arrayData;//k线数据

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_share, null);
            code = getArguments().getString("CODE");
            getData();
        }
        ((TextView) mView.findViewById(R.id.name)).setText("聊天界面");
        return mView;
    }

    public String getShareName() {
        return name;
    }

    private void getData() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc = null;
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int jidu = month / 3 + (month % 3 == 0 ? 0 : 1);
                Realm realm = TGLJApplication.getInstance().getRealm();
                RealmResults<ShareRealm> realmRealmResults = realm.where(ShareRealm.class)
                        .equalTo("code", code)
                        .findAllSorted("date", Sort.DESCENDING);
                String date = "";
                if (realmRealmResults != null && realmRealmResults.size() > 0) {
                    date = realmRealmResults.get(0).getDate();
                }
                String lundar = "";
                try {
                    lundar = CalendarUtil.solarToLunar("20181024");
                    if (!TextUtils.isEmpty(lundar)) {
                        lundar = lundar.substring(5);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (int k = 0; k < 13; k++, year--) {
                    int j = 4;
                    if (year == calendar.get(Calendar.YEAR)) {
                        j = jidu;
                    }
                    for (; j > 0; j--) {
                        try {
                            String url = "http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/" + code + ".phtml?year=" + year + "&jidu=" + j;
                            doc = Jsoup.connect(url).get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Element table = doc.getElementById("FundHoldSharesTable");
                        if (table == null) {
                            continue;
                        }
                        Elements trs = table.select("tr");
                        for (int i = 2; i < trs.size(); i++) {
                            Elements tds = trs.get(i).select("td");
                            if (tds.get(0).text().equals(date)) {
                                return;
                            }
                            try {
                                final ShareRealm shareK = new ShareRealm();
                                shareK.setId(code + tds.get(0).text());
                                shareK.setCode(code);
                                shareK.setDate(tds.get(0).text());
                                shareK.setOpenPrice(Double.valueOf(tds.get(1).text()));
                                shareK.setMaxPrice(Double.valueOf(tds.get(2).text()));
                                shareK.setClosingPrice(Double.valueOf(tds.get(3).text()));
                                shareK.setMinPrice(Double.valueOf(tds.get(4).text()));
                                shareK.setVolume(Long.valueOf(tds.get(5).text()));
                                shareK.setTurnover(Long.valueOf(tds.get(6).text()));
                                String dateTemp = shareK.getDate().replace("-", "");
                                String dateLunar = CalendarUtil.solarToLunar(dateTemp);
                                shareK.setLeap(dateLunar.contains("闰"));
                                shareK.setLunar(dateLunar.replace("闰",""));
                                arrayData.append(Integer.valueOf(dateTemp), shareK);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (int i = 0; i < arrayData.size(); i++){
                            realm.copyToRealmOrUpdate(arrayData.valueAt(i));
                        }

                    }
                });
            }
        }.start();
    }

}
