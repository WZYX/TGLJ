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
import com.example.wuzhiyun.tglj.db.ShareCodeName;
import com.example.wuzhiyun.tglj.db.ShareRealm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
            arrayData = new SparseArray<>();
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
                calendar.add(Calendar.DATE, -1);// 日期减1
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String yestoday = sdf.format(calendar.getTime());
                //已取到昨天数据。今天还未收盘
                if (!TextUtils.isEmpty(date) && date.equals(yestoday) && calendar.get(Calendar.HOUR_OF_DAY) < 15) {
                    return;
                }
                boolean isOver = false;
                for (int k = 0; k < 13; k++, year--) {
                    if (isOver) {
                        break;
                    }
                    int j = 4;
                    if (year == calendar.get(Calendar.YEAR)) {
                        j = jidu;
                    }
                    for (; j > 0; j--) {
                        if (isOver) {
                            break;
                        }
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
                        ShareCodeName shareCodeName = realm.where(ShareCodeName.class).equalTo("code", code).findFirst();
                        String text = trs.get(0).select("th").get(0).text();
                        String shareName = text.substring(0, text.indexOf("("));
                        if (shareCodeName == null || !shareName.equals(shareCodeName.getName())) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    ShareCodeName shareCodeName1 = new ShareCodeName();
                                    shareCodeName1.setCode(code);
                                    shareCodeName1.setName(shareName);
                                    realm.copyToRealmOrUpdate(shareCodeName1);

                                }
                            });
                        }
                        for (int i = 2; i < trs.size(); i++) {
                            Elements tds = trs.get(i).select("td");
                            if (tds.get(0).text().equals(date)) {
                                isOver = true;
                                break;
                            }
                            try {
                                final ShareRealm shareK = new ShareRealm();
                                String dateStr = tds.get(0).text();
                                shareK.setId(code + dateStr);
                                shareK.setCode(code);
                                shareK.setDateYear(dateStr.substring(0, 4));
                                shareK.setDate(dateStr.substring(4).replace("-", ""));
                                shareK.setOpenPrice(Double.valueOf(tds.get(1).text()));
                                shareK.setMaxPrice(Double.valueOf(tds.get(2).text()));
                                shareK.setClosingPrice(Double.valueOf(tds.get(3).text()));
                                shareK.setMinPrice(Double.valueOf(tds.get(4).text()));
                                shareK.setVolume(Long.valueOf(tds.get(5).text()));
                                shareK.setTurnover(Long.valueOf(tds.get(6).text()));
                                String dateTemp = dateStr.replace("-", "");
                                String dateLunar = CalendarUtil.solarToLunar(dateTemp);
                                shareK.setLeap(dateLunar.contains("闰"));
                                String lunarTemp = dateLunar.replace("闰", "");
                                shareK.setLunarYear(lunarTemp.substring(0, 4));
                                shareK.setLunar(lunarTemp.substring(4));
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
                        for (int i = 0; i < arrayData.size(); i++) {
                            realm.copyToRealmOrUpdate(arrayData.valueAt(i));
                        }

                    }
                });
                realm.close();
            }
        }.start();
    }

}
