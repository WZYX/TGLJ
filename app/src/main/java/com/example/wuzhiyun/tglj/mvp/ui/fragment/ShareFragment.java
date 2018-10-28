package com.example.wuzhiyun.tglj.mvp.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
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
import org.simple.eventbus.EventBus;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ShareFragment extends Fragment {
    View mView;
    @BindView(R.id.solar_today_num)
    TextView solarTodayNumTxt;
    @BindView(R.id.solar_today_rise)
    TextView solarTodayRiseTxt;
    @BindView(R.id.solar_today_fall)
    TextView solarTodayFallTxt;
    @BindView(R.id.solar_today_yang)
    TextView solarTodayYangTxt;
    @BindView(R.id.solar_today_yin)
    TextView solarTodayYinTxt;
    @BindView(R.id.lunar_today_num)
    TextView lunarTodayNumTxt;
    @BindView(R.id.lunar_today_rise)
    TextView lunarTodayRiseTxt;
    @BindView(R.id.lunar_today_fall)
    TextView lunarTodayFallTxt;
    @BindView(R.id.lunar_today_yang)
    TextView lunarTodayYangTxt;
    @BindView(R.id.lunar_today_yin)
    TextView lunarTodayYinTxt;
    Unbinder unbinder;
    private String code;//股票代码
    private SparseArray<ShareRealm> arrayData;//k线数据
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_share, null);
            code = getArguments().getString("CODE");
            arrayData = new SparseArray<>();
            getData();
        }
        unbinder = ButterKnife.bind(this, mView);
        return mView;
    }


    private void getData() {
        initData();
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc = null;
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                String today = sdf.format(calendar.getTime());
                int jidu = month / 3 + (month % 3 == 0 ? 0 : 1);
                Realm realm = TGLJApplication.getInstance().getRealm();
                RealmResults<ShareRealm> realmRealmResults = realm.where(ShareRealm.class)
                        .equalTo("code", code)
                        .findAllSorted("id", Sort.DESCENDING);
                String date = "";
                if (realmRealmResults != null && realmRealmResults.size() > 0) {
                    date = realmRealmResults.get(0).getDateYear() + realmRealmResults.get(0).getDate();
                    for (int i = 0; i < realmRealmResults.size(); i++) {
                        String dateStr = realmRealmResults.get(i).getDateYear() + realmRealmResults.get(i).getDate();
                        arrayData.append(Integer.valueOf(dateStr), realmRealmResults.get(i));
                    }
                }
                calendar.add(Calendar.DATE, -1);// 日期减1
                if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
                    calendar.add(Calendar.DATE, -1);// 日期减1
                } else if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
                    calendar.add(Calendar.DATE, -2);// 日期减2
                }
                String yestoday = sdf.format(calendar.getTime());
                calendar.add(Calendar.DATE, 2);
                if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
                    calendar.add(Calendar.DATE, 2);
                } else if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
                    calendar.add(Calendar.DATE, 1);
                }
                String tomorrow = sdf.format(calendar.getTime());
                //已取到昨天数据。今天还未收盘
                if (!TextUtils.isEmpty(date) && date.equals(yestoday) && calendar.get(Calendar.HOUR_OF_DAY) < 15) {
                    analyze(today);
                    realm.close();
                    return;
                }
                boolean isOver = false;
                for (int k = 0; k < yearNum; k++, year--) {
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
                            if(doc == null){
                                isOver = true;
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (doc == null) {
                            isOver = true;
                            break;
                        }
                        Element table = doc.getElementById("FundHoldSharesTable");
                        Log.e("wuzhiyun", code + ":" + year + " " + j);
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
                                    EventBus.getDefault().post(shareCodeName1);

                                }
                            });
                        }
                        for (int i = 2; i < trs.size(); i++) {
                            Elements tds = trs.get(i).select("td");
                            String dateStr = tds.get(0).text().replace("-", "");
                            if (date.equals(dateStr)) {
                                isOver = true;
                                break;
                            }
                            try {
                                final ShareRealm shareK = new ShareRealm();
                                shareK.setId(code + dateStr);
                                shareK.setCode(code);
                                shareK.setDateYear(dateStr.substring(0, 4));
                                shareK.setDate(dateStr.substring(4));
                                shareK.setOpenPrice(Double.valueOf(tds.get(1).text()));
                                shareK.setMaxPrice(Double.valueOf(tds.get(2).text()));
                                shareK.setClosingPrice(Double.valueOf(tds.get(3).text()));
                                shareK.setMinPrice(Double.valueOf(tds.get(4).text()));
                                shareK.setVolume(Long.valueOf(tds.get(5).text()));
                                shareK.setTurnover(Long.valueOf(tds.get(6).text()));
                                String dateLunar = CalendarUtil.solarToLunar(dateStr);
                                shareK.setLeap(dateLunar.contains("闰"));
                                String lunarTemp = dateLunar.replace("闰", "");
                                shareK.setLunarYear(lunarTemp.substring(0, 4));
                                shareK.setLunar(lunarTemp.substring(4));
                                arrayData.append(Integer.valueOf(dateStr), shareK);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //防爬虫检测
                        try {
                            Thread.sleep(new Random().nextInt(5) * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
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
                if (calendar.get(Calendar.HOUR_OF_DAY) < 15) {
                    analyze(today);
                } else {
                    analyze(tomorrow);
                }
                realm.close();
            }
        }.start();
    }

    private void analyze(String day) {
        int todayKey = Integer.valueOf(day);//yyyyMMdd
        try {
            for (int i = 0; i < yearNum; i++) {
                int key = todayKey - i * 10000;
                ShareRealm shareRealmToday = arrayData.get(key);
                if (shareRealmToday != null) {
                    solarTodayNum++;
                    if (Math.abs(shareRealmToday.getClosingPrice() - shareRealmToday.getOpenPrice()) / shareRealmToday.getOpenPrice() > rateT) {
                        if (shareRealmToday.getClosingPrice() > shareRealmToday.getOpenPrice()) {
                            solarTodayYang++;
                        } else {
                            solarTodayYin++;
                        }
                    }
                    for (int j = 1; j < 15; j++) {
                        int yestodayKey = getDayKey(key, j);
                        ShareRealm shareRealmYestoday = arrayData.get(yestodayKey);
                        if (shareRealmYestoday != null) {
                            if (Math.abs(shareRealmToday.getClosingPrice() - shareRealmYestoday.getClosingPrice()) / shareRealmYestoday.getClosingPrice() > rate) {
                                if (shareRealmToday.getClosingPrice() - shareRealmYestoday.getClosingPrice() > 0) {
                                    solarTodayRise++;
                                } else {
                                    solarTodayFall++;
                                }
                            }
                            break;
                        }
                    }
                }


            }

            String lunarTodayStr = CalendarUtil.solarToLunar(day);
            Log.e("wuzhiyun", lunarTodayStr);
            int lunarTodayYear = Integer.valueOf(lunarTodayStr.substring(0, 4));
            String lunarTodayDate = lunarTodayStr.substring(4);
            boolean isLeap = lunarTodayDate.contains("闰");
            if (isLeap) {
                lunarTodayDate.replace("闰", "");
            }
            for (int i = 0; i < yearNum; i++) {
                int key = Integer.valueOf(CalendarUtil.lunarToSolar((lunarTodayYear - i) + lunarTodayDate, isLeap));
                Log.e("wuzhiyun", "key:" + key);
                ShareRealm shareRealmToday = arrayData.get(key);
                if (shareRealmToday != null) {
                    lunarTodayNum++;
                    if (Math.abs(shareRealmToday.getClosingPrice() - shareRealmToday.getOpenPrice()) / shareRealmToday.getOpenPrice() > rateT) {
                        if (shareRealmToday.getClosingPrice() > shareRealmToday.getOpenPrice()) {
                            lunarTodayYang++;
                        } else {
                            lunarTodayYin++;
                        }
                    }
                    for (int j = 1; j < 15; j++) {
                        int yestodayKey = getDayKey(key, j);
                        ShareRealm shareRealmYestoday = arrayData.get(yestodayKey);
                        if (shareRealmYestoday != null) {
                            if (Math.abs(shareRealmToday.getClosingPrice() - shareRealmYestoday.getClosingPrice()) / shareRealmYestoday.getClosingPrice() > rate) {
                                if (shareRealmToday.getClosingPrice() - shareRealmYestoday.getClosingPrice() > 0) {
                                    lunarTodayRise++;
                                } else {
                                    lunarTodayFall++;
                                }
                            }
                            break;
                        }
                    }
                }

            }
            showData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getDayKey(Integer today, int beforNum) {
        try {
            Date date = sdf.parse(today.toString());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -beforNum);
            return Integer.valueOf(sdf.format(calendar.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private float rate = 0.01f;//上涨下跌判断标准
    private float rateT = 0.02f;//做T需要浮动比例
    private int yearNum = 13;

    private int solarTodayNum = 0;//阳历今天统计样本数
    private int solarTodayRise = 0;//阳历今天上涨次数
    private int solarTodayFall = 0;//阳历今天下跌次数
    private int solarTodayYang = 0;//阳历今天阳线次数
    private int solarTodayYin = 0;//阳历今天阴线次数

    private int lunarTodayNum = 0;//阴历今天统计样本数
    private int lunarTodayRise = 0;//阴历今天上涨次数
    private int lunarTodayFall = 0;//阴历今天下跌次数
    private int lunarTodayYang = 0;//阴历今天阳线次数
    private int lunarTodayYin = 0;//阴历今天阴线次数

    private void initData() {
        arrayData.clear();
        solarTodayNum = 0;//阳历今天统计样本数
        solarTodayRise = 0;//阳历今天上涨次数
        solarTodayFall = 0;//阳历今天下跌次数
        solarTodayYang = 0;//阳历今天阳线次数
        solarTodayYin = 0;//阳历今天阴线次数
        lunarTodayNum = 0;//阴历今天统计样本数
        lunarTodayRise = 0;//阴历今天上涨次数
        lunarTodayFall = 0;//阴历今天下跌次数
        lunarTodayYang = 0;//阴历今天阳线次数
        lunarTodayYin = 0;//阴历今天阴线次数
    }

    private void showData(){
        solarTodayNumTxt.post(new Runnable() {
            @Override
            public void run() {
                solarTodayNumTxt.setText(getString(R.string.solarTodayNum, solarTodayNum));
                solarTodayRiseTxt.setText(getString(R.string.solarTodayRise, solarTodayRise));
                solarTodayFallTxt.setText(getString(R.string.solarTodayFall, solarTodayFall));
                solarTodayYangTxt.setText(getString(R.string.solarTodayYang, solarTodayYang));
                solarTodayYinTxt.setText(getString(R.string.solarTodayYin, solarTodayYin));
                lunarTodayNumTxt.setText(getString(R.string.lunarTodayNum, lunarTodayNum));
                lunarTodayRiseTxt.setText(getString(R.string.lunarTodayRise, lunarTodayRise));
                lunarTodayFallTxt.setText(getString(R.string.lunarTodayFall, lunarTodayFall));
                lunarTodayYangTxt.setText(getString(R.string.lunarTodayYang, lunarTodayYang));
                lunarTodayYinTxt.setText(getString(R.string.lunarTodayYin, lunarTodayYin));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
